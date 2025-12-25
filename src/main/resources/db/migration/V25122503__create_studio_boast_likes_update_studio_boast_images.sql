-- studio_boast_likes 테이블 생성
CREATE TABLE studio_boast_likes
(
    studio_boast_like_id BIGINT    NOT NULL,
    created_at           TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    musician_id          BIGINT    NOT NULL,
    studio_boast_id      BIGINT    NOT NULL,

    CONSTRAINT pk_studio_boast_likes PRIMARY KEY (studio_boast_like_id)
    -- CONSTRAINT fk_studio_boast_likes_on_musician FOREIGN KEY (musician_id) REFERENCES musicians (musician_id),
    -- CONSTRAINT fk_studio_boast_likes_on_studio_boast FOREIGN KEY (studio_boast_id) REFERENCES studio_boasts (studio_boast_id)
);

-- studio_boast_images updated_at, deleted_at 추가
ALTER TABLE studio_boast_images
    ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN deleted_at TIMESTAMP;

-- updated_at 컬럼 자동 갱신을 위한 함수 생성
CREATE OR REPLACE FUNCTION update_updated_at_column()
    RETURNS TRIGGER AS
$$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- studio_boast_images 테이블에 트리거 적용
CREATE TRIGGER update_studio_boast_images_updated_at
    BEFORE UPDATE
    ON studio_boast_images
    FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();