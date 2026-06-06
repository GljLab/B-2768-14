# 构建阶段
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# 配置阿里云 APT 镜像加速
RUN sed -i 's/deb.debian.org/mirrors.aliyun.com/g' /etc/apt/sources.list.d/debian.sources || true

# 拷贝配置文件
COPY settings.xml .
COPY pom.xml .

# 预下载依赖(利用Docker层缓存)
RUN mvn -s settings.xml dependency:go-offline -B

# 拷贝源码并打包
COPY src ./src
RUN mvn -s settings.xml package -DskipTests

# 运行阶段
FROM eclipse-temurin:17-jre
WORKDIR /app

# 配置阿里云 APT 镜像加速
RUN sed -i 's/deb.debian.org/mirrors.aliyun.com/g' /etc/apt/sources.list.d/debian.sources || true

# 从构建阶段复制jar包
COPY --from=build /app/target/*.jar app.jar

# 暴露端口
EXPOSE 8268

# 启动应用
ENTRYPOINT ["java", "-Dfile.encoding=UTF-8", "-jar", "app.jar"]
