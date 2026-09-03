-- 유저 hard delete 구현을 위한 컬럼 추가
ALTER TABLE musicians
    ADD COLUMN hard_delete_at TIMESTAMPTZ;

ALTER TABLE owners
    ADD COLUMN hard_delete_at TIMESTAMPTZ;