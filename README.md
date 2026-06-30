# Login — 登录注册系统

基于 **Spring Boot 3.5** + **Vue 3** 的前后端分离登录注册系统，支持邮箱验证码注册、JWT 无状态认证、BCrypt 密码加密。

---

## 项目结构

```
Login/
├── login/                        # 后端 Spring Boot 项目
│   ├── pom.xml                   # Maven 依赖配置
│   ├── API.md                    # 前后端接口文档
│   └── src/main/java/com/fjq/login/
│       ├── LoginApplication.java
│       ├── controller/
│       │   └── UserController.java          # REST API 控制器
│       ├── service/
│       │   ├── UserService.java             # 业务接口
│       │   └── impl/
│       │       ├── UserServiceImpl.java      # 用户业务实现
│       │       └── MailService.java          # 邮件发送服务
│       ├── mapper/
│       │   └── UserMapper.java              # MyBatis 注解式 SQL 映射
│       ├── pojo/
│       │   ├── dao/User.java                # 数据库实体
│       │   ├── dto/UserDTO.java             # 注册请求体
│       │   ├── dto/UserLoginDTO.java        # 登录请求体
│       │   ├── dto/EmailRequestDTO.java     # 验证码请求体
│       │   ├── vo/UserVO.java               # 登录响应体
│       │   └── vo/UserLoginVO.java          # 用户信息响应体
│       ├── common/
│       │   ├── api/Result.java              # 统一响应封装
│       │   ├── api/ResultCode.java          # 状态码枚举
│       │   ├── config/
│       │   │   ├── SecurityConfig.java      # Spring Security 配置
│       │   │   └── WebMvcConfiguration.java # 拦截器注册
│       │   ├── context/BaseContext.java     # ThreadLocal 上下文
│       │   ├── exception/
│       │   │   ├── BusinessException.java   # 业务异常
│       │   │   └── GlobalExceptionHandler.java  # 全局异常处理
│       │   ├── interceptor/
│       │   │   └── JwtTokenAdminInterceptor.java  # JWT 鉴权拦截器
│       │   ├── properties/
│       │   │   └── JwtProperties.java       # JWT 配置属性
│       │   └── utils/JwtUtil.java           # JWT 生成/解析工具
│       └── resources/
│           ├── application.yml              # 应用配置
│           └── templates/email-code.html    # 邮件验证码 HTML 模板
│
└── login-frontend/                # 前端 Vue 3 项目
    ├── package.json
    ├── vite.config.js
    └── src/
        ├── main.js
        ├── App.vue
        ├── api/                            # API 封装层
        ├── router/                         # 路由 + 导航守卫
        ├── stores/                         # Pinia 状态管理
        ├── components/                     # 通用 UI 组件
        ├── views/                          # 页面视图
        └── styles/                         # 全局样式 + 设计系统
```

---

## 后端技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| **Java** | 17 | 运行环境 |
| **Spring Boot** | 3.5.16 | 应用框架 |
| **Spring Security** | 6.x（随 Boot 3.5 自动管理） | 安全框架 — CSRF 防护、BCrypt 加密 |
| **Spring Web MVC** | 6.x | REST API 控制器 + 拦截器 |
| **Spring Mail** | 4.x | QQ 邮箱 SMTP 发送验证码 |
| **Spring Data Redis** | 3.x | 验证码缓存（5 分钟 TTL） |
| **Thymeleaf** | 3.x | 邮件 HTML 模板引擎 |
| **MyBatis** | 3.0.5 | 数据库 ORM（注解式 SQL 映射） |
| **MySQL Connector/J** | 9.x | MySQL JDBC 驱动 |
| **jjwt** | 0.12.6 | JWT 令牌签发与校验（HMAC-SHA） |
| **Lombok** | 1.18.x | 编译期代码生成 |
| **Maven** | 3.x | 构建工具 |

---

## 后端架构设计

### 1. 分层架构

```
Controller → Service → Mapper → Database
     ↓                      ↓
 Interceptor            MyBatis
 (JWT 鉴权)           (注解 SQL)
     ↓                      ↓
 BaseContext          MySQL + Redis
 (ThreadLocal)
```

- **Controller 层** — 接收 HTTP 请求，参数校验，调用 Service，封装统一响应 `Result<T>`
- **Service 层** — 核心业务逻辑：验证码生成校验、密码加密、用户注册/登录
- **Mapper 层** — MyBatis 注解式 SQL（`@Select` / `@Insert`），无 XML 配置文件
- **Common 层** — 横切关注点：全局异常处理、JWT 工具、拦截器、统一响应体

### 2. 认证与鉴权（双层防护）

项目采用 **双层安全机制**：Spring Security 过滤链 + 自定义 JWT 拦截器。

#### 第一层：Spring Security

```java
// SecurityConfig.java
http.csrf(AbstractHttpConfigurer::disable)      // 前后端分离，禁用 CSRF
    .authorizeHttpRequests(auth -> auth
        .requestMatchers("/api/v1/auth/**").permitAll()  // 放行 auth 接口
        .anyRequest().authenticated()                     // 其他请求需认证
    );
```

- **CSRF 禁用** — 前后端分离使用 JWT 无状态认证，不存在 CSRF 攻击面
- **PermitAll 开放** — 登录、注册、发送验证码三个接口无需认证
- **BCrypt 加密** — `BCryptPasswordEncoder`（默认强度 10），不可逆加密存储密码

#### 第二层：JWT 拦截器

```java
// JwtTokenAdminInterceptor.java — preHandle()
String token = request.getHeader(tokenName);      // 从请求头读取 token
Claims claims = jwtUtil.parseJWT(token);          // jjwt 0.12 校验签名 + 过期
BaseContext.setCurrent(claims.get("email"));      // 写入 ThreadLocal
```

- **Token 名称可配** — 通过 `jwt.token-name` 动态配置（当前为 `token`）
- **ThreadLocal 上下文** — 校验通过后将用户邮箱写入 `BaseContext`，后续 Controller 通过 `BaseContext.getCurrent()` 直接获取
- **请求结束后清理** — `afterCompletion` 中主动调用 `BaseContext.removeCurrent()`，防止内存泄漏
- **校验失败** — 返回 HTTP 401 + JSON `{"code":401, "message":"未登录或Token已失效，请重新登录"}`

#### 拦截器白名单

| 接口 | 说明 | 需要 Token |
|------|------|------------|
| `/api/v1/auth/login` | 登录 | 否 |
| `/api/v1/auth/register` | 注册 | 否 |
| `/api/v1/auth/send-code` | 发送验证码 | 否 |
| `/api/v1/auth/me` | 获取用户信息 | 是 |

### 3. JWT 令牌机制

```java
// JwtUtil.java
// 初始化 — @PostConstruct 将配置中的密钥字符串转为 HMAC-SHA 密钥对象
this.key = Keys.hmacShaKeyFor(secretString.getBytes(StandardCharsets.UTF_8));

// 生成 — 自定义 claims + 签发时间 + 过期时间 + HMAC-SHA 签名
Jwts.builder()
    .claims(claims)          // email, username
    .issuedAt(now)
    .expiration(exp)         // 默认 2 小时 (7200000ms)
    .signWith(key)
    .compact();

// 校验 — jjwt 0.12.x 自动校验签名和过期
Jwts.parser().verifyWith(key).build()
    .parseSignedClaims(token).getPayload();
```

- **签名算法** — HMAC-SHA（`Keys.hmacShaKeyFor`），密钥由 `application.yml` 中的 `jwt.secret-key` 配置
- **Token Payload** — `{ email, username, iat, exp }`
- **有效期** — 2 小时（`jwt.ttl: 7200000`）

### 4. 邮箱验证码流程

```
用户输入邮箱 → POST /send-code → 检查邮箱是否已注册
    → 生成 6 位随机码 → 存入 Redis (key=login:code:{email}, TTL=5min)
    → Thymeleaf 渲染 HTML 模板 → JavaMailSender 发送 QQ 邮箱 SMTP
```

```java
// UserServiceImpl.sendCode()
String code = String.format("%06d", new Random().nextInt(1000000));
redisTemplate.opsForValue().set(CODE_PREFIX + email, code, 5, TimeUnit.MINUTES);
mailService.sendMail(email, code);
```

```java
// MailService.sendMail()
MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
Context context = new Context();
context.setVariable("code", code);
String htmlContent = templateEngine.process("email-code", context);
helper.setText(htmlContent, true);
```

- **Redis 验证码** — key 前缀 `login:code:`，5 分钟过期，注册成功后主动删除
- **邮件模板** — Thymeleaf 渲染 `email-code.html`，支持 HTML 富文本样式
- **QQ 邮箱 SMTP** — SSL 连接 `smtp.qq.com:465`，使用授权码（非登录密码）

### 5. 统一响应格式

```java
// Result<T> — 泛型统一响应体
public static <T> Result<T> success(T data);       // 成功 + 数据
public static <T> Result<T> error(Integer code, String message);  // 失败 + 自定义错误码

// ResultCode — 状态码枚举
SUCCESS(200), ERROR(500), PARAM_ERROR(400), UNAUTHORIZED(401), FORBIDDEN(403)
```

**成功响应：**
```json
{ "code": 200, "message": "操作成功", "data": "验证码已发送" }
```

**失败响应（错误码透传）：**
```json
{ "code": 409, "message": "该邮箱已被注册", "data": null }
```

### 6. 全局异常处理

```java
// GlobalExceptionHandler.java
@ExceptionHandler(BusinessException.class)
public Result<Void> handleBusinessException(BusinessException e) {
    return Result.error(e.getCode(), e.getMessage());  // 透传自定义错误码
}

@ExceptionHandler(Exception.class)
public Result<Void> handleException(Exception e) {
    return Result.error(ResultCode.ERROR);  // 兜底，不暴露内部信息给前端
}
```

- **BusinessException** — 业务异常携带精确错误码（400/401/409），通过 `GlobalExceptionHandler` 透传到前端
- **Exception 兜底** — 未知系统异常统一返回 500，**绝不暴露**异常细节给前端（防止泄露 SQL 结构、数据库账号等敏感信息）

### 7. 数据库设计

```sql
-- user 表结构（由 MyBatis @Insert 注释推断）
CREATE TABLE user (
    id         INT PRIMARY KEY AUTO_INCREMENT,
    username   VARCHAR(50)  NOT NULL,
    password   VARCHAR(255) NOT NULL,  -- BCrypt 加密后的密文
    email      VARCHAR(100) NOT NULL UNIQUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

- MyBatis `map-underscore-to-camel-case: true` 自动完成下划线（`created_at`）到驼峰（`createdAt`）的字段映射
- MyBatis 日志输出通过 `StdOutImpl` 将 SQL 打印到控制台

---

## 快速开始

### 前置环境

| 依赖 | 版本要求 |
|------|----------|
| JDK | 17+ |
| Maven | 3.6+ |
| MySQL | 8.0+ |
| Redis | 6.0+ |
| Node.js | 18+（仅前端） |

### 1. 启动后端

```bash
# 1. 初始化数据库
mysql -u root -p < create
CREATE DATABASE test;
USE test;
CREATE TABLE user (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

# 2. 修改 application.yml 中的数据库、Redis、邮箱配置

# 3. 启动 Spring Boot
cd login
mvn spring-sprinot:run
```

后端默认运行在 `http://localhost:8080`。

### 2. 启动前端

```bash
cd login-frontend
npm install
npm run dev
```

前端默认运行在 `http://localhost:5173`，API 请求通过 Vite 代理转发到后端 8080 端口。

### 3. 可用接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/v1/auth/send-code` | 发送邮箱验证码 |
| POST | `/api/v1/auth/register` | 用户注册 |
| POST | `/api/v1/auth/login` | 用户登录 |
| GET | `/api/v1/auth/me` | 获取当前用户信息（需 Token） |

详细接口文档参见 [API.md](login/API.md)。

---

## 开发说明

- **密码加密** — 用户注册时密码经 `BCryptPasswordEncoder` 加密后存储，登录时使用 `passwordEncoder.matches()` 进行密文比对
- **验证码安全** — 注册成功后 Redis 中的验证码立即删除，防止重复使用
- **Token 传递** — 前端通过请求头 `token` 传递 JWT，与后端 `jwt.token-name` 配置一致
- **MyBatis 日志** — 开发环境通过 `StdOutImpl` 在控制台输出 SQL，生产环境应关闭
