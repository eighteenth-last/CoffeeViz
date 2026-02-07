# CoffeeViz

> 数据库架构可视化生成器 - 将 SQL 脚本和数据库连接转换为精美的 ER 图

CoffeeViz 是一个强大的数据库架构可视化工具，支持从 SQL 脚本、JDBC 连接或 AI 生成的方式创建数据库 ER 图（实体关系图）。它使用 Mermaid 语法生成图表，并支持导出为 SVG、PNG 等多种格式。

## ✨ 核心特性

- 🔍 **多源解析**：支持 SQL 脚本解析、JDBC 数据库连接、AI 辅助生成
- 🎨 **可视化渲染**：基于 Mermaid 生成清晰的 ER 图
- 📦 **多格式导出**：支持 SVG、PNG、Mermaid 源码导出
- 🔐 **用户系统**：完整的用户认证、项目管理、版本控制
- 🚀 **高性能**：Redis 缓存、异步处理、API 限流
- 🎯 **智能推断**：自动识别表关系、外键约束
- 📱 **现代化 UI**：基于 Vue 3 + Naive UI 的响应式界面

## 🏗️ 技术架构

### 后端技术栈

- **框架**: Spring Boot 3.2.0 + Java 17
- **数据库**: MySQL 8.0 + MyBatis-Plus 3.5.5
- **认证**: Sa-Token 1.37.0
- **缓存**: Redis + Spring Data Redis
- **SQL 解析**: JSqlParser 4.6 + Druid 1.2.20
- **连接池**: Druid
- **工具类**: Lombok, Hutool, Guava, FastJSON2

### 前端技术栈

- **框架**: Vue 3.4 + Vite 5.0
- **UI 组件**: Naive UI 2.38
- **状态管理**: Pinia 2.1
- **路由**: Vue Router 4.2
- **HTTP 客户端**: Axios 1.6
- **样式**: Tailwind CSS 3.4

### 项目结构

```
coffeeviz/
├── coffeeviz-common/          # 公共模块（常量、异常、工具类）
├── coffeeviz-model/           # 数据模型（预留）
├── coffeeviz-core/            # 核心模块（ER 图模型、渲染引擎）
├── coffeeviz-jdbc/            # JDBC 模块（数据库元数据解析）
├── coffeeviz-sql/             # SQL 解析模块（多解析器组合）
├── coffeeviz-llm/             # LLM 模块（AI 生成，预留）
├── coffeeviz-export/          # 导出模块（SVG/PNG 生成）
├── coffeeviz-service/         # 业务服务层（用户、项目、ER 图服务）
├── coffeeviz-web/             # Web 层（Controller、配置）
└── coffeeviz-frontend/        # 前端项目（Vue 3）
```

## 🚀 快速开始

### 环境要求

- **JDK**: 17+
- **Maven**: 3.6+
- **MySQL**: 8.0+
- **Redis**: 6.0+ (可选，用于缓存)
- **Node.js**: 18+ (前端开发)
- **Mermaid CLI**: 可选，用于图表导出

### 1. 克隆项目

```bash
git clone https://github.com/yourusername/coffeeviz.git
cd coffeeviz
```

### 2. 数据库初始化

```bash
# 创建数据库
mysql -u root -p

# 执行初始化脚本
mysql -u root -p < init.sql
```

或者使用 `coffeeviz-web/src/main/resources/schema.sql` 文件。

**默认管理员账号**：
- 用户名: `admin`
- 密码: `admin123`

### 3. 配置后端

编辑 `coffeeviz-web/src/main/resources/application-dev.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/coffeeviz?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: root
    password: your_password
    
  # Redis 配置（可选）
  data:
    redis:
      host: localhost
      port: 6379
      password: 
      database: 0
```

### 4. 启动后端

```bash
# 编译项目
mvn clean install -DskipTests

# 启动应用
mvn spring-boot:run -pl coffeeviz-web
```

后端服务将在 `http://localhost:8080` 启动。

### 5. 启动前端

```bash
cd coffeeviz-frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

前端应用将在 `http://localhost:3000` 启动。

### 6. 访问应用

打开浏览器访问 `http://localhost:3000`，使用默认管理员账号登录。

## 📖 使用指南

### SQL 解析模式

1. 在主界面选择 "SQL 导入"
2. 粘贴或上传 SQL DDL 脚本
3. 配置渲染选项（视图模式、表过滤等）
4. 点击 "生成 ER 图"
5. 查看、编辑、导出生成的图表

**支持的 SQL 语法**：
- MySQL DDL (CREATE TABLE, ALTER TABLE)
- PostgreSQL DDL
- 标准 SQL DDL

### JDBC 连接模式

1. 选择 "数据库连接"
2. 填写数据库连接信息：
   - 数据库类型（MySQL/PostgreSQL）
   - JDBC URL
   - 用户名/密码
   - Schema 名称（可选）
3. 点击 "测试连接" 验证
4. 配置渲染选项
5. 点击 "生成 ER 图"

### 项目管理

- **创建项目**：保存生成的 ER 图为项目
- **版本控制**：每次更新可创建新版本
- **历史回溯**：查看和恢复历史版本
- **导出分享**：导出 Mermaid 源码、SVG、PNG

## 🔌 API 文档

### 认证接口

#### 用户登录
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}
```

#### 用户注册
```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "newuser",
  "password": "password123",
  "email": "user@example.com"
}
```

### ER 图生成接口

#### SQL 解析
```http
POST /api/er/parse-sql
Authorization: {token}
Content-Type: application/json

{
  "sqlText": "CREATE TABLE users (...)",
  "viewMode": "FULL",
  "tableFilter": "",
  "relationDepth": 3
}
```

#### JDBC 连接
```http
POST /api/er/connect-jdbc
Authorization: {token}
Content-Type: application/json

{
  "dbType": "mysql",
  "jdbcUrl": "jdbc:mysql://localhost:3306/mydb",
  "username": "root",
  "password": "password",
  "schemaName": "mydb",
  "viewMode": "FULL"
}
```

#### 测试连接
```http
POST /api/er/test-connection
Authorization: {token}
Content-Type: application/json

{
  "dbType": "mysql",
  "jdbcUrl": "jdbc:mysql://localhost:3306/mydb",
  "username": "root",
  "password": "password"
}
```

### 项目管理接口

#### 创建项目
```http
POST /api/project/create
Authorization: {token}
Content-Type: application/json

{
  "projectName": "My Database",
  "description": "Project description",
  "mermaidCode": "erDiagram..."
}
```

#### 查询项目列表
```http
POST /api/project/list
Authorization: {token}
Content-Type: application/json

{
  "page": 1,
  "size": 10,
  "keyword": "",
  "status": "active"
}
```

#### 查询项目详情
```http
GET /api/project/detail/{projectId}
Authorization: {token}
```

#### 更新项目
```http
PUT /api/project/update
Authorization: {token}
Content-Type: application/json

{
  "id": 1,
  "projectName": "Updated Name",
  "mermaidCode": "erDiagram...",
  "createVersion": true,
  "changeLog": "Updated schema"
}
```

#### 删除项目
```http
DELETE /api/project/delete/{projectId}
Authorization: {token}
```

## 🔧 配置说明

### 应用配置

主配置文件：`coffeeviz-web/src/main/resources/application.yml`

```yaml
# 服务器配置
server:
  port: 8080

# Sa-Token 配置
sa-token:
  token-name: Authorization
  timeout: 2592000  # 30天
  is-concurrent: true
  token-style: uuid

# MyBatis-Plus 配置
mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
  global-config:
    db-config:
      id-type: auto
```

### 系统配置

系统配置存储在 `sys_config` 表中，可通过管理界面修改：

- `openai.api.key`: OpenAI API 密钥（AI 生成功能）
- `mermaid.cli.path`: Mermaid CLI 路径
- `export.max.size`: 导出文件最大大小
- `sql.max.length`: SQL 文本最大长度
- `jdbc.connection.timeout`: JDBC 连接超时时间
- `rate.limit.per.second`: API 限流配置

## 🎨 渲染选项

### 视图模式

- **FULL**: 完整视图，显示所有字段和关系
- **SIMPLE**: 简化视图，只显示主键和外键
- **KEY_ONLY**: 仅显示键，最简洁的视图

### 布局方向

- **TB**: 从上到下（Top to Bottom）
- **LR**: 从左到右（Left to Right）
- **BT**: 从下到上（Bottom to Top）
- **RL**: 从右到左（Right to Left）

### 高级选项

- **表过滤**: 指定要包含的表名（逗号分隔）
- **关系深度**: 关系推断的深度（1-5）
- **显示索引**: 是否显示索引信息
- **显示注释**: 是否显示字段注释

## 🧪 测试

```bash
# 运行所有测试
mvn test

# 运行特定模块测试
mvn test -pl coffeeviz-core

# 跳过测试构建
mvn clean install -DskipTests
```

## 📦 部署

### 打包应用

```bash
# 打包后端
mvn clean package -DskipTests

# 打包前端
cd coffeeviz-frontend
npm run build
```

### Docker 部署

```bash
# 构建镜像
docker build -t coffeeviz:latest .

# 运行容器
docker run -d \
  -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://host:3306/coffeeviz \
  -e SPRING_DATASOURCE_USERNAME=root \
  -e SPRING_DATASOURCE_PASSWORD=password \
  --name coffeeviz \
  coffeeviz:latest
```

### 生产环境配置

1. 修改 `application-prod.yml` 配置
2. 配置 HTTPS 证书
3. 启用 Redis 缓存
4. 配置反向代理（Nginx）
5. 设置日志级别为 INFO
6. 修改默认管理员密码

## 🤝 贡献指南

欢迎贡献代码、报告问题或提出建议！

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

## 📝 开发规范

- 遵循阿里巴巴 Java 开发手册
- 使用 Lombok 简化代码
- 编写单元测试（目标覆盖率 > 80%）
- 提交前运行 `mvn clean test`
- 使用有意义的提交信息

## 🐛 问题排查

### 常见问题

**Q: 启动时提示数据库连接失败？**
A: 检查 MySQL 是否启动，配置文件中的数据库连接信息是否正确。

**Q: 前端无法连接后端？**
A: 检查 CORS 配置，确保后端已启动在 8080 端口。

**Q: SQL 解析失败？**
A: 确保 SQL 语法正确，支持标准 DDL 语法。复杂的存储过程可能不支持。

**Q: 图表导出失败？**
A: 需要安装 Mermaid CLI (`npm install -g @mermaid-js/mermaid-cli`)。

**Q: Redis 连接失败？**
A: Redis 是可选的，如果不使用缓存功能，可以注释掉 Redis 相关配置。

### 日志查看

```bash
# 查看应用日志
tail -f logs/coffeeviz-dev.log

# 查看 Spring Boot 日志
tail -f coffeeviz-web/logs/coffeeviz-dev.log
```

## 📄 许可证

本项目采用 MIT 许可证。详见 [LICENSE](LICENSE) 文件。

## 👥 团队

程序员Eighteen
eighteenthstuai@gmail.com

## 🙏 致谢

- [Mermaid](https://mermaid.js.org/) - 图表渲染引擎
- [Spring Boot](https://spring.io/projects/spring-boot) - 后端框架
- [Vue.js](https://vuejs.org/) - 前端框架
- [Naive UI](https://www.naiveui.com/) - UI 组件库
- [MyBatis-Plus](https://baomidou.com/) - ORM 框架
- [Sa-Token](https://sa-token.cc/) - 认证授权框架

## 📮 联系方式

- 项目主页: https://github.com/yourusername/coffeeviz
- 问题反馈: https://github.com/yourusername/coffeeviz/issues
- 邮箱: support@coffeeviz.com

---

⭐ 如果这个项目对你有帮助，请给我们一个 Star！
