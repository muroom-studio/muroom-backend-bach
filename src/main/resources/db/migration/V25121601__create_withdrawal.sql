CREATE SEQUENCE withdrawal_reason_id_seq;
CREATE SEQUENCE musician_withdrawals_id_seq;

-- 탈퇴 사유
CREATE TABLE withdrawal_reasons
(
    withdrawal_reason_id BIGINT       NOT NULL DEFAULT nextval('withdrawal_reason_id_seq'),
    code                 VARCHAR(50)  NOT NULL,
    description          VARCHAR(255) NOT NULL,
    is_active            BOOLEAN      NOT NULL DEFAULT true,
    created_at           TIMESTAMPTZ  NOT NULL,
    updated_at           TIMESTAMPTZ  NOT NULL,

    CONSTRAINT pk_withdrawal_reasons PRIMARY KEY (withdrawal_reason_id),
    CONSTRAINT unq_withdrawal_reasons_code UNIQUE (code)
);
-- 뮤지션 탈퇴 이력
CREATE TABLE musician_withdrawals
(
    musician_withdrawals_id BIGINT      NOT NULL DEFAULT nextval('musician_withdrawals_id_seq'),
    withdrawal_reason_id    BIGINT      NOT NULL,
    musician_id             BIGINT      NOT NULL,
    opinion                 TEXT        NULL,
    created_at              TIMESTAMPTZ NOT NULL,

    CONSTRAINT pk_musician_withdrawals PRIMARY KEY (musician_withdrawals_id),
    CONSTRAINT fk_musician_withdrawals_musicians FOREIGN KEY (musician_id) REFERENCES musicians (musician_id),
    CONSTRAINT fk_musician_withdrawals_withdrawal_reasons FOREIGN KEY (withdrawal_reason_id) REFERENCES withdrawal_reasons (withdrawal_reason_id)
);

