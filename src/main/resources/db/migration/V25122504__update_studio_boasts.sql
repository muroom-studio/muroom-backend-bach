-- 작업실 소개(자랑) 이벤트 참여 약관 동의 컬럼 추가
ALTER TABLE studio_boasts
    ADD COLUMN agreed_to_event_terms BOOLEAN NOT NULL DEFAULT FALSE;