# 📦 发布指南 - Docker 镜像打包与发布

本指南说明如何将项目打包为 Docker 镜像文件并发布到 GitHub Release。

## 🚀 快速开始

### 方法一：使用自动化脚本（推荐）

```powershell
# 在项目根目录运行
.\build-and-export-images.ps1
```

脚本会自动完成：
1. 停止现有容器
2. 构建镜像（无缓存）
3. 打版本标签
4. 导出为 .tar 文件

### 方法二：手动执行

```powershell
# 1. 停止容器
docker-compose down

# 2. 构建镜像
docker-compose build --no-cache

# 3. 打标签（版本号可自定义）
docker tag ai-travel-backend:latest ai-travel-backend:1.0.0
docker tag ai-travel-frontend:latest ai-travel-frontend:1.0.0

# 4. 导出镜像
docker save -o ai-travel-backend-1.0.0.tar ai-travel-backend:1.0.0
docker save -o ai-travel-frontend-1.0.0.tar ai-travel-frontend:1.0.0
```

## 📋 发布到 GitHub Release

### 1. 创建 Release

1. 访问 GitHub 仓库的 **Releases** 页面
2. 点击 **"Draft a new release"**
3. 填写信息：
   - **Tag**: `v1.0.0`（与镜像版本对应）
   - **Title**: `AI Travel Planner v1.0.0`
   - **Description**: 版本更新说明

### 2. 上传镜像文件

在 Release 页面的 **"Attach binaries"** 区域上传：
- `ai-travel-backend-1.0.0.tar`
- `ai-travel-frontend-1.0.0.tar`

### 3. Release 说明模板

```markdown
## 🎉 AI Travel Planner v1.0.0

### 📦 镜像文件

- `ai-travel-backend-1.0.0.tar` - 后端服务镜像
- `ai-travel-frontend-1.0.0.tar` - 前端应用镜像

### 🚀 快速开始

#### 1. 导入镜像

```bash
docker load -i ai-travel-backend-1.0.0.tar
docker load -i ai-travel-frontend-1.0.0.tar
```

#### 2. 配置环境变量

在项目根目录创建 `.env` 文件（参考 `ENV_SETUP.md`）：

```env
# 数据库
POSTGRES_PASSWORD=123456

# 后端
JWT_SECRET=ThisIsASecretKeyForJwtMustBeAtLeast32Bytes!
JWT_EXP_MS=86400000
QWEN_API_KEY=sk-your-qwen-api-key-here

# 前端（构建时已注入，无需配置）
```

#### 3. 启动服务

```bash
# 使用 docker-compose（推荐）
docker-compose up -d

# 或手动启动
docker run -d --name ai-travel-postgres \
  -e POSTGRES_PASSWORD=123456 \
  -p 5432:5432 \
  postgres:16.10-alpine

docker run -d --name ai-travel-backend \
  --env-file .env \
  -p 8080:8080 \
  --link ai-travel-postgres:postgres \
  ai-travel-backend:1.0.0

docker run -d --name ai-travel-frontend \
  -p 3000:80 \
  --link ai-travel-backend:backend \
  ai-travel-frontend:1.0.0
```

#### 4. 访问应用

- 前端：http://localhost:3000
- 后端 API：http://localhost:8080/api

### 📝 注意事项

- 确保已安装 Docker 和 Docker Compose
- 需要配置通义千问 API Key 才能使用 AI 行程生成功能
- 需要配置高德地图 API Key 才能使用地图功能
- 详细配置说明请参考 `ENV_SETUP.md`

### 🔗 相关文档

- [环境变量配置](ENV_SETUP.md)
- [项目 README](README.md)
```

## 📊 镜像大小参考

- **后端镜像**: 约 200-300 MB（包含 Java 8 JRE + Spring Boot 应用）
- **前端镜像**: 约 50-100 MB（包含 Nginx + 静态文件）

## ✅ 验证清单

发布前请确认：

- [ ] 镜像已成功构建（`docker images` 能看到镜像）
- [ ] 镜像已正确打标签（版本号正确）
- [ ] .tar 文件已成功导出
- [ ] 测试导入镜像：`docker load -i ai-travel-backend-1.0.0.tar`
- [ ] 测试运行容器：`docker run --rm ai-travel-backend:1.0.0 --help`
- [ ] Release 说明已完善
- [ ] 所有必要的文档已更新

## 🔄 版本更新流程

1. 更新 `build-and-export-images.ps1` 中的 `$VERSION` 变量
2. 运行构建脚本
3. 创建新的 GitHub Release
4. 上传新的镜像文件
5. 更新 Release 说明

## ❓ 常见问题

### Q: 镜像文件太大怎么办？

A: 可以考虑：
- 使用多阶段构建优化镜像大小（已在 Dockerfile 中实现）
- 压缩 .tar 文件为 .tar.gz（但 GitHub Release 通常不需要）
- 使用 Docker Hub 发布镜像（用户直接 `docker pull`）

### Q: 用户如何更新到新版本？

A: 
```bash
# 停止旧容器
docker-compose down

# 导入新镜像
docker load -i ai-travel-backend-1.0.1.tar
docker load -i ai-travel-frontend-1.0.1.tar

# 更新 docker-compose.yml 中的镜像标签
# 然后重新启动
docker-compose up -d
```

### Q: 可以发布到 Docker Hub 吗？

A: 可以！发布到 Docker Hub 后，用户可以直接 `docker pull`，无需下载 .tar 文件：

```bash
# 登录 Docker Hub
docker login

# 打标签
docker tag ai-travel-backend:1.0.0 your-username/ai-travel-backend:1.0.0
docker tag ai-travel-frontend:1.0.0 your-username/ai-travel-frontend:1.0.0

# 推送
docker push your-username/ai-travel-backend:1.0.0
docker push your-username/ai-travel-frontend:1.0.0
```

