# 콜드스타트 로컬 근사 측정 (2026-08-03)

환경: M-series Mac, 로컬 Docker PG(55432)/Valkey, jar 직접 실행, local 프로파일 + 더미 외부 키.
방법: A = 현재 코드(WarmupListener + load-on-startup:1 + context-indexer) / B = WarmupListener 비활성(@ConditionalOnProperty 임시 부여) + load-on-startup:-1. 각 기동 직후 동일 순서 curl.

| 지표 | A (워밍업 有) | B (베이스라인) |
|---|---|---|
| Spring 기동 | 5.233s | 5.204s |
| 첫 GET /api/ping | 74.4ms | 79.6ms |
| 첫 GET /api/v1/instruments (DB) | 25.1ms | 26.8ms |
| 2번째 instruments | 6.5ms | 7.2ms |

## 결론

- **로컬에서는 워밍업 유무의 차이가 오차 범위(≤6ms)** — 콜드스타트 대책(bcdff79)의 가치는 로컬 재현 불가.
- 해석: 프로드에서 겪은 콜드스타트는 ① t4g 버스터블 CPU에서의 클래스로딩/JIT ② 원격 DB로의 Hikari 풀 초기화(min-idle 10) ③ **ALB가 앱 준비 완료 전에 트래픽을 보내는 타이밍 문제**가 지배 요인이었을 것 [추론]. 특히 ③은 지연이 아니라 **정합성 문제**이고, WarmupListener의 진짜 기여는 "빨라짐"이 아니라 **명시적 `ACCEPTING_TRAFFIC` 발행으로 준비 완료 전 트래픽 수신을 구조적으로 차단**한 것.
- **블로그/면접 사용 지침**: 개선 수치를 주장하지 말 것. "we gated readiness explicitly" (correctness fix) 프레이밍이 정직하고 오히려 더 좋은 스토리. 프로드 동등 측정은 리소스 종료로 불가능해짐.
