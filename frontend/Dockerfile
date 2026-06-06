# 构建阶段
FROM node:20-alpine AS builder

WORKDIR /app

# 配置阿里云 Alpine 镜像加速
RUN sed -i 's/dl-cdn.alpinelinux.org/mirrors.aliyun.com/g' /etc/apk/repositories

# 复制package文件
COPY package*.json ./

# 配置npm淘宝镜像并安装依赖
RUN npm config set registry https://registry.npmmirror.com && \
    npm ci

# 复制源码并构建
COPY . .
RUN npm run build

# 运行阶段
FROM nginx:alpine

# 复制构建产物
COPY --from=builder /app/dist /usr/share/nginx/html

# 复制nginx配置
COPY nginx.conf /etc/nginx/conf.d/default.conf

# 暴露端口
EXPOSE 80

# 启动nginx
CMD ["nginx", "-g", "daemon off;"]
