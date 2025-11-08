# Web 版 AI 旅行规划师 — 需求分析文档

## 一、项目简介

软件目标：通过自然语言（语音与文字）交互，让用户快速生成个性化旅行行程，并在旅行过程中提供实时辅助（导航、费用记录、提醒等）。系统将支持用户注册、保存多份行程、云端同步、预算管理与语音驱动的交互体验。

用户场景举例：

* 用户用语音说："我要去日本，5天，预算 1 万元，带孩子，喜欢美食和动漫"，系统返回一个包含交通、住宿、每日景点、午餐/晚餐推荐及预算分配的可编辑行程。
* AI 会进行预算分析，记录旅行开销。用户可以通过文本或语音记录消费，系统自动更新预算并提示超支风险。

---

## 二、范围（Scope）

### 包含（MVP 功能）

1. 语音 + 文字输入的智能行程规划（核心），包括但不限于自动生成个性化的旅行路线，包括交通、住宿、景点、餐厅等详细信息。
2. 预算估算与行程层级的预算分配（AI 生成）
3. 用户注册 / 登录 / 会话持久化（云端）
4. 保存 / 编辑 / 同步行程（多设备）
5. 地图视图（行程可视化）、景点位置展示、导航入口
6. 支持语音录入费用并更新预算
7. GitHub README 中说明的 Docker 镜像运行方式

### 不包含（MVP 之后考虑）

* 离线地图导航（复杂）
* 深度个性化推荐（需长期数据）

---

## 三、关键用例与用户故事

### 用户故事（User Stories）

1. 作为游客，我希望用语音描述我的旅行需求，从而快速得到一份可编辑行程。
2. 作为游客，我希望记录每笔支出（语音或文字），系统提示当前预算状态。
3. 作为常用用户，我希望登录后查看历史行程并在不同设备间同步。

### 用例（简要）

* 提交旅行请求（语音/文字） → AI 返回初稿行程 → 用户确认/编辑 → 存为计划
* 旅途中录入消费 → 预算模块更新 → 超支提醒
* 编辑计划并同步到云端 → 其他设备可加载最新版本

---

## 四、功能需求（详细）

### 1. 智能行程规划

* 输入方式：语音（必有） + 文字
* 输入解析模块：将语音/文字转换为结构化需求（目的地、日期、预算、人数、偏好、特殊需求）
* LLM 调度：根据结构化需求调用大语言模型（可选 通义千问）生成行程草案
* 输出内容：每天行程（时间段）、交通建议（到达/城市内移动）、住宿建议、餐厅推荐（带短评）、景点详细信息、总费用估算
* 可编辑：用户可在前端在线调整日程/景点/时间

### 2. 费用预算与管理

* 预算估算：基于目的地和天数由 LLM 估算大类费用（交通、住宿、餐饮、门票、其他）
* 实时记录：支持语音或文字录入消费，消费记录保存在行程的预算模块下
* 预算告警：超过预算阈值发送提示（前端弹窗/通知）

### 3. 用户与数据管理

* 注册/登录：支持邮件/密码注册与第三方（可选）登录
* 权限：每个用户只能访问自己的行程（默认私有）
* 行程存储：行程以 JSON 结构存储到云端数据库
* 同步策略：每次保存触发云端版本更新。

### 4. 地图与导航

* 地图显示：在行程页面展示行程点在地图上的分布
* 路线链接：点击交通段可以在高德/百度地图中打开导航（深度链接）
* POI 信息：为每个景点展示地址、开放时间、评分（如可获取）

### 5. 语音模块

* 识别：支持上传音频或实时麦克风输入识别
* 技术栈：推荐使用基于科大讯飞的语音识别 API 提供语音识别功能
* 引擎：抽象为 SDK/Provider 层，默认实现：浏览器 Web Speech API + 科大讯飞（后端异步识别）


---

## 五、非功能需求（NFR）

* 安全：敏感信息（API keys）不得写入公开仓库；后端使用环境变量或密钥管理服务
* UI/UX：地图为主的交互界面，清晰的行程展示，美观的图片。
* 隐私：遵守最小化数据策略，仅存必要用户数据；用户可删除账号/数据
* 可扩展性：模块化设计
* 可维护性：清晰的 README、注释、单元测试及 CI
* 性能：页面交互响应 < 200ms（静态内容），AI 调用视模型延迟而定，需显示 loading 状态
* 可用性：移动优先设计，语音优先交互体验

---

## 六、技术选型（推荐，用户已偏好）

* 前端：Vue 3 + Vite（TypeScript），UI：Naive UI / Element Plus（可选），地图 SDK：高德（Amap）或百度（抽象 Provider）
* 后端：Spring Boot (Java, Maven/Gradle)，RESTful API + JWT 验证
* 数据库：Supabase（Postgres）或 Firebase Firestore（推荐 Supabase）
* 语音识别：浏览器 Web Speech API（客户端） + 后端接入科大讯飞适配器（可选）
* LLM：抽象模型适配层（仅推荐通义千问）
* 容器化：Docker + docker-compose

---

## 七、系统架构（高层）

1. 前端（Vue SPA）

   * 页面：登录/注册、仪表盘、行程创建（语音输入）、行程查看/编辑、设置（API keys）、费用记录
   * 组件：VoiceInput、TripEditor、MapView、BudgetPanel、Auth

2. 后端（Spring Boot）

   * 模块：AuthController（JWT）、TripController、BudgetController、LLMService（代理）、SpeechAdapter（后端语音识别回调）、StorageService（Supabase SDK）

3. 数据库（Supabase/Postgres）

   * Tables: users, trips, trip_versions, expenses, settings

4. 外部服务

   * 语音识别提供商（科大讯飞或 Web Speech API）
   * 地图服务（高德/百度）
   * LLM Provider（通义千问或其他）

---

## 八、数据模型（示例）

```sql
-- users
id UUID PK
email TEXT
password_hash TEXT
created_at TIMESTAMP

-- trips
id UUID PK
user_id UUID FK
title TEXT
destination TEXT
start_date DATE
end_date DATE
data JSONB -- 完整行程结构
budget_total NUMERIC
created_at TIMESTAMP
updated_at TIMESTAMP

-- expenses
id UUID
trip_id UUID
user_id UUID
amount NUMERIC
currency TEXT
comment TEXT
created_at TIMESTAMP

-- trip_versions
id UUID
trip_id UUID
version INT
data JSONB
created_at TIMESTAMP
```

---

## 九、API 设计（示例）

* `POST /api/auth/register` {email, password}
* `POST /api/auth/login` {email, password} -> {token}
* `POST /api/trips` {title, destination, start_date, end_date, request (user raw text)} -> 触发 LLM 生成初稿
* `GET /api/trips/{id}` -> trip data
* `PUT /api/trips/{id}` -> update trip (save)
* `POST /api/trips/{id}/expenses` {amount, comment}
* `POST /api/llm/generate` {structured_request} -> returns plan draft (for dev/testing)
* `POST /api/speech/recognize` (server-side audio transcription callback)

---

## 十、开发计划（里程碑与任务拆分）

### 里程碑（示例）

* M1：需求确认、架构设计、仓库初始化、CI 初始配置
* M2：后端基础框架（Auth, Trips CRUD, Supabase 接入）、Dockerfile
* M3：前端框架（Auth 页面、Trip Editor skeleton、VoiceInput 基本实现）、地图集成
* M4：LLM 集成、生成行程逻辑、预算模块
* M5：费用记录、同步
* M6：测试、文档、GitHub Actions 打包并推送镜像、提交 PDF 报告

### 任务分配（给 Trae 的逐步指令 — 高优先级到低）

> 下文将以明确的、可执行的步骤指导 Trae（vibe coding tool）逐步完成项目。

1. 初始化仓库与基本结构

   * 创建 mono-repo 或 repo：`ai-travel-planner`
   * 创建 `frontend/`（Vue 3 + Vite）、`backend/`（Spring Boot）、`.github/workflows/`、`docker/`、`docs/`
   * 提交初始 `README.md` 与 `.gitignore`、`LICENSE`

2. 后端骨架（Spring Boot）

   * 创建 Spring Boot 项目（Maven/Gradle），依赖：Spring Web, Spring Security (JWT), spring-data-jpa, postgresql, supabase-java-sdk（或使用 HTTP 客户端）
   * 实现 AuthController（注册/登录，JWT）
   * 实现 TripController（CRUD endpoints）
   * 配置数据库连接通过环境变量
   * 编写 Dockerfile（多阶段构建）和 `docker-compose.yml`（包含 Postgres for local）

3. 前端骨架（Vue）

   * 使用 Vite 初始化 Vue 3 + TypeScript 项目
   * 实现路由（/login, /register, /dashboard, /trips/:id）
   * 基础组件：AuthForm, VoiceInput（使用 Web Speech API）、TripEditorSkeleton、MapView（集成高德）
   * 配置环境变量（.env.example）用于地图 Key、后端 API URL

4. LLM 与语音适配层

   * 在后端实现 `LLMService`：接受结构化请求，调用 OpenAI / 模拟返回（在没有 key 情况下提供 mock）
   * SpeechAdapter：支持两种模式

     * 客户端直接使用 Web Speech API（推荐用于 MVP）
     * 后端接收音频文件并调用科大讯飞（适配器样例）

5. 预算与费用模块

   * Trip 数据结构中加入 `budget` 字段
   * 实现 `BudgetService`：调用 LLM 估算、合并实际支出

6. Map

   * 在 Trip 页面显示地图与行程点


7. 文档与交付

   * 编写 README、运行指南、Docker 运行示例
   * 生成最终 PDF（包含 GitHub 仓库地址与说明）

---

## 十一、质量保障（测试与监控）

* 集成测试：关键 API 流程（Auth, Trip CRUD）
* 日志与监控：后端基础日志（SLF4J）

---

## 十二、部署与运维建议

* 使用 Docker Compose 部署
* 生产环境中使用云数据库（Supabase）并禁用公开访问
* 定期 rotate API keys 与凭据

---

## 十三、注意事项（与评分相关）

1. **绝对不要把任何生产或测试 API key 写进 Git 仓库的源代码或 README 的公开内容。** 请使用 `.env` 与仓库 secrets。
2. README 中要明确列出如何配置第三方 key（地图、语音、LLM）及如何在三个月内提供可用 key（助教评测要求）。
3. 保留完整的 Git 提交记录以展示开发轨迹。

---

## 十四、项目交付清单（提交时）

1. GitHub 仓库地址（公开或私有并给助教访问）
2. README（包含运行与部署步骤、API key 配置、Docker 镜像地址）
3. Docker 镜像（已推送至阿里云或提供镜像文件）
4. 一份 PDF 报告（含项目简介、GitHub 地址、实现说明、使用说明）

---


