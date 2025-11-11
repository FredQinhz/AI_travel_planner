# AI Travel Planner API 文档

## 目录

- [1. 项目概述](#1-项目概述)
- [2. 认证与授权](#2-认证与授权)
- [3. API端点](#3-api端点)
  - [3.1 认证相关](#31-认证相关)
  - [3.2 行程管理](#32-行程管理)
  - [3.3 预算和费用管理](#33-预算和费用管理)
  - [3.4 地点管理](#34-地点管理)
- [4. 数据传输对象 (DTOs)](#4-数据传输对象-dtos)
  - [4.1 认证相关DTO](#41-认证相关dto)
  - [4.2 行程相关DTO](#42-行程相关dto)
  - [4.3 费用和预算相关DTO](#43-费用和预算相关dto)
  - [4.4 地点相关DTO](#44-地点相关dto)
- [5. 错误处理](#5-错误处理)
- [6. 安全考虑](#6-安全考虑)
- [7. 数据模型关系](#7-数据模型关系)

## 1. 项目概述

AI Travel Planner 是一个基于AI的旅行规划应用，帮助用户智能规划旅行行程。后端提供RESTful API接口，支持用户注册登录、行程管理、预算和费用追踪、地点信息查询等功能。系统通过LLM生成个性化行程建议，包括每日景点安排、交通建议等。

**技术栈**：
- Spring Boot 2.7.x
- Spring Security + JWT 认证
- Spring Data JPA
- PostgreSQL 数据库
- OpenAPI/Swagger 文档

## 2. 认证与授权

系统采用JWT（JSON Web Token）进行身份认证和授权管理。

### 认证流程
1. 用户通过注册接口创建账户
2. 用户登录获取JWT令牌
3. 后续请求在HTTP头中携带令牌：`Authorization: Bearer <token>`

### 令牌有效期
- JWT令牌设置了过期时间
- 令牌过期后需要重新登录获取新令牌

## 3. API端点

### 3.1 认证相关

#### 3.1.1 用户注册

**请求**
```http
POST /api/auth/register
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123"
}
```

**响应**
```
# 成功
HTTP/1.1 201 Created

# 失败 - 邮箱已存在
HTTP/1.1 400 Bad Request
"Email already exists"
```

#### 3.1.2 用户登录

**请求**
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123"
}
```

**响应**
```
# 成功
HTTP/1.1 200 OK
Content-Type: application/json

{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "username": "user@example.com"
}

# 失败 - 无效凭证
HTTP/1.1 401 Unauthorized
"Invalid email or password"
```

### 3.2 行程管理

#### 3.2.1 创建新行程

**请求**
```http
POST /api/trips
Content-Type: application/json
Authorization: Bearer <token>

{
  "title": "东京5日游",
  "destination": "东京",
  "startDate": "2023-12-20",
  "endDate": "2023-12-24",
  "budgetTotal": 10000.0,
  "companionCount": 2,
  "preferences": ["美食", "购物", "文化"],
  "request": "我想带家人去东京5天，预算1万元，喜欢美食和购物"
}
```

**响应**
```
# 成功
HTTP/1.1 200 OK
Content-Type: application/json

{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "userId": "110e8400-e29b-41d4-a716-446655440000",
  "title": "东京5日游",
  "destination": "东京",
  "startDate": "2023-12-20",
  "endDate": "2023-12-24",
  "budgetTotal": 10000.0,
  "companionCount": 2,
  "preferences": ["美食", "购物", "文化"],
  "dayPlans": [
    {
      "day": 1,
      "locations": [
        {
          "name": "东京 主要景点",
          "lng": 139.7673,
          "lat": 35.6812,
          "description": "这是东京最著名的景点之一",
          "type": "attraction"
        },
        {
          "name": "东京 特色餐厅",
          "lng": 139.7741,
          "lat": 35.6764,
          "description": "当地著名的餐厅，提供特色美食",
          "type": "restaurant"
        }
      ]
    },
    // 更多天数的行程安排...
  ],
  "createdAt": "2023-11-20T10:30:00Z",
  "updatedAt": "2023-11-20T10:30:00Z"
}

# 失败 - 日期验证错误
HTTP/1.1 400 Bad Request
"结束日期不能早于开始日期"
```

#### 3.2.2 获取用户的所有行程

**请求**
```http
GET /api/trips
Authorization: Bearer <token>
```

**响应**
```
# 成功
HTTP/1.1 200 OK
Content-Type: application/json

[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "userId": "110e8400-e29b-41d4-a716-446655440000",
    "title": "东京5日游",
    "destination": "东京",
    "startDate": "2023-12-20",
    "endDate": "2023-12-24",
    "budgetTotal": 10000.0,
    "companionCount": 2,
    "preferences": ["美食", "购物", "文化"],
    "dayPlans": [/* ... */],
    "createdAt": "2023-11-20T10:30:00Z",
    "updatedAt": "2023-11-20T10:30:00Z"
  },
  // 更多行程...
]
```

#### 3.2.3 根据ID获取行程详情

**请求**
```http
GET /api/trips/{id}
Authorization: Bearer <token>
```

**路径参数**
- `id`: 行程的UUID

**响应**
```
# 成功
HTTP/1.1 200 OK
Content-Type: application/json

{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "userId": "110e8400-e29b-41d4-a716-446655440000",
  "title": "东京5日游",
  "destination": "东京",
  "startDate": "2023-12-20",
  "endDate": "2023-12-24",
  "budgetTotal": 10000.0,
  "companionCount": 2,
  "preferences": ["美食", "购物", "文化"],
  "dayPlans": [/* ... */],
  "createdAt": "2023-11-20T10:30:00Z",
  "updatedAt": "2023-11-20T10:30:00Z"
}

# 失败 - 行程不存在或无权访问
HTTP/1.1 404 Not Found
```

#### 3.2.4 更新行程

**请求**
```http
PUT /api/trips/{id}
Content-Type: application/json
Authorization: Bearer <token>

{
  "title": "东京6日游",
  "destination": "东京",
  "startDate": "2023-12-20",
  "endDate": "2023-12-25",
  "budgetTotal": 12000.0,
  "companionCount": 2,
  "preferences": ["美食", "购物", "文化", "温泉"]
}
```

**路径参数**
- `id`: 行程的UUID

**响应**
```
# 成功
HTTP/1.1 200 OK
Content-Type: application/json

{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "userId": "110e8400-e29b-41d4-a716-446655440000",
  "title": "东京6日游",
  "destination": "东京",
  "startDate": "2023-12-20",
  "endDate": "2023-12-25",
  "budgetTotal": 12000.0,
  "companionCount": 2,
  "preferences": ["美食", "购物", "文化", "温泉"],
  "dayPlans": [/* 重新生成的行程 */],
  "createdAt": "2023-11-20T10:30:00Z",
  "updatedAt": "2023-11-21T15:45:00Z"
}

# 失败 - 无权访问
HTTP/1.1 400 Bad Request
"You don't have permission to update this trip"
```

#### 3.2.5 删除行程

**请求**
```http
DELETE /api/trips/{tripId}
Authorization: Bearer <token>
```

**路径参数**
- `tripId`: 行程的UUID

**功能说明**
此接口执行行程的级联删除操作，会按照以下顺序删除相关数据：
1. 首先删除 `locations` 表中与该行程相关的所有记录
2. 然后删除 `expenses` 表中与该行程相关的所有消费记录
3. 最后删除 `trips` 表中的行程记录，同时会自动级联删除 `trip_preferences` 表中的相关记录

**响应**
```
# 成功
HTTP/1.1 200 OK

# 失败 - 无权访问或行程不存在
HTTP/1.1 400 Bad Request
"You don't have permission to delete this trip"

# 失败 - 行程不存在
HTTP/1.1 400 Bad Request
"Trip not found with id: {tripId}"
```

### 3.3 预算和费用管理

#### 3.3.1 获取行程预算状态

**请求**
```http
GET /api/budget/{tripId}
Authorization: Bearer <token>
```

**路径参数**
- `tripId`: 行程的UUID

**响应**
```
# 成功
HTTP/1.1 200 OK
Content-Type: application/json

{
  "totalBudget": 10000.0,
  "spent": 3500.75,
  "remaining": 6499.25,
  "overspend": false
}
```

#### 3.3.2 添加单个消费记录

**请求**
```http
POST /api/expenses/{tripId}
Content-Type: application/json
Authorization: Bearer <token>

{
  "amount": 500.50,
  "currency": "CNY",
  "comment": "午餐",
  "category": "餐饮",
  "expenseDate": "2023-12-20"
}
```

**路径参数**
- `tripId`: 行程的UUID

**响应**
```
# 成功
HTTP/1.1 201 Created
Content-Type: application/json

{
  "id": "990e8400-e29b-41d4-a716-446655440000",
  "userId": "110e8400-e29b-41d4-a716-446655440000",
  "tripId": "550e8400-e29b-41d4-a716-446655440000",
  "amount": 500.50,
  "currency": "CNY",
  "comment": "午餐",
  "category": "餐饮",
  "expenseDate": "2023-12-20"
}
```

#### 3.3.3 批量上传多个消费记录

**请求**
```http
POST /api/expenses/batch/{tripId}
Content-Type: application/json
Authorization: Bearer <token>

[
  {
    "amount": 500.50,
    "currency": "CNY",
    "comment": "午餐",
    "category": "餐饮",
    "expenseDate": "2023-12-20"
  },
  {
    "amount": 1200.00,
    "currency": "CNY",
    "comment": "门票",
    "category": "景点",
    "expenseDate": "2023-12-20"
  }
]
```

**路径参数**
- `tripId`: 行程的UUID

**响应**
```
# 成功
HTTP/1.1 201 Created
Content-Type: application/json

[
  {
    "id": "990e8400-e29b-41d4-a716-446655440000",
    "userId": "110e8400-e29b-41d4-a716-446655440000",
    "tripId": "550e8400-e29b-41d4-a716-446655440000",
    "amount": 500.50,
    "currency": "CNY",
    "comment": "午餐",
    "category": "餐饮",
    "expenseDate": "2023-12-20"
  },
  {
    "id": "990e8400-e29b-41d4-a716-446655440001",
    "userId": "110e8400-e29b-41d4-a716-446655440000",
    "tripId": "550e8400-e29b-41d4-a716-446655440000",
    "amount": 1200.00,
    "currency": "CNY",
    "comment": "门票",
    "category": "景点",
    "expenseDate": "2023-12-20"
  }
]
```

#### 3.3.4 更新单个消费记录

**请求**
```http
PUT /api/expenses/{tripId}/expense/{expenseId}
Content-Type: application/json
Authorization: Bearer <token>

{
  "amount": 550.00,
  "currency": "CNY",
  "comment": "晚餐",
  "category": "餐饮",
  "expenseDate": "2023-12-20"
}
```

**路径参数**
- `tripId`: 行程的UUID
- `expenseId`: 消费记录的UUID

**响应**
```
# 成功
HTTP/1.1 200 OK
Content-Type: application/json

{
  "id": "990e8400-e29b-41d4-a716-446655440000",
  "userId": "110e8400-e29b-41d4-a716-446655440000",
  "tripId": "550e8400-e29b-41d4-a716-446655440000",
  "amount": 550.00,
  "currency": "CNY",
  "comment": "晚餐",
  "category": "餐饮",
  "expenseDate": "2023-12-20"
}
```

#### 3.3.5 删除单个消费记录

**请求**
```http
DELETE /api/expenses/{tripId}/expense/{expenseId}
Authorization: Bearer <token>
```

**路径参数**
- `tripId`: 行程的UUID
- `expenseId`: 消费记录的UUID

**响应**
```
# 成功
HTTP/1.1 200 OK
```

#### 3.3.6 查询行程的所有消费记录

**请求**
```http
GET /api/expenses/{tripId}
Authorization: Bearer <token>
```

**路径参数**
- `tripId`: 行程的UUID

**响应**
```
# 成功
HTTP/1.1 200 OK
Content-Type: application/json

[
  {
    "id": "990e8400-e29b-41d4-a716-446655440000",
    "userId": "110e8400-e29b-41d4-a716-446655440000",
    "tripId": "550e8400-e29b-41d4-a716-446655440000",
    "amount": 550.00,
    "currency": "CNY",
    "comment": "晚餐",
    "category": "餐饮",
    "expenseDate": "2023-12-20"
  },
  // 更多消费记录...
]
```

#### 3.3.7 查询单个消费记录详情

**请求**
```http
GET /api/expenses/{tripId}/expense/{expenseId}
Authorization: Bearer <token>
```

**路径参数**
- `tripId`: 行程的UUID
- `expenseId`: 消费记录的UUID

**响应**
```
# 成功
HTTP/1.1 200 OK
Content-Type: application/json

{
  "id": "990e8400-e29b-41d4-a716-446655440000",
  "userId": "110e8400-e29b-41d4-a716-446655440000",
  "tripId": "550e8400-e29b-41d4-a716-446655440000",
  "amount": 550.00,
  "currency": "CNY",
  "comment": "晚餐",
  "category": "餐饮",
  "expenseDate": "2023-12-20"
}
```

### 3.4 地点管理

#### 3.4.1 获取指定行程的所有位置

**请求**
```http
GET /api/locations/{tripId}
Authorization: Bearer <token>
```

**路径参数**
- `tripId`: 行程的UUID

**响应**
```
# 成功
HTTP/1.1 200 OK
Content-Type: application/json

[
  {
    "id": "880e8400-e29b-41d4-a716-446655440000",
    "tripId": "550e8400-e29b-41d4-a716-446655440000",
    "name": "东京 主要景点",
    "lng": 139.7673,
    "lat": 35.6812,
    "description": "这是东京最著名的景点之一",
    "type": "attraction",
    "day": 1,
    "orderIndex": 1
  },
  // 更多地点...
]
```

#### 3.4.2 获取指定行程某一天的位置

**请求**
```http
GET /api/locations/{tripId}/day/{day}
Authorization: Bearer <token>
```

**路径参数**
- `tripId`: 行程的UUID
- `day`: 天数，从1开始计数

**响应**
```
# 成功
HTTP/1.1 200 OK
Content-Type: application/json

[
  {
    "id": "880e8400-e29b-41d4-a716-446655440000",
    "tripId": "550e8400-e29b-41d4-a716-446655440000",
    "name": "东京 主要景点",
    "lng": 139.7673,
    "lat": 35.6812,
    "description": "这是东京最著名的景点之一",
    "type": "attraction",
    "day": 1,
    "orderIndex": 1
  },
  {
    "id": "880e8400-e29b-41d4-a716-446655440001",
    "tripId": "550e8400-e29b-41d4-a716-446655440000",
    "name": "东京 特色餐厅",
    "lng": 139.7741,
    "lat": 35.6764,
    "description": "当地著名的餐厅，提供特色美食",
    "type": "restaurant",
    "day": 1,
    "orderIndex": 2
  }
]
```

## 4. 数据传输对象 (DTOs)

### 4.1 认证相关DTO

#### 4.1.1 RegisterRequest
```java
@Data
public class RegisterRequest {
    private String email;      // 用户邮箱
    private String password;   // 用户密码
}
```

#### 4.1.2 LoginRequest
```java
@Data
public class LoginRequest {
    private String email;      // 用户邮箱
    private String password;   // 用户密码
}
```

#### 4.1.3 AuthResponse
```java
@Data
public class AuthResponse {
    private String token;      // JWT令牌
    private String email;   // 用户名（邮箱）
}
```

### 4.2 行程相关DTO

#### 4.2.1 TripRequest
```java
@Data
public class TripRequest {
    @NotBlank(message = "标题不能为空")
    private String title;                    // 行程标题
    
    @NotBlank(message = "目的地不能为空")
    private String destination;              // 目的地
    
    @NotNull(message = "开始日期不能为空")
    private LocalDate startDate;             // 开始日期
    
    @NotNull(message = "结束日期不能为空")
    private LocalDate endDate;               // 结束日期
    
    @NotNull(message = "预算不能为空")
    @Min(value = 0, message = "预算不能为负数")
    private Double budgetTotal;              // 总预算
    
    @Min(value = 0, message = "同行人数不能为负数")
    private Integer companionCount = 0;      // 同行人数
    
    private List<String> preferences;        // 旅行偏好
    
    private String request;                  // 用户原始自然语言请求
}
```

#### 4.2.2 TripResponse
```java
@Data
public class TripResponse {
    private UUID id;                         // 行程ID
    private UUID userId;                     // 用户ID
    private String title;                    // 行程标题
    private String destination;              // 目的地
    private LocalDate startDate;             // 开始日期
    private LocalDate endDate;               // 结束日期
    private BigDecimal budgetTotal;          // 总预算
    private Integer companionCount;          // 同行人数
    private List<String> preferences;        // 旅行偏好
    private List<DayPlanDTO> dayPlans;       // 每日行程计划
    private Instant createdAt;               // 创建时间
    private Instant updatedAt;               // 更新时间
}
```

#### 4.2.3 DayPlanDTO
```java
@Data
public class DayPlanDTO {
    private Integer day;                     // 天数（从1开始）
    private List<LocationDTO> locations;     // 当天的地点列表
}
```

### 4.3 费用和预算相关DTO

#### 4.3.1 ExpenseResponse
```java
@Data
public class ExpenseResponse {
    private UUID id;                         // 消费记录ID
    private UUID userId;                     // 用户ID
    private UUID tripId;                     // 行程ID
    private String comment;                  // 消费说明
    private BigDecimal amount;               // 消费金额
    private String currency;                 // 货币类型
    private String category;                 // 消费类别
    private LocalDate expenseDate;           // 消费日期
}
```

#### 4.3.2 BudgetResponse
```java
@Data
public class BudgetResponse {
    private BigDecimal totalBudget;          // 总预算
    private BigDecimal spent;                // 已花费金额
    private BigDecimal remaining;            // 剩余预算
    private boolean overspend;               // 是否超支
}
```

### 4.4 地点相关DTO

#### 4.4.1 LocationDTO
```java
@Data
public class LocationDTO {
    private String name;                     // 景点名称
    private Double lng;                      // 经度
    private Double lat;                      // 纬度
    private String description;              // 简介
    private String type;                     // 类型 (attraction, restaurant, cultural, shopping 等)
}
```

## 5. 错误处理

API使用标准HTTP状态码来表示请求的结果：

- **200 OK**: 请求成功
- **201 Created**: 资源创建成功
- **400 Bad Request**: 请求参数错误或验证失败
- **401 Unauthorized**: 未认证或认证失败
- **403 Forbidden**: 没有权限访问资源
- **404 Not Found**: 资源不存在

错误响应通常包含一个描述性消息，例如：
```json
{
  "message": "结束日期不能早于开始日期"
}
```

## 6. 安全考虑

1. **JWT认证**: 所有非公开接口都需要JWT令牌进行认证
2. **密码加密**: 用户密码使用BCrypt加密存储
3. **权限验证**: 确保用户只能访问和修改自己的行程和费用数据
4. **输入验证**: 使用Bean Validation对所有输入进行验证
5. **HTTPS传输**: 生产环境应使用HTTPS保护数据传输

## 7. 数据模型关系

- **用户 (User)** 一对多 **行程 (Trip)**
- **行程 (Trip)** 一对多 **地点 (Location)**
- **行程 (Trip)** 一对多 **消费记录 (Expense)**
- **行程 (Trip)** 一对多 **行程版本 (TripVersion)**
- **用户 (User)** 一对多 **消费记录 (Expense)**

以上是AI Travel Planner后端API的详细文档。前端开发人员可以根据此文档实现相应的功能对接。