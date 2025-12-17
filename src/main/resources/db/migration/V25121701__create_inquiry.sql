CREATE SEQUENCE inquiry_id_seq;
CREATE SEQUENCE inquiry_category_id_seq;
CREATE SEQUENCE inquiry_reply_id_seq;
CREATE SEQUENCE inquiry_images_id_seq;

-- 문의 카테고리
CREATE TABLE inquiry_categories
(
    inquiry_category_id BIGINT      NOT NULL DEFAULT nextval('inquiry_category_id_seq'),
    code                VARCHAR(50) NOT NULL,
    name                VARCHAR(50) NOT NULL,
    is_active           BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT pk_inquiry_categories PRIMARY KEY (inquiry_category_id),
    CONSTRAINT unq_inquiry_categories_code UNIQUE (code)
);

-- 문의
CREATE TABLE inquiries
(
    inquiry_id          BIGINT       NOT NULL DEFAULT nextval('inquiry_id_seq'),
    musician_id         BIGINT       NOT NULL,
    inquiry_category_id BIGINT       NOT NULL,
    title               VARCHAR(200) NOT NULL,
    content             TEXT         NOT NULL,
    status              VARCHAR(50)  NOT NULL,
    created_at          TIMESTAMPTZ  NOT NULL,
    updated_at          TIMESTAMPTZ  NOT NULL,
    deleted_at          TIMESTAMPTZ  NOT NULL,
    CONSTRAINT pk_inquiries PRIMARY KEY (inquiry_id),
    CONSTRAINT fk_inquiries_on_musicians FOREIGN KEY (musician_id) REFERENCES musicians (musician_id),
    CONSTRAINT fk_inquiries_on_inquiry_categories FOREIGN KEY (inquiry_category_id) REFERENCES inquiry_categories (inquiry_category_id)
);

-- 문의 답변
CREATE TABLE inquiry_replies
(
    inquiry_reply_id BIGINT      NOT NULL DEFAULT nextval('inquiry_reply_id_seq'),
    inquiry_id       BIGINT      NOT NULL,
    content          TEXT        NOT NULL,
    created_at       TIMESTAMPTZ NOT NULL,
    updated_at       TIMESTAMPTZ NOT NULL,
    CONSTRAINT pk_inquiry_replies PRIMARY KEY (inquiry_reply_id),
    CONSTRAINT fk_inquiry_replies_on_inquiries FOREIGN KEY (inquiry_id) REFERENCES inquiries (inquiry_id),
    CONSTRAINT unq_inquiry_replies_inquiry_id UNIQUE (inquiry_id)
);
-- 문의 이미지 첨부
CREATE TABLE inquiry_images
(
    inquiry_images_id BIGINT        NOT NULL DEFAULT nextval('inquiry_images_id_seq'),
    inquiry_id        BIGINT        NOT NULL,
    image_key         VARCHAR(1024) NOT NULL,
    CONSTRAINT pk_inquiry_images PRIMARY KEY (inquiry_images_id),
    CONSTRAINT fk_inquiry_images_on_inquiries FOREIGN KEY (inquiry_id) REFERENCES inquiries (inquiry_id)
);
