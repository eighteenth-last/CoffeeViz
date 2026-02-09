<div align="center">
  <img src="./coffeeviz-frontend/public/logo.png" alt="CoffeeViz Logo" width="120">
  <h1>CoffeeViz</h1>
</div>

> 数据库架构可视化生成器 - 将 SQL 脚本和数据库连接转换为精美的 ER 图

CoffeeViz 是一个强大的数据库架构可视化工具，支持从 SQL 脚本、JDBC 连接或 AI 生成的方式创建数据库 ER 图（实体关系图）。它使用 Mermaid 语法生成图表，并支持导出为 SVG、PNG 等多种格式。

## ✨ 核心特性

- 🔍 **多源解析**：支持 SQL 脚本解析、JDBC 数据库连接、AI 辅助生成
- 🤖 **AI 架构对话引擎**：通过自然语言描述业务需求，自动生成规范化数据库模型（NEW!）
- 🎨 **可视化渲染**：基于 Mermaid 生成清晰的 ER 图
- 📦 **多格式导出**：支持 SVG、PNG、Mermaid 源码导出（支持动态分辨率）
- 🔐 **用户系统**：完整的用户认证（账号/手机验证码/微信扫码）、项目管理、版本控制
- 💳 **订阅系统**：多层级订阅计划（FREE/PRO/TEAM）、配额管理、权限控制
- 💰 **支付集成**：支持微信支付、支付宝、Stripe 多种支付方式
- 🚀 **高性能**：Redis 缓存、异步处理、API 限流
- 🎯 **智能推断**：自动识别表关系、外键约束
- 📱 **现代化 UI**：基于 Vue 3 + Naive UI + Tailwind CSS 的 Glassmorphism (磨砂玻璃) 风格界面
- 🔧 **完整字段信息**：显示字段类型、长度、精度、约束、注释
- 📊 **智能缩放**：ER 图默认 80% 缩放，支持自定义缩放和回到顶部

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
├── coffeeviz-payment/         # 支付模块（微信/支付宝/Stripe）
├── coffeeviz-service/         # 业务服务层（用户、项目、订阅、配额）
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
mysql -u root -p coffeeviz < coffeeviz-web/src/main/resources/coffeeviz.sql

# 初始化订阅系统（可选）
mysql -u root -p coffeeviz < coffeeviz-web/src/main/resources/subscription_tables.sql
```

**默认管理员账号**：
- 用户名: `admin`
- 密码: `admin123`

**订阅计划**：
- FREE（社区版）：免费，最多 3 个项目
- PRO（专业版）：¥29/月，无限项目 + AI 功能
- TEAM（团队版）：¥99/团队/月，团队协作 + 优先支持

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

# 支付配置（可选，用于订阅功能）
payment:
  wechat:
    app-id: ${WECHAT_APP_ID:}
    mch-id: ${WECHAT_MCH_ID:}
    api-key: ${WECHAT_API_KEY:}
  alipay:
    app-id: ${ALIPAY_APP_ID:}
    private-key: ${ALIPAY_PRIVATE_KEY:}
  stripe:
    api-key: ${STRIPE_API_KEY:}
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

### AI 生成模式（NEW!）

1. 访问 "AI 架构对话引擎" 页面
2. 输入业务需求描述，例如：
   ```
   创建一个电商系统的数据库模型，包含：
   - 用户管理（用户信息、地址）
   - 商品管理（商品、分类、库存）
   - 订单管理（订单、订单明细、支付）
   - 评价系统（商品评价、评分）
   ```
3. 配置生成选项：
   - 数据库类型（MySQL/PostgreSQL）
   - 命名风格（snake_case/camelCase）
   - 是否生成中间表、索引、注释
4. 点击 "执行生成"
5. 查看 AI 生成的：
   - 业务说明
   - SQL DDL
   - ER 图预览
   - AI 建议
6. 保存到架构库或下载 SQL 文件

**注意**：AI 功能需要 PRO 或 TEAM 订阅，并配置 OpenAI API Key。详见 [AI 功能使用指南](AI_FEATURE_GUIDE.md)

### 项目管理

- **创建项目**：保存生成的 ER 图为项目
- **版本控制**：每次更新可创建新版本
- **历史回溯**：查看和恢复历史版本
- **导出分享**：导出 Mermaid 源码、SVG、PNG

### 订阅管理

#### 订阅计划对比

| 功能 | FREE | PRO | TEAM |
|------|------|-----|------|
| 价格 | 免费 | ¥29/月 或 ¥290/年 | ¥99/团队/月 或 ¥990/团队/年 |
| 项目数量 | 3 个 | 无限 | 无限 |
| SQL 导入 | MySQL | 多数据库 | 多数据库 |
| JDBC 连接 | ❌ | ✅ | ✅ |
| AI 辅助 | ❌ | ✅ | ✅ |
| 导出功能 | ✅ | ✅ | ✅ |
| 团队协作 | ❌ | ❌ | ✅ |
| 技术支持 | 社区 | 优先 | 7x24 |

#### 配额限制

- **repository**: 仓库数量（FREE: 3，PRO/TEAM: 无限）
- **diagram**: 每月图表生成次数
- **sql_parse**: 每日 SQL 解析次数
- **ai_generate**: 每月 AI 生成次数（仅 PRO/TEAM）

#### 升级订阅

1. 访问"订阅管理"页面
2. 选择订阅计划（月付/年付）
3. 选择支付方式（微信/支付宝/Stripe）
4. 完成支付后自动激活

## 🔌 API 文档

### 认证接口

#### 用户登录（账号密码）
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}
```

#### 发送短信验证码
```http
POST /api/auth/sms/send
Content-Type: application/json

{
  "phone": "13800138000"
}
```

#### 短信验证码登录
```http
POST /api/auth/sms/login
Content-Type: application/json

{
  "phone": "13800138000",
  "code": "123456"
}
```

#### 生成微信登录二维码
```http
GET /api/auth/wechat/qrcode
```

#### 检查微信扫码状态
```http
GET /api/auth/wechat/check/{qrCodeId}
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

#### 获取当前用户信息
```http
GET /api/auth/userinfo
Authorization: {token}
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

### 订阅管理接口

#### 获取订阅计划列表
```http
GET /api/subscription/plans
```

#### 获取当前用户订阅
```http
GET /api/subscription/current
Authorization: {token}
```

#### 取消订阅
```http
POST /api/subscription/cancel?reason=不需要了
Authorization: {token}
```

#### 检查功能权限
```http
GET /api/subscription/check-feature?feature=ai
Authorization: {token}
```

### 支付接口

#### 创建支付订单
```http
POST /api/payment/create
Authorization: {token}
Content-Type: application/json

{
  "planId": 2,
  "billingCycle": "monthly",
  "paymentMethod": "wechat"
}
```

#### 查询支付状态
```http
GET /api/payment/query/{orderNo}
Authorization: {token}
```

#### 获取用户订单列表
```http
GET /api/payment/orders
Authorization: {token}
```

### 统计接口

#### 获取用户统计数据
```http
GET /api/diagram/statistics
Authorization: {token}
```

**响应示例**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "totalTables": 42,
    "totalRelations": 18
  }
}
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
- `openai.api.base_url`: OpenAI API 基础 URL
- `openai.model.name`: OpenAI 模型名称
- `mermaid.cli.path`: Mermaid CLI 路径
- `export.max.size`: 导出文件最大大小
- `sql.max.length`: SQL 文本最大长度
- `jdbc.connection.timeout`: JDBC 连接超时时间
- `rate.limit.per.second`: API 限流配置

### 支付配置

支付配置通过环境变量设置：

```bash
# 微信支付
export WECHAT_APP_ID=your_app_id
export WECHAT_MCH_ID=your_mch_id
export WECHAT_API_KEY=your_api_key

# 支付宝
export ALIPAY_APP_ID=your_app_id
export ALIPAY_PRIVATE_KEY=your_private_key
export ALIPAY_PUBLIC_KEY=your_public_key

# Stripe
export STRIPE_API_KEY=sk_test_xxx
export STRIPE_WEBHOOK_SECRET=whsec_xxx
```

### 用户默认配置

新注册用户的默认显示名称格式：**神阁绘 + 10位UUID**

示例：`神阁绘a1b2c3d4e5`

用户可以在"系统参数"页面修改个人资料，包括：
- 显示名称
- 电子邮箱
- 手机号码
- 密码

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

**Q: ER 图显示不完整或被截断？**
A: 使用页面右下角的缩放控制按钮调整显示比例，或点击"回到顶部"按钮查看完整内容。

**Q: 字段类型显示不完整（如 VARCHAR 没有长度）？**
A: 已修复，现在会正确显示完整类型信息（如 VARCHAR(100)、DECIMAL(10,2)）。

**Q: 短信验证码登录提示"未登录或登录已过期"？**
A: 确保后端已重启，SMS 相关接口已添加到 Sa-Token 白名单。

**Q: 用户信息显示为邮箱地址而不是友好名称？**
A: 执行 `UPDATE_DISPLAY_NAME.sql` 更新现有用户的 displayName 字段。

**Q: 如何测试支付功能？**
A: 使用支付平台的沙箱环境进行测试，配置沙箱密钥即可。

**Q: 订阅过期后会怎样？**
A: 订阅过期后会自动降级到 FREE 计划，已创建的项目不会丢失，但会受到 FREE 计划的配额限制。

**Q: 如何查看配额使用情况？**
A: 在"订阅管理"页面可以查看当前配额使用情况和剩余配额。

### 已知问题修复记录

### v1.2.1 (2026-02-08)

**新功能与改进**
- 🎨 **UI 全面升级**
  - 采用 **Glassmorphism (磨砂玻璃)** 设计语言
  - 优化全局过渡动画与交互细节
  - 重构登录/注册页面，支持多方式切换（手机/扫码/账号）
  - 优化 Settings 页面布局，支持响应式双栏显示
- 💰 **支付与订阅增强**
  - 新增 **年付订阅** 选项（8.3折优惠）
  - 优化收银台体验，支持 **页内弹窗扫码支付**，无需跳转
  - 统一订阅卡片视觉风格，增加主题适配边框
- 🔧 **系统优化**
  - 移除冗余的系统集成配置
  - 修复 MyBatis-Plus 与 Spring Boot 3 兼容性问题
  - 优化前端路由结构与页面加载性能

### v1.2.0 (2026-02-08)
- ✅ 新增支付模块（微信支付、支付宝、Stripe）
- ✅ 新增订阅系统（FREE/PRO/TEAM 三层计划）
- ✅ 新增配额管理系统
- ✅ 新增权限控制（@RequireSubscription、@RequireQuota）
- ✅ 新增支付订单管理
- ✅ 新增订阅 API 接口
- ✅ 完善数据库表结构（订阅、支付、配额）

#### v1.1.0 (2026-02-07)
- ✅ 修复字段类型不显示长度和精度的问题
- ✅ 修复 Mermaid 语法错误（DECIMAL 逗号、多注释字符串、PK+FK 冲突）
- ✅ 修复 ER 图顶部内容被截断的问题
- ✅ 添加智能缩放功能（默认 80%）和回到顶部按钮
- ✅ 修复数据库连接缺少 dbType 参数的问题
- ✅ 优化 PNG 导出质量（动态分辨率，最高支持 8K）
- ✅ 修复用户信息刷新后丢失的问题（localStorage 持久化）
- ✅ 修复登录按钮样式问题
- ✅ 实现手机验证码登录功能（演示版本）
- ✅ 统一新用户默认显示名称为"神阁绘+UUID"格式
- ✅ 修复系统参数页面数据显示错误
- ✅ 优化用户信息弹窗显示

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
- 邮箱: eighteenthstuai@gmail.com

## 📋 更新日志

### v1.2.1 (2026-02-08)

**新功能**
- 🎉 **UI 全面升级**：Glassmorphism 设计、全局动画、Settings 双栏布局
- 🎉 **支付与订阅**：年付订阅 (8.3折)、页内弹窗扫码支付 (无跳转)
- 🎉 **登录体验**：支持手机/扫码/账号多 Tab 切换

**技术改进**
- 🔧 **前端架构**：引入 Tailwind CSS，优化路由结构
- 🔧 **兼容性修复**：解决 MyBatis-Plus 与 Spring Boot 3 依赖冲突

### v1.2.0 (2026-02-08)

**新功能**
- 🎉 新增支付模块（coffeeviz-payment）
  - 支持微信支付、支付宝、Stripe
  - 统一支付接口和回调处理
  - 支付订单管理
- 🎉 新增订阅系统
  - 三层订阅计划（FREE/PRO/TEAM）
  - 订阅管理（创建、升级、取消、续费）
  - 订阅权限检查
- 🎉 新增配额管理系统
  - 多种配额类型（repository/diagram/sql_parse/ai_generate）
  - 自动重置机制（daily/monthly/yearly/never）
  - 配额使用统计
- 🎉 新增权限控制
  - @RequireSubscription 注解（订阅权限）
  - @RequireQuota 注解（配额检查）
  - AOP 切面拦截

**数据库变更**
- ✨ 新增 biz_subscription_plan 表（订阅计划）
- ✨ 新增 biz_user_subscription 表（用户订阅）
- ✨ 新增 biz_payment_order 表（支付订单）
- ✨ 新增 biz_usage_quota 表（使用配额）

**API 接口**
- ✨ 新增订阅管理接口（/api/subscription/*）
- ✨ 新增支付接口（/api/payment/*）
- ✨ 新增支付回调接口（微信/支付宝/Stripe）

**技术改进**
- 🔧 模块化支付处理（Handler 模式）
- 🔧 支付配置管理（PaymentConfig）
- 🔧 订阅服务实现（SubscriptionService）
- 🔧 配额服务实现（QuotaService）
- 🔧 支付订单服务（PaymentOrderService）

**文档**
- 📚 新增《支付与订阅模块完整文档》
- 📚 更新 README 添加订阅说明
- 📚 更新 API 文档

### v1.1.0 (2026-02-07)

**新功能**
- 🎉 新增手机验证码登录功能（演示版本，支持自动注册）
- 🎉 新增微信扫码登录支持
- 🎉 新增 ER 图智能缩放功能（默认 80%，支持自定义）
- 🎉 新增"回到顶部"按钮
- 🎉 新增用户信息弹窗快捷操作（设置、退出）

**优化改进**
- ✨ 优化字段类型显示，完整显示长度和精度（VARCHAR(100)、DECIMAL(10,2)）
- ✨ 优化 PNG 导出质量，支持动态分辨率（最高 8K）
- ✨ 统一新用户默认显示名称为"神阁绘+10位UUID"格式
- ✨ 优化系统参数页面，支持修改显示名称
- ✨ 优化用户信息持久化，刷新页面不丢失
- ✨ 优化登录按钮样式

**问题修复**
- 🐛 修复 Mermaid 语法错误（DECIMAL 逗号、多注释字符串、PK+FK 冲突）
- 🐛 修复 ER 图顶部内容被截断的问题
- 🐛 修复数据库连接缺少 dbType 参数
- 🐛 修复用户信息刷新后显示为"User"的问题
- 🐛 修复系统参数页面数据显示错误
- 🐛 修复用户信息弹窗显示不全的问题

**技术改进**
- 🔧 添加 SMS 相关接口到 Sa-Token 白名单
- 🔧 优化 UserService，支持短信验证码登录
- 🔧 优化 WechatService，统一用户创建逻辑
- 🔧 添加 localStorage 持久化用户信息
- 🔧 优化前端响应式布局

---

⭐ 如果这个项目对你有帮助，请给我一个 Star！
