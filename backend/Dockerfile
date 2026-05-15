# ============================================================
# 惠福星链 · Backend Dockerfile
# 多阶段构建
# ============================================================

# ---- Stage 1: Build ----
FROM maven:3.9-eclipse-temurin-21-alpine AS builder
WORKDIR /build
COPY pom.xml .
RUN mvn dependency:go-offline -q
COPY src ./src
RUN mvn clean package -DskipTests -q

# ---- Stage 2: Runtime ----
FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S huifu && adduser -S huifu -G huifu
RUN apk add --no-cache curl tzdata && \
    cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone

WORKDIR /app
COPY --from=builder /build/target/*.jar app.jar

RUN mkdir -p /var/log/huifu-starchain && chown -R huifu:huifu /app /var/log/huifu-starchain

USER huifu
EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/api/v1/health || exit 1

ENTRYPOINT ["java", \
    "-Xms512m", "-Xmx2g", \
    "-XX:+UseZGC", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-Dspring.profiles.active=prod", \
    "-jar", "/app/app.jar"]
