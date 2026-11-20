--liquibase formatted sql

--changeset hendi:001-create-account
CREATE TABLE account (
    id          BIGSERIAL    PRIMARY KEY,
    code        VARCHAR(20)  NOT NULL UNIQUE,
    name        VARCHAR(150) NOT NULL,
    type        VARCHAR(20)  NOT NULL CHECK (type IN
                ('ASSET','LIABILITY','EQUITY','REVENUE','EXPENSE')),
    parent_id   BIGINT       REFERENCES account(id),
    active      BOOLEAN      NOT NULL DEFAULT TRUE,
    version     BIGINT       NOT NULL DEFAULT 0,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ
);
--rollback DROP TABLE account;

--changeset hendi:002-index-account-parent
CREATE INDEX idx_account_parent ON account(parent_id);
--rollback DROP INDEX idx_account_parent;
