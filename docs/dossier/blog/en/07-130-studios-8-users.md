# We catalogued 130 studios. Eight musicians came.

*The capstone of the [Muroom postmortem series](/writing/muroom-aws-on-pocket-money). The engineering posts told you how we built it; this one is about why building it wasn't the hard part.*

---

Here is Muroom's entire story in one table:

| Supply side | Demand side |
|---|---|
| 110 studio owners signed up by cold call | 19 registered musicians |
| 130 studios catalogued | 14 of them in the first month |
| 1,273 rooms with specs and photos | 549 searches — all in December |
| 40 images per studio, collected by hand | **8 real users** |

Every number on the left cost weeks of unglamorous work: phone calls, floor plans, follow-ups, data entry. Every number on the right is what arrived when we opened the doors. The rest of our traffic, for months, was health checks and bots.

## What actually worked

I want to be precise about this, because "startup fails" flattens everything into one lesson and there were at least two.

The operational side *worked*. Cold-calling 110 skeptical studio owners and getting their floor plans is genuinely hard, and the team did it. Shipping worked too: first commit to live beta in three weeks, a five-person team with clear lanes, a product that — as the [screenshots](/projects/muroom) show — looked and behaved like a real marketplace. If the lesson were "execution," we'd have been fine.

The demand engine is what never existed. We assumed — I assumed — a marketplace logic: fill the supply side densely enough and searchers will stick when they arrive. What that logic skips is the question of how they arrive at all. Our university grant was earmarked for marketing, but a budget is not a channel, and by the time the supply catalogue was beautiful, we had no repeatable way to put it in front of a musician at the moment they needed a practice room.

I don't fully know why demand didn't come — eight users is too small a sample to autopsy, and I distrust neat post-hoc explanations. My honest best guess: a search-only product competes with "just search the map app you already have," and our differentiators — thirteen filters, per-room specs, honest pricing — matter to someone *comparing* studios, a behavior that starts after liquidity, not before.

## The feature we retired on purpose

One decision from this period aged well. We shipped a community feature — musicians posting photo tours of their studios. It got two posts. Our planner called it: an empty community is worse than no community, because it broadcasts the emptiness. We removed it from the product.

At the time it stung; we'd built the whole thing — images, comments, likes. In retrospect it's the healthiest decision in the story: we looked at data, overrode sunk cost, and protected the brand with a deletion. Teams that can delete their own work are rarer than teams that can ship.

## What I'd do differently

Not "market harder" — that's a wish, not a plan. Concretely:

1. **Buy demand evidence before supply.** A landing page with fake-door search for one neighborhood would have cost a weekend and answered the only question that mattered. We had a beta collecting sign-ups in week three; I'd have pointed that same energy at musicians, not owners.
2. **Scope supply to one liquid pocket.** 130 studios across Seoul is thin everywhere. Thirty studios within a kilometer of Hongdae would have been dense *somewhere* — and Hongdae is where our December users actually searched.
3. **Sequence the grant.** Marketing money spent before you know your channel is tuition. Spent after, it's fuel.

## Why I don't regret it

The service is gone; the invoices, the git history, and six engineering write-ups are what's left. I learned what a managed database actually sells, what JWT is actually for, what an index actually costs — because every abstraction had my own money and my own users (all eight of them) behind it.

But the expensive lesson was none of those. It's that **engineering quality is a multiplier, and demand is the number it multiplies.** We built a very good zero-multiplier machine. Next time, I find the demand first — and then I already know how to build the rest in three weeks.

---

*The series: [pocket-money AWS](/writing/muroom-aws-on-pocket-money) · [deleting JWT](/writing/muroom-deleting-jwt) · [IDs vs JavaScript](/writing/muroom-ids-javascript) · [viewport search](/writing/muroom-viewport-search) · [credential rotation](/writing/muroom-credential-rotation) · [file storage](/writing/muroom-file-storage-policy)*
