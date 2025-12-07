CREATE SEQUENCE owner_id_seq;
CREATE SEQUENCE instrument_id_seq;
CREATE SEQUENCE musician_id_seq;
CREATE SEQUENCE term_id_seq;
CREATE SEQUENCE owner_agreement_id_seq;
CREATE SEQUENCE musician_agreement_id_seq;
CREATE SEQUENCE my_studio_id_seq;
CREATE SEQUENCE social_account_id_seq;
CREATE SEQUENCE studio_id_seq;
CREATE SEQUENCE option_id_seq;
CREATE SEQUENCE studio_option_id_seq;
CREATE SEQUENCE studio_image_id_seq;
CREATE SEQUENCE room_id_seq;
CREATE SEQUENCE studio_forbidden_instrument_id_seq;
CREATE SEQUENCE subway_station_id_seq;
CREATE SEQUENCE subway_line_id_seq;
CREATE SEQUENCE subway_station_line_id_seq;
CREATE SEQUENCE subway_stations_nearby_studio_id_seq;
CREATE SEQUENCE recent_search_id_seq;
CREATE SEQUENCE search_log_id_seq;
CREATE SEQUENCE studio_view_log_id_seq;

CREATE TABLE owners
(
    owner_id         BIGINT      NOT NULL DEFAULT nextval('owner_id_seq'),
    name             VARCHAR(50) NULL,
    birthdate        DATE NULL,
    phone_number     VARCHAR(16) NOT NULL,
    nickname         VARCHAR(10) NOT NULL,
    status           VARCHAR(50) NOT NULL,
    email            VARCHAR(255) NULL,
    password         VARCHAR(255) NULL,
    experience_years INTEGER NULL,
    created_at       TIMESTAMPTZ NOT NULL,
    updated_at       TIMESTAMPTZ NOT NULL,
    deleted_at       TIMESTAMPTZ NULL,

    CONSTRAINT pk_owners PRIMARY KEY (owner_id),
    CONSTRAINT unq_owners_nickname UNIQUE (nickname),
    CONSTRAINT unq_owners_email UNIQUE (email),
    CONSTRAINT unq_owners_phone_number UNIQUE (phone_number)
);

CREATE TABLE instruments
(
    instrument_id BIGINT      NOT NULL DEFAULT nextval('instrument_id_seq'),
    code          VARCHAR(50) NOT NULL,
    description   VARCHAR(50) NOT NULL,
    created_at    TIMESTAMPTZ NOT NULL,
    updated_at    TIMESTAMPTZ NOT NULL,

    CONSTRAINT pk_instruments PRIMARY KEY (instrument_id),
    CONSTRAINT unq_instruments_code UNIQUE (code),
    CONSTRAINT unq_instruments_description UNIQUE (description)
);

-- 뮤지션
CREATE TABLE musicians
(
    musician_id       BIGINT      NOT NULL DEFAULT nextval('musician_id_seq'),
    name              VARCHAR(50) NOT NULL,
    birthdate         DATE        NOT NULL,
    phone_number      VARCHAR(16) NOT NULL,
    profile_image_key TEXT        NOT NULL,
    nickname          VARCHAR(10) NOT NULL,
    status            VARCHAR(50) NOT NULL,
    instrument_id     BIGINT      NOT NULL,
    created_at        TIMESTAMPTZ NOT NULL,
    updated_at        TIMESTAMPTZ NOT NULL,
    deleted_at        TIMESTAMPTZ NULL,

    CONSTRAINT pk_musicians PRIMARY KEY (musician_id),
    CONSTRAINT unq_musicians_nickname UNIQUE (nickname),
    CONSTRAINT unq_musicians_phone_number UNIQUE (phone_number),
    CONSTRAINT fk_musicians_on_instrument FOREIGN KEY (instrument_id) REFERENCES instruments (instrument_id)
);

-- 약관
CREATE TABLE terms
(
    term_id      BIGINT      NOT NULL DEFAULT nextval('term_id_seq'),
    code         VARCHAR(50) NOT NULL,
    target_role  VARCHAR(50) NOT NULL,
    version      VARCHAR(50) NOT NULL,
    is_mandatory BOOLEAN     NOT NULL,
    effective_at TIMESTAMPTZ NULL,
    created_at   TIMESTAMPTZ NOT NULL,

    CONSTRAINT pk_terms PRIMARY KEY (term_id),
    CONSTRAINT unq_terms_code_version UNIQUE (code, version)
);

-- 약관 내용
CREATE TABLE term_contents
(
    term_id BIGINT NOT NULL,
    content TEXT   NOT NULL,

    CONSTRAINT pk_term_contents PRIMARY KEY (term_id),
    CONSTRAINT fk_term_contents_on_term FOREIGN KEY (term_id) REFERENCES terms (term_id)
);

-- 사장님 약관 동의 이력
CREATE TABLE owner_agreements
(
    owner_agreement_id BIGINT      NOT NULL DEFAULT nextval('owner_agreement_id_seq'),
    agreed_at          TIMESTAMPTZ NOT NULL,
    owner_id           BIGINT      NOT NULL,
    term_id            BIGINT      NOT NULL,

    CONSTRAINT pk_owner_agreements PRIMARY KEY (owner_agreement_id),
    CONSTRAINT fk_owner_agreements_on_owner FOREIGN KEY (owner_id) REFERENCES owners (owner_id),
    CONSTRAINT fk_owner_agreements_on_term FOREIGN KEY (term_id) REFERENCES terms (term_id)
);

-- 뮤지션 약관 동의 이력
CREATE TABLE musician_agreements
(
    musician_agreement_id BIGINT      NOT NULL DEFAULT nextval('musician_agreement_id_seq'),
    agreed_at             TIMESTAMPTZ NOT NULL,
    musician_id           BIGINT      NOT NULL,
    term_id               BIGINT      NOT NULL,

    CONSTRAINT pk_musician_agreements PRIMARY KEY (musician_agreement_id),
    CONSTRAINT fk_musician_agreements_on_musician FOREIGN KEY (musician_id) REFERENCES musicians (musician_id),
    CONSTRAINT fk_musician_agreements_on_term FOREIGN KEY (term_id) REFERENCES terms (term_id)
);

-- 뮤지션 스튜디오
CREATE TABLE my_studios
(
    my_studio_id   BIGINT       NOT NULL DEFAULT nextval('my_studio_id_seq'),
    musician_id    BIGINT       NOT NULL,
    name           VARCHAR(255) NOT NULL,
    road_address   VARCHAR(255) NOT NULL,
    detail_address VARCHAR(255) NULL,
    created_at     TIMESTAMPTZ  NOT NULL,
    updated_at     TIMESTAMPTZ  NOT NULL,
    deleted_at     TIMESTAMPTZ  NULL,

    CONSTRAINT pk_my_studios PRIMARY KEY (my_studio_id),
    CONSTRAINT fk_my_studios_on_musician FOREIGN KEY (musician_id) REFERENCES musicians (musician_id)
);

-- 소셜 계정
CREATE TABLE social_accounts
(
    social_account_id BIGINT       NOT NULL DEFAULT nextval('social_account_id_seq'),
    musician_id       BIGINT NULL,
    provider          VARCHAR(20)  NOT NULL,
    provider_user_id  VARCHAR(255) NOT NULL,
    access_token      VARCHAR(255) NOT NULL,
    refresh_token     VARCHAR(255),
    created_at        TIMESTAMPTZ  NOT NULL,
    updated_at        TIMESTAMPTZ  NOT NULL,

    CONSTRAINT pk_social_accounts PRIMARY KEY (social_account_id),
    CONSTRAINT unq_social_accounts_provider_user UNIQUE (provider, provider_user_id),
    CONSTRAINT fk_social_accounts_on_musician FOREIGN KEY (musician_id) REFERENCES musicians (musician_id)
);

-- 스튜디오
CREATE TABLE studios
(
    studio_id           BIGINT        NOT NULL DEFAULT nextval('studio_id_seq'),
    name                VARCHAR(100)  NOT NULL,
    road_name_address   VARCHAR(255)  NOT NULL,
    lot_number_address  VARCHAR(255)  NOT NULL,
    detailed_address    VARCHAR(255)  NOT NULL,
    location            GEOGRAPHY(POINT, 4326)    NOT NULL,
    view_count          BIGINT        NOT NULL,
    introduction        TEXT NULL,
    deposit_amount      INTEGER NULL,
    thumbnail_image_key VARCHAR(1024) NOT NULL,
    blueprint_image_key VARCHAR(1024) NOT NULL,
    created_at          TIMESTAMPTZ   NOT NULL,
    updated_at          TIMESTAMPTZ   NOT NULL,
    deleted_at          TIMESTAMPTZ NULL,
    owner_id            BIGINT        NOT NULL,

    CONSTRAINT pk_studios PRIMARY KEY (studio_id),
    CONSTRAINT unq_studios_name_road_address_detailed_address UNIQUE (name, road_name_address, detailed_address),
    CONSTRAINT unq_studios_name_lot_address_detailed_address UNIQUE (name, lot_number_address, detailed_address),
    CONSTRAINT fk_studios_on_owner FOREIGN KEY (owner_id) REFERENCES owners (owner_id)
);

-- [시스템] 옵션
CREATE TABLE options
(
    option_id      BIGINT      NOT NULL DEFAULT nextval('option_id_seq'),
    category       VARCHAR(50) NOT NULL,
    code           VARCHAR(50) NOT NULL,
    description    VARCHAR(50) NOT NULL,
    icon_image_key TEXT        NOT NULL,
    created_at     TIMESTAMPTZ NOT NULL,
    updated_at     TIMESTAMPTZ NOT NULL,

    CONSTRAINT pk_options PRIMARY KEY (option_id),
    CONSTRAINT unq_options_code UNIQUE (code),
    CONSTRAINT unq_options_description UNIQUE (description)
);

-- 스튜디오 옵션
CREATE TABLE studio_options
(
    studio_option_id BIGINT NOT NULL DEFAULT nextval('studio_option_id_seq'),
    studio_id        BIGINT NOT NULL,
    option_id        BIGINT NOT NULL,

    CONSTRAINT pk_studio_options PRIMARY KEY (studio_option_id),
    CONSTRAINT unq_studio_options_studio_option UNIQUE (studio_id, option_id),
    CONSTRAINT fk_studio_options_on_studio FOREIGN KEY (studio_id) REFERENCES studios (studio_id),
    CONSTRAINT fk_studio_options_on_option FOREIGN KEY (option_id) REFERENCES options (option_id)
);

-- 스튜디오 가격 범위
CREATE TABLE studio_prices
(
    studio_id BIGINT  NOT NULL,
    min_price INTEGER NOT NULL,
    max_price INTEGER NOT NULL,

    CONSTRAINT pk_studio_prices PRIMARY KEY (studio_id),
    CONSTRAINT fk_studio_prices_on_studio FOREIGN KEY (studio_id) REFERENCES studios (studio_id)
);

-- 스튜디오 건물 정보
CREATE TABLE studio_building_info
(
    studio_id                BIGINT       NOT NULL,
    floor_type               VARCHAR(50)  NOT NULL,
    floor_number             INTEGER      NOT NULL,
    has_restroom             BOOLEAN      NULL,
    restroom_location        VARCHAR(50)  NULL,
    restroom_gender          VARCHAR(50)  NULL,
    parking_fee_type         VARCHAR(50)  NULL,
    parking_fee_info         VARCHAR(50)  NULL,
    parking_spots            INTEGER      NULL,
    parking_location_name    VARCHAR(50)  NULL,
    parking_location_address VARCHAR(255) NULL,
    is_lodging_available     BOOLEAN      NULL,
    has_fire_insurance       BOOLEAN      NULL,

    CONSTRAINT pk_studio_building_info PRIMARY KEY (studio_id),
    CONSTRAINT fk_studio_building_info_on_studio FOREIGN KEY (studio_id) REFERENCES studios (studio_id)
);

-- 스튜디오 이미지
CREATE TABLE studio_images
(
    studio_image_id BIGINT      NOT NULL DEFAULT nextval('studio_image_id_seq'),
    category        VARCHAR(50) NOT NULL,
    image_key       TEXT        NOT NULL,
    sequence        INTEGER    NOT NULL,
    studio_id       BIGINT      NOT NULL,

    CONSTRAINT pk_studio_images PRIMARY KEY (studio_image_id),
    CONSTRAINT unq_studio_images_image_key UNIQUE (image_key),
    CONSTRAINT fk_studio_images_on_studio FOREIGN KEY (studio_id) REFERENCES studios (studio_id)
);

-- 스튜디오 내 방
CREATE TABLE rooms
(
    room_id      BIGINT       NOT NULL DEFAULT nextval('room_id_seq'),
    name         VARCHAR(100) NOT NULL,
    sequence     INTEGER      NOT NULL,
    height_mm    INTEGER      NULL,
    width_mm     INTEGER      NULL,
    base_price   INTEGER NULL,
    is_available BOOLEAN      NULL,
    available_at DATE NULL,
    created_at   TIMESTAMPTZ  NOT NULL,
    updated_at   TIMESTAMPTZ  NOT NULL,
    deleted_at   TIMESTAMPTZ  NULL,
    studio_id    BIGINT       NOT NULL,

    CONSTRAINT pk_rooms PRIMARY KEY (room_id),
    CONSTRAINT unq_rooms_studio_name UNIQUE (studio_id, name),
    CONSTRAINT fk_rooms_on_studio FOREIGN KEY (studio_id) REFERENCES studios (studio_id)
);

-- 스튜디오 내 금지 악기
CREATE TABLE studio_forbidden_instruments
(
    studio_forbidden_instrument_id BIGINT NOT NULL DEFAULT nextval('studio_forbidden_instrument_id_seq'),
    instrument_id                  BIGINT NOT NULL,
    studio_id                      BIGINT NOT NULL,

    CONSTRAINT pk_studio_forbidden_instruments PRIMARY KEY (studio_forbidden_instrument_id),
    CONSTRAINT unq_studio_forbidden_instruments_instrument_studio UNIQUE (instrument_id, studio_id),
    CONSTRAINT fk_studio_forbidden_instruments_instrument FOREIGN KEY (instrument_id) REFERENCES instruments (instrument_id),
    CONSTRAINT fk_studio_forbidden_instruments_studio FOREIGN KEY (studio_id) REFERENCES studios (studio_id)
);

-- [시스템] 지하철역
CREATE TABLE subway_stations
(
    subway_station_id BIGINT      NOT NULL DEFAULT nextval('subway_station_id_seq'),
    name              VARCHAR(50) NOT NULL,
    location          GEOGRAPHY(POINT, 4326)    NOT NULL,

    CONSTRAINT pk_subway_stations PRIMARY KEY (subway_station_id)
);

-- 스튜디오 인근 지하철역
CREATE TABLE subway_stations_nearby_studios
(
    subway_station_nearby_studio_id BIGINT      NOT NULL DEFAULT nextval('subway_stations_nearby_studio_id_seq'),
    sequence                        INTEGER     NOT NULL,
    subway_station_id               BIGINT      NOT NULL,
    studio_id                       BIGINT      NOT NULL,
    created_at                      TIMESTAMPTZ NOT NULL,

    CONSTRAINT pk_subway_stations_nearby_studios PRIMARY KEY (subway_station_nearby_studio_id),
    CONSTRAINT fk_subway_stations_nearby_studios_on_subway_station FOREIGN KEY (subway_station_id) REFERENCES subway_stations (subway_station_id),
    CONSTRAINT fk_subway_stations_nearby_studios_on_studio FOREIGN KEY (studio_id) REFERENCES studios (studio_id)
);

-- [시스템] 지하철 호선
CREATE TABLE subway_lines
(
    subway_line_id BIGINT       NOT NULL DEFAULT nextval('subway_line_id_seq'),
    name           VARCHAR(255) NOT NULL,
    color          VARCHAR(255) NOT NULL,
    description    VARCHAR(255) NOT NULL,
    created_at     TIMESTAMPTZ  NOT NULL,
    updated_at     TIMESTAMPTZ  NOT NULL,

    CONSTRAINT pk_subway_lines PRIMARY KEY (subway_line_id)
    -- CONSTRAINT unq_subway_lines_name UNIQUE (name)
);

-- [시스템] 지하철역 <> 호선 매핑
CREATE TABLE subway_station_lines
(
    subway_station_line_id BIGINT      NOT NULL DEFAULT nextval('subway_station_line_id_seq'),
    subway_station_id      BIGINT      NOT NULL,
    subway_line_id         BIGINT      NOT NULL,
    created_at             TIMESTAMPTZ NOT NULL,
    updated_at             TIMESTAMPTZ NOT NULL,

    CONSTRAINT pk_subway_station_lines PRIMARY KEY (subway_station_line_id),
    CONSTRAINT unq_subway_station_lines_station_line UNIQUE (subway_station_id, subway_line_id),
    CONSTRAINT fk_subway_station_lines_on_subway_station FOREIGN KEY (subway_station_id) REFERENCES subway_stations (subway_station_id),
    CONSTRAINT fk_subway_station_lines_on_subway_line FOREIGN KEY (subway_line_id) REFERENCES subway_lines (subway_line_id)
);

-- 최근 검색 이력
CREATE TABLE recent_searches
(
    recent_search_id     BIGINT       NOT NULL DEFAULT nextval('recent_search_id_seq'),
    musician_id          BIGINT       NOT NULL,
    keyword              VARCHAR(255) NOT NULL,
    created_at           TIMESTAMPTZ  NOT NULL,
    recently_searched_at TIMESTAMPTZ  NOT NULL,

    CONSTRAINT pk_recent_searches PRIMARY KEY (recent_search_id),
    CONSTRAINT unq_recent_searches_musician_keyword UNIQUE (musician_id, keyword),
    CONSTRAINT fk_recent_searches_on_musician FOREIGN KEY (musician_id) REFERENCES musicians (musician_id)
);

-- 검색 로그
CREATE TABLE search_logs
(
    search_log_id     BIGINT       NOT NULL DEFAULT nextval('search_log_id_seq'),
    musician_id       BIGINT,
    anonymous_user_id VARCHAR(255),
    search_keyword    VARCHAR(255) NOT NULL,
    searched_at       TIMESTAMPTZ  NOT NULL,

    CONSTRAINT pk_search_logs PRIMARY KEY (search_log_id),
    CONSTRAINT fk_search_logs_on_musician FOREIGN KEY (musician_id) REFERENCES musicians (musician_id),
    CHECK ((musician_id IS NOT NULL AND anonymous_user_id IS NULL) OR (musician_id IS NULL AND anonymous_user_id IS NOT NULL))
);

-- 스튜디오 조회 로그
CREATE TABLE studio_view_logs
(
    studio_view_log_id BIGINT      NOT NULL DEFAULT nextval('studio_view_log_id_seq'),
    studio_id          BIGINT      NOT NULL,
    musician_id        BIGINT,
    anonymous_user_id  VARCHAR(255),
    viewed_at          TIMESTAMPTZ NOT NULL,

    CONSTRAINT pk_studio_view_logs PRIMARY KEY (studio_view_log_id),
    CONSTRAINT fk_studio_view_logs_on_studio FOREIGN KEY (studio_id) REFERENCES studios (studio_id),
    CONSTRAINT fk_studio_view_logs_on_musician FOREIGN KEY (musician_id) REFERENCES musicians (musician_id),
    CHECK ((musician_id IS NOT NULL AND anonymous_user_id IS NULL) OR (musician_id IS NULL AND anonymous_user_id IS NOT NULL))
);

-- Beta 관련 테이블
CREATE SEQUENCE beta_registrations_registration_id_seq;
CREATE SEQUENCE beta_introductory_images_introductory_image_id_seq;
CREATE SEQUENCE beta_inquiries_inquiry_id_seq;

CREATE TABLE beta_registrations
(
    registration_id        BIGINT      NOT NULL DEFAULT nextval('beta_registrations_registration_id_seq'),
    name                   VARCHAR(50) NOT NULL,
    phone_number           VARCHAR(20) NOT NULL,
    third_party_url        TEXT        NOT NULL,
    agreed_to_personal_info_collection BOOLEAN NOT NULL,
    agreed_to_content_collection       BOOLEAN NOT NULL,
    agreed_to_third_party_provision    BOOLEAN NOT NULL,
    agreed_to_marketing     BOOLEAN     NOT NULL,
    feature_suggestions    TEXT        NULL,
    created_at             TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_beta_registrations PRIMARY KEY (registration_id)
);


CREATE TABLE beta_introductory_images
(
    introductory_image_id    BIGINT       NOT NULL DEFAULT nextval('beta_introductory_images_introductory_image_id_seq'),
    file_key                 TEXT         NOT NULL,
    registration_id          BIGINT       NOT NULL,
    CONSTRAINT pk_beta_introductory_images PRIMARY KEY (introductory_image_id)
);

ALTER TABLE beta_introductory_images
    ADD CONSTRAINT fk_beta_registration FOREIGN KEY (registration_id)
        REFERENCES beta_registrations(registration_id) ON DELETE RESTRICT ON UPDATE RESTRICT;

CREATE INDEX idx_introductory_images_on_registration_id
    ON beta_introductory_images(registration_id);

CREATE TABLE beta_inquiries (
                                inquiry_id           BIGINT      NOT NULL DEFAULT nextval('beta_inquiries_inquiry_id_seq'),
                                name                 VARCHAR(50) NOT NULL,
                                phone_number         VARCHAR(20) NOT NULL,
                                content              TEXT        NOT NULL,
                                agreed_to_privacy    BOOLEAN     NOT NULL,
                                created_at           TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                CONSTRAINT pk_beta_inquiries PRIMARY KEY (inquiry_id)
);