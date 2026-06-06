# 🎨 手作作品展示分享空间

> 面向手工创作者打造的原创作品展示社区平台

[![Vue](https://img.shields.io/badge/Vue-3.4-4FC08D?logo=vue.js&logoColor=white)](https://vuejs.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3-6DB33F?logo=spring&logoColor=white)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?logo=mysql&logoColor=white)](https://www.mysql.com/)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker&logoColor=white)](https://www.docker.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

## ✨ 项目简介

手作空间是一个面向手工创作者的原创作品展示与交流社区。创作者可以发布布艺、陶艺、编织、木艺等各类手作作品，记录用料清单、创作思路与制作周期；访客可以浏览、搜索、收藏心仪作品，发现更多美好创意。

平台聚焦内容展示与创作交流，不接入商品交易、在线下单等电商功能，致力于打造纯粹的手作爱好者社区。

## 🛠 技术栈

### 前端技术

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.4 | 渐进式 JavaScript 框架 |
| Vite | 5.2 | 下一代前端构建工具 |
| Vue Router | 4.3 | Vue.js 官方路由 |
| Pinia | 2.1 | Vue 状态管理库 |
| Element Plus | 2.7 | Vue 3 组件库 |
| Axios | 1.6 | HTTP 客户端 |
| Sass | 1.75 | CSS 预处理器 |
| Mitt | 3.0 | 事件总线 |

### 后端技术

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.3 | 微服务框架 |
| MyBatis Plus | 3.5 | ORM 增强工具 |
| MySQL | 8.0 | 关系型数据库 |
| Redis | 7.x | 缓存数据库 |
| Hutool | 5.8 | Java 工具包 |
| Lombok | - | 简化 Java 代码 |
| JDK | 17 | Java 开发环境 |

### 部署与运维

- **容器化**: Docker + Docker Compose
- **反向代理**: Nginx
- **国内镜像加速**: DaoCloud / npmmirror / 阿里云

## 🎯 核心功能

### 1. 作品发布管理
- 支持多图上传，最多 9 张作品图片
- 记录用料清单、创作思路、制作周期
- 选择手作品类（编织/陶艺/布艺/木艺）
- 作品上下架管理与编辑

### 2. 作品浏览探索
- 首页瀑布流展示，响应式布局适配
- 热门作品推荐，Redis 缓存加速
- 按分类筛选作品
- 关键词搜索（标题 + 描述）
- 无限滚动加载更多

### 3. 个人收藏系统
- 一键收藏/取消收藏作品
- 个人收藏夹集中管理
- 收藏数实时统计展示

### 4. 创作者主页
- 个人作品集汇总展示
- 作品数、浏览量、收藏数统计
- 个人简介与头像展示
- 支持查看他人主页

### 5. 图片上传与处理
- 本地文件存储，按日期目录组织
- 支持 WebP 格式转换与压缩
- 定时清理过期无效图片（每日凌晨 2 点）

## 🏗 项目结构

```
.
├── frontend/                    # 前端项目
│   ├── src/
│   │   ├── components/          # 公共组件
│   │   │   ├── HeaderComponent.vue    # 顶部导航
│   │   │   ├── FooterComponent.vue    # 底部页脚
│   │   │   └── WorkCard.vue           # 作品卡片
│   │   ├── views/               # 页面视图
│   │   │   ├── HomeView.vue          # 首页
│   │   │   ├── WorkDetailView.vue    # 作品详情
│   │   │   ├── CategoryView.vue      # 分类页
│   │   │   ├── PublishView.vue       # 发布作品
│   │   │   ├── ProfileView.vue       # 个人主页
│   │   │   └── FavoritesView.vue     # 我的收藏
│   │   ├── router/              # 路由配置
│   │   ├── store/               # Pinia 状态管理
│   │   ├── utils/               # 工具函数
│   │   └── assets/              # 静态资源
│   ├── index.html
│   ├── vite.config.js           # Vite 配置
│   ├── package.json
│   ├── nginx.conf               # Nginx 配置
│   ├── .npmrc                   # npm 镜像源
│   └── Dockerfile               # 前端 Dockerfile
│
├── backend/                     # 后端项目
│   ├── src/main/java/com/handmade/
│   │   ├── HandmadeApplication.java   # 启动类
│   │   ├── controller/           # 控制层
│   │   │   ├── WorkController        # 作品接口
│   │   │   ├── UserController        # 用户接口
│   │   │   ├── CategoryController    # 分类接口
│   │   │   ├── FavoriteController    # 收藏接口
│   │   │   └── FileController        # 文件上传
│   │   ├── service/              # 服务层
│   │   │   ├── WorkService
│   │   │   ├── UserService
│   │   │   ├── CategoryService
│   │   │   └── FavoriteService
│   │   ├── service/impl/         # 服务实现
│   │   ├── mapper/               # 数据访问层
│   │   ├── entity/               # 实体类
│   │   ├── config/               # 配置类
│   │   ├── task/                 # 定时任务
│   │   └── vo/                   # 返回值封装
│   ├── src/main/resources/
│   │   └── application.yml       # 应用配置
│   ├── pom.xml                   # Maven 配置
│   ├── settings.xml              # Maven 镜像源
│   └── Dockerfile                # 后端 Dockerfile
│
├── docker/                       # Docker 相关
│   └── mysql/
│       └── init.sql              # 数据库初始化脚本
│
├── scripts/                      # 脚本工具
│   ├── start.sh                  # 一键启动脚本
│   └── stop.sh                   # 停止服务脚本
│
├── .env                          # 环境变量配置
├── .env.example                  # 环境变量示例
├── docker-compose.yml            # Docker Compose 配置
└── README.md                     # 项目说明文档
```

## 🚀 快速开始

### 环境要求

- Docker & Docker Compose v2+
- 无需本地安装 Node.js、JDK、Maven（容器内构建）

### 一键启动

```bash
# 给脚本添加执行权限
chmod +x scripts/*.sh

# 一键构建并启动
./scripts/start.sh
```

启动脚本会自动完成：
1. ✅ 检测端口占用，自动分配可用端口
2. ✅ 构建前后端 Docker 镜像
3. ✅ 启动 MySQL、Redis、后端、前端服务
4. ✅ 输出前端访问地址

### 停止服务

```bash
./scripts/stop.sh
```

### 手动启动

```bash
# 加载环境变量并构建启动
docker compose up --build -d

# 查看服务日志
docker compose logs -f

# 停止并移除容器
docker compose down
```

### 访问地址

启动成功后，默认访问地址：

| 服务 | 地址 | 说明 |
|------|------|------|
| 前端 | http://localhost:3008 | 手作空间首页 |
| 后端 API | http://localhost:8088 | Spring Boot 接口服务 |
| MySQL | localhost:3309 | 数据库服务 |
| Redis | localhost:6380 | 缓存服务 |

> 实际端口以启动脚本输出为准，端口冲突时会自动递增分配

## ⚙️ 配置说明

所有配置统一通过 `.env` 文件管理。

### 端口配置

| 变量名 | 默认值 | 说明 |
|--------|--------|------|
| `FRONTEND_PORT` | 3008 | 前端 Nginx 服务端口 |
| `BACKEND_PORT` | 8088 | 后端 API 服务端口 |
| `MYSQL_PORT` | 3309 | MySQL 数据库端口 |
| `REDIS_PORT` | 6380 | Redis 缓存端口 |

> 已避开常用默认端口：80、443、8080、3306、6379、5432、9200 等

### 镜像仓库配置

```env
DOCKER_REGISTRY=docker.m.daocloud.io/
```

所有基础镜像（node、nginx、maven、jdk、mysql、redis）统一使用该镜像仓库前缀，全链路共用同一地址。

### 依赖镜像源

- **npm**: `https://registry.npmmirror.com`（淘宝 npmmirror 镜像）
- **Maven**: `https://maven.aliyun.com/repository/public`（阿里云镜像）

## 📊 数据库设计

### 数据表概览

| 表名 | 说明 | 主要字段 |
|------|------|----------|
| `sys_user` | 用户表 | id, username, password, nickname, avatar, bio, email |
| `category` | 分类表 | id, name, description, icon, sort, status |
| `work` | 作品表 | id, user_id, category_id, title, description, cover_image, images, materials, creation_idea, production_cycle, view_count, favorite_count, like_count, status, is_hot |
| `favorite` | 收藏表 | id, user_id, work_id, create_time |

详细建表语句见 [docker/mysql/init.sql](docker/mysql/init.sql)

### 预置数据

- **分类数据**：编织、陶艺、布艺、木艺 4 个分类
- **用户数据**：admin、小手巧、陶然、布布 4 个测试用户

## ⚡ 性能优化

### 前端优化

1. **图片优化**：支持 WebP 格式，压缩率提升约 30%
2. **Gzip 压缩**：静态资源构建时全局开启 gzip 压缩
3. **瀑布流布局**：CSS Columns 实现，原生响应式
4. **代码分割**：路由级别的代码分割，按需加载
5. **依赖分包**：element-plus、vue-vendor 独立 chunk
6. **国内镜像源**：npm 使用淘宝镜像，依赖下载速度提升

### 后端优化

1. **Redis 缓存**：热门作品列表缓存，减轻数据库压力
2. **数据库索引**：关键字段建立索引，查询性能优化
3. **逻辑删除**：MyBatis Plus 逻辑删除，保护数据
4. **定时任务**：每日凌晨 2 点自动清理过期图片
5. **统一返回格式**：标准 `{code, message, data}` 响应结构

### Docker 构建优化

项目采用 Docker 原生分层缓存机制，确保构建效率。

**前端构建分层：**
1. 基础镜像层 → node:18-alpine
2. npm 配置层 → 设置国内镜像源
3. 依赖层 → 复制 package.json，执行 npm install
4. 源码层 → 复制全部源码
5. 构建层 → 执行 npm run build
6. 运行层 → nginx:alpine + dist 静态资源

**后端构建分层：**
1. 基础镜像层 → maven:3.9-eclipse-temurin-17
2. Maven 配置层 → 复制 settings.xml
3. 依赖层 → 复制 pom.xml，执行 mvn dependency:resolve
4. 源码层 → 复制 src 目录
5. 构建层 → 执行 mvn clean package
6. 运行层 → eclipse-temurin:17-jre + jar 包

> 只要 `package.json` 或 `pom.xml` 不变，依赖层就会复用缓存

## 📝 开发规范

### 前端规范

- 使用 Vue 3 Composition API
- 组件命名采用 PascalCase
- 页面视图放在 `views` 目录
- 可复用组件放在 `components` 目录
- 工具函数放在 `utils` 目录
- 状态管理使用 Pinia

### 后端规范

- 遵循 RESTful API 设计规范
- 统一返回格式: `{code, message, data}`
- Service 层接口与实现分离
- 使用 MyBatis Plus 简化 CRUD 操作
- 控制器只做参数校验和结果封装
- 业务逻辑下沉至 Service 层

## ❓ 常见问题

### Q1: 构建速度慢怎么办？

首次构建需要下载依赖，属于正常现象。后续构建只要不修改 `package.json` 或 `pom.xml`，会自动复用 Docker 分层缓存，构建速度大幅提升。

### Q2: 端口被占用如何处理？

启动脚本会自动检测端口占用并分配可用端口（默认端口 +100 范围内）。如需手动指定，直接修改 `.env` 文件中的对应端口号即可。

### Q3: 镜像拉取失败怎么办？

确保 `.env` 文件中的 `DOCKER_REGISTRY` 配置正确。默认使用 DaoCloud 国内镜像源，无需 VPN 即可访问。如遇镜像源不可用，可更换为其他国内镜像源。

### Q4: 如何强制重新构建？

```bash
# 强制重新构建所有镜像
docker compose build --no-cache

# 或者启动时带 --build 参数
docker compose up --build -d
```

### Q5: 如何查看服务日志？

```bash
# 查看所有服务日志
docker compose logs -f

# 查看指定服务日志
docker compose logs -f backend
docker compose logs -f frontend
```

## 📄 License

MIT License

---

<div align="center">
  <p>用心打造，致敬每一位手艺人 💝</p>
</div>
