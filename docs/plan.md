# 二手交易平台 Plan

> 基于 Explore agent 报告（round 1，7 个 issue）已重写 6 处

## 架构概览

系统采用前后端分离：微信小程序（原生 WXML/WXSS/JS + TypeScript）负责展示与交互，Spring Boot 4 + JDK 21 + Gradle 8 (Groovy DSL) 后端提供 REST API，MySQL 8 持久化，微信云存储保存图片（后端转发换 HTTPS URL 落库）。小程序通过 `wx.request` 调用后端，鉴权基于 `wx.login` code 换取的 JWT（开发期可用 Mock 登录兜底，登录页"演示账号"一键选小白 / 小红 / 小黑）。后端分三层：Controller → Service → Mapper（MyBatis-Plus），Service 内以事务保证商品锁定与钱包冻结的原子性。订单与商品状态联动、钱包流水与收藏等通过数据库唯一索引与事务约束保障一致性，协商与留言采用短轮询，无需 WebSocket。

- 小程序端：首页/搜索/详情/发布/订单/协商/收藏/个人中心 8 个页面组，封装 `request` 与 `auth` 工具
- 后端服务：认证、用户、商品、订单、钱包、收藏、留言/协商、评价、个人中心聚合 9 个逻辑域，共享鉴权拦截器与统一异常处理
- 存储：MySQL 单库 9 张主表（8 张业务表 + 1 张可选的 file_meta 文件元数据表，见文件模块设计），图片由 `POST /api/files/upload` 接口接收上传并换取 HTTPS URL 落库（具体存储后端选型见 [`tech-stack.md` §四](tech-stack.md)）

### Spec 覆盖映射
<!-- review-fix: ISSUE-1 -->
| Spec | 归属模块 | 对应接口/页面 |
|------|----------|---------------|
| F1 用户身份 | 认证与用户模块 | POST /api/auth/login, GET /api/users/me；GET /api/items 与详情页为游客可匿名访问白名单 |
| F2 商品发布与管理 | 商品模块 | POST /api/items, PUT /api/items/{id}/off, DELETE /api/items/{id}, GET /api/items?sellerId=me（我的发布） |
| F3 浏览与搜索 | 商品模块 | GET /api/items（筛选/排序/搜索）, GET /api/items/{id} |
| F4 收藏 | 收藏模块 | POST/DELETE /api/favorites/{itemId}, GET /api/favorites |
| F5 公开留言 | 留言与协商模块 | GET/POST /api/items/{id}/comments |
| F6 订单与交易 | 订单模块 | POST /api/orders, POST /api/orders/{id}/confirm|reject|cancel|complete, GET /api/orders?role=buy|sell |
| F7 钱包 | 钱包模块 | POST /api/wallet/recharge, GET /api/wallet, GET /api/wallet/flows |
| F8 订单协商 | 留言与协商模块 | GET/POST /api/orders/{id}/messages |
| F9 评价 | 评价模块 | POST /api/orders/{id}/review, GET /api/users/{id}/reviews |
| F10 个人中心 | 个人中心聚合（复用用户/商品/订单/收藏/评价） | profile 页面聚合调用上述 5 个查询接口，无独立后端表 |

## 核心数据结构

### User
- id: Long 主键
- openId: String 微信 openId，唯一索引，Mock 模式下为 mock 前缀
- nickname: String 昵称，1-32 字符
- avatarUrl: String 头像 URL，长度 ≤ 512，http(s) 格式
- role: Enum USER/ADMIN，默认 USER
- createdAt: DateTime

### Item
- id: Long 主键
- sellerId: Long 外键 User.id，索引
- title: String 标题 5-60 字符，非空
- description: String 描述 10-2000 字符
- category: Enum 数码/服饰/美妆/书籍/生活用品/运动/其他
- condition: Enum 全新/95新/9成新/需维修
- price: Decimal(10,2) >0，单位元，保留两位
- tradeMethod: Enum OFFLINE_ONLY/ESCROW_ONLY/BOTH
- status: Enum ON_SALE/RESERVED/SOLD/OFF_SHELF，默认 ON_SALE，索引
- images: JSON 数组，存储图片 URL 列表，1 张及以上（前端建议 1-9 张，后端不设上限），单张 ≤5MB，URL 长度 ≤1024
<!-- review-fix: ISSUE-4 -->
- createdAt, updatedAt: DateTime

### Order
- id: Long 主键
- itemId: Long 唯一索引，保障一物一单
- sellerId, buyerId: Long 索引
- priceSnapshot: Decimal(10,2) 下单时拷贝 Item.price，单位元
- tradeMethod: Enum OFFLINE/ESCROW 下单时确定且必须在 Item.tradeMethod 允许范围内（BOTH 时二选一，OFFLINE_ONLY 仅可 OFFLINE）
- status: Enum PENDING/REJECTED/PENDING_HANDOVER/COMPLETED/CANCELLED，索引
- createdAt, updatedAt, completedAt: DateTime

### Wallet / WalletFlow
- Wallet: userId 主键，balance Decimal(10,2) 默认 0 且 CHECK(balance>=0)，frozen Decimal(10,2) 默认 0 且 CHECK(frozen>=0 AND frozen<=balance)，可用余额 = balance - frozen
<!-- review-fix: ISSUE-5 -->
- WalletFlow: id, userId, orderId 可空, type Enum RECHARGE/FREEZE/UNFREEZE/SETTLEMENT_IN/SETTLEMENT_OUT, amount Decimal(10,2) >0 单位元（符号由 type 决定），balanceAfter Decimal(10,2) 操作后可用余额，createdAt；type 与 amount 对应关系：RECHARGE 增加可用，FREEZE 增加 frozen，UNFREEZE 减少 frozen，SETTLEMENT_OUT 减少 balance 与 frozen，SETTLEMENT_IN 增加 balance

### Favorite
- id: Long 主键，唯一索引 (userId, itemId)

### Comment
- id: Long 主键
- itemId: Long 索引
- userId: Long
- parentId: Long 可空，回复合用
- content: String 1-500 字符
- createdAt: DateTime

### OrderMessage（订单协商）
- id: Long 主键
- orderId: Long 索引
- senderId: Long
- content: String 1-500 字符
- createdAt: DateTime

### Review
- id: Long 主键，唯一索引 (orderId, authorId)
- orderId: Long
- authorId, targetUserId: Long
- rating: Int 1-5
- content: String 0-500 字符
- createdAt: DateTime

## 模块设计

### 认证与用户模块
**职责：** 微信 code2Session 换 openId、签发 JWT、Mock 登录兜底、用户资料维护；游客白名单放行 GET /api/items 与 GET /api/items/{id}
**对外接口：**
- POST /api/auth/login Body {code: String 必填} → 200 {token: String, user: User}；400 code 无效
- GET /api/users/me Header Authorization → 200 User；401 未登录
- PUT /api/users/me Body {nickname, avatarUrl} → 200 User；400 校验失败
**依赖：** User 表，JWT 工具，微信接口（可 Mock）
**错误码：** 401 未登录，400 参数错误

### 商品模块
**职责：** 发布的校验与落库、状态流转（ON_SALE↔RESERVED→SOLD/OFF_SHELF）、首页倒序分页、按分类/成色/tradeMethod/价格区间筛选、标题描述 LIKE 搜索、详情查询；提供“我的发布”查询
**对外接口：**
- POST /api/items Header Authorization Body {title, description, category, condition, price, tradeMethod, images: String[]} → 201 {item}；400 必填/枚举/价格>0/图片≥1 校验失败；401 未登录
- GET /api/items Query {category?, condition?, tradeMethod?, minPrice?, maxPrice? Decimal, keyword? String, sort? latest|price_asc|price_desc 默认 latest, page? Int 默认1, size? Int 默认20 1-50} → 200 PageResult<Item>；keyword 对 title/description LIKE
- GET /api/items/{id} → 200 Item 含卖家 User；404 商品不存在
- PUT /api/items/{id}/off Header Authorization → 200 Item；403 非卖家；400 状态非法
- DELETE /api/items/{id} Header Authorization → 204；403 非卖家；400 已成交不可删
- GET /api/items?sellerId=me Header Authorization → 200 PageResult<Item> 我的发布
**依赖：** Item 表，User 鉴权（仅卖家可下架），文件模块返回的 URL
<!-- review-fix: ISSUE-2, ISSUE-3 -->

### 订单模块
**职责：** 单件下单的原子锁定、卖家确认/拒绝、双方取消、待交付到完成的确认、商品与订单状态联动、交易方式一致性校验、幂等保障
**对外接口：**
- POST /api/orders Header Authorization Body {itemId: Long 必填, tradeMethod: OFFLINE|ESCROW 必填且在 Item 允许范围内} → 201 Order；400 余额不足/交易方式不匹配；401 未登录；403 买本人商品；409 商品已被预订
- POST /api/orders/{id}/confirm Header Authorization → 200 Order；403 非卖家；400 非 PENDING；409 已被取消
- POST /api/orders/{id}/reject Header Authorization → 200 Order；403 非卖家；400 非 PENDING
- POST /api/orders/{id}/cancel Header Authorization → 200 Order；403 非买卖双方；400 非 PENDING/PENDING_HANDOVER
- POST /api/orders/{id}/complete Header Authorization → 200 Order；403 非买家；400 非 PENDING_HANDOVER；幂等重复返回 200 不重复结算
- GET /api/orders Query {role: buy|sell 必填, status?, page?, size?} Header Authorization → 200 PageResult<Order>
**依赖：** Order、Item、Wallet；事务内 SELECT ... FOR UPDATE 锁定 Item 行，ESCROW 时冻结/解冻/结算
<!-- review-fix: ISSUE-2, ISSUE-3 -->

### 钱包模块
**职责：** 充值、余额与流水查询、冻结/解冻/结算的事务操作、余额不足拦截与非负约束
**对外接口：**
- POST /api/wallet/recharge Header Authorization Body {amount: Decimal 必填 >0 单位元两位小数} → 200 {balance, flow}；400 amount≤0
- GET /api/wallet Header Authorization → 200 {balance, frozen, available}
- GET /api/wallet/flows Query {page?, size?} Header Authorization → 200 PageResult<WalletFlow>
**依赖：** Wallet、WalletFlow；仅本人可操作

### 收藏模块
**职责：** 收藏与取消、我的收藏列表及失效标记
**对外接口：**
- POST /api/favorites/{itemId} Header Authorization → 201；409 已收藏；404 商品不存在
- DELETE /api/favorites/{itemId} Header Authorization → 204；404 未收藏
- GET /api/favorites Query {page?, size?} Header Authorization → 200 PageResult<Item> 含 Item.status 失效标记
**依赖：** Favorite、Item（联查状态）

### 留言与协商模块
**职责：** 商品留言的公开读写、订单协商的私有读写与权限校验（仅买卖双方）、时间正序与轮询分页
**对外接口：**
- GET /api/items/{id}/comments Query {page?, size?} → 200 PageResult<Comment> 正序
- POST /api/items/{id}/comments Header Authorization Body {content: 1-500, parentId?} → 201 Comment；400 内容空
- GET /api/orders/{id}/messages Query {since? DateTime, page?, size?} Header Authorization → 200 PageResult<OrderMessage> 正序；403 非买卖双方
- POST /api/orders/{id}/messages Header Authorization Body {content: 1-500} → 201 OrderMessage；403 非买卖双方
**依赖：** Comment、OrderMessage、Order 权限校验

### 评价模块
**职责：** 订单完成后双方各一次评价的创建与查询，不可修改
**对外接口：**
- POST /api/orders/{id}/review Header Authorization Body {rating: 1-5 必填, content: 0-500} → 201 Review；400 非 COMPLETED/已评价；403 非买卖双方
- GET /api/orders/{id}/reviews → 200 List<Review>
- GET /api/users/{id}/reviews Query {page?, size?} → 200 PageResult<Review>
**依赖：** Review、Order 状态校验（仅 COMPLETED 可评）

### 文件模块
**职责：** 图片的完整生命周期管理，保障发布图片的可追溯与清理
**对外接口：**
- POST /api/files/upload Header Authorization multipart {file: binary 单张 ≤5MB} → 201 {url: String}；400 文件空/超限
- GET /api/files/{id} → 200 {url}；404 不存在
- DELETE /api/files/{id} Header Authorization → 204；403 非上传者
**依赖：** 微信云存储 SDK（[`tech-stack.md` §四](tech-stack.md) 锁定的具体实现）；`FileMeta` 表（scaffold 阶段不建，演进时按需添加——MVP 阶段依赖图片 URL 反查 `Item.images`，删除权限在 Item 维度校验）
<!-- review-fix: ISSUE-6 -->

### 个人中心聚合
**职责：** 无独立后端表，profile 页面按需聚合调用用户、商品、订单、收藏、评价 5 类查询接口，展示我的发布/买到/卖出/收藏/评价
**依赖：** 上述各模块只读接口

### 通用约定
<!-- review-fix: ISSUE-2 -->
- 统一响应体：`{code: Int, msg: String, data: T}`，成功 code 0，失败见下表
- HTTP 状态码：200 成功，201 创建，204 无内容，400 参数/状态非法/余额不足，401 未登录，403 无权限，404 不存在，409 已被预订/已收藏/重复评价
- 错误码表：1001 未登录，1002 无权限，2001 商品不存在，2002 已被预订，2003 交易方式不匹配，3001 余额不足，3002 冻结不足，4001 订单状态非法，5001 已评价
- 分页结构：`PageResult<T> {list: T[], total: Long, page: Int, size: Int}`

## 模块交互

1. 发布链：小程序选择图片 → POST /api/files/upload 批量获 URL（1 张及以上）→ POST /api/items 带 URL 列表落库，校验必填与枚举，状态 ON_SALE
2. 浏览链：首页 GET /api/items?category=&condition=&tradeMethod=&minPrice=&maxPrice=&keyword=&sort=&page=&size=，后端按索引过滤与分页返回；详情 GET /api/items/{id} 联查卖家与留言
3. 下单链：POST /api/orders 事务内 SELECT ... FOR UPDATE 校验 Item.ON_SALE 且 tradeMethod 兼容，更新 Item→RESERVED，插入 Order PENDING，若 ESCROW 则 Wallet 冻结并写 FREEZE 流水，失败回滚；并发下第二请求因 Item 已非 ON_SALE 返回 409 已被预订
4. 履约链：卖家 confirm → Order→PENDING_HANDOVER；卖家 reject 或任一方 cancel → Item→ON_SALE 且 ESCROW 时 UNFREEZE；买家 complete → 若 ESCROW 则解冻并 SETTLEMENT_OUT/SETTTLEMENT_IN，Item→SOLD，Order→COMPLETED；重复 complete 幂等返回 200
5. 协商链：订单详情页每 30 秒 GET /api/orders/{id}/messages?since= 与下拉刷新，发送 POST /api/orders/{id}/messages 需校验为买卖双方
6. 评价链：COMPLETED 后 POST /api/orders/{id}/review，唯一索引 (orderId, authorId) 保障各一次，查询时联查写入用户收到的评价列表

## 文件组织

```
/
├── CONTEXT.md                          — 统一语言
├── spec.md                             — 需求规格
├── plan.md                             — 技术计划
├── docs/adr/
│   ├── 0001-虚拟钱包双轨交易.md          — 支付模型决策
│   ├── 0002-单件商品无购物车.md          — 商品模型决策
│   ├── 0003-轮询留言替代实时IM.md        — 实时性决策
│   └── 0004-Java后端与无管理后台.md      — 技术栈与治理决策
├── backend/
│   ├── build.gradle                    — Gradle 依赖与构建
│   ├── src/main/java/com/campus/market/
│   │   ├── MarketApplication.java       — 启动类
│   │   ├── config/
│   │   │   ├── SecurityConfig.java     — JWT 过滤与白名单
│   │   │   ├── WebConfig.java          — CORS 与拦截器注册
│   │   │   └── JwtInterceptor.java     — 鉴权拦截器
│   │   ├── common/
│   │   │   ├── Result.java             — 统一响应体 code/msg/data
│   │   │   ├── PageResult.java         — 分页结构
│   │   │   ├── BusinessException.java  — 业务异常
│   │   │   └── GlobalExceptionHandler.java — 异常转错误码
│   │   ├── auth/
│   │   │   ├── AuthController.java     — 登录与刷新
│   │   │   ├── AuthService.java        — code2Session 与 Mock
│   │   │   └── JwtUtil.java            — 签名与校验
│   │   ├── user/
│   │   │   ├── UserController.java     — 资料查询与更新
│   │   │   ├── UserService.java        — 业务逻辑
│   │   │   └── UserMapper.java         — MyBatis 映射
│   │   ├── item/
│   │   │   ├── ItemController.java     — 发布与查询
│   │   │   ├── ItemService.java        — 状态流转与筛选
│   │   │   └── ItemMapper.java         — 商品表访问
│   │   ├── order/
│   │   │   ├── OrderController.java    — 下单与履约
│   │   │   ├── OrderService.java       — 事务与锁
│   │   │   └── OrderMapper.java        — 订单表访问
│   │   ├── wallet/
│   │   │   ├── WalletController.java   — 充值与查询
│   │   │   ├── WalletService.java      — 冻结结算
│   │   │   ├── WalletMapper.java       — 钱包表访问
│   │   │   └── WalletFlowMapper.java   — 流水表访问
│   │   ├── favorite/
│   │   │   ├── FavoriteController.java — 收藏操作
│   │   │   ├── FavoriteService.java    — 收藏逻辑
│   │   │   └── FavoriteMapper.java     — 收藏表访问
│   │   ├── discussion/
│   │   │   ├── CommentController.java  — 公开留言
│   │   │   ├── CommentService.java     — 留言逻辑
│   │   │   ├── CommentMapper.java      — 留言表访问
│   │   │   ├── OrderMessageController.java — 私有协商
│   │   │   ├── OrderMessageService.java    — 协商逻辑
│   │   │   └── OrderMessageMapper.java     — 协商表访问
│   │   ├── review/
│   │   │   ├── ReviewController.java   — 评价提交
│   │   │   ├── ReviewService.java      — 评价校验
│   │   │   └── ReviewMapper.java       — 评价表访问
│   │   └── file/
│   │       ├── FileController.java     — 上传/查询/删除
│   │       ├── WechatCloudStorageService.java — 微信云存储 SDK 封装
│   │       └── FileMetaMapper.java     — 文件元数据（scaffold 阶段不建，演进时按需添加）
│   └── src/main/resources/
│       ├── application.yml             — 数据源与存储配置
│       ├── db/schema.sql               — 8 张表建表语句
│       ├── db/data.sql                 — 分类与示例数据
│       └── mapper/
│           ├── UserMapper.xml          — 用户复杂查询（简单 CRUD 走 MyBatis-Plus BaseMapper）
│           ├── ItemMapper.xml          — 商品筛选与 SELECT ... FOR UPDATE 行锁
│           ├── OrderMapper.xml         — 订单联查
│           ├── WalletMapper.xml        — 钱包事务更新（冻结 / 解冻 / 结算）
│           ├── WalletFlowMapper.xml    — 流水写入
│           ├── FavoriteMapper.xml      — 收藏联查商品状态
│           ├── CommentMapper.xml       — 留言查询
│           ├── OrderMessageMapper.xml  — 协商 since 增量查询
│           ├── ReviewMapper.xml        — 评价联查
│           └── FileMetaMapper.xml      — 文件元数据（scaffold 阶段不建，演进时按需添加）
└── miniprogram/
    ├── app.js                          — 全局配置与鉴权初始化
    ├── app.json                        — 页面路由
    ├── utils/
    │   ├── request.js                  — 封装 wx.request 与错误码处理
    │   ├── auth.js                     — 登录与 token 管理
    │   └── format.js                   — 价格与时间格式化
    ├── pages/
    │   ├── home/home.*                 — 列表、筛选、搜索、排序
    │   ├── detail/detail.*             — 商品详情、留言、收藏、下单
    │   ├── publish/publish.*           — 发布表单与图片上传
    │   ├── order/list.*                — 订单列表（买/卖）
    │   ├── order/detail.*              — 订单详情、协商、确认收货
    │   ├── favorite/favorite.*         — 我的收藏
    │   ├── wallet/wallet.*             — 余额、充值、流水
    │   ├── review/review.*             — 评价提交与展示
    │   └── profile/profile.*           — 聚合入口
    └── components/
        ├── item-card/item-card.*       — 商品卡片复用
        ├── price-filter/price-filter.* — 价格区间组件
        └── comment-list/comment-list.* — 留言列表组件
```
<!-- review-fix: ISSUE-7, ISSUE-6 -->

## 技术决策

| 决策点 | 选择 | 理由 | 被否决备选 |
|--------|------|------|------------|
| 后端框架 | **Spring Boot 4 + JDK 21 LTS + Gradle 8 (Groovy DSL) + MyBatis-Plus 3.5.9+ + MySQL 8.0 LTS** | 团队熟悉 Java，生态与校内评审一致；MyBatis-Plus 减少 CRUD 样板；JDK 21 虚拟线程作为未来性能 buffer；Gradle Groovy DSL 与 plan.md 原 Maven 风格有差异但社区资料多 | Node.js/云开发：需额外学习且校内资料少 |
| 鉴权 | **JJWT（io.jsonwebtoken:jjwt）+ 自写 JwtInterceptor + `WebMvcConfigurer` 白名单**，wx.login code2Session，开发期 Mock 登录（登录页"演示账号"按钮一键选小白 / 小红 / 小黑） | 不引入 Spring Security，砍掉 60% 依赖体积；真实微信登录需 AppSecret 且个人小程序受限，Mock 保障任何网络可演示 | 纯 Mock 无微信：答辩无法证明可对接真实微信；Spring Security：plan.md 8 模块接口数量有限，配置复杂，杀鸡用牛刀 |
| 图片存储 | **微信云存储 + 后端转发换 HTTPS URL 落库**，发布时传 URL 列表 | 个人云开发免费 5GB 配额，零运维；前端拿到永久 HTTPS 直链渲染稳定；接口契约不变，未来可零成本切 MinIO / 阿里 OSS / 腾讯 COS | 本地文件系统：部署迁移成本高且小程序外网访问受限；通用对象存储：需额外服务或账号，演示网络一变图就加载不出 |
| 搜索 | MySQL LIKE + 复合索引（category, condition, status, createdAt） | 1 万条内性能足够，无需引入 ES 运维成本 | Elasticsearch：期末运维与分词配置过重 |
| 并发控制 | 事务内 SELECT ... FOR UPDATE 锁定 Item 行 | 保障一物一单不超卖，实现简单且与 MySQL 兼容 | 乐观锁版本号：需重试逻辑且对二手低并发收益有限 |
| 实时性 | 30 秒轮询 + 下拉刷新 | 无需 WebSocket 服务，满足约时间地点需求，接口可平滑升级 | WebSocket：需额外服务与断线重连，期末不稳定 |
| 管理后台 | 暂不实现页面，预留 role 与状态字段，治理走 DB 直操 | 聚焦 MVP 闭环，字段预留保障未来零成本补齐 | 完整后台：占用 30% 工期且非交易闭环必要 |
| 状态建模 | Item 与 Order 状态枚举分离，Wallet 通过流水表追溯 | 职责清晰，钱包流水可审计，幂等通过订单状态幂等与流水唯一性保障 | 单一状态机：订单与商品耦合导致查询与回退复杂 |

