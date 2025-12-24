-- 1) 기존 UNIQUE 제약 제거
ALTER TABLE musicians DROP CONSTRAINT IF EXISTS unq_musicians_nickname;
ALTER TABLE musicians DROP CONSTRAINT IF EXISTS unq_musicians_phone_number;

-- 2) 활성(미삭제) 레코드에만 유니크 적용되는 인덱스 생성
CREATE UNIQUE INDEX IF NOT EXISTS unq_musicians_nickname_active
    ON musicians (nickname)
    WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX IF NOT EXISTS unq_musicians_phone_number_active
    ON musicians (phone_number)
    WHERE deleted_at IS NULL;