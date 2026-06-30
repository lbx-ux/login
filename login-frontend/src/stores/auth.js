import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, getMe } from '@/api/auth'

export const useAuthStore = defineStore('auth', () => {
  // --- State ---
  const token = ref(localStorage.getItem('token') || '')
  const user = ref({
    username: localStorage.getItem('username') || '',
    email: localStorage.getItem('email') || ''
  })

  // --- Getters ---
  const isLoggedIn = computed(() => !!token.value)

  // --- Actions ---
  async function loginAction({ email, password }) {
    const res = await loginApi({ email, password })
    const { token: t, username, email: userEmail } = res.data

    token.value = t
    user.value = { username, email: userEmail }

    localStorage.setItem('token', t)
    localStorage.setItem('username', username)
    localStorage.setItem('email', userEmail)
  }

  async function fetchMe() {
    try {
      const res = await getMe()
      user.value = {
        username: res.data.username,
        email: res.data.email
      }
      localStorage.setItem('username', res.data.username)
      localStorage.setItem('email', res.data.email)
    } catch {
      // Token 失效 → 登出
      logout()
      throw new Error('获取用户信息失败')
    }
  }

  function logout() {
    token.value = ''
    user.value = { username: '', email: '' }
    localStorage.removeItem('token')
    localStorage.removeItem('username')
    localStorage.removeItem('email')
  }

  return { token, user, isLoggedIn, loginAction, fetchMe, logout }
})
