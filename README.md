# 员工管理系统

一个现代化的员工管理系统，支持员工信息的增删改查功能，采用前后端分离架构，完全容器化部署。

## 🛠 技术栈

### 前端
- **框架**: Vite 5 + Vue 3.5
- **UI组件库**: Ant Design Vue 4
- **状态管理**: Pinia 2
- **路由**: Vue Router 4
- **HTTP客户端**: Axios
- **容器**: Nginx (Alpine)

### 后端
- **框架**: Spring Boot 3.2
- **数据库**: MySQL 8.0
- **ORM**: MyBatis Plus 3.5
- **API文档**: SpringDoc OpenAPI 3 (Swagger)
- **运行环境**: Java 17 (Eclipse Temurin)

### 容器化
- **编排工具**: Docker Compose
- **数据持久化**: Docker Volume
- **网络**: 自定义Bridge网络

## 🚀 启动指南

### 前置要求
- 已安装 Docker Desktop 并确保正在运行
- Docker 版本 >= 20.10
- Docker Compose 版本 >= 2.0

### 一键启动

在项目根目录执行以下命令：

```bash
docker compose up --build
```

**首次构建时间**：约 5-8 分钟（取决于网络速度）  
**后续启动时间**：约 30 秒

等待所有容器启动完成后，即可访问系统。

### 停止服务

```bash
docker compose down
```

**保留数据并停止**：
```bash
docker compose down
```

**完全清理（包括数据）**：
```bash
docker compose down -v
```

## 🔗 服务地址

| 服务 | 地址 | 说明 |
|------|------|------|
| 前端应用 | http://localhost:3268 | Vue3 + Ant Design Vue |
| 后端API | http://localhost:8268/api | Spring Boot REST API |
| Swagger文档 | http://localhost:8268/swagger-ui/index.html | API接口文档 |
| MySQL数据库 | localhost:3368 | 用户: root / 密码: root123456 |

## 🧪 测试账号

| 用户名 | 密码 | 说明 |
|--------|------|------|
| admin | 123456 | 管理员账号 |

## ✨ 功能特性

### 用户认证
- ✅ 登录功能（用户名密码验证）
- ✅ 登录状态管理
- ✅ 路由守卫保护

### 员工管理
- ✅ 员工列表展示（分页）
- ✅ 关键词搜索（姓名、邮箱、手机号、部门、职位）
- ✅ 新增员工
- ✅ 编辑员工信息
- ✅ 删除员工（二次确认）
- ✅ 表单验证（前后端双重验证）

### UI/UX设计
- ✅ 现代化渐变设计
- ✅ 响应式布局
- ✅ 平滑动画效果
- ✅ 加载状态提示
- ✅ 操作结果反馈（Toast提示）
- ✅ 所有文案中文化

## 📊 数据库设计

### 员工表 (employee)
- id - 员工ID（主键，自增）
- name - 姓名
- email - 邮箱（唯一）
- phone - 手机号
- department - 部门
- position - 职位
- hire_date - 入职日期
- status - 状态（1=在职，0=离职）
- created_at - 创建时间
- updated_at - 更新时间

### 初始数据
系统启动时会自动导入10条员工示例数据，无需手动添加。

## 🏗 项目结构

```
.
├── backend/                    # 后端SpringBoot项目
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/employee/
│   │   │   │   ├── config/    # 配置类
│   │   │   │   ├── controller/ # 控制器
│   │   │   │   ├── service/   # 服务层
│   │   │   │   ├── entity/    # 实体类
│   │   │   │   ├── mapper/    # Mapper接口
│   │   │   │   └── dto/       # 数据传输对象
│   │   │   └── resources/
│   │   │       ├── application.yml
│   │   │       └── init.sql   # 数据库初始化脚本
│   ├── pom.xml
│   ├── settings.xml           # Maven阿里云镜像配置
│   └── Dockerfile
├── frontend/                   # 前端Vue3项目
│   ├── src/
│   │   ├── api/               # API接口封装
│   │   ├── views/             # 页面组件
│   │   ├── stores/            # Pinia状态管理
│   │   ├── router/            # 路由配置
│   │   └── utils/             # 工具函数
│   ├── package.json
│   ├── vite.config.js
│   ├── nginx.conf             # Nginx配置
│   └── Dockerfile
├── docker-compose.yml         # Docker编排配置
└── README.md
```

## 🐳 Docker配置说明

### 端口映射
- 前端：3268 → 80 (Nginx)
- 后端：8268 → 8268 (Spring Boot)
- 数据库：3368 → 3306 (MySQL)

### 网络架构
所有服务运行在同一个自定义Bridge网络中，通过服务名进行通信：
- 前端 → 后端：http://backend:8268
- 后端 → 数据库：jdbc:mysql://db:3306

### 数据持久化
MySQL数据存储在Docker Volume `mysql_data` 中，停止容器后数据不会丢失。

### 健康检查
数据库配置了健康检查，确保MySQL完全就绪后才启动后端服务，避免连接失败。

## 🔧 常见问题

### Q: 端口冲突怎么办？
A: 如果3268或8268端口被占用，可以修改`docker-compose.yml`中的端口映射。

### Q: 构建失败怎么办？
A: 
1. 检查Docker Desktop是否正常运行
2. 检查网络连接
3. 尝试清理缓存：`docker system prune -a`
4. 重新构建：`docker compose up --build --force-recreate`

### Q: 数据库连接失败？
A: 等待数据库初始化完成，首次启动需要约30秒。查看日志：`docker compose logs db`

### Q: 前端页面无法访问后端API？
A: 确保所有容器都在运行中：`docker compose ps`

## 📝 开发说明

### 本地开发

**后端开发**：
```bash
cd backend
mvn spring-boot:run
```

**前端开发**：
```bash
cd frontend
npm install
npm run dev
```

### 查看日志

```bash
# 查看所有服务日志
docker compose logs

# 查看特定服务日志
docker compose logs frontend
docker compose logs backend
docker compose logs db

# 实时跟踪日志
docker compose logs -f
```

## 📄 许可证

本项目仅用于学习和演示目的。

---

**开发时间**: 2024年
**版本**: 1.0.0
