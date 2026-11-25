-- ============================================================
-- 实验七 图书管理系统 数据库表结构
-- 数据库：library_exp7  字符集：utf8mb4
-- ============================================================

CREATE TABLE IF NOT EXISTS admin (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    username    VARCHAR(50)  NOT NULL COMMENT '管理员账号',
    password    VARCHAR(100) NOT NULL COMMENT '密码（课程演示使用明文）',
    name        VARCHAR(50)  NOT NULL COMMENT '姓名',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_admin_username (username)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '管理员表';

CREATE TABLE IF NOT EXISTS reader (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    username    VARCHAR(50)  NOT NULL COMMENT '读者账号',
    password    VARCHAR(100) NOT NULL COMMENT '密码（课程演示使用明文）',
    name        VARCHAR(50)  NOT NULL COMMENT '姓名',
    phone       VARCHAR(20)  DEFAULT NULL COMMENT '手机号',
    email       VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1 正常 0 禁用',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_reader_username (username)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '读者表';

CREATE TABLE IF NOT EXISTS book (
    id               BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    title            VARCHAR(100)  NOT NULL COMMENT '书名',
    author           VARCHAR(100)  NOT NULL COMMENT '作者',
    isbn             VARCHAR(32)   NOT NULL COMMENT 'ISBN',
    publisher        VARCHAR(100)  DEFAULT NULL COMMENT '出版社',
    category         VARCHAR(50)   DEFAULT NULL COMMENT '分类',
    price            DECIMAL(10, 2) DEFAULT 0.00 COMMENT '价格',
    total_copies     INT           NOT NULL DEFAULT 1 COMMENT '馆藏总数',
    available_copies INT           NOT NULL DEFAULT 1 COMMENT '当前可借数量',
    description      VARCHAR(500)  DEFAULT NULL COMMENT '简介',
    create_time      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_book_isbn (isbn)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '图书表';

CREATE TABLE IF NOT EXISTS borrow_record (
    id          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    reader_id   BIGINT      NOT NULL COMMENT '读者 ID',
    book_id     BIGINT      NOT NULL COMMENT '图书 ID',
    borrow_date DATE        NOT NULL COMMENT '借阅日期',
    due_date    DATE        NOT NULL COMMENT '应还日期',
    return_date DATE        DEFAULT NULL COMMENT '归还日期',
    status      VARCHAR(20) NOT NULL DEFAULT 'BORROWED' COMMENT '状态：BORROWED 借阅中 / RETURNED 已归还',
    create_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_borrow_reader (reader_id),
    KEY idx_borrow_book (book_id),
    CONSTRAINT fk_borrow_reader FOREIGN KEY (reader_id) REFERENCES reader (id),
    CONSTRAINT fk_borrow_book FOREIGN KEY (book_id) REFERENCES book (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '借阅记录表';
