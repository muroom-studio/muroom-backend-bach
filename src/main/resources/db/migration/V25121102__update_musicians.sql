-- 뮤지션 테이블의 profile_image_key 컬럼에서 NOT NULL 제약 조건 제거
ALTER TABLE musicians
    ALTER COLUMN profile_image_key DROP NOT NULL;