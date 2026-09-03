-- 내 작업실 소개(자랑)
CREATE TABLE studio_boasts
(
    studio_boast_id          BIGINT        NOT NULL,
    thumbnail_image_file_key VARCHAR(1024) NOT NULL,
    content                  TEXT          NOT NULL,
    studio_name              VARCHAR(255)  NOT NULL,
    road_name_address        VARCHAR(255)  NOT NULL,
    lot_number_address       VARCHAR(255)  NOT NULL,
    detailed_address         VARCHAR(255)  NOT NULL,
    like_count               BIGINT        NOT NULL,
    instagram_account        VARCHAR(100)  NULL,
    created_at               TIMESTAMPTZ   NOT NULL,
    updated_at               TIMESTAMPTZ   NOT NULL,
    deleted_at               TIMESTAMPTZ   NULL,

    creator_user_id          BIGINT        NOT NULL,
    studio_id                BIGINT        NULL,

    CONSTRAINT pk_studio_boasts PRIMARY KEY (studio_boast_id)
    -- CONSTRAINT fk_studio_boasts_on_creator_user FOREIGN KEY (creator_user_id) REFERENCES musicians (musician_id),
    -- CONSTRAINT fk_studio_boasts_on_studio FOREIGN KEY (studio_id) REFERENCES studios (studio_id)
);

CREATE TABLE studio_boast_images
(
    studio_boast_image_id BIGINT        NOT NULL,
    image_file_key        VARCHAR(1024) NOT NULL,
    sequence              INT           NOT NULL,
    created_at            TIMESTAMPTZ   NOT NULL,

    studio_boast_id       BIGINT        NOT NULL,

    CONSTRAINT pk_studio_boast_images PRIMARY KEY (studio_boast_image_id)
    -- CONSTRAINT fk_studio_boast_images_on_studio_boast FOREIGN KEY (studio_boast_id) REFERENCES studio_boasts (studio_boast_id)
);