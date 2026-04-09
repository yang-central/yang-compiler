# 多阶段构建Dockerfile
FROM maven:3.8.6-jdk-8 AS builder

WORKDIR /app

# 复制pom.xml并下载依赖（利用Docker缓存层）
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 复制源代码并构建
COPY src ./src
RUN mvn clean package -DskipTests -B

# 运行时镜像
FROM openjdk:8-jre-slim

LABEL maintainer="YANG Compiler Team"
LABEL description="YANG Compiler - A plugin-extensible tool for compiling and processing YANG models"

WORKDIR /opt/yang-compiler

# 从builder阶段复制jar包
COPY --from=builder /app/target/yang-compiler-*.jar yang-compiler.jar

# 创建plugins目录
RUN mkdir -p plugins

# 创建非root用户
RUN groupadd -r yangc && useradd -r -g yangc yangc
RUN chown -R yangc:yangc /opt/yang-compiler
USER yangc

# 设置环境变量
ENV JAVA_OPTS="-Xms256m -Xmx512m"

# 入口点
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar yang-compiler.jar \"$@\"", "--"]
CMD ["option=build.json"]
