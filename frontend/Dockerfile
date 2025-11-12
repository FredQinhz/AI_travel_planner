########## Build stage #######################################################
FROM node:20-alpine AS builder

WORKDIR /app

COPY package*.json ./
RUN npm ci --legacy-peer-deps

COPY . .

# 允许通过构建参数覆盖 Vite 环境变量
ARG VITE_API_BASE=http://localhost:8080/api
ARG VITE_AMAP_KEY=
ARG VITE_AMAP_SECURITY_JS_CODE=
ENV VITE_API_BASE=$VITE_API_BASE
ENV VITE_AMAP_KEY=$VITE_AMAP_KEY
ENV VITE_AMAP_SECURITY_JS_CODE=$VITE_AMAP_SECURITY_JS_CODE

RUN npm run build

########## Runtime stage #####################################################
FROM nginx:1.27-alpine

COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=builder /app/dist /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]

