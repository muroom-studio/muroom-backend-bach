# We built JWT refresh tokens, then deleted the whole thing

*Part 2 of a three-part postmortem on Muroom, a five-person studio-search startup I founded. Part 1 — [how we ran AWS on pocket money](./01-pocket-money-aws.md) — explains the project; this one is about the day our auth architecture told us what it wanted to be.*

---

There's a moment in every JWT implementation where you quietly start building a session store and refuse to call it that. Ours came about a week in.

Our service had two kinds of users — musicians logging in with Kakao or Google OAuth, and studio owners with email and password. The obvious modern answer was JWT: short-lived access tokens, long-lived refresh tokens. My teammate built it properly, and "properly" is exactly where the trouble starts.

Refresh tokens need to be revocable — logout, stolen-token response, password change. Revocable means server-side state. So into Redis went every refresh token's ID, keyed by `jti`, with per-user sets so we could revoke all of a user's tokens at once, plus rotation-with-reuse-detection: if an old refresh token gets replayed after rotation, treat it as theft and kill the whole family.

If you've read an OAuth2 hardening guide, you'll recognize all of this as *correct*. It's the textbook design. And one evening, looking at the Redis keyspace — `refresh:{jti}`, `refresh:user:{id}` — I said the thing out loud:

**We have reimplemented sessions, with more steps.**

## The property we were paying for but not receiving

The entire value proposition of JWT is statelessness: any instance can verify a token with nothing but a key. The moment revocation forced a Redis lookup into the flow, that property was gone. We were paying JWT's full costs — two token lifetimes to tune, rotation edge cases, clock skew, a wider attack surface — and receiving none of its benefit.

Meanwhile, our deployment was a single Spring Boot instance behind one load balancer, serving one country. The scaling scenario that justifies stateless auth did not exist. And if it ever arrived, `spring-session-data-redis` is a one-dependency retrofit — the exit cost of sessions is low and known.

So I proposed we stop: rip out the token plumbing, authenticate with plain servlet sessions and a cookie, keep Spring Security's method-level role checks exactly as they were. My teammate — who had built the JWT flow — implemented the switch. I want to note that explicitly, because deleting your own working code because the architecture is better without it is a professional act that deserves credit.

## Where JWT survived

JWT didn't disappear entirely, and the survivors mark exactly where it earns its keep: **short-lived, single-purpose handshake tokens.**

- A ten-minute *signup token* carries OAuth identity across the gap between "we don't recognize this Kakao account" and "account created".
- A *phone-verification token* proves an SMS check passed, consumed by the signup and phone-change flows.

Both are self-contained claims with tiny TTLs, where statelessness is genuinely convenient and revocation is genuinely irrelevant. That's the JWT sweet spot, and it's a lot smaller than the internet suggests.

## The fossil record

The pivot is still visible in the repo: a fully implemented `RefreshTokenService` — rotation, reuse detection, the works — with zero callers, and Swagger docs still describing a token flow the API no longer speaks. I've left them in this story deliberately, because the lesson isn't "sessions good, JWT bad." It's narrower and more useful:

**If your JWT design keeps requiring server-side state to be safe, the architecture is telling you what it wants to be. Listen earlier than we did.**

*When would I reach for JWT again?* Multiple services verifying identity independently; third parties consuming your tokens; horizontal scale where a shared session store is a *measured* bottleneck; short-TTL delegated authorization. None of that describes a pre-launch startup this size — and pretending otherwise is how you end up maintaining distributed-systems infrastructure for eight users.

---

*Next in the series: [The day our IDs stopped fitting in JavaScript](./03-ids-javascript.md)*
