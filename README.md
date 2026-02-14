<div align="center">
  <img src="./coffeeviz-frontend/public/logo.png" alt="CoffeeViz Logo" width="120">
  <h1>CoffeeViz</h1>
</div>

> 数据库架构可视化生成器 - 将 SQL 脚本和数据库连接转换为精美的 ER 图与系统功能结构图

CoffeeViz 是一个强大的数据库架构可视化工具，支持从 SQL 脚本、JDBC 连接或 AI 生成的方式创建数据库 ER 图（实体关系图）和系统功能结构图（Functional Breakdown Structure）。它使用 Mermaid 语法生成图表，并支持导出为 SVG、PNG 等多种格式。

## ✨ 核心特性

- 🔍 **多源解析**：支持 SQL 脚本解析、JDBC 数据库连接、AI 辅助生成
- 🤖 **AI 架构对话引擎**：通过自然语言描述业务需求，自动生成规范化数据库模型
- 🏗️ **系统功能结构图**：从 DDL 或项目文档提取功能模块层级，生成树状结构图
- 🗄️ **多数据库支持**：MySQL、PostgreSQL、Oracle、SQL Server、MariaDB、SQLite、达梦 DM、人大金仓
- 🎨 **可视化渲染**：基于 Mermaid 生成清晰的 ER 图和功能结构图
- 📦 **多格式导出**：支持 SVG、PNG、Mermaid 源码导出（支持动态分辨率）
- 🔐 **用户系统**：完整的用户认证（账号/邮箱验证码/手机验证码/微信扫码）、项目管理、版本控制
- 💳 **订阅系统**：多层级订阅计划（FREE/PRO/TEAM）、配额管理、权限控制
- 👥 **团队协作**：创建团队、邀请成员、共享项目库、成员权限管理
- 💰 **支付集成**：支持微信支付、支付宝、Stripe 多种支付方式
- � **站内通知**：系统通知推送、未读提醒、支持站内信/邮件/短信多渠道
- 📊 **管理后台**：独立的 Admin 管理面板，用户/团队/订阅/支付/邮件/通知全方位管理
- 📈 **数据统计**：API 调用日志、额度使用记录、Dashboard 增长趋势图表
- 🚀 **高性能**：Redis 缓存、异步线程池、API 限流、AI 并发优化（Semaphore 控制）
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
- **SQL 解析**: JSqlParser 4.9 + Druid 1.2.20
- **连接池**: Druid
- **文件存储**: MinIO
- **网关**: Spring Cloud Gateway + Nacos
- **工具类**: Lombok, Hutool, Guava, FastJSON2, BCrypt

### 前端技术栈

- **框架**: Vue 3.4 + Vite 5.0
- **UI 组件**: Naive UI 2.38
- **状态管理**: Pinia 2.1
- **路由**: Vue Router 4.2
- **HTTP 客户端**: Axios 1.6
- **样式**: Tailwind CSS 3.4
- **图表**: Chart.js 4.4（管理后台）
- **Markdown**: Marked 17.0（文档解析）
- **二维码**: QRCode 1.5（微信登录）

### 项目结构

```
coffeeviz/
├── coffeeviz-common/          # 公共模块（常量、异常、工具类）
├── coffeeviz-model/           # 数据模型（预留）
├── coffeeviz-core/            # 核心模块（ER 图模型、渲染引擎）
├── coffeeviz-jdbc/            # JDBC 模块（数据库元数据解析，8 种数据库）
├── coffeeviz-sql/             # SQL 解析模块（JSqlParser + Druid 多解析器组合）
├── coffeeviz-llm/             # LLM 模块（OpenAI API 集成，AI 生成）
├── coffeeviz-export/          # 导出模块（SVG/PNG 生成）
├── coffeeviz-payment/         # 支付模块（微信/支付宝/Stripe）
├── coffeeviz-service/         # 业务服务层（用户、项目、订阅、配额、团队）
├── coffeeviz-web/             # Web 层（Controller、AOP 切面、配置）
├── coffeeviz-gateway/         # API 网关（Nacos 服务发现）
├── coffeeviz-frontend/        # 前端项目（Vue 3 客户端）
├── coffeeviz-admin/           # 管理后台（Vue 3 Admin Dashboard）
└── Database/                  # 数据库初始化脚本
```


## 🚀 快速开始

### 环境要求

- **JDK**: 17+
- **Maven**: 3.6+
- **MySQL**: 8.0+
- **Redis**: 6.0+（缓存 + Sa-Token 会话）
- **Node.js**: 18+（前端开发）
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
mysql -u root -p coffeeviz < Database/coffeeviz.sql
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
    
  data:
    redis:
      host: localhost
      port: 6379
      password: 
      database: 0

# MinIO 配置
minio:
  endpoint: http://localhost:9000
  access-key: minioadmin
  secret-key: minioadmin
  bucket-name: coffeeviz
  region: cn-beijing-1
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
# 客户端（端口 3000）
cd coffeeviz-frontend
npm install
npm run dev

# 管理后台（端口 3001）
cd coffeeviz-admin
npm install
npm run dev
```

### 6. 访问应用

- 客户端：`http://localhost:3000`
- 管理后台：`http://localhost:3001`

## 📖 使用指南

### SQL 解析模式

1. 在主界面选择 "E-R 图生成"
2. 粘贴或上传 SQL DDL 脚本
3. 配置渲染选项（视图模式、表过滤等）
4. 点击 "生成 ER 图"
5. 查看、编辑、导出生成的图表

**支持的 SQL 语法**：MySQL / MariaDB、PostgreSQL、Oracle、SQL Server (T-SQL)、SQLite、达梦 DM、人大金仓 KingbaseES、标准 SQL DDL

> 解析引擎采用三层策略：L1 JSqlParser（精确解析）→ L2 Druid（方言解析）→ L3 正则降级，覆盖绝大多数 DDL 语法。

### JDBC 连接模式

1. 选择 "实时数据库连接"
2. 选择数据库类型（支持 8 种）：

| 数据库 | 驱动状态 | 说明 |
|--------|---------|------|
| MySQL | ✅ 内置 | 支持 v5.7, v8.0 及 MariaDB |
| PostgreSQL | ✅ 内置 | 多 Schema 结构优化 |
| SQL Server | ✅ 内置 | 支持 2012+，Azure SQL |
| MariaDB | ✅ 内置 | 原生驱动，Galera 集群 |
| SQLite | ✅ 内置 | 嵌入式，直接读取 .db 文件 |
| Oracle | ⚠️ 需手动安装 | ojdbc 不在 Maven Central |
| 达梦 DM | ⚠️ 需手动安装 | 从达梦官网下载驱动 |
| 人大金仓 | ⚠️ 需手动安装 | 从金仓官网下载驱动 |

3. 填写 JDBC URL、用户名/密码、Schema（可选）
4. 点击 "测试连接" 验证
5. 点击 "建立同步通道" 生成 ER 图

### AI 生成模式

1. 访问 "AI 自然语言建模" 页面
2. 输入业务需求描述，例如：
   ```
   创建一个电商系统的数据库模型，包含：
   - 用户管理（用户信息、地址）
   - 商品管理（商品、分类、库存）
   - 订单管理（订单、订单明细、支付）
   - 评价系统（商品评价、评分）
   ```
3. 配置生成选项（数据库类型、命名风格、是否生成中间表/索引/注释）
4. 点击 "执行生成"
5. 查看 AI 生成的业务说明、SQL DDL、ER 图预览、AI 建议
6. 保存到架构库或下载 SQL 文件

> AI 功能需要 PRO 或 TEAM 订阅，并在管理后台配置 OpenAI API Key。

### 系统功能结构图

从 DDL 或项目文档中提取功能模块层级，生成树状功能结构图（类似组织结构图/WBS）。

**三种模式**：

- **DDL 解析**（零 AI）：按表名前缀自动聚类为子系统 → 功能模块
  ```
  sys_user, sys_role → 系统管理子系统
  ord_order, ord_item → 订单管理子系统
  ```
- **项目文档**（规则优先，降级 AI）：从 Markdown 标题层级（# → ## → ###）提取结构
- **混合模式**：DDL + 文档合并生成

**配额规则**：
- DDL 模式：消耗 `sql_parse` + `ai_generate`
- 文档模式（规则提取）：免费
- 文档模式（AI 提取）：消耗 `ai_generate`
- 上传文件：消耗 `ai_generate`
- 混合模式：根据输入内容组合扣除

**使用方式**：
1. 访问 "系统架构图" 页面
2. 选择模式，输入 DDL 或 Markdown 文档（也支持上传 txt/md/docx/pdf 文件）
3. 点击 "生成功能结构图"
4. 支持图表/Mermaid 源码/树形三种视图切换
5. 点击 "保存到归档库" 保存到架构库（与 ER 图共享同一归档库）

### 团队协作

1. 在 "团队协作" 页面创建团队
2. 通过邀请链接邀请成员加入
3. 团队成员可共享架构库中的项目
4. 支持 Owner / Member 角色权限控制
5. 团队操作日志审计追踪

### 站内通知

- 点击右上角铃铛图标查看通知下拉列表
- 未读通知显示角标数量，每 60 秒自动刷新
- 点击某条通知弹出详情弹窗，自动标记为已读
- 支持 "全部标为已读" 操作
- 管理员可在后台发送通知（支持站内信/邮件/短信多渠道）

### 额度使用记录

- 在 "额度使用记录" 页面查看配额消耗明细
- 支持按类型筛选（SQL 解析、AI 生成、仓库创建、图表创建）
- 分页展示消耗时间、类型、数量

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

## 🛠️ 管理后台

独立的管理面板（`coffeeviz-admin`，端口 3001），提供以下功能：

- **Dashboard**：总用户数、活跃团队、月收入、AI 调用次数，增长趋势折线图 + 订阅分布饼图
- **用户管理**：查看/搜索用户、重置密码（`PUT /api/admin/users/{userId}/reset-password`，重置为 `123456`）
- **团队管理**：查看/封禁/删除团队，成员统计
- **订阅计划**：管理 FREE/PRO/TEAM 计划配置
- **支付配置**：微信支付/支付宝/Stripe 商户参数配置，支持开关和测试模式
- **邮件配置**：阿里云 DirectMail 集成，邮件模板管理，批量发送，发送日志
- **通知管理**：发送系统通知（支持站内信/邮件/短信渠道），通知历史记录
- **系统设置**：OpenAI API 配置、Mermaid CLI 路径、导出限制等系统参数


## 🔌 API 文档

### 认证接口

```http
POST /api/auth/login                    # 账号密码登录
POST /api/auth/register                 # 用户注册
POST /api/auth/sms/send                 # 发送短信验证码
POST /api/auth/sms/login                # 短信验证码登录
POST /api/auth/email/send-code          # 发送邮箱验证码
GET  /api/auth/wechat/qrcode            # 生成微信登录二维码
GET  /api/auth/wechat/check/{qrCodeId}  # 检查微信扫码状态
GET  /api/auth/userinfo                 # 获取当前用户信息
```

### ER 图生成接口

```http
POST /api/er/parse-sql                  # SQL 解析生成 ER 图
POST /api/er/connect-jdbc               # JDBC 连接生成 ER 图
POST /api/er/test-connection            # 测试数据库连接
```

### 系统功能结构图接口

```http
POST /api/architecture/generate         # 生成功能结构图（DDL/文档/混合模式）
POST /api/architecture/generate/upload  # 上传文件生成（支持 txt/md/docx/pdf）
```

### 项目管理接口

```http
POST   /api/project/create              # 创建项目
POST   /api/project/list                # 查询项目列表
GET    /api/project/detail/{id}         # 查询项目详情
PUT    /api/project/update              # 更新项目
DELETE /api/project/delete/{id}         # 删除项目
```

### 架构库接口

```http
POST   /api/repository/create           # 创建架构库
GET    /api/repository/list             # 架构库列表
GET    /api/repository/{id}             # 架构库详情
POST   /api/diagram/create              # 创建架构图
```

### 订阅管理接口

```http
GET  /api/subscription/plans            # 获取订阅计划列表
GET  /api/subscription/current          # 获取当前用户订阅
POST /api/subscription/cancel           # 取消订阅
GET  /api/subscription/check-feature    # 检查功能权限
```

### 支付接口

```http
POST /api/payment/create                # 创建支付订单
GET  /api/payment/query/{orderNo}       # 查询支付状态
GET  /api/payment/orders                # 获取用户订单列表
POST /api/payment/callback/wechat       # 微信支付回调
POST /api/payment/callback/alipay       # 支付宝回调
POST /api/payment/callback/stripe       # Stripe Webhook
```

### 配额接口

```http
GET /api/quota/usage-logs               # 额度使用记录（分页，支持类型筛选）
```

### 通知接口

```http
GET  /api/notifications                 # 获取站内通知列表（分页）
GET  /api/notifications/unread-count    # 获取未读通知数量
POST /api/notifications/{id}/read       # 标记通知为已读
POST /api/notifications/read-all        # 全部标记为已读
```

### 团队接口

```http
POST   /api/team/create                 # 创建团队
GET    /api/team/list                   # 我的团队列表
GET    /api/team/{id}                   # 团队详情
POST   /api/team/{id}/invite            # 生成邀请链接
POST   /api/team/join/{inviteCode}      # 加入团队
DELETE /api/team/{id}/members/{userId}   # 移除成员
```

### 统计接口

```http
GET /api/diagram/statistics             # 用户统计数据
GET /api/admin/dashboard/stats          # 管理后台 Dashboard 统计
```

### 管理后台接口

```http
GET    /api/admin/users                        # 用户列表
PUT    /api/admin/users/{userId}/reset-password # 重置用户密码
GET    /api/admin/teams                        # 团队列表
POST   /api/admin/notifications/send           # 发送通知
GET    /api/admin/notifications/history        # 通知历史
GET    /api/admin/payment/config               # 获取支付配置
PUT    /api/admin/payment/config               # 更新支付配置
GET    /api/admin/email/config                 # 获取邮件配置
PUT    /api/admin/email/config                 # 更新邮件配置
POST   /api/admin/email/send                   # 发送邮件
```

## 🔧 配置说明

### 应用配置

主配置文件：`coffeeviz-web/src/main/resources/application.yml`

```yaml
server:
  port: 8080

sa-token:
  token-name: Authorization
  timeout: 2592000        # 30天
  is-concurrent: true
  token-style: uuid

mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
  global-config:
    db-config:
      id-type: auto

spring:
  data:
    redis:
      host: localhost
      port: 6379
  cloud:
    nacos:
      discovery:
        server-addr: localhost:8848

minio:
  endpoint: http://localhost:9000
  access-key: minioadmin
  secret-key: minioadmin
  bucket-name: coffeeviz
```

### 系统配置（数据库 sys_config 表）

通过管理后台修改：

- `openai.api.key`: OpenAI API 密钥
- `openai.api.base_url`: OpenAI API 基础 URL
- `openai.model.name`: OpenAI 模型名称
- `mermaid.cli.path`: Mermaid CLI 路径
- `export.max.size`: 导出文件最大大小
- `sql.max.length`: SQL 文本最大长度
- `jdbc.connection.timeout`: JDBC 连接超时时间
- `rate.limit.per.second`: API 限流配置

### 支付配置

支付参数通过管理后台 "支付配置" 页面可视化配置，支持：
- 微信支付：AppID、商户号、API 密钥、回调地址
- 支付宝：AppID、私钥、公钥
- Stripe：API Key、Webhook Secret
- 各渠道独立开关和测试模式

### 邮件配置

通过管理后台 "邮件配置" 页面配置：
- 服务商：阿里云 DirectMail
- AccessKey ID / Secret
- 发件人邮箱和名称
- 邮件模板管理（支持 HTML 模板和变量替换）
- 测试模式

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
cd coffeeviz-frontend && npm run build

# 打包管理后台
cd coffeeviz-admin && npm run build
```

### Docker 部署

```bash
docker build -t coffeeviz:latest .

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
A: Redis 用于缓存和 Sa-Token 会话管理，生产环境必须配置。

**Q: ER 图显示不完整或被截断？**
A: 使用页面右下角的缩放控制按钮调整显示比例，或点击"回到顶部"按钮查看完整内容。

**Q: 短信验证码登录提示"未登录或登录已过期"？**
A: 确保后端已重启，SMS 相关接口已添加到 Sa-Token 白名单。

**Q: 如何测试支付功能？**
A: 在管理后台 "支付配置" 页面开启测试模式，使用支付平台的沙箱环境。

**Q: 订阅过期后会怎样？**
A: 订阅过期后自动降级到 FREE 计划，已创建的项目不会丢失，但受 FREE 计划配额限制。

**Q: 如何查看配额使用情况？**
A: 在客户端 "额度使用记录" 页面查看消耗明细，或在 "订阅计划" 页面查看剩余配额。

**Q: 架构图生成失败提示 "AI 服务不可用"？**
A: 在管理后台检查 OpenAI API Key 配置是否正确，确保 API 可访问。

## 📝 开发规范

- 遵循阿里巴巴 Java 开发手册
- 使用 Lombok 简化代码
- 编写单元测试（目标覆盖率 > 80%）
- 提交前运行 `mvn clean test`
- 使用有意义的提交信息
- `@RequestParam` 需显式指定 `value` 参数名（Spring Boot 3 编译限制）

## 🤝 贡献指南

欢迎贡献代码、报告问题或提出建议！

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request


## 📋 更新日志

### v1.4.0 (2026-02-14)

**新功能**
- 🔔 **站内通知系统**：Header 铃铛下拉通知面板，未读角标，点击查看详情弹窗，全部标为已读
- 📊 **API 调用日志**：AOP 切面自动记录所有 API 调用（方法/路径/状态码/耗时/IP），Dashboard 展示调用趋势
- 📈 **额度使用记录**：独立页面查看配额消耗明细，支持按类型筛选和分页
- 📧 **邮件配置管理**：管理后台集成阿里云 DirectMail，邮件模板管理，批量发送，发送日志
- 💳 **支付配置可视化**：管理后台支付参数配置页面，支持微信/支付宝/Stripe 独立开关
- 🏗️ **架构图配额规则**：按模式精细化扣减配额（DDL/文档/混合/上传文件各有不同规则）
- 🔐 **邮箱验证码注册**：注册时支持邮箱验证码校验

**技术改进**
- ⚡ **AI 并发优化**：新增 `aiTaskExecutor` 线程池（8核/30最大）+ Semaphore(20) 控制并发
- 🔧 **Dashboard 统计**：真实数据驱动（用户数、团队数、月收入、AI 调用次数、增长趋势）
- 🔧 **配额失败不扣费**：QuotaAspect 检查 Result.code != 200 才消耗配额
- 🔧 **AI JSON 修复**：`fixTruncatedJson()` 自动补全 AI 返回的不完整 JSON
- 🔧 **文档 AI 识别增强**：`isProjectDesignDocument()` 自动检测项目设计文档，优先 AI 路径

### v1.3.0 (2026-02-11)

**新功能**
- 🏗️ **系统功能结构图**
  - 从 DDL 按表名前缀自动聚类生成树状功能结构图（零 AI）
  - 从 Markdown 项目文档提取标题层级结构（规则优先，降级 AI）
  - 混合模式：DDL + 文档合并生成
  - 支持图表/Mermaid 源码/树形三种视图切换
  - 保存到归档库（与 ER 图共享同一归档库，PNG 上传 MinIO）
- 🗄️ **多数据库 JDBC 连接**
  - 新增 Oracle、SQL Server、MariaDB、SQLite、达梦 DM、人大金仓支持
  - 按数据库类型适配 catalog/schema 策略
  - SQLite 嵌入式数据库免用户名密码连接
- 📝 **多方言 SQL DDL 解析**
  - Druid SQL Parser 扩展支持 Oracle、SQL Server、SQLite、DM、KingbaseES 方言
  - JSqlParser 放开方言限制，支持所有标准 SQL

**技术改进**
- 🔧 新增 JDBC 驱动：mssql-jdbc、mariadb-java-client、sqlite-jdbc
- 🔧 Oracle/达梦/金仓驱动标记为 optional，附 mvn install 命令
- 🔧 架构图 PNG 截图使用 data URI 避免 tainted canvas 跨域问题

### v1.2.1 (2026-02-08)

**新功能**
- 🎉 **UI 全面升级**：Glassmorphism 设计、全局动画、Settings 双栏布局
- 🎉 **支付与订阅**：年付订阅 (8.3折)、页内弹窗扫码支付 (无跳转)
- 🎉 **登录体验**：支持手机/扫码/账号多 Tab 切换

**技术改进**
- 🔧 引入 Tailwind CSS，优化路由结构
- 🔧 解决 MyBatis-Plus 与 Spring Boot 3 依赖冲突

### v1.2.0 (2026-02-08)

**新功能**
- 🎉 新增支付模块（微信支付、支付宝、Stripe）
- 🎉 新增订阅系统（FREE/PRO/TEAM 三层计划）
- 🎉 新增配额管理系统（repository/diagram/sql_parse/ai_generate）
- 🎉 新增权限控制（@RequireSubscription、@RequireQuota）

**数据库变更**
- ✨ 新增 biz_subscription_plan、biz_user_subscription、biz_payment_order、biz_plan_quota、biz_user_quota_tracking

### v1.1.0 (2026-02-07)

**新功能**
- 🎉 手机验证码登录、微信扫码登录
- 🎉 ER 图智能缩放（默认 80%）、回到顶部按钮
- 🎉 PNG 导出动态分辨率（最高 8K）

**问题修复**
- 🐛 修复 Mermaid 语法错误（DECIMAL 逗号、多注释字符串、PK+FK 冲突）
- 🐛 修复 ER 图顶部内容截断、字段类型不显示长度精度
- 🐛 修复用户信息刷新后丢失（localStorage 持久化）

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
- [Chart.js](https://www.chartjs.org/) - 图表库
- [Tailwind CSS](https://tailwindcss.com/) - CSS 框架

## 📮 联系方式

- 项目主页: https://github.com/yourusername/coffeeviz
- 问题反馈: https://github.com/yourusername/coffeeviz/issues
- 邮箱: eighteenthstuai@gmail.com

---

⭐ 如果这个项目对你有帮助，请给我一个 Star！
