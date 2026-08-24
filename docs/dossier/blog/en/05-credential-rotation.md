# RDS-grade credential rotation on a database AWS doesn't manage

*A how-to companion to [the pocket-money AWS story](/writing/muroom-aws-on-pocket-money). We ran PostgreSQL on a bare EC2 instance to save money; this is how it still got automatic password rotation, with zero redeploys.*

---

When you leave RDS, the thing you miss is the boring hygiene that happens without you, not the dashboard. Automated credential rotation is the one I refused to give up. A password that never rotates eventually leaks into a laptop, a log, or a screenshot.

The trick is that AWS's rotation machinery doesn't actually care whether your Postgres is "managed." It speaks the PostgreSQL wire protocol like everyone else.

## The pieces

**1. The rotation Lambda (the standard one).** The Serverless Application Repository ships `SecretsManagerRDSPostgreSQLRotationSingleUser`, the exact Lambda RDS uses. Deployed as a CloudFormation stack via Terraform, placed in the VPC's private subnets with a security group that can reach the database on 5432 and Secrets Manager on 443. "RDS" is in the name, not in the requirements. The Lambda just connects over libpq to whatever host the secret points at, including a Docker container on a `t4g.small`.

**2. The secret, in the shape the Lambda expects.** A JSON secret per environment: `{engine, host, port, username, password, dbname}`, with `host` set to the EC2 instance's private IP. Two Terraform details that bit me:

- `lifecycle { ignore_changes = [secret_string] }` on the secret; otherwise the next `terraform apply` regenerates the random password and stomps the one the Lambda just set.
- The rotation schedule is its own resource. We ran **dev every 1 day, prod every 7**, and the daily dev rotation was really a canary: if rotation breaks, I want to find out in dev within twenty-four hours, not in prod within a week.

**3. The application side (the part nobody tells you about).** Rotation is useless if every rotation kills your connection pool. The AWS Advanced JDBC Wrapper solves this:

```yaml
url: jdbc:aws-wrapper:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}
driver-class-name: software.amazon.jdbc.Driver
hikari:
  data-source-properties:
    wrapperPlugins: awsSecretsManager
    secretsManagerSecretId: muroom/postgres-prod/credentials
    secretsManagerRegion: ${AWS_REGION}
```

The `awsSecretsManager` plugin fetches credentials from the secret at connect time and **re-fetches on authentication failure**. Existing connections survive rotation (Postgres doesn't kill authenticated sessions on password change); new connections transparently pick up the new password. No redeploy, no restart, no 3 a.m. connection storm. One IAM subtlety: the **task role**, not only the execution role, needs `secretsmanager:GetSecretValue`, because it's your application making this call at runtime, not ECS at container start.

## The evening it took

I'll admit wiring this took four commits in about thirty minutes, preserved in git like insects in amber. Commit one added the wrapper. Commit two fixed the JDBC URL; I'd forgotten the `jdbc:aws-wrapper:` scheme, so the wrapper never engaged. Commit three added the wrong dependency (the Spring Cloud AWS starter, which is a different universe). Commit four added the right one (`software.amazon.awssdk:secretsmanager`, one line) and is titled *"fix: … 3차!!!!!!!!!!!!!!!"*. I could rewrite that history. I choose not to.

## Did it actually work?

Yes, and I can prove it in two ways. The secret's `LastChangedDate` kept advancing on schedule for six months. And in the most backhanded validation imaginable, months after we stopped operating the service, the Lambda was *still* faithfully rotating the dev password every twenty-four hours for zero users. Automation outlives intent. When I audited the account for the postmortem, that dutiful little Lambda was the healthiest thing in it.

**When to copy this**: you're self-managing Postgres for cost or control, your app runs on the JVM, and you want managed-grade hygiene. **When not to**: if you're on RDS already (it's built in), or your password never changing is somehow acceptable (it isn't).

---

*The series: [pocket-money AWS](/writing/muroom-aws-on-pocket-money) · [deleting JWT](/writing/muroom-deleting-jwt) · [IDs vs JavaScript](/writing/muroom-ids-javascript) · [viewport search](/writing/muroom-viewport-search)*
