import axios from 'axios'
import { useAuthStore } from '@/stores/auth'
import router from '@/router'

const request = axios.create({
  baseURL: '/api/v1',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json;charset=UTF-8'
  }
})

// 请求拦截器 — 自动附加 Token
request.interceptors.request.use(
  config => {
    const authStore = useAuthStore()
    if (authStore.token) {
      config.headers['token'] = authStore.token
    }
    return config
  },
  error => Promise.reject(error)
)

// 响应拦截器 — 统一错误处理
request.interceptors.response.use(
  response => {
    const data = response.data
    if (data.code !== 200) {
      // 业务错误码统一通过 Promise.reject 返回，方便调用方 catch
      return Promise.reject(new BusinessError(data.code, data.message))
    }
    return data
  },
  error => {
    if (error.response) {
      const status = error.response.status
      if (status === 401) {
        const authStore = useAuthStore()
        authStore.logout()
        router.push({ name: 'Login', query: { expired: '1' } })
        return Promise.reject(new BusinessError(401, '登录已过期，请重新登录'))
      }
      return Promise.reject(new BusinessError(status, `请求失败 (${status})`))
    }
    if (error.code === 'ECONNABORTED') {
      return Promise.reject(new BusinessError(0, '请求超时，请检查网络'))
    }
    return Promise.reject(new BusinessError(0, '网络连接异常'))
  }
)

class BusinessError extends Error {
  constructor(code, message) {
    super(message)
    this.code = code
    this.name = 'BusinessError'
  }
}

export { BusinessError }
export default request
