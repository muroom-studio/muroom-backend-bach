-- inquiry_reply_image 테이블 생성
CREATE SEQUENCE inquiry_reply_image_id_seq;

CREATE TABLE inquiry_reply_images
(
    inquiry_reply_image_id BIGINT      NOT NULL DEFAULT nextval('inquiry_reply_image_id_seq'),
    inquiry_reply_id       BIGINT      NOT NULL,
    image_key              TEXT        NOT NULL,
    created_at             TIMESTAMPTZ NOT NULL,
    updated_at             TIMESTAMPTZ NOT NULL,

    CONSTRAINT pk_inquiry_reply_images PRIMARY KEY (inquiry_reply_image_id),
    CONSTRAINT fk_inquiry_reply_images_on_inquiry_replies FOREIGN KEY (inquiry_reply_id) REFERENCES inquiry_replies (inquiry_reply_id)
);