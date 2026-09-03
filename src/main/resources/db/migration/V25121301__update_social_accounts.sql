-- social account token DB -> Redis 이동
ALTER TABLE social_accounts
    DROP COLUMN access_token,
    DROP COLUMN refresh_token;