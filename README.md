# Login — 登录注册系统

基于 **Spring Boot 3.5** + **Vue 3** 的前后端分离登录注册系统，支持邮箱验证码注册、JWT 无状态认证、BCrypt 密码加密、Redis 防刷限流、异步邮件发送。

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
│       │       ├── UserServiceImpl.java      # 用户业务实现（含防刷）
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
│       │   │   ├── AsyncConfig.java         # 异步邮件线程池配置
│       │   │   └── SecurityConfig.java      # Spring Security 配置
│       │   ├── context/BaseContext.java     # ThreadLocal 上下文
│       │   ├── exception/
│       │   │   ├── BusinessException.java   # 业务异常
│       │   │   └── GlobalExceptionHandler.java  # 全局异常处理
│       │   ├── filter/
│       │   │   └── JwtAuthenticationFilter.java  # JWT 认证过滤器
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
| **Spring Security** | 6.x | 安全框架 — Filter Chain 认证、BCrypt 加密、无状态会话 |
| **Spring Web MVC** | 6.x | REST API 控制器 |
| **Spring Mail** | 4.x | QQ 邮箱 SMTP SSL 发送验证码 |
| **Spring Data Redis** | 3.x | 验证码缓存 + 防刷限流 Key |
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
     ↓                     ↓
JwtAuthenticationFilter   MyBatis
(OncePerRequestFilter)   (注解 SQL)
     ↓                     ↓
 BaseContext +           MySQL + Redis
 SecurityContextHolder
```

- **Controller 层** — 接收 HTTP 请求，参数校验，调用 Service，封装统一响应 `Result<T>`
- **Service 层** — 核心业务逻辑：防刷校验、验证码生成/校验、密码加密、用户注册/登录
- **Mapper 层** — MyBatis 注解式 SQL（`@Select` / `@Insert`），无 XML 配置文件
- **Filter 层** — `JwtAuthenticationFilter` 替代传统拦截器，与 Spring Security 深度集成

---

### 2. 认证与鉴权（Spring Security + JWT Filter）

项目将 JWT 鉴权从传统的 `HandlerInterceptor` 升级为 Spring Security 的 `OncePerRequestFilter`，实现与安全框架的深度集成。

#### 架构对比

| 特性 | 旧方案（Interceptor） | 新方案（Filter） |
|------|----------------------|-------------------|
| 集成方式 | 拦截 Controller 方法 | 插入 Security Filter Chain |
| Security 上下文 | 不同步 | 自动注入 `SecurityContextHolder` |
| 访问控制 | 手动判断路径白名单 | Security `permitAll()` / `authenticated()` 声明式配置 |
| 认证对象 | 无 | `UsernamePasswordAuthenticationToken` |
| 会话管理 | 无 | `SessionCreationPolicy.STATELESS` 显式声明无状态 |

#### SecurityConfig — 声明式安全策略

```java
// SecurityConfig.java
http
    .csrf(AbstractHttpConfigurer::disable)       // 前后端分离，禁用 CSRF
    .sessionManagement(session ->
        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))  // 无状态会话
    .authorizeHttpRequests(auth -> auth
        .requestMatchers(                         // 精确白名单
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/auth/send-code"
        ).permitAll()
        .anyRequest().authenticated()
    )
    .addFilterBefore(jwtAuthFilter,               // JWT 过滤器
        UsernamePasswordAuthenticationFilter.class);
```

#### JwtAuthenticationFilter — 核心认证逻辑

```java
// JwtAuthenticationFilter.java extends OncePerRequestFilter
// 每次请求只执行一次，确保 JWT 校验的确定性

@Override
protected void doFilterInternal(HttpServletRequest request, ...) {

    // 1. 从请求头动态读取 Token（名称由 jwt.token-name 配置）
    String token = request.getHeader(jwtProperties.getTokenName());

    // 2. 无 Token → 放行（由 Security 后续拦截器决定是否拒绝）
    if (!StringUtils.hasText(token)) {
        filterChain.doFilter(request, response);
        return;
    }

    try {
        // 3. jjwt 0.12.x 校验签名 + 过期时间
        Claims claims = jwtUtil.parseJWT(token);

        // 4. 将用户信息注入 Spring Security 上下文
        UsernamePasswordAuthenticationToken authToken =
            new UsernamePasswordAuthenticationToken(
                email, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(authToken);

        // 5. 兼容旧代码：写入 ThreadLocal（BaseContext）供 Controller 使用
        BaseContext.setCurrent(email);

        filterChain.doFilter(request, response);

    } catch (Exception ex) {
        // 6. Token 无效 → 401 + JSON 响应，请求被彻底拦截
        response.setStatus(401);
        response.getWriter().write(
            "{\"code\":401, \"message\":\"未登录或Token已失效，请重新登录\"}");
        // 不调用 filterChain.doFilter — 请求在此终止
    } finally {
        // 7. 请求结束后清理 ThreadLocal，防止内存泄漏
        BaseContext.removeCurrent();
    }
}
```

**设计要点：**

- **OncePerRequestFilter** — 保证单次请求只过滤一次，避免重复校验
- **双重上下文同步** — 同时维护 `SecurityContextHolder`（Spring Security 标准）和 `BaseContext`（旧代码兼容）
- **finally 自动清理** — `BaseContext.removeCurrent()` 在 `finally` 块中执行，无论成功或异常都会清理
- **Token 校验失败直接拦截** — 不调用 `filterChain.doFilter()`，请求在此层终止，不再进入 Controller

---

### 3. JWT 令牌机制

```java
// JwtUtil.java
// 初始化 — @PostConstruct 将配置中的密钥字符串转为 HMAC-SHA 密钥对象
@PostConstruct
public void init() {
    this.key = Keys.hmacShaKeyFor(
        secretString.getBytes(StandardCharsets.UTF_8));
}

// 生成 — 自定义 claims + 签发时间 + 过期时间 + HMAC-SHA 签名
public String createJWT(Map<String, Object> claims) {
    return Jwts.builder()
        .claims(claims)                        // email, username
        .issuedAt(new Date())
        .expiration(new Date(now + ttl))       // 默认 2 小时
        .signWith(key)
        .compact();
}

// 校验 — jjwt 0.12.x 自动校验签名合法性 + 过期时间
public Claims parseJWT(String token) {
    return Jwts.parser()
        .verifyWith(key)
        .build()
        .parseSignedClaims(token)
        .getPayload();
}
```

| 配置项 | 值 | 说明 |
|--------|-----|------|
| 签名算法 | HMAC-SHA | `Keys.hmacShaKeyFor()` |
| 密钥 | `jwt.secret-key` | application.yml 可配 |
| 有效期 | `jwt.ttl` | 默认 7200000ms（2 小时） |
| 请求头名称 | `jwt.token-name` | 当前为 `token` |

---

### 4. 防刷机制（Redis 原子操作）

`sendCode` 接口采用 **Redis `setIfAbsent` 原子指令** 实现 60 秒发送间隔限制，防止恶意高频调用消耗邮件配额。

#### 实现原理

```java
// UserServiceImpl.sendCode() — 防刷段
String limitKey = "login:limit:" + email;

// setIfAbsent：原子操作，无竞态条件
// 成功 → Key 不存在，本次设置成功，正常发送
// 失败 → Key 已存在，60 秒内已有发送记录，拒绝本次请求
Boolean isAbsent = redisTemplate.opsForValue()
    .setIfAbsent(limitKey, "1", 60, TimeUnit.SECONDS);

if (Boolean.FALSE.equals(isAbsent)) {
    // 查询剩余冷却时间，返回友好提示
    Long expire = redisTemplate.getExpire(limitKey, TimeUnit.SECONDS);
    long waitSeconds = (expire != null && expire > 0) ? expire : 60;
    throw new BusinessException(429,
        "发送过于频繁，请 " + waitSeconds + " 秒后再试");
}
```

#### 为什么用 `setIfAbsent` 而非 `INCR + EXPIRE`？

| 方案 | 原子性 | 内存占用 | 竞态条件 |
|------|--------|----------|----------|
| `setIfAbsent` | ✅ 单条指令 | 每个邮箱 1 个 Key | 无 |
| `INCR` + `EXPIRE` | ❌ 两条指令 | 每个邮箱 1 个 Key | `INCR` 后 `EXPIRE` 前崩溃 → Key 永不过期 |

#### 冷启动示例

```
T+0s   — 用户点击"发送验证码"
         setIfAbsent("login:limit:user@qq.com", "1", 60s) → true
         邮件正常发送

T+10s  — 用户再次点击
         setIfAbsent("login:limit:user@qq.com", "1", 60s) → false
         getExpire → 50s
         返回 429 "发送过于频繁，请 50 秒后再试"

T+61s  — Key 已过期，再次点击
         setIfAbsent → true
         邮件正常发送
```

#### 前端适配

前端在收到 `429` 错误码时自动解析剩余秒数并启动本地倒计时，与 Redis 锁保持一致（详见前端代码中 `RegisterView.vue` 的 `handleSendCode` 方法）。

---

### 5. 异步邮件发送（线程池）

为避免 SMTP 连接阻塞主请求线程、提升接口响应速度，邮件发送通过自定义线程池异步执行。

#### AsyncConfig — 线程池配置

```java
// AsyncConfig.java
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "mailExecutor")
    public Executor mailExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);            // 核心线程：空闲时保持 5 个
        executor.setMaxPoolSize(10);            // 最大线程：高峰扩容至 10 个
        executor.setQueueCapacity(100);         // 缓冲队列：最多 100 个任务排队
        executor.setKeepAliveSeconds(60);       // 超核心线程 60s 空闲后回收
        executor.setThreadNamePrefix("mail-async-");  // 线程名便于日志追踪

        // 拒绝策略：队列满 + 线程满 → 由调用方主线程处理（保证邮件不丢失）
        executor.setRejectedExecutionHandler(
            new ThreadPoolExecutor.CallerRunsPolicy());

        return executor;
    }
}
```

#### 线程池参数设计

| 参数 | 值 | 设计原因 |
|------|-----|----------|
| corePoolSize | 5 | 常规负载下复用 5 个线程，避免频繁创建/销毁 |
| maxPoolSize | 10 | 突发流量时最多 10 个线程并发发邮件 |
| queueCapacity | 100 | 高峰期最多 100 封邮件排队等待 |
| 拒绝策略 | CallerRunsPolicy | 极端情况由主线程兜底，邮件不丢失 |
| keepAliveSeconds | 60 | 空闲超核心线程 1 分钟后回收，释放资源 |

#### 与传统同步发送对比

```
同步发送：
  用户请求 → 校验通过 → 存储验证码 → 等待 SMTP 连接 → 等待发送完成 → 响应"已发送"
                                              ↑ 可能耗时 1-3 秒

异步发送：
  用户请求 → 校验通过 → 存储验证码 → 提交任务到线程池 → 立即响应"已发送"
                                      ↓
                              mail-async-1 线程异步发送（用户已收到响应）
```

---

### 6. 邮箱验证码流程

```
用户输入邮箱 → POST /send-code
    ├── 防刷检查（setIfAbsent，60 秒限制）
    ├── 检查邮箱是否已注册（409）
    ├── 生成 6 位随机码
    ├── 存入 Redis (key=login:code:{email}, TTL=5min)
    └── 提交异步任务到 mailExecutor 线程池
           └── Thymeleaf 渲染 HTML 模板 → QQ SMTP SSL 发送
```

```java
// MailService.sendMail() — 邮件渲染与发送
MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
Context context = new Context();
context.setVariable("code", code);                    // 注入验证码变量
String htmlContent = templateEngine.process("email-code", context);
helper.setText(htmlContent, true);                    // true = HTML 格式
mailSender.send(message);
```

| 组件 | 说明 |
|------|------|
| Redis Key | `login:code:{email}`，5 分钟 TTL |
| 限流 Key | `login:limit:{email}`，60 秒 TTL |
| SMTP | QQ 邮箱 `smtp.qq.com:465` SSL 直连 |
| 模板 | Thymeleaf 渲染 `email-code.html` |
| 异常处理 | `MessagingException` → `BusinessException(500)`，不将 SMTP 错误暴露给前端 |

---

### 7. 统一响应格式

```java
// Result<T> — 泛型统一响应体
public static <T> Result<T> success(T data);                          // 成功 + 数据
public static <T> Result<T> error(Integer code, String message);      // 失败 + 自定义错误码
```

**成功响应：**
```json
{ "code": 200, "message": "操作成功", "data": "验证码已发送" }
```

**防刷限流响应：**
```json
{ "code": 429, "message": "发送过于频繁，请 42 秒后再试", "data": null }
```

**业务错误响应：**
```json
{ "code": 409, "message": "该邮箱已被注册", "data": null }
```

---

### 8. 全局异常处理

```java
// GlobalExceptionHandler.java
@ExceptionHandler(BusinessException.class)
public Result<Void> handleBusinessException(BusinessException e) {
    return Result.error(e.getCode(), e.getMessage());   // 透传 400/401/409/429 等业务错误码
}

@ExceptionHandler(Exception.class)
public Result<Void> handleException(Exception e) {
    return Result.error(ResultCode.ERROR);              // 系统异常统一返回 500
}
```

- **BusinessException** — 业务异常携带精确错误码（400/401/409/429），通过 `GlobalExceptionHandler` 透传到前端
- **Exception 兜底** — 未知系统异常统一返回 500，**绝不暴露**异常细节（防止泄露 SQL 结构、数据库账号等敏感信息）

---

### 9. 数据库设计

```sql
CREATE TABLE user (
    id         INT PRIMARY KEY AUTO_INCREMENT,
    username   VARCHAR(50)  NOT NULL,
    password   VARCHAR(255) NOT NULL,        -- BCrypt 加密后的密文
    email      VARCHAR(100) NOT NULL UNIQUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

- MyBatis `map-underscore-to-camel-case: true` 自动完成下划线到驼峰映射
- `StdOutImpl` 开发环境打印 SQL 到控制台

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
mysql -u root -p -e "
CREATE DATABASE IF NOT EXISTS test DEFAULT CHARSET utf8mb4;
USE test;
CREATE TABLE IF NOT EXISTS user (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
"

# 2. 修改 application.yml 中的数据库、Redis、邮箱授权码配置

# 3. 启动
cd login
mvn spring-boot:run
```

### 2. 启动前端

```bash
cd login-frontend
npm install
npm run dev
```

后端 `http://localhost:8080`，前端 `http://localhost:5173`，API 通过 Vite 代理转发。

### 3. API 速查

| 方法 | 路径 | 说明 | 需要 Token |
|------|------|------|------------|
| POST | `/api/v1/auth/send-code` | 发送邮箱验证码（60s 防刷） | 否 |
| POST | `/api/v1/auth/register` | 用户注册 | 否 |
| POST | `/api/v1/auth/login` | 用户登录 | 否 |
| GET | `/api/v1/auth/me` | 获取当前用户信息 | 是 |

详细接口文档参见 [API.md](login/API.md)。

---

## 开发说明

- **密码安全** — BCrypt（强度 10）不可逆加密，`passwordEncoder.matches()` 密文比对
- **验证码安全** — 注册成功后 Redis 验证码立即 `delete`，防止重放攻击
- **防刷机制** — `setIfAbsent` 原子操作实现 60 秒冷却，429 错误码精确提示剩余等待时间
- **异步邮件** — 自定义线程池 `mailExecutor` 异步发送，`CallerRunsPolicy` 兜底保证不丢邮件
- **无状态会话** — `SessionCreationPolicy.STATELESS`，每次请求独立校验 JWT
- **ThreadLocal 清理** — `JwtAuthenticationFilter.doFilterInternal()` 的 `finally` 块自动调用 `BaseContext.removeCurrent()`
- **SMTP 配置** — QQ 邮箱 465 端口 SSL 直连 + `SSLSocketFactory`，已修复 STARTTLS 协议冲突导致的延迟问题
