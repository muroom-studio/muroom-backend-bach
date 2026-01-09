-- 1) 기존 UNIQUE 제약 제거
ALTER TABLE owners
    DROP CONSTRAINT IF EXISTS unq_owners_nickname;
ALTER TABLE owners
    DROP CONSTRAINT IF EXISTS unq_owners_phone_number;
ALTER TABLE owners
    DROP CONSTRAINT IF EXISTS unq_owners_email;

-- 2) 활성(미삭제) 레코드에만 유니크 적용되는 인덱스 생성
CREATE UNIQUE INDEX IF NOT EXISTS unq_owners_nickname_active
    ON owners (nickname)
    WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX IF NOT EXISTS unq_owners_phone_number_active
    ON owners (phone_number)
    WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX IF NOT EXISTS unq_owners_email_active
    ON owners (email)
    WHERE deleted_at IS NULL;