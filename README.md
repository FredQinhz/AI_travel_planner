# 🌍 AI Travel Planner

一个面向个人旅者与小组团队的 **AI 智能旅行规划平台**。项目整合大模型行程生成、语音/文本创建、预算管理、地图路线展示等能力，为用户提供端到端的旅行策划体验。当前版本已实现行程异步生成、可视化地图、费用管理以及完备的账号体系，适合继续扩展至生产级应用。

---

## ✨ 核心特性

- 🤖 **AI 行程生成**：支持文本与语音创建需求，调用大模型自动生成多日行程 (Trip + DayPlan + Locations)。
- 🗺️ **独立地图页面**：通过高德地图展示行程地点、路线规划，并提供路线规划与清除功能。
- 📅 **每日行程浏览**：行程详情页可分视图查看每日计划、费用列表，并提供刷新按钮实时获取 LLM 最新结果。
- 💰 **预算与支出管理**：支持多条支出记录的添加、编辑、删除与预算概览。
- 🔐 **账号体系**：基于 JWT 的登录注册流程，保障私人行程安全。
- 🚀 **异步生成体验**：后端保存行程后即返回，LLM 处理在后台异步完成，避免前端长时间等待。
- 🧭 **响应式布局**：配合 Element Plus + 自定义样式，兼顾桌面端与移动端浏览体验。

---

## 🧱 技术栈一览

| 层级 | 技术 | 说明 |
| --- | --- | --- |
| 前端 | Vue 3.5, TypeScript 5.9, Vite 7, Element Plus 2.11, Pinia 3, Vue Router 4, Axios 1 | SPA 应用、状态管理、UI 组件库、接口封装 |
| 后端 | Java 8, Spring Boot 2.7.18, Spring Security, Spring Data JPA, JWT | REST API、认证授权、数据访问 |
| AI/地图 | 通义千问 (DashScope)、高德地图 JS API | LLM 行程生成、地图与路线规划 |
| 数据库 | PostgreSQL (建议使用 Supabase) | 核心数据存储 |
| 构建部署 | Maven, npm, Docker (可选) | 构建工具与容器化支持 |

---

## ⚙️ 环境要求

| 组件 | 版本要求 | 备注 |
| --- | --- | --- |
| Node.js | ≥ 18.0.0 | Vite 7 / Element Plus 官方要求 |
| npm | ≥ 9.0.0 | 匹配 Node 18；也可使用 pnpm/yarn |
| Java JDK | 8u201+ | 与 `pom.xml` 中的 `java.version=1.8` 保持一致 |
| Maven | ≥ 3.6 | 构建 Spring Boot 2.7 项目 |
| PostgreSQL | ≥ 13 | 推荐使用 Supabase 托管 |
| API 密钥 | 通义千问 DashScope、高德地图 AMap | `.env` 或系统变量配置 |

后端启动会自动读取项目根目录的 `.env` 文件，示例参见 [backend/README-backend.md](backend/README-backend.md)。

---

## 📁 项目结构

```bash
ai-travel-planner/
├─ backend/              # Spring Boot 后端服务
├─ frontend/             # Vue 3 + TS 前端应用
├─ .github/workflows/    # CI/CD (GitHub Actions)
├─ API_Documentation.md  # REST API 说明
├─ requirements_specification.md # 需求规格说明
├─ ENV_SETUP.md          # 环境变量配置说明
├─ RELEASE_GUIDE.md      # 发布指南
└─ README.md
```

更多子模块结构与使用说明请查看各自目录下的 README：

- 📘 后端：[backend/README-backend.md](backend/README-backend.md)
- 📗 前端：[frontend/README-frontend.md](frontend/README-frontend.md)

---

## 🚀 部署方式

> 三种方式任选其一，所有环境变量说明请参考 [`ENV_SETUP.md`](ENV_SETUP.md)。

### ✅ 方式一：下载 Release 镜像（推荐给体验者）

1. 前往 GitHub **Releases** 页下载 `ai-travel-backend-*.tar` 与 `ai-travel-frontend-*.tar` 镜像文件，必要时解压。
2. 导入镜像：
   ```bash
   docker load -i ai-travel-backend-1.0.0.tar
   docker load -i ai-travel-frontend-1.0.0.tar
   ```
3. 在项目根目录配置 `.env`（见 [`ENV_SETUP.md`](ENV_SETUP.md)）。
4. 使用现成的 `docker-compose.yml` 启动：
   ```bash
   docker-compose up -d
   ```
   > Release 说明模板与更多细节可参见 [`RELEASE_GUIDE.md`](RELEASE_GUIDE.md)。

### 🧰 方式二：本地构建 Docker（适合定制部署）

1. 准备 `.env`（参考 [`ENV_SETUP.md`](ENV_SETUP.md)）。
2. 执行：
   ```bash
   docker-compose up -d --build
   ```
3. 访问地址：
   - 前端（Nginx）：<http://localhost:3000>
   - 后端 API：<http://localhost:8080/api>
   - PostgreSQL（宿主机）：`localhost:5432`
4. 日常运维：
   ```bash
   docker-compose logs -f backend
   docker-compose down
   ```

### 💻 方式三：本地源码运行（适合开发调试）

1. **准备环境**
   - PostgreSQL ≥ 13 并创建数据库
   - `.env`：后端放在项目根目录（被 Spring Boot 自动读取），前端放在 `frontend/.env`（仅开发模式需要）
   - 必填变量：`POSTGRES_*`、`JWT_SECRET`、`QWEN_API_KEY`、`VITE_AMAP_KEY`
2. **启动后端**
   ```bash
   cd backend
   mvn spring-boot:run
   ```
3. **启动前端**
   ```bash
   cd frontend
   npm install
   npm run dev
   ```
4. 打开浏览器访问 <http://localhost:5173>。

---

### 1. 克隆项目
```bash
git clone https://github.com/your-org/ai-travel-planner.git
cd ai-travel-planner
```

### 2. 启动后端
详见 [backend/README-backend.md](backend/README-backend.md)：
- 配置 `.env` 或系统环境变量
- `mvn spring-boot:run` 或 `mvn clean package` 后执行 `java -jar`

### 3. 启动前端
详见 [frontend/README-frontend.md](frontend/README-frontend.md)：
- `cd frontend`
- `npm install`
- `npm run dev`

---

## 📄 接口文档

- [API_Documentation.md](API_Documentation.md)：涵盖认证、行程、预算、地点等 REST 接口详情。

---

## 🪪 许可证

本项目基于 [MIT License](LICENSE) 开源。欢迎提交 Issue 与 Pull Request，一起完善智能旅行体验。🚀