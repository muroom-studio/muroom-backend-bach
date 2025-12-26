-- studio_boast_comments 테이블 생성
CREATE TABLE studio_boast_comments
(
    studio_boast_comment_id BIGINT      NOT NULL,
    created_at              TIMESTAMPTZ NOT NULL,
    updated_at              TIMESTAMPTZ NOT NULL,
    deleted_at              TIMESTAMPTZ,
    content                 TEXT        NOT NULL,
    is_secret               BOOLEAN     NOT NULL DEFAULT FALSE,

    creator_user_id         BIGINT      NOT NULL,
    tagged_user_id          BIGINT,
    parent_id               BIGINT,
    studio_boast_id         BIGINT      NOT NULL,

    CONSTRAINT pk_studio_boast_comments PRIMARY KEY (studio_boast_comment_id)
    -- CONSTRAINT fk_studio_boast_comments_on_creator_user FOREIGN KEY (creator_user_id) REFERENCES users (user_id),
    -- CONSTRAINT fk_studio_boast_comments_on_studio_boast FOREIGN KEY (studio_boast_id) REFERENCES studio_boasts (studio_boast_id),
    -- CONSTRAINT fk_studio_boast_comments_on_parent_comment FOREIGN KEY (parent_id) REFERENCES studio_boast_comments (studio_boast_comment_id),
    -- CONSTRAINT fk_studio_boast_comments_on_tagged_user FOREIGN KEY (tagged_user_id) REFERENCES users (user_id)
);

CREATE INDEX idx_comment_studio_boast_id ON studio_boast_comments (studio_boast_id);
CREATE INDEX idx_comment_parent_id ON studio_boast_comments (parent_id);
CREATE INDEX idx_comment_creator_user_id ON studio_boast_comments (creator_user_id);
CREATE INDEX idx_comment_tagged_user_id ON studio_boast_comments (tagged_user_id);

-- 댓글 좋아요 테이블
CREATE TABLE studio_boast_comment_likes
(
    studio_boast_comment_like_id BIGINT                      NOT NULL,
    created_at                   TIMESTAMP(6) WITH TIME ZONE NOT NULL,

    musician_id                  BIGINT                      NOT NULL,
    studio_boast_comment_id      BIGINT                      NOT NULL,

    CONSTRAINT pk_studio_boast_comment_likes PRIMARY KEY (studio_boast_comment_like_id),
    CONSTRAINT uk_comment_like_musician_id_comment_id UNIQUE (musician_id, studio_boast_comment_id)
    -- CONSTRAINT fk_studio_boast_comment_likes_on_musician FOREIGN KEY (musician_id) REFERENCES musicians (musician_id),
    -- CONSTRAINT fk_studio_boast_comment_likes_on_studio_boast_comment FOREIGN KEY (studio_boast_comment_id) REFERENCES studio_boast_comments (studio_boast_comment_id)
);

CREATE INDEX idx_comment_like_musician_id ON studio_boast_comment_likes (musician_id);
CREATE INDEX idx_comment_like_comment_id ON studio_boast_comment_likes (studio_boast_comment_id);

ALTER TABLE studio_boast_likes
    ALTER COLUMN created_at TYPE TIMESTAMPTZ;
ALTER TABLE studio_boast_images
    ALTER COLUMN updated_at TYPE TIMESTAMPTZ,
    ALTER COLUMN deleted_at TYPE TIMESTAMPTZ;