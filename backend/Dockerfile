# ============================================================
# 多阶段构建：builder (Gradle + JDK21) → runtime (JRE21)
# ============================================================

# ---- Stage 1: 构建 ----
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /workspace

# 先复制 wrapper 与构建脚本,利用 Docker 缓存
COPY gradlew settings.gradle build.gradle gradle.properties ./
COPY gradle ./gradle
RUN chmod +x ./gradlew && ./gradlew --version

# 复制源码并打包(跳过测试加速构建)
COPY src ./src
RUN ./gradlew bootJar -x test --no-daemon

# ---- Stage 2: 运行时 ----
FROM eclipse-temurin:21-jre
WORKDIR /app

# 时区与 curl(健康检查用)
RUN apt-get update \
 && apt-get install -y --no-install-recommends curl tzdata ca-certificates \
 && rm -rf /var/lib/apt/lists/* \
 && ln -snf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \
 && echo "Asia/Shanghai" > /etc/timezone

# 复制构建产物
COPY --from=builder /workspace/build/libs/naiwa-market.jar /app/naiwa-market.jar

EXPOSE 8080

HEALTHCHECK --interval=10s --timeout=5s --start-period=30s --retries=5 \
  CMD curl -fsS http://127.0.0.1:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-XX:+UseG1GC", "-jar", "/app/naiwa-market.jar"]