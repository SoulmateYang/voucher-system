CREATE TABLE sys_user (
    id BIGINT PRIMARY KEY,
    username VARCHAR(64) NOT NULL UNIQUE,
    password VARCHAR(128) NOT NULL,
    real_name VARCHAR(64),
    role VARCHAR(16) NOT NULL DEFAULT 'EMPLOYEE',
    enabled TINYINT NOT NULL DEFAULT 1,
    employee_no VARCHAR(64),
    mobile VARCHAR(32),
    department VARCHAR(64),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_username ON sys_user (username);

CREATE TABLE voucher_batch (
    id BIGINT PRIMARY KEY,
    batch_name VARCHAR(100) NOT NULL,
    voucher_type VARCHAR(32) NOT NULL DEFAULT 'RESOURCE_USAGE',
    resource_desc VARCHAR(255),
    total_count INT NOT NULL DEFAULT 0,
    valid_days INT NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
    created_by VARCHAR(64),
    discount_type VARCHAR(32),
    discount_value DECIMAL(10,2),
    min_order_amount DECIMAL(10,2),
    face_value DECIMAL(10,2),
    category_id BIGINT,
    bonus_value DECIMAL(10,2),
    transferable INT DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE voucher (
    id BIGINT PRIMARY KEY,
    batch_id BIGINT NOT NULL,
    voucher_code VARCHAR(32) NOT NULL UNIQUE,
    holder_id VARCHAR(64) NOT NULL,
    holder_name VARCHAR(64),
    status VARCHAR(16) NOT NULL DEFAULT 'ISSUED',
    version INT NOT NULL DEFAULT 0,
    issued_at DATETIME NOT NULL,
    expire_at DATETIME NOT NULL,
    used_at DATETIME,
    cancelled_at DATETIME,
    approve_ref VARCHAR(128),
    remark VARCHAR(255),
    transferable INT DEFAULT 1,
    source VARCHAR(32),
    image_url VARCHAR(255),
    is_favorite INT DEFAULT 0,
    is_pinned INT DEFAULT 0,
    pinned_at DATETIME,
    face_value DECIMAL(10,2),
    initial_balance DECIMAL(10,2),
    remaining_balance DECIMAL(10,2),
    category_id BIGINT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_v_holder ON voucher (holder_id);
CREATE INDEX idx_v_batch ON voucher (batch_id);
CREATE INDEX idx_v_status ON voucher (status);
CREATE INDEX idx_v_category ON voucher (category_id);

CREATE TABLE voucher_category (
    id BIGINT PRIMARY KEY,
    name VARCHAR(64) NOT NULL UNIQUE,
    voucher_type VARCHAR(32) NOT NULL DEFAULT 'COUPON',
    sort_order INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE verification_log (
    id BIGINT PRIMARY KEY,
    voucher_id BIGINT NOT NULL,
    voucher_code VARCHAR(32) NOT NULL,
    holder_id VARCHAR(64),
    holder_name VARCHAR(64),
    operator_id VARCHAR(64) NOT NULL,
    operator_name VARCHAR(64),
    verified_at DATETIME NOT NULL,
    order_amount DECIMAL(10,2),
    remark VARCHAR(255),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE audit_log (
    id BIGINT PRIMARY KEY,
    entity_type VARCHAR(32),
    entity_id BIGINT,
    action VARCHAR(32) NOT NULL,
    operator_id VARCHAR(64),
    operator_name VARCHAR(64),
    detail TEXT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE voucher_consumption (
    id BIGINT PRIMARY KEY,
    voucher_id BIGINT NOT NULL,
    voucher_code VARCHAR(32) NOT NULL,
    consume_amount DECIMAL(10,2) NOT NULL,
    balance_before DECIMAL(10,2) NOT NULL,
    balance_after DECIMAL(10,2) NOT NULL,
    order_amount DECIMAL(10,2),
    operator_id VARCHAR(64) NOT NULL,
    operator_name VARCHAR(64),
    remark VARCHAR(255),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE voucher_gift (
    id BIGINT PRIMARY KEY,
    voucher_id BIGINT NOT NULL,
    from_user_id VARCHAR(64) NOT NULL,
    from_user_name VARCHAR(64),
    to_user_id VARCHAR(64),
    to_user_name VARCHAR(64),
    message VARCHAR(255),
    status VARCHAR(16) NOT NULL DEFAULT 'PENDING',
    gift_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    handled_at DATETIME,
    expire_at DATETIME,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);
