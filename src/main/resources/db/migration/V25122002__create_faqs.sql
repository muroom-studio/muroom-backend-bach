CREATE SEQUENCE faq_category_id_seq;
CREATE SEQUENCE faq_id_seq;

-- faq_category 테이블
CREATE TABLE faq_categories
(
    faq_category_id BIGINT      NOT NULL DEFAULT nextval('faq_category_id_seq'),
    name            VARCHAR(50) NOT NULL,
    is_active       BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL,
    updated_at      TIMESTAMPTZ NOT NULL,

    CONSTRAINT pk_faq_categories PRIMARY KEY (faq_category_id)
);

-- faq 테이블
CREATE TABLE faqs
(
    faq_id          BIGINT      NOT NULL DEFAULT nextval('faq_id_seq'),
    faq_category_id BIGINT      NOT NULL,
    question        TEXT        NOT NULL,
    answer          TEXT        NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL,
    updated_at      TIMESTAMPTZ NOT NULL,
    deleted_at      TIMESTAMPTZ NOT NULL,

    CONSTRAINT pk_faqs PRIMARY KEY (faq_id),
    CONSTRAINT fk_faqs_on_category FOREIGN KEY (faq_category_id) REFERENCES faq_categories (faq_category_id)
);