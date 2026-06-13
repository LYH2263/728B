# 构建阶段
FROM node:20-alpine AS builder
WORKDIR /app

# 配置 npm 镜像及网络重试（避免瞬时网络中断导致构建失败）
RUN npm config set registry https://registry.npmmirror.com \
    && npm config set fetch-retries 5 \
    && npm config set fetch-retry-mintimeout 20000 \
    && npm config set fetch-retry-maxtimeout 120000 \
    && npm config set fetch-timeout 600000

# 复制依赖文件
COPY package*.json ./

# 安装依赖（带重试，应对 ECONNRESET 等瞬时网络错误）
RUN npm install --no-audit --no-fund \
    || (sleep 5 && npm install --no-audit --no-fund) \
    || (sleep 15 && npm install --no-audit --no-fund)

# 复制源码
COPY . .

# 构建
RUN npm run build

# 生产阶段
FROM nginx:alpine

# 复制构建产物
COPY --from=builder /app/dist /usr/share/nginx/html

# 复制 nginx 配置
COPY nginx.conf /etc/nginx/conf.d/default.conf

# 暴露端口
EXPOSE 3000

# 启动 nginx
CMD ["nginx", "-g", "daemon off;"]
