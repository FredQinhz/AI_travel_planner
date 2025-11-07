# AI Travel Planner Backend

基于Spring Boot的后端服务，提供旅行规划的API接口，支持用户认证、行程管理、预算控制、AI集成等功能。

## 技术栈

- Java 17
- Spring Boot 3.2.0
- Spring Web (RESTful API)
- Spring Security (JWT认证)
- Spring Data JPA
- Supabase (PostgreSQL) - 推荐数据库
- 科大讯飞语音识别服务（适配器）
- LLM集成（通义千问）
- Docker 容器化

## 快速开始

### 前提条件

- JDK 17 或更高版本
- Maven 3.6 或更高版本
- Supabase 账户（或本地PostgreSQL）
- 可选：科大讯飞API账户、通义千问API密钥

### 环境配置

在运行前，需要配置以下环境变量：

```
# 数据库连接信息
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/ai_travel_planner
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=your_password

# JWT密钥
JWT_SECRET=your_jwt_secret_key

# 可选：API密钥
DASHSCOPE_API_KEY=your_dashscope_api_key
XFUN_API_KEY=your_xunfei_api_key
```

### 运行项目

1. 克隆仓库
2. 进入backend目录
3. 运行以下命令：

```bash
mvn spring-boot:run
```

### 构建项目

```bash
mvn clean package
```

构建完成后，可以在`target`目录下找到生成的jar文件。

## 项目结构

```
src/main/java/com/aitravelplanner/backend/
├── Application.java         # 应用入口类
├── config/                  # 配置类
│   ├── SecurityConfig.java  # 安全配置
│   └── JwtConfig.java       # JWT配置
├── controller/              # 控制器
│   ├── AuthController.java  # 认证相关接口
│   ├── TripController.java  # 行程管理接口
│   ├── BudgetController.java # 预算管理接口
│   └── SpeechController.java # 语音识别接口
├── service/                 # 服务层
│   ├── UserService.java     # 用户服务
│   ├── TripService.java     # 行程服务
│   ├── BudgetService.java   # 预算服务
│   ├── LLMService.java      # LLM集成服务
│   └── SpeechService.java   # 语音服务
├── repository/              # 数据访问层
│   ├── UserRepository.java  # 用户数据访问
│   └── TripRepository.java  # 行程数据访问
├── model/                   # 数据模型
│   ├── User.java            # 用户模型
│   ├── Trip.java            # 行程模型
│   └── Expense.java         # 费用模型
├── dto/                     # 数据传输对象
│   ├── AuthRequest.java     # 认证请求
│   ├── TripRequest.java     # 行程请求
│   └── SpeechRequest.java   # 语音请求
└── util/                    # 工具类
    └── JwtUtil.java         # JWT工具
```

## API文档

主要API端点：

- `POST /api/auth/register` - 用户注册
- `POST /api/auth/login` - 用户登录
- `POST /api/trips` - 创建行程
- `GET /api/trips` - 获取用户行程列表
- `GET /api/trips/{id}` - 获取行程详情
- `PUT /api/trips/{id}` - 更新行程
- `POST /api/trips/{id}/expenses` - 添加费用记录
- `POST /api/llm/generate` - 生成行程草稿

## 数据库配置

项目使用Supabase PostgreSQL作为数据库，支持以下数据表：
- users - 用户信息
- trips - 行程信息
- trip_versions - 行程版本历史
- expenses - 费用记录
- settings - 用户设置

## 安全配置

项目使用Spring Security配合JWT进行身份认证和授权管理，确保每个用户只能访问自己的行程数据。

## Docker支持

项目提供Dockerfile和docker-compose.yml配置，支持容器化部署。