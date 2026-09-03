# Muroom Backend Bach

Spring Boot 3.5.8 / Java 21 / PostgreSQL 17 + PostGIS / Valkey(Redis)

## 빌드 및 실행

```bash
docker-compose up -d                                                        # PostgreSQL + Valkey 실행
./gradlew bootRun --args='--spring.profiles.active=local'                   # 로컬 서버 실행
./gradlew build                                                             # 빌드
```

## 코드 스타일

- Google Java Style Guide 준수: https://google.github.io/styleguide/javaguide.html
- Javadoc 생략
- Hard wrap: 140자

## API 네이밍

```
/api/{version}/{domain(복수형)}/...
```

```
/api/v1/studios/{studioId}
/api/v1/rooms/{roomId}
```

## 레이어 아키텍처

모든 모듈은 아래 4-레이어 구조를 따른다.

```
presentation/          # Controller, Request/Response DTO
application/
  command/             # 쓰기 작업 (CommandService + Command DTO)
  query/               # 읽기 작업 (QueryService)
domain/
  entity/              # JPA Entity
  valueobject/         # Java Record (불변 값 객체)
  repository/          # 데이터 접근 인터페이스
  enums/               # 도메인 열거형
exception/             # 모듈별 ErrorCode
```

## 엔티티 규칙

### 생성자
```java
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
// @Setter 금지
```

### ID — 모든 PK는 @Tsid (Long 타입)
```java
@Id
@Tsid
@Column(name = "studio_id")
private Long id;
```

### Response DTO에서 ID는 String 타입으로 반환
```java
public record StudioResponse(String studioId, ...) {}
```

### 엔티티 상속 계층
```
CreatedDateEntity          # createdAt
└── AuditableEntity        # + updatedAt
    └── SoftDeletableEntity  # + deletedAt (@SQLDelete + @SQLRestriction)
```
소프트 딜리트가 필요 없는 엔티티는 `AuditableEntity`를 상속한다.

### Value Object
```java
// 불변 데이터는 반드시 Java Record 사용
public record AddressInfo(String roadNameAddress, String lotNumberAddress, String detailedAddress) {}
```

## JPA 규칙

- DB 레벨 외래키 제약 없음 (FK 미설정)
- 동일 도메인 내: `@ManyToOne`으로 엔티티 객체 참조
- `@OneToMany` 절대 금지
- 타 도메인 참조 시: 해당 엔티티의 PK 필드(Long)만 참조
- Cascade 등 연관 제약은 Application Service 레벨에서 직접 관리

```java
// ✅ 동일 도메인 — 엔티티 참조
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "owner_id")
private Owner owner;

// ✅ 타 도메인 — PK만 참조
@Column(name = "musician_id", nullable = false)
private Long musicianId;

// ❌ 금지
@OneToMany
private List<Room> rooms;
```

## Entity ↔ DTO 변환 (Assembler)

- 도메인당 1개의 Assembler 클래스
- 클래스명: `{도메인명}Assembler`
- 메서드명: DTO 클래스명에서 도메인명 제거 + `to` prefix

```java
@Component
public class StudioBoastAssembler {

    public DetailResponse toDetailResponse(StudioBoasting boasting, Member member) {
        return DetailResponse.builder()
                .creatorInfo(toCreatorInfo(member))
                .build();
    }
}
```

## ErrorCode 규칙

### 형식
- Prefix: 도메인 대표 2자 (예: Auth → `AU`, User → `US`, Studio → `ST`, External → `EX`)
- 코드 형식: `{PREFIX}-{HTTP STATUS}-{NUMBER}`

### 네이밍 패턴
| 상황 | HttpStatus | 코드명 |
|------|-----------|--------|
| 유효성 검사 실패 (서비스 레벨) | 400 | `INVALID_{TARGET}_{설명}` |
| 미인증 접근 | 401 | `UNAUTHORIZED` (AuthErrorCode에만 정의) |
| 권한 없음 | 403 | `FORBIDDEN` (AuthErrorCode에만 정의) |
| 엔티티 없음 | 404 | `{TARGET}_NOT_FOUND` |
| 리소스 충돌 | 409 | `{TARGET}_ALREADY_EXISTS` |

동일 타겟 + 동일 HttpStatus가 여러 개인 경우 Number PostFix를 10 단위로 분리:
```java
PHONENUMBER_ALREADY_EXISTS(HttpStatus.CONFLICT, "US-409-11", "이미 존재하는 전화번호입니다.")
NICKNAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "US-409-21", "이미 존재하는 닉네임입니다.")
```

### 외부 API 예외
```java
// ❌ 금지 — 외부 API 예외를 BusinessException으로 처리
throw new BusinessException(ExternalErrorCode.KAKAO_API_ERROR);

// ✅ 반드시 ExternalApiException 사용
catch (Exception e) {
    throw new ExternalApiException(
        "Kakao Directions API 호출에 실패했습니다.",
        "APIS-NAVI.KAKAOMOBILITY.COM/DIRECTIONS",
        e
    );
}
```

## API 응답

```java
return ResponseEntity.ok(ApiResponse.success(data));
return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(data));
return ResponseEntity.ok(ApiResponse.deleted());
```

## 모듈 맵

| 모듈 | 역할 |
|------|------|
| `studio` | 스튜디오 등록/관리, 임시저장(draft) |
| `room` | 룸/공간 정보 및 가격 |
| `owner` | 사장님 계정 관리 |
| `musician` | 뮤지션 프로필 |
| `auth` | 인증/인가 (OAuth2 Kakao·Google, JWT) |
| `filestorage` | S3 파일 업로드/관리 정책 엔진 |
| `search` | 검색 |
| `map` | 지도/지오코딩 (PostGIS) |
| `subway` | 근처 지하철역 |
| `report` | 신고 |
| `inquiry` | 문의 |
| `sms` | NCP SENS SMS |
| `admin` | 관리자 |
| `common` | 공통 인프라 (예외, 유틸, 베이스 엔티티) |

## 크로스 모듈 규칙

### 파일 스토리지
파일 처리는 반드시 `FileStorageService`만 사용. `S3Executor` 직접 호출 금지.
세부 정책은 `filestorage/CLAUDE.md` 참고.
