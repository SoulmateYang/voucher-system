-- V6: personal features - gift, favorite, pin, manual entry

ALTER TABLE voucher
    ADD COLUMN transferable TINYINT DEFAULT 1,
    ADD COLUMN source VARCHAR(16) DEFAULT 'BATCH',
    ADD COLUMN image_url VARCHAR(255),
    ADD COLUMN is_favorite TINYINT DEFAULT 0,
    ADD COLUMN is_pinned TINYINT DEFAULT 0,
    ADD COLUMN pinned_at DATETIME;

ALTER TABLE voucher_batch
    ADD COLUMN transferable TINYINT DEFAULT 1;

CREATE TABLE voucher_gift (
    id            BIGINT PRIMARY KEY,
    voucher_id    BIGINT NOT NULL,
    from_user_id  VARCHAR(64) NOT NULL,
    from_user_name VARCHAR(64) NOT NULL,
    to_user_id    VARCHAR(64) NOT NULL,
    to_user_name  VARCHAR(64) NOT NULL,
    message       VARCHAR(200),
    status        VARCHAR(16) NOT NULL DEFAULT 'PENDING',
    gift_at       DATETIME NOT NULL,
    handled_at    DATETIME,
    expire_at     DATETIME NOT NULL,
    created_at    DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_gift_from ON voucher_gift(from_user_id, status);
CREATE INDEX idx_gift_to ON voucher_gift(to_user_id, status);
CREATE INDEX idx_gift_voucher ON voucher_gift(voucher_id);
