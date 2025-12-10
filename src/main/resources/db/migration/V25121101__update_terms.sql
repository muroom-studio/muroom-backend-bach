-- 약관 제목 컬럼 추가
-- 1단계: 일단 NULL 허용으로 컬럼 생성
ALTER TABLE term_contents
    ADD COLUMN title VARCHAR(255);

-- 2단계: 기존 데이터에 값 채워넣기 (Update)
UPDATE term_contents
SET title = '제목 필요'
WHERE title IS NULL;

-- 3단계: 값이 다 채워졌으니 NOT NULL 제약 걸기
ALTER TABLE term_contents
    ALTER COLUMN title SET NOT NULL;