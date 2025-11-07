# AI Travel Planner Backend

基于Spring Boot的后端服务，提供旅行规划的API接口。

## 技术栈

- Java 17
- Spring Boot 3.2.0
- Spring Web
- Spring Security
- Spring Data JPA
- H2 Database (开发环境)

## 快速开始

### 前提条件

- JDK 17 或更高版本
- Maven 3.6 或更高版本

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
├── controller/              # 控制器
├── service/                 # 服务层
├── repository/              # 数据访问层
├── model/                   # 数据模型
└── dto/                     # 数据传输对象
```

## API文档

API文档将在应用启动后通过Swagger UI访问，地址为：
`http://localhost:8080/swagger-ui/index.html`

## 数据库配置

开发环境使用H2内存数据库，配置信息在`application.properties`文件中。

## 安全配置

项目使用Spring Security进行身份认证和授权管理。