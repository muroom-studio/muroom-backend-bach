# Our third reviewer was an AI

*From the [Muroom postmortem series](/writing/muroom-aws-on-pocket-money): what a standing AI reviewer actually did for a two-person backend team — including the part where we stopped listening.*

---

A two-person backend team has a structural problem: every review is a peer review between exactly two people who share the same blind spots and the same deadline. There is no senior engineer down the hall. In 2025 we tried the obvious substitute: an AI reviewer on every pull request.

The mechanism was deliberately boring. Our PR template shipped with a standing request block — the same three asks on all 94 PRs:

> - Architecture: does this fit our domain layering? Better designs welcome.
> - Performance: unnecessary queries or inefficient logic?
> - Security: anything we're missing?

That template turned the AI from a novelty into a fixture. Nobody had to remember to invoke it or decide what to ask; review-by-default was simply the shape of a PR. Think of it as a linter that reads intent.

## What it actually caught

The honest answer: real things, mostly early.

The example I keep citing is the keyword search. My first implementation of "match studios by nearby station name" fetched station IDs in one query and stuffed them into an `IN` clause — two round trips, and a list that could grow silly. The review comment suggested the correlated `EXISTS` subquery form. The refactor landed in a commit literally titled "PR#14 code review reflected," and that shape survived to the end of the project. My teammate's commit history has its own versions — "fix: AI feedback applied" appears more than once.

None of these were things we *couldn't* have caught. They were things two juniors moving fast *didn't* catch, at the exact moment catching them was cheapest. For a team with no senior reviewer, that's not a gimmick; that's the missing role, imperfectly filled.

## Where it decayed

Around the later stretch of the project we started skimming, then skipping, the reviews. Deadline pressure, and a creeping sense that we'd "heard it all before" — the reviewer keeps flagging the same categories, and you keep being in a hurry.

I can't prove what that cost us. But I can point at a suspicious correlation: the defects we found during this postmortem — a content-type validation that silently vanished during a rewrite, a dead scheduler annotation, an unprotected admin path — are all from the era of skimmed reviews, and all are exactly the category of thing the standing template asked about. Maybe the reviewer flagged them into the void. Maybe it wouldn't have caught them. The point is we *chose* not to know.

There's a sharper lesson from the same audit: one of those defects was lost in an AI-*assisted* rewrite, where a whitelist present in the old code simply didn't appear in the new one. AI review and AI coding fail differently — reviews fail loudly (a comment you ignore), generation fails silently (a deletion you don't notice). **Review the deleted lines of generated diffs with more suspicion than the added ones.**

## Would I do it again?

Yes, with two changes.

1. **Make the standing prompts sharper over time.** Ours never evolved. The template should accumulate the team's actual past failures — "check that soft-deleted parents don't break child reads" is worth infinitely more than "any security issues?"
2. **Make ignoring it expensive.** Not blocking — AI reviewers are wrong too often to gate merges — but a one-line "review dismissed because: ___" field in the PR template. The failure mode isn't bad advice; it's unread advice. Friction on the *dismissal* is the cheapest fix.

A two-person team can't afford a third engineer. It can afford a third reviewer — as long as it remembers that a reviewer you've stopped reading is just latency.

---

*The series: [pocket-money AWS](/writing/muroom-aws-on-pocket-money) · [130 studios, 8 users](/writing/muroom-130-studios-8-users) · [the null-result fix](/writing/muroom-cold-start-null-result)*
