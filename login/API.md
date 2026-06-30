# 登录注册系统 API 接口文档

## 1. 基础信息

| 项目 | 说明 |
|------|------|
| 版本 | v1.0 |
| 基础路径 | `/api/v1` |
| 认证方式 | JWT Token（登录后接口在 Header 中携带 `token: <jwt>`） |
| 内容格式 | `application/json;charset=UTF-8` |

## 2. 通用响应格式

所有接口统一返回如下 JSON 结构：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {}
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| code | Integer | 状态码，200 成功，其他见错误码表 |
| message | String | 提示信息 |
| data | Object / null | 响应数据，无数据时为 `null` |

> **注意：** 成功响应的 `message` 字段统一为 `"操作成功"`，业务提示信息（如 `"验证码已发送"`）放置在 `data` 字段中。

## 3. 错误码定义

| code | 说明 |
|------|------|
| 200 | 成功 |
| 400 | 请求参数有误 / 验证码错误 |
| 401 | 未登录 / Token 过期 / 密码错误 |
| 409 | 邮箱已注册 |
| 500 | 服务器内部错误 |

> **注意：** 业务异常（`BusinessException`）携带的自定义错误码会正确透传到响应的 `code` 字段，而非一律返回 500。

## 4. 接口详情

### 4.1 发送邮箱验证码

用于注册时向指定邮箱发送验证码。

| 项目 | 说明 |
|------|------|
| **URL** | `/api/v1/auth/send-code` |
| **Method** | `POST` |
| **认证** | 无需 Token |

**请求参数：**

```json
{
  "email": "user@example.com"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| email | String | 是 | 接收验证码的邮箱地址 |

**成功响应：**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": "验证码已发送"
}
```

**失败响应：**

```json
{
  "code": 409,
  "message": "该邮箱已被注册",
  "data": null
}
```

**说明：**
- 验证码有效期 5 分钟，存储在 Redis 中，key 为 `login:code:{email}`
- 生成 6 位随机数字验证码

---

### 4.2 用户注册

| 项目 | 说明 |
|------|------|
| **URL** | `/api/v1/auth/register` |
| **Method** | `POST` |
| **认证** | 无需 Token |

**请求参数：**

```json
{
  "username": "zhangsan",
  "password": "Abc@123456",
  "email": "user@example.com",
  "code": "482917"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| username | String | 是 | 用户名 |
| password | String | 是 | 密码 |
| email | String | 是 | 邮箱地址 |
| code | String | 是 | 6 位邮箱验证码 |

**成功响应：**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": "注册成功"
}
```

**失败响应：**

```json
{
  "code": 400,
  "message": "验证码错误或已过期",
  "data": null
}
```

**说明：**
- 密码使用 BCrypt 加密存储，不存明文
- 注册成功后验证码自动从 Redis 中删除

---

### 4.3 用户登录

| 项目 | 说明 |
|------|------|
| **URL** | `/api/v1/auth/login` |
| **Method** | `POST` |
| **认证** | 无需 Token |

**请求参数：**

```json
{
  "email": "user@example.com",
  "password": "Abc@123456"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| email | String | 是 | 用户邮箱 |
| password | String | 是 | 用户密码 |

**成功响应：**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJlbWFpbCI6InVzZXJAZXhhbX...",
    "username": "zhangsan",
    "email": "user@example.com"
  }
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| token | String | JWT 令牌，前端存入 localStorage，后续请求携带在 `token` 请求头中 |
| username | String | 用户名 |
| email | String | 邮箱 |

**失败响应：**

```json
{
  "code": 401,
  "message": "当前邮箱尚未注册账号",
  "data": null
}
```

```json
{
  "code": 401,
  "message": "密码输入错误",
  "data": null
}
```

**说明：**
- JWT 有效期 2 小时（7200000ms）
- Token 存储在 `token` 请求头中（通过 `jwt.token-name` 配置，默认为 `token`）
- JWT payload 中包含 `email` 和 `username` 字段

---

### 4.4 获取当前用户信息

登录后获取用户基本信息（用户名 + 邮箱），用于登录后页面展示。

| 项目 | 说明 |
|------|------|
| **URL** | `/api/v1/auth/me` |
| **Method** | `GET` |
| **认证** | 需要 JWT Token |

**请求头：**

```
token: eyJhbGciOiJIUzI1NiJ9.eyJlbWFpbCI6InVzZXJAZXhhbX...
```

**成功响应：**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "username": "zhangsan",
    "email": "user@example.com"
  }
}
```

**失败响应（Token 无效或过期）：**

```json
{
  "code": 401,
  "message": "未登录或Token已失效，请重新登录",
  "data": null
}
```

> **注意：** Token 过期时 HTTP 响应状态码为 401，响应体为 JSON 格式。

---

## 5. 鉴权说明

### 5.1 Token 传递方式

Token 通过名为 `token` 的 HTTP 请求头传递（由 `application.yml` 中的 `jwt.token-name` 配置项控制，默认为 `token`）。

### 5.2 需要 Token 的接口

所有 `/api/v1/auth/**` 路径下的接口都需要携带 Token，以下接口除外：

| URL | 说明 |
|-----|------|
| `/api/v1/auth/login` | 登录接口 |
| `/api/v1/auth/register` | 注册接口 |
| `/api/v1/auth/send-code` | 发送验证码接口 |

### 5.3 Token 校验流程

1. 拦截器 `JwtTokenAdminInterceptor` 从请求头中读取 `token`
2. 使用 `jjwt 0.12.x` 解析并校验 Token（签名 + 过期时间）
3. 校验通过后，将 `email` 存入 `BaseContext`（基于 ThreadLocal），后续 Controller 可通过 `BaseContext.getCurrent()` 获取当前用户邮箱
4. 请求结束后，`afterCompletion` 自动清理 ThreadLocal 数据

---

## 6. 前端页面与接口对应关系

```
┌──────────────┐      ┌──────────────┐      ┌──────────────┐
│   登录页面    │      │   注册页面    │      │   用户主页    │
│              │      │              │      │              │
│ 输入:邮箱    │      │ 输入:用户名  │      │ 显示:用户名  │
│ 输入:密码    │      │ 输入:邮箱    │      │ 显示:邮箱    │
│              │      │ 输入:密码    │      │              │
│ [登录] 按钮  │      │ 点击:发送验证码│     │ GET /me     │
│ POST /login │      │ 输入:验证码  │      │              │
│              │      │ [注册] 按钮  │      │              │
│              │      │ POST /register│     │              │
└──────────────┘      └──────────────┘      └──────────────┘

流程：
  login 页面 ──没账号?──→ register 页面 ──注册成功──→ login 页面
  login 页面 ──登录成功──→ 用户主页（显示用户名 + 邮箱）
```

## 7. 前端调用示例

**登录：**

```javascript
fetch('/api/v1/auth/login', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ email: 'user@example.com', password: 'Abc@123456' })
})
  .then(res => res.json())
  .then(data => {
    if (data.code === 200) {
      localStorage.setItem('token', data.data.token);
      localStorage.setItem('username', data.data.username);
      window.location.href = '/home.html';
    }
  });
```

**注册-发送验证码：**

```javascript
fetch('/api/v1/auth/send-code', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ email: 'user@example.com' })
})
  .then(res => res.json())
  .then(data => {
    if (data.code === 200) {
      alert('验证码已发送，请查看邮箱');
    }
  });
```

**注册-提交注册：**

```javascript
fetch('/api/v1/auth/register', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    username: 'zhangsan',
    password: 'Abc@123456',
    email: 'user@example.com',
    code: '482917'
  })
})
  .then(res => res.json())
  .then(data => {
    if (data.code === 200) {
      alert('注册成功');
      window.location.href = '/login.html';
    }
  });
```

**获取用户信息（登录后展示）：**

```javascript
fetch('/api/v1/auth/me', {
  headers: {
    'token': localStorage.getItem('token')
  }
})
  .then(res => {
    if (res.status === 401) {
      window.location.href = '/login.html';
      return;
    }
    return res.json();
  })
  .then(data => {
    if (data && data.code === 200) {
      document.getElementById('username').innerText = data.data.username;
      document.getElementById('email').innerText = data.data.email;
    }
  });
```

## 8. 接口速查表

| 序号 | 方法 | URL | 说明 | 需要 Token |
|------|------|-----|------|------------|
| 1 | POST | `/api/v1/auth/send-code` | 发送邮箱验证码 | 否 |
| 2 | POST | `/api/v1/auth/register` | 用户注册 | 否 |
| 3 | POST | `/api/v1/auth/login` | 用户登录 | 否 |
| 4 | GET | `/api/v1/auth/me` | 获取当前用户信息 | 是 |
