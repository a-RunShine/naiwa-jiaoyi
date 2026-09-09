# Scaffold 任务清单

> 状态：📋 待执行（2026-09-09）
> 范围：从空仓库到"能跑起来"的最简骨架
> 与 [`tech-stack.md`](tech-stack.md) + [`plan.md`](plan.md) 对齐

## 0. 仓库根目录现状

```
naiwa-jiaoyi/
├── .git/                          ← git 仓库已存在
├── .gitignore                     ← 只有 .DS_Store（需补充）
├── CONTEXT.md                     ← 统一语言（已有）
├── docs/                          ← 已有 spec / plan / adr / tech-stack / scaffold-tasks
└── icons/                         ← 设计稿（已有 4 张 PNG/JPG）
```

**没有 `backend/`、`miniprogram/`、`docker-compose.yml`**——scaffold 就是建这两个目录 + 容器编排文件。

---

## 1. 根目录任务（前置）

### T0.1 补全 `.gitignore`

需要在 `.gitignore` 末尾补：

```gitignore
# Java / Gradle
backend/build/
backend/.gradle/
backend/out/
*.class
*.jar
*.war
hs_err_pid*

# IntelliJ IDEA
.idea/
*.iml
*.iws
*.ipr

# VS Code
.vscode/

# 小程序
miniprogram/miniprogram_npm/
miniprogram/types/api.d.ts.bak

# 本地环境
.env
.env.local
*.local

# Docker
.docker/

# 日志
*.log
logs/
```

### T0.2 新增 `README.md`

最小可用的项目入口：项目简介 + 启动命令 + 演示账号。

### T0.3 新增 `docs/CHANGELOG.md`（可选）

记录 ADR-0004 与 plan.md 的修订历史，便于答辩时引用。

---

## 2. 后端 scaffold 任务（`backend/`）

### T2.1 目录结构

按 plan.md 文件组织段：

```
backend/
├── build.gradle                   ← Gradle 构建脚本（核心）
├── settings.gradle                ← 项目名 + 子模块配置
├── gradle.properties              ← JVM 参数
├── gradlew                        ← Gradle Wrapper 脚本（Unix）
├── gradlew.bat                    ← Gradle Wrapper 脚本（Windows）
├── gradle/wrapper/
│   ├── gradle-wrapper.jar
│   └── gradle-wrapper.properties  ← 钉 Gradle 8.10.x
├── Dockerfile                     ← 后端镜像构建（多阶段）
├── src/main/java/com/campus/market/
│   ├── MarketApplication.java     ← 启动类
│   ├── config/
│   │   ├── WebConfig.java         ← CORS / 拦截器注册（本项目不配 CORS）
│   │   ├── JwtInterceptor.java    ← 鉴权拦截器
│   │   ├── MybatisPlusConfig.java ← 分页插件 + 枚举处理器
│   │   └── OpenApiConfig.java     ← springdoc 配置
│   ├── common/
│   │   ├── Result.java            ← 统一响应体 {code, msg, data}
│   │   ├── PageResult.java        ← 分页结构 {list, total, page, size}
│   │   ├── BusinessException.java ← 业务异常（带错误码）
│   │   └── GlobalExceptionHandler.java ← @RestControllerAdvice
│   ├── auth/
│   │   ├── AuthController.java    ← POST /api/auth/login + mock-login
│   │   ├── AuthService.java       ← code2Session + JWT 签发
│   │   └── JwtUtil.java           ← JJWT 封装
│   ├── user/                      ← 见 §模块 scaffold
│   ├── item/
│   ├── order/
│   ├── wallet/
│   ├── favorite/
│   ├── discussion/
│   ├── review/
│   └── file/
│       ├── FileController.java    ← POST /api/files/upload
│       ├── WechatCloudStorageService.java ← 微信云存储封装（RestClient 直调 HTTP API，不引第三方 SDK）
│       └── WxApiClient.java       ← 微信开放接口封装（code2Session / getAccess_token）
├── src/main/resources/
│   ├── application.yml            ← 单文件配置（数据源、JWT 密钥、文件上传大小、微信云存储凭证）
│   ├── db/
│   │   ├── schema.sql             ← 8 张业务表建表 + 索引（FileMeta scaffold 阶段不建）
│   │   └── data.sql               ← 演示账号 + 示例商品
│   ├── mapper/                    ← 见 plan.md §文件组织
│   └── logback-spring.xml         ← 日志格式（可选，Spring Boot 默认也行）
└── src/test/java/com/campus/market/
    └── auth/JwtUtilTest.java      ← 第一个测试用例
```

### T2.2 `build.gradle` 关键依赖

```groovy
plugins {
    id 'java'
    id 'org.springframework.boot' version '4.0.8'  // Spring Boot 4.0.8（2026-08-20 发布，最稳 4.0.x）
    id 'io.spring.dependency-management' version '1.1.6'
}

group = 'com.campus'
version = '0.1.0'
java {
    toolchain { languageVersion = JavaLanguageVersion.of(21) }
}

dependencies {
    // Web（spring-boot-starter-web 已包含 RestClient / RestTemplate）
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-validation'  // Jakarta Bean Validation 3.1 + Hibernate Validator 9.0

    // ORM（注意：Spring Boot 4 必须用 spring-boot4-starter，spring-boot3-starter 与 SB4 不兼容）
    implementation 'org.springframework.boot:spring-boot-starter-jdbc'
    implementation 'com.baomidou:mybatis-plus-spring-boot4-starter:3.5.17'  // 从 3.5.13 起官方支持 SB4

    // 数据库
    runtimeOnly 'com.mysql:mysql-connector-j'

    // 鉴权（JJWT 0.12.6+ 在 JDK 21 上已测试通过，0.12.7 是当前补丁最新版）
    implementation 'io.jsonwebtoken:jjwt-api:0.12.7'
    runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.12.7'
    runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.12.7'

    // 接口文档（springdoc 官方兼容矩阵：SB 4.x → springdoc 3.x；3.1.1 是当前最新）
    implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:3.1.1'

    // 工具库（仅 core 子模块，避开与 Jackson/Logback/MyBatis-Plus 冲突）
    implementation 'cn.hutool:hutool-core:5.8.32'

    // 微信开放接口（不引 WxJava！WxJava 4.8.x 内部引用 HttpClient 4.x 的 TrustStrategy，
    // 与 Spring Boot 4（不再传递 HttpClient 4.x）有 NoClassDefFoundError 冲突。
    // 改用 Spring 自带的 RestClient 直调微信 HTTP API，3 个端点自己封：
    //   1. https://api.weixin.qq.com/sns/jscode2session  (code2Session，鉴权)
    //   2. https://api.weixin.qq.com/cgi-bin/token         (getAccessToken，凭证)
    //   3. https://api.weixin.qq.com/tcb/uploadfile       (cloud uploadFile，图片上传)
    // 见 WechatCloudStorageService.java 与 WxApiClient.java)

    // 测试
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'org.junit.jupiter:junit-jupiter'
    testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
}
```

### T2.3 `application.yml` 关键配置

```yaml
server:
  port: 8080

spring:
  application:
    name: naiwa-market
  datasource:
    url: jdbc:mysql://localhost:3306/naiwa_market?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: naiwa
    password: ${DB_PASSWORD:naiwa_dev}
    driver-class-name: com.mysql.cj.jdbc.Driver
  servlet:
    multipart:
      max-file-size: 5MB
      max-request-size: 50MB

mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
  global-config:
    db-config:
      logic-delete-field: ''  # 不启用软删除
      id-type: ASSIGN_ID

springdoc:
  swagger-ui:
    path: /swagger-ui.html
  api-docs:
    path: /v3/api-docs

app:
  jwt:
    secret: ${JWT_SECRET:dev-secret-key-must-be-at-least-32-bytes-long-xxxx}
    expire-hours: 720
  wechat:
    app-id: ${WX_APPID:your_appid_here}
    app-secret: ${WX_APP_SECRET:your_secret_here}
    cloud-env: ${WX_CLOUD_ENV:your-cloud-env-id}    # 微信云开发环境 ID
    api-base: https://api.weixin.qq.com
  mock:
    enabled: true   # dev=true，prod=false
```

### T2.4 验收点

- [ ] `./gradlew bootRun` 能启动，端口 8080
- [ ] 访问 `http://localhost:8080/swagger-ui.html` 看到空文档（只有 health endpoint）
- [ ] `docker compose up -d mysql` 后，schema.sql + data.sql 自动跑
- [ ] `./gradlew test` 跑过 JwtUtilTest
- [ ] `GET /api/items` 返回空列表（200，游客白名单生效）

---

## 3. 小程序 scaffold 任务（`miniprogram/`）

### T3.1 目录结构

```
miniprogram/
├── app.js                         ← 全局配置与登录初始化
├── app.json                       ← 页面路由
├── app.wxss                       ← 全局样式
├── tsconfig.json                  ← TypeScript strict 配置
├── project.config.json            ← 微信开发者工具项目配置（自动生成）
├── sitemap.json                   ← 索引规则
├── types/
│   ├── api.d.ts                   ← Result<T> / PageResult<T> / 接口响应类型
│   ├── enums.d.ts                 ← 分类 / 成色 / 交易方式 / 状态枚举
│   └── wx.d.ts                    ← wx API 类型补丁（如需）
├── utils/
│   ├── config.ts                  ← BASE_URL 集中导出
│   ├── request.ts                 ← wx.request 封装 + 错误码处理
│   ├── auth.ts                    ← 登录与 token 管理
│   └── format.ts                  ← 价格 / 时间格式化
├── pages/
│   ├── login/login.{ts,wxml,wxss,json}     ← 登录页（含演示账号按钮）
│   ├── home/home.{ts,wxml,wxss,json}       ← 首页 / 搜索 / 筛选
│   ├── detail/detail.{ts,wxml,wxss,json}   ← 商品详情 / 留言 / 收藏 / 下单
│   ├── publish/publish.{ts,wxml,wxss,json} ← 发布表单 / 图片上传
│   ├── order/
│   │   ├── list.{ts,wxml,wxss,json}        ← 订单列表（买 / 卖）
│   │   └── detail.{ts,wxml,wxss,json}      ← 订单详情 / 协商 / 确认收货
│   ├── favorite/favorite.{ts,wxml,wxss,json}
│   ├── wallet/wallet.{ts,wxml,wxss,json}   ← 余额 / 充值 / 流水
│   ├── review/review.{ts,wxml,wxss,json}
│   └── profile/profile.{ts,wxml,wxss,json} ← 聚合入口
├── components/
│   ├── item-card/item-card.{ts,wxml,wxss,json}
│   ├── price-filter/price-filter.{ts,wxml,wxss,json}
│   └── comment-list/comment-list.{ts,wxml,wxss,json}
└── images/                        ← tabBar 图标等（如需）
```

### T3.2 `tsconfig.json`

```json
{
  "compilerOptions": {
    "target": "ES2022",
    "module": "ESNext",
    "moduleResolution": "node",
    "strict": true,
    "noImplicitAny": true,
    "strictNullChecks": true,
    "esModuleInterop": true,
    "skipLibCheck": true,
    "forceConsistentCasingInFileNames": true,
    "resolveJsonModule": true,
    "lib": ["ES2022"],
    "types": []
  },
  "include": ["**/*.ts"],
  "exclude": ["node_modules"]
}
```

### T3.3 `utils/config.ts`

```typescript
export const BASE_URL = 'http://192.168.1.100:8080'; // 演示时改 IP
export const USE_MOCK = true; // dev=true，prod=false
```

### T3.4 `app.json` 路由

8 个 tabBar 页面 + login + 详情等子页面。

### T3.5 验收点

- [ ] 微信开发者工具能打开 miniprogram 目录，无 TS 编译错误
- [ ] 登录页"演示账号"按钮可见，点击能跳过 wx.login
- [ ] `utils/request.ts` 拦截 401 跳登录页

---

## 4. Docker / 部署任务

### T4.1 `docker-compose.yml`（项目根）

```yaml
version: '3.8'

services:
  mysql:
    image: mysql:8.0
    container_name: naiwa-mysql
    restart: unless-stopped
    environment:
      MYSQL_ROOT_PASSWORD: naiwa_root
      MYSQL_DATABASE: naiwa_market
      MYSQL_USER: naiwa
      MYSQL_PASSWORD: naiwa_dev
      TZ: Asia/Shanghai
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
      - ./backend/src/main/resources/db/schema.sql:/docker-entrypoint-initdb.d/01-schema.sql
      - ./backend/src/main/resources/db/data.sql:/docker-entrypoint-initdb.d/02-data.sql
    command:
      - --character-set-server=utf8mb4
      - --collation-server=utf8mb4_unicode_ci
      - --default-authentication-plugin=mysql_native_password

  backend:
    build:
      context: ./backend
      dockerfile: Dockerfile
    container_name: naiwa-backend
    restart: unless-stopped
    depends_on:
      - mysql
    environment:
      DB_PASSWORD: naiwa_dev
      JWT_SECRET: dev-secret-key-must-be-at-least-32-bytes-long-xxxx
      WX_APPID: ${WX_APPID:-your_appid_here}
      WX_APP_SECRET: ${WX_APP_SECRET:-your_secret_here}
      WX_CLOUD_ENV: ${WX_CLOUD_ENV:-your-cloud-env-id}
    ports:
      - "8080:8080"

volumes:
  mysql_data:
```

### T4.2 `backend/Dockerfile`

多阶段构建：builder（Gradle build）→ runtime（JRE 21）。

### T4.3 验收点

- [ ] `docker compose up -d` 一键起 MySQL + backend
- [ ] 后端健康检查通过
- [ ] 首次启动自动跑 schema.sql + data.sql
- [ ] 小程序能调通 `GET /api/items`

---

## 5. 执行顺序（建议）

按依赖关系，**自上而下**执行：

```
T0.1 .gitignore  ← 最快，先做
T0.2 README.md   ← 与 T0.1 并行
       ↓
T2.1 后端目录结构（空目录占位）
T2.2 build.gradle
T2.3 application.yml
T2.4 MarketApplication.java（启动类 + 一个 health controller）
       ↓
T4.1 docker-compose.yml
T4.2 backend/Dockerfile
       ↓
T0.2 README.md 补启动命令
       ↓
T3.1 小程序目录结构
T3.2 tsconfig.json
T3.3 utils/config.ts
T3.4 app.json
       ↓
T3.5 8 个 page 占位骨架（空 wxml 显示 "Hello"）
       ↓
✅ 全栈跑通：docker compose up + 微信开发者工具打开 → 看到商品列表（即使是空的）
```

---

## 6. 不在本次 scaffold 范围内（后续按模块开发）

- 8 个模块的 Service / Controller / Mapper 业务实现
- 8 张业务表的具体建表 SQL（scaffold 时只跑 schema.sql 的占位版本；FileMeta scaffold 阶段不建）
- 完整单测覆盖（scaffold 时只跑 1 个 JwtUtilTest）
- CI、Checkstyle、Docker 镜像推送
- **AC11 压力测试 SQL 脚本**：`db/load-test/seed-10k-items.sql`（生成 1 万条在售商品，覆盖 7 个分类与 4 种成色），由 [`tech-stack.md` §五](tech-stack.md) 提及但不在 scaffold 范围；演示 AC11 验收前由 DDL/数据负责人临时跑一次

这些会在按模块开发阶段（第 5 步）逐个落地。

