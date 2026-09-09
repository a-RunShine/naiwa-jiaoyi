# 奶娃二手交易 — 技术栈共识

> 状态：✅ 已达成共识（2026-09-09，通过 /grilling 一问一答敲定）
> 范围：项目级技术栈决策；与 `spec.md`、`plan.md`、`docs/adr/*` 配套使用
> 修订原则：本文档变更需要重新走一次决策；与 plan.md 冲突时以本文档为准并同步更新 plan.md

## 一、后端

| # | 决策点 | 选择 | 备注 |
|---|--------|------|------|
| 1 | 后端框架 | **Spring Boot 4** | 与 ADR-0004 保持一致 |
| 2 | JDK | **JDK 21 LTS** | 虚拟线程作为未来性能 buffer |
| 3 | 语言 | **纯 Java 21** | 不引入 Kotlin 混编 |
| 4 | 构建工具 | **Gradle 8.x + Groovy DSL** | `build.gradle`（非 Kotlin DSL） |
| 5 | ORM | **MyBatis-Plus 3.5.9+** | 接受 Spring Boot 4 无官方背书的风险；plan.md 原 MyBatis 升级 |
| 6 | 数据库 | **MySQL 8.0.x LTS** | 支持 CHECK 约束（钱包非负） |
| 7 | 连接池 | **HikariCP**（Spring Boot 4 默认） | 零额外配置 |
| 8 | 部署形态 | **Docker + docker-compose** | 本地开发与演示同构 |
| 9 | 鉴权 | **JJWT + 自写 JwtInterceptor** | 不引入 Spring Security |
| 10 | 事务 | **`@Transactional` + MySQL 行锁 `SELECT ... FOR UPDATE`** | 与 plan.md 一致 |
| 11 | 校验 | **Jakarta Bean Validation 3.0 + Hibernate Validator** | 注解驱动，统一异常处理 |
| 12 | 异常 | **`@RestControllerAdvice` + `BusinessException(code, msg)`** | 落地 plan.md 错误码表（1001/2001/3001...） |
| 13 | 日志 | **Logback**（Spring Boot 4 默认） | 不上异步日志 |
| 14 | JSON | **Jackson**（Spring Boot 4 默认） | 与 hutool-json 互斥 |
| 15 | 时间 | **`java.time`**（JDK 21 内置） | |
| 16 | 工具库 | **Hutool `hutool-core` 5.8.x** | 仅 core 子模块，避开与 Jackson/Logback/MyBatis-Plus 冲突 |
| 17 | 接口文档 | **springdoc-openapi-starter-webmvc-ui** | Swagger UI `/swagger-ui.html` |
| 18 | 分页 | **MyBatis-Plus `PaginationInnerInterceptor`** | 不引 PageHelper |
| 19 | 枚举 | **Java enum + MyBatis-Plus `IEnum` / `@EnumValue` + DB VARCHAR** | 中文枚举值可读 |
| 20 | 乐观锁 / 软删除 | **不引入** | 行锁 + 物理删除足够 |
| 21 | 测试 | **JUnit 5 + Mockito + `@WebMvcTest` 切片** | Service 状态机与 Controller HTTP 契约 |
| 22 | 代码风格 | **不引入 Checkstyle / Spotless** | 团队 IDE 格式统一 |
| 23 | CI | **不做 CI** | 本地演示 |
| 24 | Redis | **不引入** | MySQL 8 + 复合索引够用 |
| 25 | 数据库迁移 | **手写 SQL + docker-entrypoint-initdb.d** | `schema.sql` / `data.sql` 首次启动自动跑 |
| 26 | API 路径 | **`/api/...` 不带版本** | 演示级 |
| 27 | 多环境 | **单 `application.yml` + 注释** | dev / 演示同文件 |
| 28 | CORS | **不配** | 小程序无 CORS 问题 |

## 二、后端数据模型关键约束

- 8 张主表：`user / item / order / wallet / wallet_flow / favorite / comment / order_message / review`
- 商品图片：单字段 `images JSON`（MySQL 8 原生 JSON）
- 钱包流水：5 种类型 `RECHARGE / FREEZE / UNFREEZE / SETTLEMENT_IN / SETTLEMENT_OUT`
- 评价：唯一索引 `(orderId, authorId)`
- 订单协商：`since` 增量拉取 + `page/size` 兜底
- 图片限制：单张 ≤5MB，前端引导 1-9 张，后端不设张数上限
- 一物一单：`Order.itemId` 唯一索引 + `Item.status` 行锁

## 三、小程序端

| # | 决策点 | 选择 | 备注 |
|---|--------|------|------|
| 1 | 形态 | **微信原生 WXML/WXSS/JS** | 不上 uni-app / Taro |
| 2 | 语言 | **TypeScript** | strict 模式 |
| 3 | TS 严格度 | **`strict: true`** | 完整类型检查 |
| 4 | npm 包 | **不引入** | 保持纯净，原生 API + 自写 utils |
| 5 | 全局类型声明 | **`miniprogram/types/`** | `api.d.ts` / `enums.d.ts` |
| 6 | 编译方式 | **微信开发者工具"增强编译"自动编译 TS** | 零配置 |
| 7 | UI 库 | **不引入** | 自写 `item-card` / `price-filter` / `comment-list` |
| 8 | 状态管理 | **`getApp().globalData` + `wx.setStorageSync`** | 不引 mobx-miniprogram |
| 9 | baseURL | **`utils/config.ts` 集中导出** | 演示时改 IP 即可 |
| 10 | Mock 登录 | **登录页"演示账号"按钮** | 一键选小白 / 小红 / 小黑 |
| 11 | 工具版本 | 微信开发者工具最新版 + TS 5.x + 基础库 ≥ 3.4.0 | |

## 四、图片存储

- **方案**：**微信云存储**（个人云开发免费配额 5GB，演示场景零成本）
- **落地方式**：保留 plan.md 的 `POST /api/files/upload` 接口，后端转发到微信云存储，换取 HTTPS URL 落库
- **SDK 选型**：**不引入 WxJava（com.github.binarywang:weixin-java-miniapp）**——WxJava 4.8.x 内部引用 HttpClient 4.x 的 `org.apache.http.ssl.TrustStrategy`，与 Spring Boot 4（不再传递 HttpClient 4.x）有 NoClassDefFoundError 冲突。改用 **Spring 自带的 RestClient** 直调微信 HTTP API，只需 3 个端点：
  - `POST /sns/jscode2session`（code2Session，鉴权）
  - `GET  /cgi-bin/token`（getAccessToken，凭证）
  - `POST /tcb/uploadfile`（cloud uploadFile，图片上传）
- **理由**：微信托管零运维；前端拿到的是永久 HTTPS 直链，渲染稳定；plan.md 既定契约不变；零额外依赖
- **未来迁移**：接口抽象留好，后期可零成本切换到 MinIO / 阿里 OSS / 腾讯 COS

## 四点五、关键版本号（2026-09 验证后锁定）

| 依赖 | 锁定版本 | 来源 |
|------|---------|------|
| Spring Boot | **4.0.8**（不是 4.0.0，也不是 4.1.1） | [versionlog.com](https://versionlog.com/spring-boot/4.0/) 4.0.x 最新 patch |
| MyBatis-Plus SB4 starter | **3.5.17**（用 `-spring-boot4-starter`，**不是** `-spring-boot3-starter`） | baomidou 官方文档（3.5.13 起官方支持 SB4） |
| springdoc-openapi | **3.1.1**（SB4 → springdoc 3.x） | springdoc.org 兼容性矩阵 |
| JJWT | **0.12.7**（0.12.x 在 JDK 21 已测试通过） | mvnrepository.com |
| Jakarta Validation | **3.1** + Hibernate Validator **9.0** | SB4 升级 |
| HikariCP | **7.0**（SB4 默认） | SB4 Release Notes |

## 五、演示数据

- `db/data.sql` 内置：
  - 3 个分类枚举（数码 / 服饰 / 美妆 / 书籍 / 生活用品 / 运动 / 其他）
  - 2-3 个测试用户（小白 / 小红 / 小黑）
  - 5-10 条示例商品
  - 一定余额的钱包
  - 几条历史订单
- 演示账号密码写在小程序登录页"演示账号"按钮上方，Mock 模式自动登录
- AC11 压力测试（1 万条商品）单独 SQL 脚本，演示时不跑

## 六、与现有文档的差异 & 待更新

### 6.1 待更新文件（2026-09-09 已全部完成）

- [x] **`docs/adr/0004-Java后端与无管理后台.md`**
  - ✅ 已重写：补 JDK 21 / Gradle 8 / MyBatis-Plus / 微信云存储细化
- [x] **`docs/plan.md`**
  - ✅ 架构概览段：已改为 TypeScript + Spring Boot 4 + JDK 21 + Gradle 8
  - ✅ 技术决策表的"图片存储"行：已改为"微信云存储 + 后端转发换 HTTPS URL"
  - ✅ 文件组织的 `backend/pom.xml` → `backend/build.gradle`
  - ✅ 文件模块依赖：从"对象存储 SDK"改为"微信云存储 SDK（RestClient 直调 HTTP API）"
  - ✅ FileMeta 表明确"scaffold 阶段不建，演进时按需添加"
- [x] **`docs/scaffold-tasks.md`**
  - ✅ build.gradle 版本号全更新：SB 4.0.8 / MP-spring-boot4-starter 3.5.17 / springdoc 3.1.1 / JJWT 0.12.7
  - ✅ file/ 模块加 WxApiClient.java（code2Session + getAccessToken）
  - ✅ application.yml 加 wechat.api-base

### 6.2 推翻的 plan.md 决策（追溯）

| plan.md 原决策 | 新决策 | 原因 |
|---------------|--------|------|
| Spring Boot 3 + Maven | Spring Boot 4 + Gradle 8 (Groovy DSL) | 用户决策 |
| MyBatis | MyBatis-Plus | 减少 CRUD 样板 |
| 纯 Java enum（隐含） | Java enum + MP `IEnum` + DB VARCHAR | 显式化 |
| 对象存储（隐含通用 OSS） | 微信云存储 | 0 成本 + 0 运维 |
| pom.xml | build.gradle | 用户决策 |
| 不写明 JDK 版本 | JDK 21 LTS | 用户决策 |
| （未规划） | 后端用 RestClient 直调微信云存储 HTTP API，**不引 WxJava** | WxJava 4.8.x 与 SB4 有 HttpClient 4.x 冲突 |

## 七、下一步建议（按顺序）

1. **更新 ADR-0004 + plan.md**（把"对象存储"改为"微信云存储"；补 JDK / Gradle / MyBatis-Plus 标注）
2. **生成 scaffold 任务清单**（目录结构、Gradle 依赖、文件骨架）
3. **后端 scaffold**：`backend/` 目录、`build.gradle`、`MarketApplication.java`、`application.yml`、`docker-compose.yml`
4. **小程序 scaffold**：`miniprogram/` 目录、TypeScript 配置、8 个页面骨架、3 个 utils、3 个 components
5. **按模块开发**：先做商品模块端到端跑通，再做订单 / 钱包 / 协商

