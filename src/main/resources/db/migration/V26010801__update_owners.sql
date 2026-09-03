-- owners 필요없는 컬럼 삭제
ALTER TABLE owners
    DROP COLUMN birthdate;