-- faqs 테이블 정렬용 컬럼 추가
ALTER TABLE faqs
    ADD COLUMN sequence INTEGER NOT NULL DEFAULT 0;