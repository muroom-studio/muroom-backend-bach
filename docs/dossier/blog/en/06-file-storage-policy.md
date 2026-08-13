# Three tries at file storage: from one bucket to a policy enum

*A refactoring story from the [Muroom postmortem series](/writing/muroom-aws-on-pocket-money): the S3 layer we rewrote twice, and the version that finally stopped growing.*

---

Every studio listing carried up to forty images — floor plans, rooms, amenities — uploaded by us during onboarding and later by users for community posts. The S3 layer that handled them was rewritten twice in four months. Each version solved the previous one's actual problem, and the final one did something rare for a refactor: it made the codebase *smaller* while adding capability.

## V0: one bucket and a for-loop

The beta shipped with the simplest thing that works: a single bucket, presigned PUT URLs (browser uploads directly to S3 — on `t4g`-class instances you do not want image bytes flowing through your JVM), and a service method that looped over filenames issuing URLs in batch. Content-type validation was a hardcoded whitelist inside the domain service.

It worked, and it had the classic V0 disease: **no lifecycle**. Files uploaded but never attached to an entity just… stayed. Public and private content shared one bucket and one access model. Every new upload use-case meant another bespoke loop.

## V1: two buckets and a temp/ protocol

The December rewrite introduced the structure that survived to the end:

- **Two buckets** — public (thumbnails, studio photos; stable URLs) and private (documents, drafts; presigned GETs only).
- **A `temp/` prefix protocol.** Uploads land in `temp/`. When the owning entity is saved, the service *moves* each key to its permanent home; S3 has no move, so it's copy-then-delete. Anything still in `temp/` after seven days is garbage by definition — an S3 lifecycle rule deletes it. Application cleanup and infrastructure cleanup are deliberately redundant: if a bug leaks a file, the bucket policy is the backstop.
- **Soft delete as a prefix.** Deleting means moving to `deletion-scheduled/`, where another lifecycle rule expires it. Seven days of undo, for free.

V1's disease was interface growth. Public and private needs diverged, so methods split: `generatePresignedPutUrlForPublic`, `...ForPrivate`, validator callbacks injected per call site. Every new location — a drafts area, a report-evidence snapshot store — threatened two more methods. (Archaeological note: a javadoc example in this era mentions "trainer profile photos" — relics of reference code from an older side project. Code you copy carries its ghosts.)

## V2: the policy table

The insight of the final rewrite is that all those methods differed in exactly two values. So the policy became data:

```java
public enum FileStorageLocation {
  PUBLIC_PERMANENT (PUBLIC,  ""),
  PUBLIC_TEMP      (PUBLIC,  "temp/"),
  PUBLIC_TRASH     (PUBLIC,  "deletion-scheduled/"),
  PRIVATE_PERMANENT(PRIVATE, ""),
  PRIVATE_TEMP     (PRIVATE, "temp/"),
  PRIVATE_DRAFT    (PRIVATE, "draft/"),
  PRIVATE_REPORT   (PRIVATE, "snapshot/report/"),
  PRIVATE_TRASH    (PRIVATE, "deletion-scheduled/");
}
```

One service with five verbs — `getUploadUrl`, `getViewUrl`, `move`, `softDelete`, `copyToReportSnapshot` — each taking a location. Below it, a single `S3Executor` is the only class that imports the AWS SDK. Above it, one rule: **no caller may ever write a path string.** A new storage area is now one enum line, not two methods.

The diff is my favorite part: **+236 / −268**. Eleven files, more capability, net negative code. That's the signature of a refactor that found the right abstraction rather than adding one.

## What it cost

Two honest scars. First, somewhere between V0 and V2, the content-type whitelist quietly disappeared — the presigned URL now signs whatever type the client declares. I found this during a post-shutdown audit, not a review; my best forensics say it was lost in an AI-assisted rewrite, which is a lesson about reviewing the *deleted* lines of generated diffs, not just the added ones. Second, S3 operations run inside DB transactions with no compensation — a rollback after a `move` leaves storage and database briefly disagreeing. The `temp/` lifecycle rule bounds the damage, which is exactly why the redundant backstop earns its keep.

**The takeaway**: when a service interface grows a method per use-case, the use-cases are probably rows in a table you haven't written yet.

---

*The series: [pocket-money AWS](/writing/muroom-aws-on-pocket-money) · [deleting JWT](/writing/muroom-deleting-jwt) · [IDs vs JavaScript](/writing/muroom-ids-javascript) · [viewport search](/writing/muroom-viewport-search) · [credential rotation](/writing/muroom-credential-rotation)*
