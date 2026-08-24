# I fixed our cold start. Then I measured it: nothing.

*A short one from the [Muroom postmortem series](/writing/muroom-aws-on-pocket-money). A fix that worked, a benchmark that said otherwise, and why both can be true.*

---

In January our first request after every deploy was slow. Slow enough to feel, on a `t4g`-class instance, right when the ALB started routing traffic to a freshly started Spring Boot app.

I fixed it the way you fix things under deadline: all at once. One commit added three remedies — a warmup runner that touches the database and then *explicitly* publishes `ReadinessState.ACCEPTING_TRAFFIC`, `load-on-startup: 1` so the DispatcherServlet initializes at boot instead of on the first request, and `spring-context-indexer` to speed component scanning. A month later the ALB health check was pointed at `/actuator/health/readiness`, completing the design: no traffic until the app says it's warm.

It felt better in production. We moved on. I never measured it — there were 130 studios to catalogue.

## The benchmark, seven months late

While writing this series I finally ran the A/B I should have run in January: same app, same local database, warmup stack on versus off, timing the first requests after boot.

| | With fixes | Without |
|---|---|---|
| Startup | 5.23 s | 5.20 s |
| First request | 74 ms | 80 ms |
| First DB query | 25 ms | 27 ms |

Six milliseconds. Noise. On my laptop, the celebrated fix does approximately nothing.

## Why both things are true

The lazy conclusion is "the fix was placebo." The interesting conclusion is that I fixed a different problem than the one I named.

The laptop benchmark can't reproduce what production actually was: a burstable ARM instance cold-loading and JIT-compiling thousands of classes, a connection pool filling ten connections against a *remote* database, and, above all, **a load balancer that had already started routing traffic before any of that finished.** On my machine those costs are milliseconds. On a `t4g` behind an ALB, they stack into the first user's request.

Which means the component doing the real work was probably never the warmup query or the classpath index. It was the *readiness gate*, the explicit "do not send traffic until I say so." Which is a correctness fix, not a performance one. Slow initialization is fine if nobody's request is waiting inside it.

## What I actually learned

1. **Bundle fixes, inherit confusion.** Three remedies in one commit means never knowing which one worked. I'd still ship them together under deadline, but I'd label the commit with the mechanism I *believed* in, so future-me knows what to test.
2. **Measure at the time, not at the postmortem.** The production condition that made the fix matter no longer exists (the infrastructure is wound down); my window for a real number is gone forever.
3. **A null result is still a result.** "My fix shows no effect in the environment I can test" is uncomfortable to publish and more useful than most benchmarks I've read, because the next person shipping a warmup listener should know the speed-up may be an illusion, and the readiness gating is the part to keep.

If you take one line from this: **when you can't make it fast, make it not matter that it's slow**. And be honest about which one you achieved.

---

*The series: [pocket-money AWS](/writing/muroom-aws-on-pocket-money) · [130 studios, 8 users](/writing/muroom-130-studios-8-users)*
