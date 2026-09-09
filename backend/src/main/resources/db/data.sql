-- 奶娃二手交易 — 演示数据
-- 由 docker-entrypoint-initdb.d 在 schema.sql 之后自动执行
--
-- 注意:所有中文字符串前都加了 _utf8mb4 前缀。原因:
--   docker-entrypoint.sh 调 mysql 客户端时未指定 --default-character-set=utf8mb4,
--   默认走 latin1,导致 UTF-8 字节被二次编码成双重 UTF-8。
--   用 _utf8mb4'...' 字面量前缀,MySQL 会强制按 UTF-8 解析字符串,客户端字符集不再有影响。
--   (hex 验证:小=E5B08F,正确)

USE naiwa_market;

-- ============================================================
-- 演示账号(登录页"演示账号"按钮可一键登录)
--   id=1  小白 mock_open_xiaobai
--   id=2  小红 mock_open_xiaohong
--   id=3  小黑 mock_open_xiaohei
-- ============================================================
INSERT INTO `user` (`id`, `open_id`, `nickname`, `avatar_url`, `role`) VALUES
    (1, _utf8mb4'mock_open_xiaobai', _utf8mb4'小白', _utf8mb4'https://api.dicebear.com/7.x/avataaars/svg?seed=xiaobai', _utf8mb4'USER'),
    (2, _utf8mb4'mock_open_xiaohong', _utf8mb4'小红', _utf8mb4'https://api.dicebear.com/7.x/avataaars/svg?seed=xiaohong', _utf8mb4'USER'),
    (3, _utf8mb4'mock_open_xiaohei', _utf8mb4'小黑', _utf8mb4'https://api.dicebear.com/7.x/avataaars/svg?seed=xiaohei', _utf8mb4'USER');

-- ============================================================
-- 钱包(每人 1000 元演示余额)
-- ============================================================
INSERT INTO `wallet` (`user_id`, `balance`, `frozen`) VALUES
    (1, 1000.00, 0.00),
    (2, 1000.00, 0.00),
    (3, 1000.00, 0.00);

-- ============================================================
-- 示例商品(覆盖 7 个分类与 4 种成色)
-- ============================================================
INSERT INTO `item`
    (`id`, `seller_id`, `title`, `description`, `category`, `condition`, `price`, `trade_method`, `status`, `images`)
VALUES
    (1, 1, _utf8mb4'索尼 WH-1000XM5 头戴式降噪耳机',
     _utf8mb4'去年九月买的,主要用于通勤,配件齐全,无明显使用痕迹,降噪效果一流。',
     _utf8mb4'数码', _utf8mb4'95新', 1499.00, _utf8mb4'BOTH', _utf8mb4'ON_SALE',
     JSON_ARRAY(_utf8mb4'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=800')),

    (2, 1, _utf8mb4'iPad Air 5 64G WiFi 版 深空灰',
     _utf8mb4'原装贴膜+原装保护壳,电池效率 96%,无磕碰,送 Apple Pencil 一代(非原装笔尖已用)。',
     _utf8mb4'数码', _utf8mb4'9成新', 3299.00, _utf8mb4'BOTH', _utf8mb4'ON_SALE',
     JSON_ARRAY(_utf8mb4'https://images.unsplash.com/photo-1544244015-0df4b3ffc6b0?w=800')),

    (3, 2, _utf8mb4'优衣库摇粒绒外套 M 码 灰色',
     _utf8mb4'去年冬天穿了几次,尺码合适,机洗过几次,无起球无掉色。',
     _utf8mb4'服饰', _utf8mb4'9成新', 89.00, _utf8mb4'OFFLINE_ONLY', _utf8mb4'ON_SALE',
     JSON_ARRAY(_utf8mb4'https://images.unsplash.com/photo-1591047139829-d91aecb6caea?w=800')),

    (4, 2, _utf8mb4'雅诗兰黛小棕瓶 50ml 全新未拆封',
     _utf8mb4'生日礼物,但我是混油皮用不了,全新转让,保质期到 2028 年。',
     _utf8mb4'美妆', _utf8mb4'全新', 599.00, _utf8mb4'BOTH', _utf8mb4'ON_SALE',
     JSON_ARRAY(_utf8mb4'https://images.unsplash.com/photo-1556228720-195a672e8a03?w=800')),

    (5, 3, _utf8mb4'《深入理解 Java 虚拟机》第 3 版',
     _utf8mb4'读完一遍,书页微黄但无笔记,适合 JVM 进阶。',
     _utf8mb4'书籍', _utf8mb4'9成新', 45.00, _utf8mb4'OFFLINE_ONLY', _utf8mb4'ON_SALE',
     JSON_ARRAY(_utf8mb4'https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=800')),

    (6, 3, _utf8mb4'小米空气净化器 4 Pro',
     _utf8mb4'搬家出,滤芯还有 30% 寿命,功能正常。',
     _utf8mb4'生活用品', _utf8mb4'95新', 599.00, _utf8mb4'OFFLINE_ONLY', _utf8mb4'ON_SALE',
     JSON_ARRAY(_utf8mb4'https://images.unsplash.com/photo-1585771724684-38269d6639fd?w=800')),

    (7, 1, _utf8mb4'迪卡侬瑜伽垫 6mm 加厚',
     _utf8mb4'用了两个月,无异味,附送绑带。',
     _utf8mb4'运动', _utf8mb4'95新', 49.00, _utf8mb4'OFFLINE_ONLY', _utf8mb4'ON_SALE',
     JSON_ARRAY(_utf8mb4'https://images.unsplash.com/photo-1601925260368-ae2f83cf8b7f?w=800')),

    (8, 2, _utf8mb4'罗技 MX Master 3S 无线鼠标',
     _utf8mb4'手感绝佳,电池续航 70 天,原装数据线。',
     _utf8mb4'其他', _utf8mb4'95新', 499.00, _utf8mb4'BOTH', _utf8mb4'ON_SALE',
     JSON_ARRAY(_utf8mb4'https://images.unsplash.com/photo-1527864550417-7fd91fc51a46?w=800'));

-- ============================================================
-- 演示收藏(小白收藏了 id=2 的 iPad)
-- ============================================================
INSERT INTO `favorite` (`user_id`, `item_id`) VALUES
    (1, 2),
    (1, 4),
    (2, 1);

-- ============================================================
-- 演示留言(公开留言)
-- ============================================================
INSERT INTO `comment` (`item_id`, `user_id`, `parent_id`, `content`) VALUES
    (1, 2, NULL, _utf8mb4'支持当面交易吗?能小刀吗?'),
    (1, 1, 1,    _utf8mb4'可以面交,价格小刀空间不大~');