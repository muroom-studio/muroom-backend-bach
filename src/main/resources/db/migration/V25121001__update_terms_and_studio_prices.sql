-- 1. 기존 UNIQUE 제약조건 삭제
ALTER TABLE terms
DROP CONSTRAINT unq_terms_code_version;

-- 2. code + version + target_role 기준으로 UNIQUE 제약조건 생성
ALTER TABLE terms
    ADD CONSTRAINT unq_terms_code_version_role UNIQUE (code, version, target_role);

-- 3. is_active 컬럼 추가 (예: NOT NULL + 기본값 false)
ALTER TABLE terms
    ADD COLUMN is_active BOOLEAN NOT NULL DEFAULT false;

-- 4. updated_at 컬럼 추가 (NOT NULL)
ALTER TABLE terms
    ADD COLUMN updated_at TIMESTAMPTZ NOT NULL DEFAULT now();

-- studio_prices 테이블의 min_price 컬럼에서 NOT NULL 제약 제거
ALTER TABLE studio_prices ALTER COLUMN min_price DROP NOT NULL;

-- studio_prices 테이블의 max_price 컬럼에서 NOT NULL 제약 제거
ALTER TABLE studio_prices ALTER COLUMN max_price DROP NOT NULL;