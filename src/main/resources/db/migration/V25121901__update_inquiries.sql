-- inquiries not null 제약 삭제
ALTER TABLE inquiries
    ALTER COLUMN deleted_at DROP NOT NULL;