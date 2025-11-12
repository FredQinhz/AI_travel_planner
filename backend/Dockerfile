# === Build stage ============================================================
FROM maven:3.9.6-eclipse-temurin-8 AS builder

# 设置工作目录
WORKDIR /app

# 复制pom.xml并下载依赖
COPY pom.xml .
RUN mvn -B dependency:go-offline

# 复制源代码并构建
COPY src/ ./src/
RUN mvn -B clean package -DskipTests

# === Runtime stage ==========================================================
FROM eclipse-temurin:8-jre

# 更新证书和安装必要工具
RUN apt-get update && \
    apt-get install -y --no-install-recommends ca-certificates curl && \
    update-ca-certificates && \
    rm -rf /var/lib/apt/lists/*

# 设置工作目录
WORKDIR /app

# 从构建阶段复制构建产物
COPY --from=builder /app/target/ai-travel-planner-backend-0.0.1-SNAPSHOT.jar app.jar

# 暴露应用端口
EXPOSE 8080

# 设置环境变量默认值
ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_OPTS="-Xmx512m -XX:+UseG1GC"

# 运行应用
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]