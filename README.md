# 奶娃二手交易 · Naiwa Marketplace

> 校园/同城 C2C 二手闲置交易微信小程序,演示完整"发布-浏览-下单-钱包-协商-评价"闭环。

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.8-6DB33F)](https://spring.io/projects/spring-boot)
[![JDK](https://img.shields.io/badge/JDK-21%20LTS-ED8B00)](https://openjdk.org/projects/jdk/21/)
[![MyBatis-Plus](https://img.shields.io/badge/MyBatis--Plus-3.5.17-2C3E50)](https://baomidou.com/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0%20LTS-4479A1)](https://www.mysql.com/)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

---

## 目录

- [项目简介](#项目简介)
- [技术栈](#技术栈)
- [项目结构](#项目结构)
- [快速启动(Docker 一键部署)](#快速启动docker-一键部署)
- [演示账号](#演示账号)
- [核心 API](#核心-api)
- [本地开发](#本地开发)
- [测试](#测试)
- [文档导航](#文档导航)
- [演示数据 / 验收对照](#演示数据--验收对照)

---

## 项目简介

奶娃二手交易是基于微信小程序的二手闲置流转平台,支持:

- **两种履约方式**:当面交付 + 平台担保交易(虚拟钱包模拟)
- **完整商品生命周期**:发布 → 浏览/搜索/筛选 → 下单 → 卖家确认 → 买家收货 → 评价
- **公开留言 + 私有协商**:商品详情公开问答 + 订单内买卖双方 30s 轮询协商
- **虚拟钱包**:充值 / 冻结 / 解冻 / 结算四类流水,MySQL CHECK 约束保证非负
- **收藏 / 评价**:买家与卖家各一次评价(唯一索引兜底)

> 详细功能清单见 [`docs/spec.md`](docs/spec.md),接口契约见 [`docs/plan.md`](docs/plan.md)。

---

## 技术栈

| 层 | 选型 | 备注 |
|---|---|---|
| 后端框架 | Spring Boot 4.0.8 | JDK 21 LTS,虚拟线程 buffer |
| 语言 | Java 21 | 不引入 Kotlin |
| 构建 | Gradle 8.14 (Groovy DSL) | `build.gradle` |
| ORM | MyBatis-Plus 3.5.17 (spring-boot4-starter) | `-jsqlparser` 分页插件 |
| 数据库 | MySQL 8.0 LTS | utf8mb4 + CHECK 约束 |
| 鉴权 | JJWT 0.12.7 + 自写 JwtInterceptor | 不引 Spring Security |
| 接口文档 | springdoc-openapi 3.1.1 | `/swagger-ui.html` |
| 工具 | Hutool `hutool-core` 5.8.32 | 仅 core 子模块 |
| 小程序 | 微信原生 WXML/WXSS/JS + TypeScript | 不引 Taro/uni-app |
| 部署 | Docker + docker-compose | 本地与演示同构 |

> 完整决策记录见 [`docs/tech-stack.md`](docs/tech-stack.md),架构决策见 [`docs/adr/`](docs/adr/)。

---

## 项目结构

```
naiwa-jiaoyi/
├── docs/                         设计文档
│   ├── README.md                 文档索引
│   ├── spec.md                   F1-F10 需求规格 + AC1-AC14 验收标准
│   ├── plan.md                   9 模块 / 30+ API / 8 表 / 状态机
│   ├── tech-stack.md             技术栈共识(28 项后端决策)
│   ├── scaffold-tasks.md         scaffold 任务清单
│   └── adr/                      架构决策记录
│       ├── 0001-虚拟钱包双轨交易.md
│       ├── 0002-单件商品无购物车.md
│       ├── 0003-轮询留言替代实时IM.md
│       └── 0004-Java后端与无管理后台.md
│
├── backend/                      Spring Boot 4 后端(已 MVP)
│  ├── build.gradle / settings.gradle / gradle/wrapper/
│  ├── Dockerfile                 多阶段构建(JDK21 → JRE21)
│  ├── src/main/java/com/campus/market/
│  │  ├── MarketApplication.java
│  │  ├── common/                 Result / PageResult / BusinessException / ErrorCode
│  │  ├── config/                 JwtInterceptor / WebConfig / MybatisPlusConfig / MapperConfig ...
│  │  ├── auth/                   Mock 登录 + JJWT + WxApiClient(骨架)
│  │  ├── user/                   用户资料
│  │  ├── item/                   商品(发布/筛选/详情/下架)
│  │  ├── order/                  订单状态机(行锁 + 钱包联动)
│  │  ├── wallet/                 钱包(充值/冻结/结算)
│  │  ├── favorite/               收藏
│  │  ├── discussion/             留言 + 订单协商
│  │  ├── review/                 评价
│  │  ├── file/                   本地存储(替代微信云存储)
│  │  └── profile/                个人中心聚合
│  ├── src/main/resources/
│  │  ├── application.yml
│  │  ├── db/schema.sql           9 张表 DDL
│  │  ├── db/data.sql             演示账号 + 商品 + 钱包
│  │  └── mapper/ItemMapper.xml   动态筛选 SQL + FOR UPDATE
│  └── src/test/java/             Service + Controller 单元测试(15 个)
│
├── docker-compose.yml            MySQL + Backend 一键部署
└── README.md                     本文件
```

---

## 快速启动(Docker 一键部署)

> 前置依赖:Docker Desktop + JDK 21(可选,后端容器内自带 JRE)。

### 1. 启动 MySQL

```bash
git clone https://github.com/a-RunShine/naiwa-jiaoyi.git
cd naiwa-jiaoyi
docker compose up -d mysql
# 首次启动自动跑 schema.sql + data.sql(约 15 秒)
```

### 2. 启动后端

```bash
docker compose up -d --build backend
# 构建后端镜像 + 启动,端口 8080
```

### 3. 验证

```bash
curl http://localhost:8080/api/items
# 返 8 条 demo 商品
```

或浏览器打开:`http://localhost:8080/swagger-ui.html` 查看完整 API 文档。

### 4. 停止

```bash
docker compose down      # 保留数据卷
docker compose down -v   # 同时清空 MySQL 数据
```

---

## 演示账号

登录页"演示账号"按钮一键登录(Mock 模式,绕过 `wx.login`):

| 账号 | 密码 | openId | 用途 |
|---|---|---|---|
| 小白 | (无,直接拿 JWT) | `mock_open_xiaobai` | seller 演示(发布商品、收订单) |
| 小红 | (无) | `mock_open_xiaohong` | 卖家演示 |
| 小黑 | (无) | `mock_open_xiaohei` | 买家演示(下单、收货) |

每个账号初始钱包余额 **1000 元**。登录后所有钱包操作均可独立演示。

---

## 核心 API

所有接口 `/api/...` 前缀,统一响应体 `{code, msg, data}`。详见 [`docs/plan.md`](docs/plan.md) §模块设计。

### 认证 / 用户

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/api/auth/login` | Mock 登录:`{code":"","mockUserId":1}` → `{token, user}` |
| GET  | `/api/users/me` | 当前用户 |
| PUT  | `/api/users/me` | 更新昵称 / 头像 |

### 商品(部分白名单)

| 方法 | 路径 | 说明 | 鉴权 |
|---|---|---|---|
| GET  | `/api/items` | 商品列表(筛选 / 排序 / 分页) | 游客 |
| GET  | `/api/items/{id}` | 商品详情(含卖家信息) | 游客 |
| POST | `/api/items` | 发布商品 | 需登录 |
| PUT  | `/api/items/{id}/off` | 下架(仅卖家) | 需登录 |
| DELETE | `/api/items/{id}` | 删除(仅卖家,已成交不可删) | 需登录 |

### 订单

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/api/orders` | 下单(itemId + tradeMethod) |
| POST | `/api/orders/{id}/confirm` | 卖家确认 → PENDING_HANDOVER |
| POST | `/api/orders/{id}/reject` | 卖家拒绝 → REJECTED + ESCROW 解冻 |
| POST | `/api/orders/{id}/cancel` | 双方取消(待确认/待交付) |
| POST | `/api/orders/{id}/complete` | 买家确认收货(幂等) |
| GET  | `/api/orders?role=buy\|sell` | 订单列表 |

### 钱包

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/api/wallet/recharge` | 充值(任意金额) |
| GET  | `/api/wallet` | 余额 / 冻结 / 可用 |
| GET  | `/api/wallet/flows` | 流水(RECHARGE / FREEZE / UNFREEZE / SETTLEMENT_IN / SETTLEMENT_OUT) |

### 其他

- 收藏:`POST/DELETE/GET /api/favorites...`
- 商品留言:`GET/POST /api/items/{id}/comments`
- 订单协商:`GET/POST /api/orders/{id}/messages`(支持 `since` 增量轮询)
- 评价:`POST /api/orders/{id}/review`、`GET /api/users/{id}/reviews`
- 文件上传:`POST /api/files/upload`(multipart,5MB,本地存储到 `backend/uploads/`)
- 个人中心:`GET /api/profile/me`(聚合 6 类查询)

### 错误码

| 码 | 含义 |
|---|---|
| 0 | 成功 |
| 1001 | 未登录(缺 / 错 JWT) |
| 1002 | 无权限(非卖家 / 非买卖双方) |
| 2001 | 商品不存在 |
| 2002 | 已被预订(并发下单) |
| 2003 | 交易方式不匹配 |
| 3001 | 余额不足 |
| 3002 | 冻结不足 |
| 4001 | 订单状态非法 |
| 5001 | 已评价(唯一索引兜底) |

---

## 本地开发

### 后端(不用 Docker)

```bash
cd backend
./gradlew bootRun
# → http://localhost:8080
# → http://localhost:8080/swagger-ui.html
```

> 本地 `application.yml` 默认连 `localhost:3306`,需先 `docker compose up -d mysql`。
> 容器内跑后端时 `SPRING_DATASOURCE_URL` 环境变量会被覆盖为 `mysql:3306`(容器内 DNS)。

### IntelliJ IDEA

1. 打开 `backend/` 目录(Gradle 项目自动识别)
2. 等待依赖下载(首次 3-5 分钟)
3. 启动 `MarketApplication.main()`
4. 安装 Lombok / MyBatis-Plus 插件(可选,本项目不引 Lombok)

---

## 测试

```bash
cd backend
./gradlew test
# 15 个测试:JwtUtilTest / AuthControllerTest / ItemServiceTest / ItemControllerTest /
#          WalletServiceTest / WalletControllerTest
```

覆盖:
- Service 状态机(Item 上下架、Wallet 冻结/结算、Order 幂等)
- Controller HTTP 契约(白名单、鉴权错误码返回)

---

## 文档导航

| 文档 | 内容 |
|---|---|
| [`docs/README.md`](docs/README.md) | 文档索引(读这份开始) |
| [`docs/spec.md`](docs/spec.md) | F1-F10 功能需求 + AC1-AC14 验收标准 |
| [`docs/plan.md`](docs/plan.md) | 30+ REST API / 9 模块 / 8 张表 / 状态机设计 |
| [`docs/tech-stack.md`](docs/tech-stack.md) | 28 项后端技术决策 + 小程序 + 关键版本号 |
| [`docs/scaffold-tasks.md`](docs/scaffold-tasks.md) | 从空仓库到跑通的最小任务清单 |
| [`docs/adr/0001-虚拟钱包双轨交易.md`](docs/adr/0001-虚拟钱包双轨交易.md) | 支付模型决策 |
| [`docs/adr/0002-单件商品无购物车.md`](docs/adr/0002-单件商品无购物车.md) | 商品模型决策 |
| [`docs/adr/0003-轮询留言替代实时IM.md`](docs/adr/0003-轮询留言替代实时IM.md) | 实时性决策 |
| [`docs/adr/0004-Java后端与无管理后台.md`](docs/adr/0004-Java后端与无管理后台.md) | 技术栈与治理决策 |
| [`CONTEXT.md`](CONTEXT.md) | 统一语言(术语表,避免词) |

---

## 演示数据 / 验收对照

首次启动 MySQL 时自动初始化:

- **3 个用户**:小白(id=1) / 小红(id=2) / 小黑(id=3),各 1000 元余额
- **8 条示例商品**:覆盖 7 个分类(数码/服饰/美妆/书籍/生活用品/运动/其他)+ 4 种成色
- **3 条收藏** + **2 条留言**

| 验收点 | 验证方式 |
|---|---|
| AC1 游客浏览 | `curl http://localhost:8080/api/items` 无 token 成功 |
| AC2 发布 + 下架 | Mock 登录 → POST `/api/items` → PUT `/api/items/{id}/off` |
| AC3 筛选 / 排序 | `?category=DIGITAL&sort=price_asc` |
| AC4 收藏失效标记 | 收藏后下单,收藏项 `invalid=true` |
| AC6 卖家拒绝 → 商品回滚 | POST `/api/orders/{id}/reject` → 商品状态 ON_SALE |
| AC7 ESCROW 冻结 | POST `/api/orders {tradeMethod:ESCROW}` → wallet frozen +amount |
| AC7 解冻取消 | POST `/api/orders/{id}/cancel` → wallet frozen -amount |
| AC7 确认收货 → 结算 | POST `/api/orders/{id}/complete` → 卖家 wallet +amount |
| AC9 双方各评一次 | POST `/api/orders/{id}/review` × 2,唯一索引兜底 |
| AC14 complete 幂等 | 二次 POST `/api/orders/{id}/complete` 直接返 COMPLETED 不重复结算 |

---


## 路线图

- [x] 后端 MVP 9 模块(`feat: 后端 MVP 全量实现 + Docker 部署`)
- [ ] 微信小程序端 scaffold(8 个页面 + 3 utils + 3 components)
- [ ] AC11 压测 SQL 脚本(`db/load-test/seed-10k-items.sql`,1 万条商品)
- [ ] 真实微信云存储替换本地存储(接口契约不变,改 `FileStorageService` 实现即可)
- [ ] CI:GitHub Actions 自动跑 `./gradlew test` + 镜像构建推送

---

## License

MIT License — 详情见 [LICENSE](LICENSE)。

---

