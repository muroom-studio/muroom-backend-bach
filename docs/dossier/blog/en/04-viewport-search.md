# Thirteen filters and no spatial index

*A technical companion to the [Muroom postmortem series](/writing/muroom-aws-on-pocket-money). How the map search actually worked, and why the "obvious" index never got built.*

---

The core interaction of Muroom was moving a map. Every pan or zoom sent the viewport's corners to the backend, which returned every studio inside the rectangle, as price-tagged markers and as a synced list, filtered by up to thirteen criteria at once. This post is about the three decisions inside that endpoint: how the geometry works, how thirteen optional filters compose into one query, and why, against every best-practice checklist, there is no spatial index.

## The viewport is one predicate

Studios store a `location` column of type `geography(Point, 4326)`. The viewport query is a single PostGIS predicate, injected through QueryDSL's template mechanism so it composes with everything else:

```java
Expressions.booleanTemplate(
    "st_intersects({0}, st_makeenvelope({1}, {2}, {3}, {4}, 4326))",
    studio.location, minLng, minLat, maxLng, maxLat);
```

No radius math, no bounding-box arithmetic in Java. The database answers "which points fall inside this envelope." We kept the whole query in QueryDSL rather than dropping to native SQL for one reason: the viewport is never alone. It's one `BooleanBuilder` entry among fourteen.

## Thirteen filters, one builder

Every filter is a method that returns a predicate, or `null` when its parameter is absent, which `BooleanBuilder.and()` silently skips. Price range, room dimensions (millimeters; studio owners think in exact wall sizes), floor type, parking, lodging, fire insurance, restroom location and gender, forbidden instruments, per-category amenities, and keyword.

Three of them taught me something:

- **Amenities use ALL-semantics.** "Has water purifier AND shower" is a `GROUP BY studio_id HAVING COUNT(*) = n` subquery, not an `IN`. Users filtering by amenities want every box ticked, not any.
- **The restroom filter is one client parameter, two predicates.** The UI sends a single `restroomTypes` set; a `@JsonCreator` constructor fans values out into location (`INTERNAL/EXTERNAL`) and gender (`SEPARATE/UNISEX`) enums server-side. The API stays simple; the domain stays precise.
- **Keyword search matches studios *and* their nearest stations.** Searching "홍대입구" finds studios near that station via an `EXISTS` over the station-link table. The first version fetched station IDs in a separate query and stuffed them into an `IN` clause; an automated code review pointed out the correlated-subquery form, and that refactor stuck.

Pagination is the classic two-phase pattern: an ID query with joins, ordering, and `LIMIT`, then an entity query by `id IN (...)` with in-memory reordering, so row-multiplying joins never corrupt page sizes. Sorting is a whitelist of exactly two keys (`latest`, `price`), because an open `ORDER BY` on client-supplied properties is an injection surface I didn't want to reason about.

## The index I didn't build

`studios.location` has **no GiST index**, so every viewport query is a sequential scan. Every checklist says this is wrong.

I know this not from the migrations (where an index conspicuously isn't) but from measuring the production database: `pg_indexes` shows a primary key and two unique constraints. And the same measurement explains why it doesn't matter: the table holds **130 studios**. A sequential scan over 130 rows fits in one page read; a GiST index would add write overhead and planner complexity to accelerate a query that was never slow.

We didn't forget the index. At our scale it was premature, the fix is one line (`CREATE INDEX ... USING GIST (location)`), and I know which threshold would trigger it: thousands of studios, city-wide viewports. Growth roadmap, in order: GiST on `location`, B-trees on the `studio_id` join keys the filter subqueries hit, `pg_trgm` for the leading-wildcard keyword match, and collapsing seven `building_info` EXISTS subqueries into one join.

## What I'd defend and what I'd change

I'd defend the QueryDSL templates (the filters compose; a 13-way dynamic native query is string soup), the ALL-semantics on amenities, the sort whitelist, and measuring before indexing.

I'd change two things. The map-marker endpoint has no result cap; it trusts the client's zoom level to bound the viewport, which is a contract that should live in the server. And the displayed min-price and the price *sort* are computed by two independent implementations (Java fold vs SQL `COALESCE`), which can disagree on studios with partially-priced rooms. Nobody ever noticed — eight users — but I'd unify them first thing.

---

*The series: [pocket-money AWS](/writing/muroom-aws-on-pocket-money) · [deleting JWT](/writing/muroom-deleting-jwt) · [IDs vs JavaScript](/writing/muroom-ids-javascript)*
