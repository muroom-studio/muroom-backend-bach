# The day our IDs stopped fitting in JavaScript

*Part 3 of a three-part postmortem on Muroom, a five-person studio-search startup I founded. Parts 1 and 2: [pocket-money AWS](./01-pocket-money-aws.md), [deleting our JWT infrastructure](./02-deleting-jwt.md).*

---

We migrated our primary keys twice in one week. The second migration took three hours and was caused by the first.

## Migration one: sequences → TSID

Our schema began life conventionally: twenty-four PostgreSQL sequences, one per table, with `allocationSize = 1` — which means Hibernate made a round-trip to the database on every single INSERT just to ask "what number comes next?".

For a service whose feeds sort by recency, I wanted time-ordered IDs generated in the application. That left three candidates, and the comparison is worth showing because the winner isn't obvious:

| | TSID | UUID v4 | UUID v7 |
|---|---|---|---|
| Size | **8 bytes (bigint)** | 16 bytes | 16 bytes |
| Time-sortable | Yes | No | Yes |
| B-tree insert locality | Append-friendly | Random — page splits, cache misses | Append-friendly |
| Postgres support (2025, pg 17) | Plain `bigint` | Native `uuid` | No native generator yet |
| JSON safety | **Breaks past 2⁵³** (foreshadowing…) | Fine — it's a string | Fine |

UUID v4 fails the sorting requirement outright and shreds index locality on top. The interesting fight is TSID vs **v7**, which also sorts by time — and there the deciding argument was width. Our schema references entities by bare ID columns everywhere (more on why in the last section), so ID size doesn't appear once; it multiplies across every reference column and every index on it. Eight bytes versus sixteen is a structural difference, not a rounding error. Add that in late 2025 Postgres 17 had no native `uuidv7()` while the TSID library was mature, and TSID won.

I rolled it out the cautious way: one new domain as a pilot first, then every entity two days later, dropping thirty sequences in a single migration. IDs now cost zero round-trips and sort chronologically for free.

You may have noticed the one cell in that table where UUID quietly wins. So did I — eventually.

## Migration two: the one I didn't see coming

Within days, the frontend started showing entities that couldn't be found. The Next.js dev tools told the story: IDs in API responses didn't match the IDs in subsequent requests — the last few digits differed.

JavaScript's `Number` is an IEEE-754 double. Integers are exact only up to 2⁵³ − 1. A 64-bit TSID sails past that, and `JSON.parse` silently rounds it — no error, no warning, adjacent entities collapsing onto the same rounded number.

The fix is boring and industry-standard — serialize IDs as strings — but I didn't know that yet, and the PR I opened that afternoon still contains my favorite sentence I've ever written in a code review:

> "Is this really how it's done in production? Do people actually do this?"

They do. Twitter's API has shipped an `id_str` field alongside every numeric ID since 2010, for precisely this reason. Thirty-three files later, every response DTO in the codebase returned string IDs, and the rule stuck for everything we built afterwards.

## The quieter migration: deleting our foreign keys

A month later, the same instinct — *the database is not where this belongs* — went further. On the advice of a working backend engineer we consulted, we dropped every FK constraint in the studio domain: fourteen of them.

His argument was operational, not academic. When you're firefighting a live-user issue, FK chains dictate the order in which you're allowed to touch data. And they punish the soft-delete pattern our whole schema relied on, where parent rows vanish behind `deleted_at` while their children live on. Our rule became: **relax the database, enforce in code.**

Cross-domain references shrank to bare `studio_id` bigint columns. Integrity checks moved into the service layer. And uniqueness under soft delete got solved properly with partial unique indexes — `WHERE deleted_at IS NULL` — so a departed user's nickname is reusable, but only ever by one living account.

## Would I do it again?

At a bank? No — and that's the point. These were context decisions, not doctrine. Application-generated 64-bit IDs bought us insert throughput and free ordering, at the price of a frontend contract we had to learn the hard way. An FK-less schema bought operational freedom, at the price of integrity work the application now owns.

Even the TSID choice deserves a re-grade: Postgres 18 now ships a native `uuidv7()`, so the ecosystem argument has narrowed since we decided. The width argument hasn't — but a decision this contextual should be re-run, not defended.

What I'd keep in any context is the meta-lesson: **your ID type and your constraints are API contracts with every system downstream — including the JavaScript runtime you forgot was a decimal.**

---

*That's the series. The service is gone; the invoices, the git history, and these three lessons are what's left — which, for a first startup, is not a bad trade.*
