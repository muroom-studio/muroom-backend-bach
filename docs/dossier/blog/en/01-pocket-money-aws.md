# Our school grant went to marketing, so I ran AWS on pocket money

*Part 1 of a three-part postmortem on Muroom, a five-person studio-search startup I founded and we wound down after ten months. All numbers come from our actual AWS bills and git history.*

---

Last year I founded Muroom, a map-based search service for music practice studios in Seoul, with a five-person team: two designers, a frontend developer, and two of us on the backend. One Spring Boot monolith, PostgreSQL with PostGIS, and a cold-call operation that signed up 110 studio owners and catalogued 130 studios with 1,273 rooms.

Our school's startup grant had one catch. We had allocated all of it to marketing. Server costs came out of our own pockets. That single constraint shaped almost every infrastructure decision I made, and taught me more about cloud engineering than any tutorial.

We eventually received $1,000 in startup-program credits. Before that, every architectural choice had one KPI, and it was my bank account.

## What "cost-optimized" actually looked like

I owned the infrastructure end to end: first hand-clicked in the AWS console, then migrated to Terraform in February. The final architecture ran production *and* a dev environment for roughly $150/month, and here is what that money didn't buy:

- **A NAT instance instead of a NAT Gateway.** A `t4g.nano` running nftables masquerade costs about $3/month; a managed NAT Gateway starts around $32 plus data processing. The trade-off is that it's a single point of failure, which is why it got its own CloudWatch alarms: two of the only four alarms I allowed myself.
- **Self-managed PostgreSQL on EC2 instead of RDS.** We actually *started* on RDS. When the pocket-money phase began to bite, I moved Postgres onto a `t4g.small`, with PostGIS built in the instance's user-data. My first attempt used a popular community PostGIS image and failed in three different ways; the "build it yourself from the official image" setup that looks like a considered choice in the repo was born from that failure.
- **Valkey on EC2 instead of ElastiCache**, ACL-authenticated, AOF-persisted.
- **One VPC and one ALB shared by prod and dev**, split by host-header rules, with a fixed 404 as the default action, so hitting the bare IP tells you nothing.
- **Twelve third-party API keys bundled into one Secrets Manager secret per environment**, because Secrets Manager bills per secret. The comment in my Terraform still reads "비용 절감 차원" (*for cost reasons*).

## Giving up managed services without giving up their guarantees

Dropping RDS means dropping automated backups, failover, and credential rotation. I tried to win the important ones back:

- **Three backup layers.** Daily EBS snapshots via DLM, a nightly `pg_dump` to S3 on Intelligent-Tiering, and a systemd loop shipping WAL segments to S3 every ten seconds. A poor man's point-in-time recovery.
- **Credential rotation on a database AWS doesn't manage.** I deployed the standard Serverless Application Repository rotation Lambda, the one built for RDS, against my self-hosted Postgres. It speaks plain libpq, so it works. On the application side, the AWS Advanced JDBC Wrapper's `awsSecretsManager` plugin re-fetches credentials on auth failure, so a rotation never requires a redeploy. Dev rotated daily as a canary, prod weekly. Wiring this took one long evening and four commits, the last one titled *"fix … 3차!!!!!!!!!!!!!!!"*. I keep it in the history as a monument to what "simple integration" means.
- **No SSH anywhere.** Every instance is reachable only through SSM Session Manager. There isn't a single key pair in the account.

## When cost pressure reaches the product

The clearest lesson wasn't in the infrastructure at all. Search results originally showed *walking time* to the nearest subway station, computed by calling a paid directions API, once per studio, per page. Cost scaled linearly with browsing.

The free alternatives didn't save us either. For geocoding we had already chosen the government road-address API, partly because it's free and partly because the commercial map APIs forbid storing their results in your own database, which I confirmed by literally phoning the agency to ask whether we were allowed to persist coordinates (we were). But public APIs come with strict rate limits; a bulk job I tried to build against one died at five requests per ten seconds.

So I shipped a hotfix that deleted the directions integration and showed straight-line Haversine distance computed in Java instead. Strictly worse information, at exactly zero marginal cost. Pre-revenue, that's the right trade, and making it *consciously* is the skill I'd defend in any design review.

## The invoice tells the story

The monthly bill draws the whole arc by itself: **$98** in November (console-clicked infra, RDS), climbing to a peak of **$236** in January as we added a dev environment the manual way, then dropping to a steady **~$150** the month I finished the Terraform migration and the RDS-to-EC2 move. That $86-per-month cliff is the optimization itself, sitting right there in the ledger.

Except ~$150 wasn't the real footprint. It was ~$125.

## What it cost us

The savings had a price. Most of it I knew at the time:

- **Everything data lives in one availability zone.** EBS volumes pin you; multi-AZ would have doubled the data-tier bill. Production's "multi-AZ" ECS spread was cosmetic; an AZ-a outage would have taken the whole system down.
- **Backup retention measured in days, not months.** WAL kept three days, dumps sixty, dev basically ephemeral.

And the part I *didn't* know: **about $25 a month of my bill was zombie infrastructure.** The old RDS instance, which I was sure I'd deleted after the migration, ran for six more months at zero connections, alongside the entire old hand-clicked VPC and a mystery instance literally named `temp`. I only found them while auditing costs for this write-up. By then the credits had arrived, and credits are wonderful, but they are also anesthesia. When the invoice says $0, you stop reading the invoice.

My favorite detail is that the credential-rotation Lambda is *still* dutifully rotating the dev password every twenty-four hours, on a service nobody uses. Automation outlives the intentions of whoever set it up.

## Epilogue

Muroom never got past soft launch. Eight real users, the rest friends-and-family testing. We're winding the infrastructure down this month as the credits run out. By the usual startup metrics, that's a failure.

But I walked away with a working mental model of everything a managed service does for you, because I had to rebuild each piece by hand and decide, line by line, which guarantees were worth paying for. The next time someone asks me "RDS or self-managed?", my answer starts with a number, not an opinion.

---

*Next in the series: [We built JWT refresh tokens, then deleted the whole thing](/writing/muroom-deleting-jwt)*
