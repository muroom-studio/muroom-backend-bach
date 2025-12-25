CREATE TABLE report_reasons (
                                report_reason_id  BIGINT       NOT NULL,
                                code              VARCHAR(50)  NOT NULL,
                                is_active         BOOLEAN      NOT NULL DEFAULT true,
                                description       VARCHAR(255) NOT NULL,
                                created_at        TIMESTAMPTZ  NOT NULL,
                                updated_at        TIMESTAMPTZ  NOT NULL,

                                CONSTRAINT pk_report_reasons PRIMARY KEY (report_reason_id),
                                CONSTRAINT unq_report_reasons_code UNIQUE (code)
);

CREATE TABLE reports (
     report_id         BIGINT       NOT NULL,
     reporter_id       BIGINT       NOT NULL,
     target_type       VARCHAR(30)  NOT NULL,
     target_id         BIGINT       NOT NULL,
     report_reason_id  BIGINT       NOT NULL,
     description       VARCHAR(1000) NULL,
     status            VARCHAR(30)  NOT NULL,
     snapshot          JSONB        NOT NULL,
     created_at        TIMESTAMPTZ  NOT NULL,
     updated_at        TIMESTAMPTZ  NOT NULL,
     deleted_at        TIMESTAMPTZ  NOT NULL,

     CONSTRAINT pk_reports PRIMARY KEY (report_id),
     CONSTRAINT unq_reports_reporter_target UNIQUE (reporter_id, target_type, target_id),
     CONSTRAINT fk_reports_on_musicians FOREIGN KEY (reporter_id) REFERENCES musicians(musician_id),
     CONSTRAINT fk_reports_on_report_reasons FOREIGN KEY (report_reason_id) REFERENCES report_reasons(report_reason_id)
);

CREATE TABLE reports_reply (
   report_reply_id   BIGINT       NOT NULL,
   report_id         BIGINT       NOT NULL,
   message           VARCHAR(1000) NOT NULL,
   created_at        TIMESTAMPTZ  NOT NULL,
   updated_at        TIMESTAMPTZ  NOT NULL,

   CONSTRAINT pk_reports_reply PRIMARY KEY (report_reply_id),
   CONSTRAINT fk_reports_on_reply_report FOREIGN KEY (report_id) REFERENCES reports(report_id) ON DELETE CASCADE
);
