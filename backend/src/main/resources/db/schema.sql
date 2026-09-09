-- 奶娃二手交易 — 数据库 schema
-- MySQL 8.0 LTS,首次启动由 docker-entrypoint-initdb.d 自动执行
-- 字符集 utf8mb4,排序规则 utf8mb4_unicode_ci

USE naiwa_market;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================
-- 1. user 用户表
-- ============================================================
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `open_id`     VARCHAR(64)  NOT NULL                COMMENT '微信 openId,Mock 模式下为 mock_xxx',
    `nickname`    VARCHAR(32)  NOT NULL DEFAULT ''     COMMENT '昵称,1-32 字符',
    `avatar_url`  VARCHAR(512) NOT NULL DEFAULT ''     COMMENT '头像 URL,http(s) 格式,≤512',
    `role`        VARCHAR(16)  NOT NULL DEFAULT 'USER' COMMENT 'USER/ADMIN',
    `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_open_id` (`open_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ============================================================
-- 2. item 商品表
-- ============================================================
DROP TABLE IF EXISTS `item`;
CREATE TABLE `item` (
    `id`           BIGINT         NOT NULL AUTO_INCREMENT COMMENT '主键',
    `seller_id`    BIGINT         NOT NULL                COMMENT '卖家 user.id',
    `title`        VARCHAR(60)    NOT NULL                COMMENT '标题,5-60 字符',
    `description`  VARCHAR(2000)  NOT NULL DEFAULT ''     COMMENT '描述,10-2000 字符',
    `category`     VARCHAR(16)    NOT NULL                COMMENT '分类:数码/服饰/美妆/书籍/生活用品/运动/其他',
    `condition`    VARCHAR(16)    NOT NULL                COMMENT '成色:全新/95新/9成新/需维修',
    `price`        DECIMAL(10, 2) NOT NULL                COMMENT '价格,单位元,>0',
    `trade_method` VARCHAR(16)    NOT NULL                COMMENT 'OFFLINE_ONLY/ESCROW_ONLY/BOTH',
    `status`       VARCHAR(16)    NOT NULL DEFAULT 'ON_SALE' COMMENT 'ON_SALE/RESERVED/SOLD/OFF_SHELF',
    `images`       JSON           NOT NULL                COMMENT '图片 URL 数组,1 张及以上',
    `created_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_seller_id`  (`seller_id`),
    KEY `idx_status_ctg` (`status`, `category`),
    KEY `idx_status_cond_price` (`status`, `condition`, `price`),
    KEY `idx_created_at` (`created_at`),
    CONSTRAINT `fk_item_seller` FOREIGN KEY (`seller_id`) REFERENCES `user` (`id`),
    CONSTRAINT `chk_item_price` CHECK (`price` > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品表';

-- ============================================================
-- 3. order 订单表(一物一单)
-- ============================================================
DROP TABLE IF EXISTS `order`;
CREATE TABLE `order` (
    `id`             BIGINT         NOT NULL AUTO_INCREMENT COMMENT '主键',
    `item_id`        BIGINT         NOT NULL                COMMENT '商品 id,唯一索引保障一物一单',
    `seller_id`      BIGINT         NOT NULL                COMMENT '卖家',
    `buyer_id`       BIGINT         NOT NULL                COMMENT '买家',
    `price_snapshot` DECIMAL(10, 2) NOT NULL                COMMENT '下单时拷贝 Item.price',
    `trade_method`   VARCHAR(16)    NOT NULL                COMMENT 'OFFLINE/ESCROW',
    `status`         VARCHAR(32)    NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/REJECTED/PENDING_HANDOVER/COMPLETED/CANCELLED',
    `created_at`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `completed_at`   DATETIME     NULL DEFAULT NULL          COMMENT '确认收货时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_item_id` (`item_id`),
    KEY `idx_seller_id` (`seller_id`),
    KEY `idx_buyer_id`  (`buyer_id`),
    KEY `idx_status`    (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

-- ============================================================
-- 4. wallet 钱包表
-- ============================================================
DROP TABLE IF EXISTS `wallet`;
CREATE TABLE `wallet` (
    `user_id`  BIGINT         NOT NULL                COMMENT '主键,user.id',
    `balance`  DECIMAL(10, 2) NOT NULL DEFAULT 0.00  COMMENT '可用余额',
    `frozen`   DECIMAL(10, 2) NOT NULL DEFAULT 0.00  COMMENT '冻结金额',
    `updated_at` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`user_id`),
    CONSTRAINT `fk_wallet_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
    CONSTRAINT `chk_wallet_balance` CHECK (`balance` >= 0),
    CONSTRAINT `chk_wallet_frozen`  CHECK (`frozen` >= 0 AND `frozen` <= `balance`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='钱包表';

-- ============================================================
-- 5. wallet_flow 钱包流水表
-- ============================================================
DROP TABLE IF EXISTS `wallet_flow`;
CREATE TABLE `wallet_flow` (
    `id`            BIGINT         NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`       BIGINT         NOT NULL                COMMENT '用户',
    `order_id`      BIGINT         NULL DEFAULT NULL       COMMENT '关联订单,可空',
    `type`          VARCHAR(32)    NOT NULL                COMMENT 'RECHARGE/FREEZE/UNFREEZE/SETTLEMENT_IN/SETTLEMENT_OUT',
    `amount`        DECIMAL(10, 2) NOT NULL                COMMENT '变动金额(>0,符号由 type 决定)',
    `balance_after` DECIMAL(10, 2) NOT NULL                COMMENT '操作后可用余额',
    `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user_id_created` (`user_id`, `created_at`),
    KEY `idx_order_id`        (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='钱包流水表';

-- ============================================================
-- 6. favorite 收藏表
-- ============================================================
DROP TABLE IF EXISTS `favorite`;
CREATE TABLE `favorite` (
    `id`         BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`    BIGINT   NOT NULL                COMMENT '用户',
    `item_id`    BIGINT   NOT NULL                COMMENT '商品',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_item` (`user_id`, `item_id`),
    KEY `idx_item_id` (`item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收藏表';

-- ============================================================
-- 7. comment 商品公开留言表
-- ============================================================
DROP TABLE IF EXISTS `comment`;
CREATE TABLE `comment` (
    `id`         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `item_id`    BIGINT       NOT NULL                COMMENT '商品',
    `user_id`    BIGINT       NOT NULL                COMMENT '留言用户',
    `parent_id`  BIGINT       NULL DEFAULT NULL       COMMENT '父留言 id,回复用',
    `content`    VARCHAR(500) NOT NULL                COMMENT '1-500 字符',
    `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_item_id_created` (`item_id`, `created_at`),
    KEY `idx_user_id`         (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品留言表';

-- ============================================================
-- 8. order_message 订单协商表(私有)
-- ============================================================
DROP TABLE IF EXISTS `order_message`;
CREATE TABLE `order_message` (
    `id`         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `order_id`   BIGINT       NOT NULL                COMMENT '订单',
    `sender_id`  BIGINT       NOT NULL                COMMENT '发送者 user.id',
    `content`    VARCHAR(500) NOT NULL                COMMENT '1-500 字符',
    `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_order_id_created` (`order_id`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单协商表';

-- ============================================================
-- 9. review 评价表
-- ============================================================
DROP TABLE IF EXISTS `review`;
CREATE TABLE `review` (
    `id`             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `order_id`       BIGINT       NOT NULL                COMMENT '订单',
    `author_id`      BIGINT       NOT NULL                COMMENT '评价作者',
    `target_user_id` BIGINT       NOT NULL                COMMENT '被评价用户',
    `rating`         TINYINT      NOT NULL                COMMENT '1-5 星',
    `content`        VARCHAR(500) NOT NULL DEFAULT ''     COMMENT '0-500 字符',
    `created_at`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_author` (`order_id`, `author_id`),
    KEY `idx_target_user` (`target_user_id`),
    CONSTRAINT `chk_review_rating` CHECK (`rating` BETWEEN 1 AND 5)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评价表';

SET FOREIGN_KEY_CHECKS = 1;