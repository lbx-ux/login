import request from './request'

/**
 * 发送邮箱验证码
 * POST /api/v1/auth/send-code
 */
export function sendCode(email) {
  return request.post('/auth/send-code', { email })
}

/**
 * 用户注册
 * POST /api/v1/auth/register
 */
export function register({ username, password, email, code }) {
  return request.post('/auth/register', { username, password, email, code })
}

/**
 * 用户登录
 * POST /api/v1/auth/login
 */
export function login({ email, password }) {
  return request.post('/auth/login', { email, password })
}

/**
 * 获取当前用户信息
 * GET /api/v1/auth/me
 */
export function getMe() {
  return request.get('/auth/me')
}
