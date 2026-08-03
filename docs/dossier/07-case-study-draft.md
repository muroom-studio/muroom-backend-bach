# 07. Case Study Draft (EN) — 블로그 초안

> 플래그십 1편 + 후속 백로그 2편 개요. 어투는 1인칭 회고("I" = 인프라/스튜디오 도메인 소유자, "we" = 팀).
> 사실 검증: 모든 수치·사건은 01~06 문서의 [확인]/[실측]/[증언] 근거와 대응. 과장 없음.

---

## Flagship: "Our school grant went to marketing, so I ran AWS on pocket money"

*(대안 제목: "Running a two-person startup's cloud for ~$120/month — and what it actually cost us")*

**Target: ~1,100 words / 5-min read. Sections below are the draft.**

### The bill arrives in your own name

Last year, a teammate and I built Muroom — a map-based search service for music practice studios in Seoul. Two backend-adjacent students, one Spring Boot monolith, PostgreSQL with PostGIS, and a cold-call operation that signed up 110 studio owners and catalogued 130 studios with 1,273 rooms.

Our school's startup grant had one catch: we'd allocated all of it to marketing. Server costs came out of our own pockets. That single constraint shaped almost every infrastructure decision I made — and taught me more about cloud engineering than any tutorial.

We eventually received $1,000 in startup-program credits. Before that, every architectural choice had a very direct KPI: my bank account.

### What "cost-optimized" actually looked like

I owned the infrastructure end to end — first hand-clicked in the AWS console, then migrated to Terraform in February. The final architecture ran production *and* a dev environment for roughly $120/month:

- **A NAT instance instead of a NAT Gateway.** A `t4g.nano` running nftables masquerade costs ~$3/month; a managed NAT Gateway starts around $32 plus data processing. Trade-off: it's a single point of failure — so it got its own CloudWatch alarms, the only two alarms I allowed myself beyond the database.
- **Self-managed PostgreSQL on EC2 instead of RDS.** We actually *started* on RDS. When the pocket-money phase began to bite, I moved Postgres onto a `t4g.small` with PostGIS built in the instance's user-data. (First attempt used a community PostGIS image and failed; the "build it yourself from the official image" design everyone might mistake for elegance was born from that failure.)
- **Valkey on EC2 instead of ElastiCache**, ACL-authenticated, AOF-persisted.
- **One VPC, one ALB for both environments**, split by host-header rules — with a fixed 404 as the default action so the bare IP reveals nothing.
- **Twelve third-party API keys bundled into one Secrets Manager secret per environment**, because Secrets Manager bills per secret. The comment in my Terraform still says "비용 절감 차원" — for cost reasons.

### Giving up managed services without giving up their guarantees

Dropping RDS means dropping automated backups, failover, and credential rotation. I tried to win the important ones back:

- **Three backup layers**: daily EBS snapshots via DLM, nightly `pg_dump` to S3 (Intelligent-Tiering), and a systemd loop shipping WAL segments to S3 every 10 seconds — poor man's PITR.
- **Credential rotation on a database AWS doesn't manage.** I deployed the standard Serverless Application Repository rotation Lambda — the one built for RDS — against my self-hosted Postgres. It speaks plain libpq, so it works. The application side uses the AWS Advanced JDBC Wrapper's `awsSecretsManager` plugin, which re-fetches credentials on auth failure — so a rotation never requires a redeploy. Dev rotated daily as a canary; prod weekly. Wiring this up took one long evening and four commits, the last one titled "fix … 3차!!!!!!!!!!!!!!!" — I keep it in the history as a reminder of what "simple integration" means.
- **No SSH anywhere.** Every instance is reachable only through SSM Session Manager; there isn't a key pair in the account.

### When cost pressure reaches the product

The clearest lesson wasn't in the infrastructure at all. Search results originally showed *walking time* to the nearest subway station, computed by calling a paid directions API — once per studio, per page. Cost scaled linearly with browsing. I looked for a free public-data alternative for geocoding-adjacent needs and found the government road-address API (which we adopted for geocoding — after I phoned the agency to confirm we were allowed to store results in our DB; the commercial map APIs forbid that). But its rate limit made bulk work impossible, and nothing free would give us walking time at browse-time volume.

So I shipped a hotfix: delete the directions integration, compute straight-line Haversine distance in Java, show that instead. Strictly worse information — strictly zero marginal cost. Pre-revenue, that's the right trade, and making it *consciously* is the skill I'd defend in any design review.

### The invoice tells the story

The monthly bill draws the whole arc by itself: **$98** in November (console-clicked infra, RDS), climbing to a peak of **$236** in January as we added a dev environment the manual way, then dropping to a steady **~$150** the month I finished the Terraform migration and the RDS-to-EC2 move. That $86/month cliff is the optimization, visible in the ledger.

Except ~$150 wasn't the real footprint. It was ~$125.

### What it cost us

Honesty section. The savings had a price, and I knew most of it at the time:

- **Everything data lives in one availability zone.** EBS volumes pin you; multi-AZ would have doubled the data-tier bill. Production's "multi-AZ" ECS spread was cosmetic — an AZ-a outage would have taken the whole system down.
- **Backup retention measured in days, not months.** WAL kept 3 days, dumps 60, dev basically ephemeral.
- And the part I *didn't* know: **~$25/month of my bill was zombie infrastructure.** The old RDS instance — I was sure I'd deleted it after the migration — ran for six more months at zero connections, alongside the entire old hand-clicked VPC and a mystery instance literally named `temp`. I only found them while auditing costs for this write-up. By then we'd received startup credits, and credits are wonderful — but they are also anesthesia. When the invoice says $0, you stop reading the invoice. (My favorite detail: the credential-rotation Lambda is *still* dutifully rotating the dev password every 24 hours, on a service nobody uses. Automation outlives intent.)

### Epilogue

Muroom never got past soft launch — eight real users, the rest friends-and-family testing. We're winding the infrastructure down this month as the credits run out. By the usual startup metrics, that's a failure.

But I walked away with a working mental model of everything a managed service does for you — because I had to rebuild each piece by hand and decide, line by line, which guarantees were worth paying for. The next time someone asks me "RDS or self-managed?", my answer starts with a number, not an opinion.

---

## Post #2 (full draft): "We built JWT refresh tokens, then deleted the whole thing"

*(~700 words / 3.5-min read)*

There's a moment in every JWT implementation where you quietly start building a session store and refuse to call it that. Ours came about a week in.

Our service had two kinds of users — musicians logging in with Kakao or Google OAuth, studio owners with email and password. The obvious modern answer was JWT: short-lived access tokens, long-lived refresh tokens. My teammate built it properly, and "properly" is exactly where the trouble starts. Refresh tokens need to be revocable — logout, stolen-token response, password change. Revocable means server-side state. So into Redis went every refresh token's ID, keyed by `jti`, with per-user sets so we could revoke all of a user's tokens at once, plus rotation-with-reuse-detection: if an old refresh token gets replayed after rotation, treat it as theft and kill the family.

If you've read an OAuth2 hardening guide, you'll recognize all of this as *correct*. It's the textbook design. And one evening, looking at the Redis keyspace — `refresh:{jti}`, `refresh:user:{id}` — I said the thing out loud: **we have reimplemented sessions, with more steps.**

The entire value proposition of JWT is statelessness: any instance can verify a token with nothing but a key. The moment revocation forced us to check Redis on every refresh, that property was gone. We were paying JWT's costs — two token lifetimes to tune, rotation edge cases, clock skew, a bigger attack surface — and receiving none of its benefit. Meanwhile our deployment was a single Spring Boot instance behind one load balancer, serving one country. The scaling scenario that justifies stateless auth did not exist, and if it ever did, `spring-session-data-redis` is a one-dependency retrofit.

So I proposed we stop: rip out the token plumbing, authenticate with plain servlet sessions and a cookie, keep Spring Security's method-level role checks. My teammate — who had built the JWT flow — implemented the switch, which I want to note because deleting your own working code is a professional act that deserves credit.

JWT didn't disappear entirely, and the survivors mark exactly where it earns its keep: short-lived, single-purpose handshake tokens. A ten-minute signup token carries OAuth identity between "we don't know you" and "account created". A phone-verification token proves an SMS check passed. Both are self-contained claims with tiny TTLs where statelessness is genuinely convenient and revocation is genuinely irrelevant.

The fossil record of the pivot is still in the repo: a fully-implemented `RefreshTokenService` — rotation, reuse detection, the works — with zero callers, and Swagger docs still describing a token flow the API no longer speaks. I've left them in this story deliberately, because the lesson isn't "sessions good, JWT bad." It's narrower and more useful: **if your JWT design keeps requiring server-side state to be safe, the architecture is telling you what it wants to be.** Listen earlier than we did.

*When would I reach for JWT again?* Multiple services verifying identity independently; third parties consuming your tokens; horizontal scale where a shared session store is a measured bottleneck; delegated authorization with short TTLs. None of that describes a pre-launch two-person startup — and pretending otherwise is how you end up maintaining distributed-systems infrastructure for eight users.

## Post #3 (full draft): "The day our IDs stopped fitting in JavaScript"

*(~650 words / 3-min read)*

We migrated our primary keys twice in one week. The second migration took three hours and was caused by the first.

**Migration one: sequences → TSID.** Our schema began life conventionally: twenty-four PostgreSQL sequences, one per table, `allocationSize = 1` — which means Hibernate makes a round-trip to the database for every single INSERT just to ask "what number is next?". For a service whose feeds sort by recency, I wanted time-ordered IDs; we compared UUIDs (128 bits, and v4 fragments your B-tree) against TSID — a 64-bit, time-sortable, Snowflake-style ID generated in the application. TSID won. I rolled it out the cautious way: new domain first as a pilot, then every entity two days later, dropping thirty sequences in one migration. IDs now cost zero round-trips and sort chronologically for free.

**Migration two: the one I didn't see coming.** Within days, our frontend started showing entities that couldn't be found. The Next.js dev tools told the story: IDs in API responses didn't match IDs in requests — the last few digits differed. JavaScript's `Number` is an IEEE-754 double; integers are exact only up to 2^53 − 1. A 64-bit TSID sails past that, and `JSON.parse` silently rounds it. No error, no warning. Adjacent entities collapse onto the same rounded number.

The fix is boring and industry-standard — serialize IDs as strings — but I didn't know that yet, and the PR I opened that afternoon still contains my favorite sentence I've ever written in a code review: *"Is this really how it's done in production systems? Do people actually do this?"* (They do. Twitter's API has shipped an `id_str` field alongside every numeric ID since 2010, for precisely this reason.) Thirty-three files later, every response DTO in the codebase returned string IDs, and the rule stuck for everything we built afterwards.

**The quieter migration: deleting our foreign keys.** A month later, the same "the database is not where this belongs" instinct went further. On advice from a working backend engineer we consulted, we dropped every FK constraint in the studio domain — fourteen of them. His argument was operational: when you're firefighting a live-user issue, FK chains dictate the order you can touch data, and they punish the soft-delete pattern (parent rows vanish behind `deleted_at` while children live on) that our whole schema relied on. Our rule became: **relax the database, enforce in code.** Cross-domain references shrank to bare `studio_id` bigint columns; integrity checks moved into the service layer; uniqueness under soft delete got solved properly with partial unique indexes (`WHERE deleted_at IS NULL` — so a departed user's nickname is reusable, but only by one living account).

Would I make the same three calls at a bank? No, and that's the point — these were context decisions, not doctrine. Application-generated 64-bit IDs bought us insert throughput and ordering at the price of a frontend contract we had to learn the hard way. FK-less schemas bought operational freedom at the price of integrity work the application now owns. What I'd keep in any context is the meta-lesson: **your ID type and your constraints are API contracts with every system downstream — including the JavaScript runtime you forgot was a decimal.**

## 저자 표기 원칙 (블로그 공통)

- "I": 인프라 전체, 스튜디오/검색 도메인, filestorage, 데이터 계층 결정, JWT→세션 **결정·설계**.
- "we/teammate": 인증 구현, musician/owner 도메인, SMS, 마이페이지 계열.
- 수치는 실측/증언 근거 있는 것만: $150→$120, $1,000 크레딧, 8 users, 130 studios/1,273 rooms, 110 cold calls, $21/mo orphan RDS.
