-- 用户表
CREATE TABLE sys_user (
    id BIGINT PRIMARY KEY,
    username VARCHAR(64) NOT NULL UNIQUE COMMENT '用户名(工号)',
    password VARCHAR(128) NOT NULL COMMENT '密码(BCrypt)',
    real_name VARCHAR(64) COMMENT '真实姓名',
    role VARCHAR(16) NOT NULL DEFAULT 'EMPLOYEE' COMMENT 'ADMIN/EMPLOYEE',
    enabled TINYINT NOT NULL DEFAULT 1 COMMENT '启用状态',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户';

-- 预置管理员账号 admin / admin123
INSERT INTO sys_user (id, username, password, real_name, role) VALUES
(1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '系统管理员', 'ADMIN');

-- 卡券批次表
CREATE TABLE voucher_batch (
    id BIGINT PRIMARY KEY,
    batch_name VARCHAR(100) NOT NULL COMMENT '批次名称',
    voucher_type VARCHAR(32) NOT NULL DEFAULT 'RESOURCE_USAGE' COMMENT '券类型',
    resource_desc VARCHAR(255) COMMENT '资源描述',
    total_count INT NOT NULL DEFAULT 0 COMMENT '已发放总量',
    valid_days INT NOT NULL COMMENT '有效天数',
    status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE/PAUSED/FINISHED',
    created_by VARCHAR(64),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='卡券批次';

-- 卡券实例表
CREATE TABLE voucher (
    id BIGINT PRIMARY KEY,
    batch_id BIGINT NOT NULL COMMENT '所属批次',
    voucher_code VARCHAR(32) NOT NULL UNIQUE COMMENT '券码',
    holder_id VARCHAR(64) NOT NULL COMMENT '持有人工号',
    holder_name VARCHAR(64) COMMENT '持有人姓名',
    status VARCHAR(16) NOT NULL DEFAULT 'ISSUED' COMMENT 'ISSUED/USED/EXPIRED/CANCELLED',
    version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    issued_at DATETIME NOT NULL COMMENT '发放时间',
    expire_at DATETIME NOT NULL COMMENT '过期时间(当天23:59:59)',
    used_at DATETIME COMMENT '核销时间',
    cancelled_at DATETIME COMMENT '作废时间',
    approve_ref VARCHAR(128) COMMENT '审批单号',
    remark VARCHAR(255),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_holder (holder_id),
    INDEX idx_batch (batch_id),
    INDEX idx_status (status),
    INDEX idx_created_status (created_at, status),
    INDEX idx_expire_status (expire_at, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='卡券实例';

-- 核销记录表
CREATE TABLE verification_log (
    id BIGINT PRIMARY KEY,
    voucher_id BIGINT NOT NULL,
    voucher_code VARCHAR(32) NOT NULL,
    holder_id VARCHAR(64),
    holder_name VARCHAR(64),
    operator_id VARCHAR(64) NOT NULL COMMENT '核销操作人',
    operator_name VARCHAR(64),
    verified_at DATETIME NOT NULL,
    remark VARCHAR(255),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_voucher (voucher_id),
    INDEX idx_operator (operator_id),
    INDEX idx_verified (verified_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='核销记录';

-- 审计日志表
CREATE TABLE audit_log (
    id BIGINT PRIMARY KEY,
    entity_type VARCHAR(32) COMMENT 'voucher/batch/user',
    entity_id BIGINT,
    action VARCHAR(32) NOT NULL COMMENT 'ISSUE/VERIFY/CANCEL/EXPIRE/LOGIN',
    operator_id VARCHAR(64),
    operator_name VARCHAR(64),
    detail TEXT COMMENT '变更详情JSON',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_entity (entity_type, entity_id),
    INDEX idx_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审计日志';
