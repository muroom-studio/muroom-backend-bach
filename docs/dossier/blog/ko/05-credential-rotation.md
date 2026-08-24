# AWS가 관리하지 않는 DB에 RDS급 비밀번호 로테이션 달기

*[사비로 돌린 AWS](/writing/muroom-aws-on-pocket-money)의 how-to 편. 비용 때문에 PostgreSQL을 맨 EC2에서 돌리면서도, 재배포 없는 자동 로테이션을 포기하지 않은 방법.*

---

RDS를 떠나면 그리운 건 대시보드가 아니라, 나 없이 알아서 굴러가던 지루한 위생입니다. 그중 제가 포기하지 않기로 한 게 자격증명 자동 로테이션입니다. 영원히 안 바뀌는 DB 비밀번호는 언젠가 노트북에, 로그에, 스크린샷에 새어 나가는 비밀번호니까요.

핵심 트릭은, AWS의 로테이션 기계가 당신의 Postgres가 "관리형"인지 아닌지 전혀 신경 쓰지 않는다는 겁니다. 그것도 남들처럼 PostgreSQL 와이어 프로토콜로 말할 뿐입니다.

## 부품 셋

**1. 로테이션 Lambda: 표준 그대로.** Serverless Application Repository에 `SecretsManagerRDSPostgreSQLRotationSingleUser`가 있습니다. RDS가 쓰는 바로 그 Lambda입니다. Terraform으로 CloudFormation 스택으로 배포하고, VPC 프라이빗 서브넷에 넣고, DB 5432와 Secrets Manager 443에 닿는 보안그룹을 줍니다. 이름에 "RDS"가 있지 요구사항에 있는 게 아닙니다. 시크릿이 가리키는 호스트에 libpq로 접속할 뿐이고, 그 호스트가 `t4g.small` 위의 Docker 컨테이너여도 상관없습니다.

**2. 시크릿: Lambda가 기대하는 모양으로.** 환경당 JSON 시크릿 하나: `{engine, host, port, username, password, dbname}`, `host`는 EC2 프라이빗 IP. 저를 문 Terraform 디테일 둘:

- 시크릿에 `lifecycle { ignore_changes = [secret_string] }`. 없으면 다음 `terraform apply`가 랜덤 비밀번호를 재생성해서 Lambda가 방금 설정한 값을 밟아버립니다.
- 로테이션 스케줄은 별도 리소스입니다. 우리는 **dev 1일, prod 7일**로 돌렸습니다. dev 매일은 편집증이 아니라 카나리입니다. 로테이션이 깨지면 prod에서 일주일 안이 아니라 dev에서 24시간 안에 알고 싶으니까.

**3. 애플리케이션 쪽: 아무도 안 알려주는 부분.** 로테이션할 때마다 커넥션 풀이 죽으면 무용지물입니다. AWS Advanced JDBC Wrapper가 이걸 풉니다:

```yaml
url: jdbc:aws-wrapper:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}
driver-class-name: software.amazon.jdbc.Driver
hikari:
  data-source-properties:
    wrapperPlugins: awsSecretsManager
    secretsManagerSecretId: muroom/postgres-prod/credentials
    secretsManagerRegion: ${AWS_REGION}
```

`awsSecretsManager` 플러그인은 연결 시점에 시크릿에서 자격증명을 가져오고, **인증 실패 시 다시 가져옵니다.** 기존 연결은 로테이션을 견디고(Postgres는 비밀번호 변경으로 인증된 세션을 끊지 않습니다), 새 연결은 투명하게 새 비밀번호를 집습니다. 재배포도, 재시작도, 새벽 3시의 커넥션 폭풍도 없습니다. IAM 미묘함 하나: `secretsmanager:GetSecretValue`가 실행 롤이 아니라 **태스크 롤**에 필요합니다. 이 호출은 컨테이너 기동 시 ECS가 아니라 런타임에 당신의 앱이 하는 거니까요.

## 그날 저녁의 기록

털어놓자면 이걸 붙이는 데 약 30분간 커밋 4개가 들었고, 호박 속 곤충처럼 git에 보존돼 있습니다. 1번: wrapper 추가. 2번: JDBC URL 수정. `jdbc:aws-wrapper:` 스킴을 까먹어서 wrapper가 아예 안 물렸습니다. 3번: 엉뚱한 의존성(Spring Cloud AWS 스타터, 다른 우주의 물건입니다). 4번: 맞는 것, `software.amazon.awssdk:secretsmanager` 한 줄. 제목은 *"fix: … 3차!!!!!!!!!!!!!!!"*. 히스토리를 고쳐 쓸 수도 있습니다. 안 고치기로 했습니다.

## 진짜 동작했나?

네, 두 가지로 증명됩니다. 시크릿의 `LastChangedDate`가 여섯 달 동안 스케줄대로 전진했습니다. 가장 얄궂은 검증도 있습니다. 서비스 운영을 멈추고 몇 달이 지난 뒤에도, 그 Lambda는 사용자 0명을 위해 dev 비밀번호를 24시간마다 성실하게 돌리고 있었습니다. 자동화는 의도보다 오래 삽니다. 회고를 위해 계정을 감사할 때, 그 성실한 작은 Lambda가 계정에서 가장 건강한 존재였습니다.

**따라 할 때**: 비용이나 제어권 때문에 Postgres를 자체 운영하고, 앱이 JVM이고, 관리형급 위생을 원할 때. **하지 말 때**: 이미 RDS라면(내장입니다), 혹은 비밀번호가 영원히 안 바뀌어도 괜찮다면(안 괜찮습니다).

---

*시리즈: [사비로 돌린 AWS](/writing/muroom-aws-on-pocket-money) · [JWT를 지우다](/writing/muroom-deleting-jwt) · [ID vs 자바스크립트](/writing/muroom-ids-javascript) · [뷰포트 검색](/writing/muroom-viewport-search)*
