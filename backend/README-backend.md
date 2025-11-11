# AI Travel Planner Backend

基于Spring Boot的后端服务，提供旅行规划的API接口，支持用户认证、行程管理、预算控制、AI集成等功能。

## 技术栈

- Java 17
- Spring Boot 3.2.0
- Spring Web (RESTful API)
- Spring Security (JWT认证)
- Spring Data JPA
- Supabase (PostgreSQL) - 推荐数据库
- LLM集成（通义千问）
- Docker 容器化
- OpenAPI/Swagger (API文档)
- JWT (用户认证与授权)

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
SPRING_DATASOURCE_PASSWORD=123

# JWT密钥 (至少32字节)
JWT_SECRET=ThisIsASecretKeyForJwtMustBeAtLeast32Bytes!

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
├── Application.java              # 应用入口类
├── config/                       # 配置类
│   ├── JpaConfig.java            # JPA配置
│   └── OpenApiConfig.java        # OpenAPI配置
├── controller/                   # 控制器
│   ├── AuthController.java       # 认证相关接口
│   ├── TripController.java       # 行程管理接口
│   ├── BudgetController.java     # 预算管理接口
│   ├── ExpenseController.java    # 费用管理接口
│   └── LocationController.java   # 地点管理接口
├── service/                      # 服务层
│   ├── UserService.java          # 用户服务接口
│   ├── TripService.java          # 行程服务接口
│   ├── BudgetService.java        # 预算服务接口
│   ├── ExpenseService.java       # 费用服务接口
│   ├── LLMService.java           # LLM集成服务接口
│   ├── LocationService.java      # 地点服务接口
│   └── impl/                     # 服务实现类
│       ├── BudgetServiceImpl.java
│       ├── ExpenseServiceImpl.java
│       ├── LLMServiceImpl.java
│       ├── LocationServiceImpl.java
│       ├── MockLLMService.java
│       └── TripServiceImpl.java
├── repository/                   # 数据访问层
│   ├── UserRepository.java       # 用户数据访问
│   ├── TripRepository.java       # 行程数据访问
│   ├── ExpenseRepository.java    # 费用数据访问
│   ├── LocationRepository.java   # 地点数据访问
│   └── TripVersionRepository.java # 行程版本数据访问
├── model/                        # 数据模型
│   ├── User.java                 # 用户模型
│   ├── Trip.java                 # 行程模型
│   ├── Expense.java              # 费用模型
│   ├── Location.java             # 地点模型
│   └── TripVersion.java          # 行程版本模型
├── dto/                          # 数据传输对象
│   ├── AuthResponse.java         # 认证响应
│   ├── LoginRequest.java         # 登录请求
│   ├── RegisterRequest.java      # 注册请求
│   ├── TripRequest.java          # 行程请求
│   ├── TripResponse.java         # 行程响应
│   ├── BudgetResponse.java       # 预算响应
│   ├── ExpenseResponse.java      # 费用响应
│   ├── DayPlanDTO.java           # 每日计划DTO
│   └── LocationDTO.java          # 地点DTO
└── security/                     # 安全相关
    ├── JwtAuthenticationFilter.java # JWT认证过滤器
    ├── JwtUtil.java              # JWT工具类
    ├── SecurityConfig.java       # 安全配置
    └── UserDetailsServiceImpl.java # 用户详情服务实现
```

## API文档

主要API端点：

### 认证相关
- `POST /api/auth/register` - 用户注册
- `POST /api/auth/login` - 用户登录，返回JWT令牌

### 行程管理
- `POST /api/trips` - 创建新行程
- `GET /api/trips` - 获取用户的所有行程
- `GET /api/trips/{id}` - 根据ID获取行程详情
- `PUT /api/trips/{id}` - 更新行程
- `DELETE /api/trips/{id}` - 删除行程

### 预算和费用管理
- `GET /api/budget/{tripId}` - 获取行程预算
- `POST /api/expenses/{tripId}` - 添加单个消费记录
- `POST /api/expenses/batch/{tripId}` - 批量上传多个消费记录
- `PUT /api/expenses/{tripId}/expense/{expenseId}` - 更新单个消费记录
- `DELETE /api/expenses/{tripId}/expense/{expenseId}` - 删除单个消费记录
- `GET /api/expenses/{tripId}` - 查询行程的所有消费记录

### 地点管理
- `GET /api/locations/{tripId}` - 获取行程相关的地点信息
- `GET /api/locations/{tripId}/day/{day}` - 获取行程某一天的地点信息


## 数据库配置

项目使用Supabase PostgreSQL作为数据库，支持以下数据表：
- users - 用户信息
- trips - 行程信息
- trip_versions - 行程版本历史
- expenses - 费用记录
- settings - 用户设置

## 安全配置

项目使用Spring Security配合JWT进行身份认证和授权管理，实现了以下安全特性：

- 基于JWT的无状态认证机制
- 用户密码加密存储
- 细粒度的访问控制，确保每个用户只能访问自己的行程数据
- 请求级别的认证过滤
- 基于Spring Security的安全配置

安全组件位于`security`包下，包括JWT工具类、认证过滤器、安全配置和用户详情服务实现。

## Docker支持

项目提供Dockerfile和docker-compose.yml配置，支持容器化部署。