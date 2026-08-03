# [RAW] 인증/인가 서브시스템 조사 결과 (에이전트 원본 + 검증, 2026-08-02)

> **헤드라인**: `auth/jwt` + `auth/filter` 패키지가 있음에도, 런타임 요청 인증은 **HTTP 세션(JSESSIONID) 기반**이지 JWT가 아니다. JWT는 (1) 가입/휴대폰 인증용 단기 핸드셰이크 토큰과 (2) **어떤 엔드포인트에도 연결되지 않은** 리프레시 토큰 로테이션 서비스에만 쓰인다.
> 핵심 주장 3건은 오케스트레이터가 grep으로 재검증함: `@EnableScheduling` 부재, admin 컨트롤러 `@PreAuthorize` 부재, `RefreshTokenService` 호출자 부재 — 모두 [확인].

## 1. Spring Security 구성

- 단일 `SecurityFilterChain` (`auth/config/SecurityConfig.java:22`), **커스텀 필터 add 없음**.
- CSRF 비활성(`:25`), 세션 `IF_REQUIRED`(`:26-27`), formLogin/httpBasic 비활성, entry point → bare 401 / access denied → bare 403(`:31-35`).
- `@EnableMethodSecurity`(`:17`) — 실제 권한 체크는 컨트롤러의 `@PreAuthorize`.
- CORS: `Customizer.withDefaults()`인데 `CorsConfigurationSource` 빈 없음; 유일한 CORS 정의는 MVC 레벨 `WebConfig.java:36-42` (`allowCredentials(true)`).
- permitAll 목록(`:37-56`): `/api/ping`, `/actuator/**`, swagger, `/api/v1/auth/**`, `/api/v1/musicians/register|nickname/check|phone/check`, `/api/v1/studios/**`, `/api/v1/faq/**`, `/api/v1/inquiry-categories/**`, `/api/v1/instruments/**`, `/api/v1/search-histories/**`, `/api/v1/subways/**`, `/api/v1/terms/**`, `/docs`, `/api/v1/sms/**` → 나머지 `.anyRequest().authenticated()`.

### 역할 모델
- `UserType(OWNER, MUSICIAN, ADMIN)`. 권한은 문자열 조립, 역할 테이블 없음.
- 권한 발급처 3곳이 서로 불일치: ① `SessionAuthService.java:40` `ROLE_{userType}` — **실제로 동작하는 유일한 경로** ② `OwnerUserDetailsService.java:29` ROLE_OWNER(로그인 내부용, 폐기됨) ③ `JwtAuthenticationFilter.java:41` `ROLE_USER` — 어느 `@PreAuthorize`와도 매칭 안 됨.
- **[확인·중대] ADMIN 인가가 전혀 없음**: `hasRole('ADMIN')` 0건, `ROLE_ADMIN` 0건, admin 컨트롤러 7개 전부 `@PreAuthorize` 없음, `/api/admin/**`은 permitAll도 아님 → `.anyRequest().authenticated()`로 떨어져 **로그인한 뮤지션/사장님 누구나 모든 admin API 호출 가능**. (오케스트레이터 grep 재검증 완료)

### queries/ SQL 2개
- `[2]admin-level.sql` = terms 시드, `[3]owner-level.sql` = owner/studio/room 시드. 어디서도 참조 안 됨 — 수동 시드 스크립트. admin 인증과 무관.

## 2. OAuth2 로그인 (Kakao/Google)

- **spring-security-oauth2-client 미사용** (의존성은 있으나 `oauth2Login`/`ClientRegistration` 참조 0건). Feign도 아니고 **필드로 `new RestTemplate()`** 수동 토큰 교환.
- 구조: `MusicianAuthController` → `OAuthLoginService`(오케스트레이터) → SPI `OAuthClientService` → `KakaoOAuthClientService`/`GoogleOAuthClientService`.

### `POST /api/v1/auth/musician/login` 트레이스
1. Body `{provider, providerId}` — **`providerId`가 실제로는 authorization code**. `Origin` 헤더가 redirect-URI 베이스가 됨 (`MusicianAuthController.java:34-47`).
2. `OAuthLoginService.login`(`:44`): provider enum 변환, `@PostConstruct`로 만든 clientMap에서 클라이언트 선택. **NAVER는 enum에 있지만 빈이 없어 `clientMap.get(NAVER)`=null → NPE** (UNSUPPORTED가 아니라).
3. 토큰 교환: Kakao `https://kauth.kakao.com/oauth/token` (redirect = `origin+"/redirect/oauth/kakao"`), Google `https://oauth2.googleapis.com/token` (code URL-decode 후). `id_token`만 사용.
4. sub 추출: **Kakao는 `JWT.decode`만 — 서명 미검증** (`KakaoOAuthClientService.java:48`). **Google은 완전 검증** — JWKS fetch + RSA256 + aud/iss (`GoogleOAuthClient.java:83-102`). 비대칭.
5. `SocialAccountRepository.findByProviderAndProviderUserId` — 있으면 musicianId 반환, 없으면 **signupToken**(JWT, type=SIGNUP, provider, providerId) 발급 + `SIGNUP_REQUIRED`.
6. LOGIN이면 `SessionAuthService.login(..., MUSICIAN, userId)` → **JSESSIONID 발급. 로그인 시 JWT 액세스/리프레시 발급 없음.**
7. 가입 완료는 별도 호출 `POST /api/v1/musicians/register`: signupToken + smsVerifyToken 파싱 → Musician 생성 → SocialAccount 링크 → MyStudio 생성. **가입 후 클라이언트가 다시 /login 호출해야 세션 획득.**

## 3. JWT

- **라이브러리 2개 다 사용**: jjwt 0.12.3 = 자체 토큰 발급/파싱, auth0 java-jwt = 프로바이더 ID 토큰 디코드/검증.
- `JwtTokenProvider` — HMAC-SHA256, `jwt.secret-key`. 토큰 4종:

| 토큰 | 클레임 | TTL |
|---|---|---|
| ACCESS | sub, type, userType | local **30일**(주석은 "15분"!), dev/prod 15분 |
| REFRESH | sub, type, userType, jti | 1시간 |
| SIGNUP | type, provider, providerId (sub 없음) | 10분 |
| PHONE_VERIFY | type, phoneNumber, jti | 10분 |

- **`RefreshTokenService`(Valkey 저장, jti 키, rotate/reuse-검출까지 구현) — 호출자 0. `/refresh` 엔드포인트 없음. [확인] 데드코드.** Swagger docs(`OwnerAuthControllerDocs.java:24-29` 등)는 여전히 "Access/Refresh Token 발급" 흐름을 문서화 — 코드와 문서 불일치.
- **`JwtAuthenticationFilter`는 시큐리티 체인에 미등록** — `@Component`라 서블릿 필터로만 자동 등록되는데 이는 springSecurityFilterChain **뒤** 순서. principal이 raw Long이라 양쪽 ArgumentResolver 모두 거부, 권한도 ROLE_USER로 무의미 → **Bearer 토큰 경로 전체가 사실상 비기능** [확인 수준: 코드 정황 매우 강함].
- `parseRefreshToken`은 `UserType.MUSICIAN` 하드코딩(`:156`) — 미사용이라 무해.
- PHONE_VERIFY/SIGNUP의 jti는 "1회 사용 처리용" 주석에도 불구 **저장/소각 안 함** → TTL 내 재사용 가능.

## 4. 세션

- `SessionAuthPrincipal(userType, userId)` record — 라이브 SecurityContext에 저장되는 유일한 principal.
- `SessionAuthService.login`: ① `AnonymousUserContext` 읽어 **게스트 즐겨찾기 → 회원 마이그레이션** (Redis Lua, `StudioFavoriteCommandService.java:93-109`) ② `ROLE_{userType}` 인증 토큰 구성 ③ 자체 생성한 `HttpSessionSecurityContextRepository`로 `SPRING_SECURITY_CONTEXT`를 HttpSession에 저장.
- **세션 저장소는 in-memory Tomcat** — spring-session 의존성 없음, `spring.session.*` 설정 없음. **Valkey에는 세션이 없다.** Valkey 용도: 리프레시 jti(데드), SMS 인증 상태(`sms:*`), 스튜디오 즐겨찾기(`fav:*`/`favset:*`/`favcnt:*`).
- 다중 인스턴스 배포 시 세션 유실 문제 내재(현재 desired_count=1이라 표면화 안 됨) — 인프라 문서와 연결.

## 5. 익명 사용자 장치

- `AnonymousUserFilter`(서블릿 필터): 쿠키 `anonymous_user_id` + `anonymous_user_sig`(HMAC, **`jwt.secret-key` 재사용**), 없으면 UUID 발급(1년, HttpOnly, Secure, SameSite=Lax). ThreadLocal `AnonymousUserContext`에 저장, finally에서 클리어.
- `@CurrentSubjectId` resolver: 로그인 시 `"U:"+userId`, 비로그인 시 `"G:"+anonymousUserId` — 게스트 허용 엔드포인트(Studio 검색/즐겨찾기)에서 사용. 이 접두사가 `SubjectParser`(`common/util`)로 다시 해석됨.
- `@CurrentUserId` resolver: SecurityContext에서 SpEL `"userId"` 평가 → SessionAuthPrincipal.userId.

## 6. Owner 로그인 (패스워드) + SMS

- 사장님은 OAuth 없음 — 이메일+비밀번호. `POST /api/v1/auth/owner/login` → `OwnerPasswordLoginService` → 전용 `AuthenticationManager`(`ProviderManager(DaoAuthenticationProvider)`, `SecurityUserConfig.java:24-36`) → `OwnerUserDetailsService.loadUserByUsername` → `findByEmail` + `isActive` 필터 → BCrypt 검증 → **ownerId만 꺼내고 principal 폐기** → `SessionAuthService.login(OWNER)` → JSESSIONID.
- SMS 인증 (`/api/v1/sms/**`, NCP SENS): 6자리 SecureRandom, TTL 3분, 재전송 쿨다운 10초, 폰당 5회/일, IP당 30회/일, 실패 5회 제한 (`SmsVerificationService`). 성공 시 **PHONE_VERIFY JWT** 반환.
- smsVerifyToken 소비처: owner 가입(`OwnerCommandService.registerOwner:47-69` — 파싱→중복검사→필수약관검사→BCrypt 인코딩 저장), owner 전화번호 변경(`:150-166`), musician 가입(`MusicianService.java:64`).

## 7. 미해결 질문 / 특이점 (에이전트 제기, 중요도순)

1. **admin API 무보호** — 게이트웨이/별도 admin 서비스가 앞단에 있는가? (인프라 조사로는 ALB → ECS 직결, WAF 없음)
2. JWT→세션 전환은 의도된 후퇴인가? (JwtAuthenticationFilter/RefreshTokenService/LogoutRequest 데드코드 + Swagger 문서 잔재)
3. permitAll 경로 불일치: `/api/v1/faq/**` vs 실제 `/api/v1/faqs`, `/api/v1/subways/**` vs `/api/v1/subway` → 인증 요구됨. **owner 가입 계열(`/api/v1/owners/register` 등)이 permitAll에 없어 비로그인 가입이 막혀 보임.**
4. CSRF 비활성 + 쿠키 세션 + allowCredentials(true) + JSESSIONID SameSite 미설정 — CSRF 노출면.
5. Kakao ID 토큰 서명/aud/iss/exp 미검증 (Google과 비대칭).
6. PasswordEncoder 빈 2개(SecurityUserConfig, WebConfig) — 파라미터명으로 우연히 해소되는 fragile 구성.
7. `RestTemplate` 필드 new — 타임아웃 없음, Google JWKS 매 로그인 fetch(캐시 없음).
8. `MusicianAuthControllerDocs.java:49,73` Swagger 설명에 실제로 보이는 Kakao REST 키/Google client ID 하드코딩.
9. `OAuthLoginService.login`이 `@Transactional(readOnly=true)`인데 가입 분기 진입점.
10. AnonymousIdSigner가 jwt.secret-key 재사용 — 목적 다른 키 재사용.
