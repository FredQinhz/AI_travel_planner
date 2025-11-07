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

## 项目结构

```
src/
├── assets/             # 静态资源
├── components/         # Vue组件
├── views/              # 页面视图
├── router/             # 路由配置
├── stores/             # Pinia状态管理
├── services/           # API服务
├── utils/              # 工具函数
├── App.vue             # 根组件
└── main.ts             # 应用入口
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