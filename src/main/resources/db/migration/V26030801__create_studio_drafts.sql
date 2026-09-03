CREATE TABLE studio_drafts
(
    studio_draft_id   BIGINT                   NOT NULL,
    owner_id          BIGINT                   NOT NULL,
    step              INTEGER                  NOT NULL,
    studio_name       VARCHAR(100)             NULL,
    studio_draft_data JSONB                    NOT NULL,
    expires_at        TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at        TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at        TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT pk_studio_drafts PRIMARY KEY (studio_draft_id)
);

CREATE INDEX idx_studio_drafts_owner_id ON studio_drafts (owner_id);
CREATE INDEX idx_studio_drafts_expires_at ON studio_drafts (expires_at);
