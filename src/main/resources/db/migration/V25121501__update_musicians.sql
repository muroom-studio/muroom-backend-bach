-- 생일 / 프로필 이미지 컬럼 제거
ALTER TABLE musicians
    DROP COLUMN birthdate,
    DROP COLUMN profile_image_key;