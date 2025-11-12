# AI Travel Planner Frontend

基于Vue 3 + TypeScript的前端应用，为用户提供直观的旅行规划界面。

## 技术栈

- Vue 3
- TypeScript
- Vue Router 4
- Pinia (状态管理)
- Element Plus (UI框架)
- Vite (构建工具)

## 快速开始

### 前提条件

- Node.js 16 或更高版本
- npm 或 yarn

### 运行项目

1. 克隆仓库
2. 进入frontend目录
3. 安装依赖：

```bash
npm install
```

4. 启动开发服务器：

```bash
npm run dev
```

### 构建项目

```bash
npm run build
```

构建完成后，可以在`dist`目录下找到生成的静态文件。

## 项目结构（建议）

```bash
frontend/
├─ src/
│  ├─ api/
│  │   └─ axios.ts            # axios 实例，统一附加 JWT
│  ├─ assets/
│  │   └─ vue.svg
│  ├─ components/
│  │   ├─ AuthForm.vue
│  │   ├─ HelloWorld.vue
│  │   ├─ TextTripForm.vue
│  │   ├─ VoiceTripForm.vue
│  │   ├─ layout/
│  │   │   └─ AppLayout.vue
│  │   └─ trip/
│  │       ├─ DailyPlans.vue
│  │       ├─ ExpenseManagement.vue
│  │       └─ TripInfo.vue
│  ├─ pages/
│  │   ├─ Dashboard.vue
│  │   ├─ Login.vue
│  │   ├─ MapPage.vue
│  │   ├─ Register.vue
│  │   ├─ TripCreatePage.vue
│  │   └─ TripDetail.vue
│  ├─ router/
│  │   └─ index.ts
│  ├─ services/
│  │   └─ MapService.ts       # 高德地图封装
│  ├─ stores/
│  │   ├─ auth.ts
│  │   ├─ expenses.ts
│  │   └─ trips.ts
│  ├─ App.vue
│  ├─ main.ts
│  ├─ style.css
│  └─ vite-env.d.ts
├─ package.json
├─ vite.config.ts
└─ README-frontend.md
```

## 路由配置

项目使用Vue Router进行路由管理，主要页面包括：

- 首页
- 旅行计划管理
- 目的地推荐
- 用户中心

## 状态管理

使用Pinia管理应用状态，主要store包括：

- 用户状态
- 旅行计划状态
- UI状态

## API集成

前端通过axios与后端API进行通信，所有API调用封装在services目录下。

## 小贴士和常见坑

- 前端展示与提交日期时，要使用统一时区（ISO 日期字符串）。
- 处理 LLM 调用延迟时，前端已实现「提交后立即返回、后台异步生成」的交互逻辑：  
  - 创建行程后会直接跳转到行程详情页，标题下的「每日行程安排」区域提供刷新按钮。  
  - 若仍在生成中，刷新提示“LLM 正在处理中”；生成完成后，刷新即可看到最新的每日计划。
- 行程详情页与地图页分离展示，如果需要查看地图，请通过「查看地图」按钮跳转独立页面。
