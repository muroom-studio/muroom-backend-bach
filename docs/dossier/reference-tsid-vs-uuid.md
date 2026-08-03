# TSID vs UUID — 채택 근거 정리 (Confluence 원본 유실 후 재구성)

> 2026-08-03 재구성. 원본은 2025-12 작성 후 접근 유실 [증언 — Q9: "정렬 등 여러 이점"].
> 이 문서는 기술 사실 기반 재구성이며, 당시 문서와 표현은 다를 수 있음. 실제 채택 컨텍스트: Spring Boot 3.5 / JPA / PostgreSQL 17 / 최신순 피드 정렬 / `tsid-creator` 5.2.6, NODE_BITS=8.

## 결론 먼저

**64-bit 시간 정렬 ID가 필요했고, UUID는 128-bit이거나(전부) 무작위였다(v4).** TSID는 bigint 하나로 시간 정렬·컴팩트·앱 사이드 생성을 동시에 만족. 대가는 ID의 JS 직렬화 문제(53-bit)와 순차성으로 인한 추측 가능성.

## 비교표

| 기준 | TSID (64-bit) | UUID v4 | UUID v7 |
|---|---|---|---|
| 크기 | **8B (bigint)** | 16B | 16B |
| 시간 정렬 | **O** (ms 단위 + 노드 + 카운터) | X (완전 무작위) | O (ms 단위) |
| B-tree 삽입 지역성 | **O** — 우측 append, 페이지 스플릿 최소 | **X** — 무작위 삽입, 스플릿·캐시 미스·WAL 증폭 | O |
| PK/보조 인덱스 크기 | **기준** | PK·모든 FK 컬럼·모든 인덱스가 2배 | 2배 |
| DB 왕복 | 앱 생성 = 0 (시퀀스 allocationSize=1의 INSERT당 왕복 제거) | 앱 생성 = 0 | 앱 생성 = 0 |
| PG 타입 | bigint (네이티브, 별도 타입 불필요) | uuid 타입 | uuid 타입 (**pg 18부터 `uuidv7()` 내장 — 채택 시점(2025-12, pg 17)엔 확장/앱 생성 필요**) |
| 문자 표현 | Crockford Base32 **13자** (URL 친화) | 36자 | 36자 |
| JSON/JS | **취약** — number로 내보내면 2^53 초과 시 절단 → **String 직렬화 강제** (실제로 당한 사고, PR #51) | 안전 (애초에 문자열) | 안전 |
| 추측 가능성 | 준순차 — 열거 공격 표면 있음 (참조 무결성보단 URL 노출 설계 시 고려) | **무작위 — 추측 불가** | 시간 부분 노출 |
| 생성 시각 노출 | O (ID에서 생성 시각 유추 가능) | X | O |
| 노드 조정 | NODE_BITS=8 → **256 노드 한도**, 노드 ID 배정 필요 (우리는 랜덤 — 단일 인스턴스라 충돌 확률 무시 가능 수준) | 불필요 | 불필요 |
| 표준화 | 사실상 라이브러리 관행 (Snowflake 계열) | RFC 표준 | RFC 9562 (2024) |

## 당시 판단의 재구성

1. **피드가 최신순 정렬** → ID 자체가 시간 정렬이면 `ORDER BY created_at` 없이 PK 인덱스로 커서 페이지네이션 가능. v4는 탈락.
2. **인덱스 지역성** — v4의 무작위 삽입은 쓰기 워크로드에서 B-tree를 조각냄. 소형 인스턴스(t4g) 전제에서 캐시 효율이 실비용.
3. **크기** — FK 없는 설계라 크로스 도메인 참조 컬럼(`studio_id` 등)이 도처에 있음 → ID 크기가 모든 참조 컬럼·인덱스에 곱해짐. 8B vs 16B는 구조적 차이.
4. **v7은 왜 아니었나** — 정렬 문제는 풀지만 크기(16B)는 그대로였고, 채택 시점엔 PG 네이티브 지원 전(pg 18의 `uuidv7()`은 2025-09 릴리스, 우리는 pg 17). 라이브러리 성숙도도 tsid-creator가 앞서 있었음 [추론 — 당시 문서에 v7 언급 여부는 미상].
5. **수용한 대가** — ① JS 53-bit → 전 DTO String 직렬화 (이틀 만에 실제로 지불한 대가) ② ID에서 생성 시각 유추 가능 ③ 준순차 열거 가능성 ④ 256 노드 한도(단일 인스턴스라 무관).

## 면접 한 단락 버전 (EN)

> "We needed time-sortable IDs for recency-ordered feeds, generated app-side to kill the per-INSERT sequence round-trip. UUIDv4 fails sorting and shreds B-tree locality; v7 fixes ordering but is still 16 bytes — and since our schema references entities by bare ID columns everywhere, ID width multiplies across every reference and index. TSID gave us a native bigint, time-ordered, at half the width. We paid for it two days later when JavaScript silently rounded our 64-bit IDs past 2^53 — which is why every ID in our API is a string, same as Twitter's id_str. Today, on Postgres 18 with native uuidv7(), I'd re-run the comparison — the ecosystem argument has narrowed, the width argument hasn't."

관련: 03-data-layer-decisions.md §2, 08-interview-prep.md S3, blog/en/03.
