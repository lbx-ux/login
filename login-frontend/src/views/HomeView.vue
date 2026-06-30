<template>
  <div class="home">
    <!-- 顶部导航 -->
    <header class="home__header">
      <div class="home__header-inner">
        <div class="home__logo">
          <svg viewBox="0 0 36 36" fill="none" width="36" height="36">
            <rect width="36" height="36" rx="9" fill="#4F46E5"/>
            <path d="M10 18L16 23L26 12" stroke="white" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          <span class="home__logo-text">登录系统</span>
        </div>
        <div class="home__user">
          <div class="home__user-avatar">{{ userInitial }}</div>
          <span class="home__user-name">{{ user.username }}</span>
          <button class="home__logout-btn" @click="handleLogout" title="退出登录">
            <svg viewBox="0 0 20 20" fill="none" width="18" height="18">
              <path d="M7 3H4a1 1 0 00-1 1v12a1 1 0 001 1h3m6-7l3-3m0 0l-3-3m3 3H7" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </button>
        </div>
      </div>
    </header>

    <!-- 主体 -->
    <main class="home__main">
      <div class="home__card">
        <!-- 加载态 -->
        <div v-if="loading" class="home__loading">
          <div class="home__spinner"></div>
          <p>加载中...</p>
        </div>

        <!-- 错误态 -->
        <div v-else-if="error" class="home__error">
          <div class="home__error-icon">
            <svg viewBox="0 0 48 48" fill="none" width="48" height="48">
              <circle cx="24" cy="24" r="20" stroke="#DC2626" stroke-width="2"/>
              <path d="M24 14v12m0 6v2" stroke="#DC2626" stroke-width="2.5" stroke-linecap="round"/>
            </svg>
          </div>
          <p class="home__error-text">{{ error }}</p>
          <AppButton variant="outline" @click="fetchUserInfo">重新加载</AppButton>
        </div>

        <!-- 正常态 -->
        <template v-else>
          <div class="home__welcome">
            <div class="home__avatar-large">{{ userInitial }}</div>
            <h1 class="home__greeting">欢迎回来，{{ user.username }}</h1>
            <p class="home__email">{{ user.email }}</p>
          </div>

          <div class="home__info-grid">
            <div class="home__info-item">
              <div class="home__info-icon home__info-icon--purple">
                <svg viewBox="0 0 20 20" fill="currentColor" width="20" height="20">
                  <path d="M10 8a3 3 0 100-6 3 3 0 000 6zM3.465 14.493a1.23 1.23 0 00.41 1.412A9.957 9.957 0 0010 18c2.31 0 4.438-.784 6.131-2.1.43-.333.604-.903.408-1.41a7.002 7.002 0 00-13.074.003z"/>
                </svg>
              </div>
              <div class="home__info-content">
                <span class="home__info-label">用户名</span>
                <span class="home__info-value">{{ user.username }}</span>
              </div>
            </div>

            <div class="home__info-item">
              <div class="home__info-icon home__info-icon--green">
                <svg viewBox="0 0 20 20" fill="currentColor" width="20" height="20">
                  <path d="M3 4a2 2 0 00-2 2v1.161l8.441 4.221a1.25 1.25 0 001.118 0L19 7.162V6a2 2 0 00-2-2H3z"/>
                  <path d="M19 8.839l-7.77 3.885a2.75 2.75 0 01-2.46 0L1 8.839V14a2 2 0 002 2h14a2 2 0 002-2V8.839z"/>
                </svg>
              </div>
              <div class="home__info-content">
                <span class="home__info-label">邮箱</span>
                <span class="home__info-value">{{ user.email }}</span>
              </div>
            </div>

            <div class="home__info-item">
              <div class="home__info-icon home__info-icon--amber">
                <svg viewBox="0 0 20 20" fill="currentColor" width="20" height="20">
                  <path fill-rule="evenodd" d="M10 1a4.5 4.5 0 00-4.5 4.5V9H5a2 2 0 00-2 2v6a2 2 0 002 2h10a2 2 0 002-2v-6a2 2 0 00-2-2h-.5V5.5A4.5 4.5 0 0010 1zm3 8V5.5a3 3 0 10-6 0V9h6z" clip-rule="evenodd"/>
                </svg>
              </div>
              <div class="home__info-content">
                <span class="home__info-label">账号安全</span>
                <span class="home__info-value">密码已加密存储</span>
              </div>
            </div>
          </div>
        </template>
      </div>
    </main>

    <!-- Toast -->
    <ToastMessage
      v-if="toastMessage"
      :message="toastMessage"
      :type="toastType"
      @close="toastMessage = ''"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import AppButton from '@/components/AppButton.vue'
import ToastMessage from '@/components/ToastMessage.vue'

const router = useRouter()
const authStore = useAuthStore()

const loading = ref(true)
const error = ref('')
const toastMessage = ref('')
const toastType = ref('info')

const user = computed(() => authStore.user)
const userInitial = computed(() => {
  const name = user.value.username || 'U'
  return name.charAt(0).toUpperCase()
})

async function fetchUserInfo() {
  loading.value = true
  error.value = ''
  try {
    await authStore.fetchMe()
  } catch (e) {
    error.value = e.message || '获取用户信息失败'
  } finally {
    loading.value = false
  }
}

function handleLogout() {
  authStore.logout()
  router.replace({ name: 'Login' })
  toastMessage.value = '已退出登录'
  toastType.value = 'info'
}

onMounted(fetchUserInfo)
</script>

<style scoped>
.home {
  min-height: 100vh;
  background:
    radial-gradient(ellipse 60% 50% at 50% -10%, rgba(79, 70, 229, 0.05), transparent),
    var(--color-bg);
}

/* --- Header --- */
.home__header {
  background: var(--color-white);
  border-bottom: 1px solid var(--color-gray-200);
}

.home__header-inner {
  max-width: 900px;
  margin: 0 auto;
  padding: 0 var(--space-6);
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.home__logo {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.home__logo-text {
  font-size: var(--text-lg);
  font-weight: var(--font-bold);
  color: var(--color-gray-900);
}

.home__user {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.home__user-avatar {
  width: 34px;
  height: 34px;
  border-radius: var(--radius-full);
  background: var(--color-primary);
  color: white;
  font-size: var(--text-sm);
  font-weight: var(--font-semibold);
  display: flex;
  align-items: center;
  justify-content: center;
}

.home__user-name {
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
  color: var(--color-gray-700);
}

.home__logout-btn {
  background: none;
  border: none;
  cursor: pointer;
  padding: var(--space-2);
  border-radius: var(--radius-md);
  color: var(--color-gray-400);
  transition: all var(--transition-fast);
  display: flex;
}

.home__logout-btn:hover {
  color: var(--color-error);
  background: var(--color-error-light);
}

/* --- Main --- */
.home__main {
  max-width: 600px;
  margin: 0 auto;
  padding: var(--space-12) var(--space-6);
}

.home__card {
  background: var(--color-white);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-lg);
  padding: var(--space-12) var(--space-10);
}

/* --- Loading --- */
.home__loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-4);
  padding: var(--space-16) 0;
  color: var(--color-gray-400);
  font-size: var(--text-sm);
}

.home__spinner {
  width: 32px;
  height: 32px;
  border: 3px solid var(--color-gray-200);
  border-top-color: var(--color-primary);
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* --- Error --- */
.home__error {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-4);
  padding: var(--space-12) 0;
  text-align: center;
}

.home__error-icon {
  margin-bottom: var(--space-2);
}

.home__error-text {
  font-size: var(--text-sm);
  color: var(--color-gray-500);
  margin-bottom: var(--space-2);
}

/* --- Welcome --- */
.home__welcome {
  text-align: center;
  margin-bottom: var(--space-10);
}

.home__avatar-large {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  background: linear-gradient(135deg, #4F46E5, #7C3AED);
  color: white;
  font-size: var(--text-3xl);
  font-weight: var(--font-bold);
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto var(--space-5);
  box-shadow: 0 8px 20px rgba(79, 70, 229, 0.25);
}

.home__greeting {
  font-size: var(--text-xl);
  font-weight: var(--font-bold);
  color: var(--color-gray-900);
  margin-bottom: var(--space-2);
}

.home__email {
  font-size: var(--text-sm);
  color: var(--color-gray-500);
}

/* --- Info Grid --- */
.home__info-grid {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.home__info-item {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  padding: var(--space-4);
  background: var(--color-gray-50);
  border-radius: var(--radius-lg);
  border: 1px solid var(--color-gray-100);
  transition: box-shadow var(--transition-fast);
}

.home__info-item:hover {
  box-shadow: var(--shadow-sm);
}

.home__info-icon {
  width: 44px;
  height: 44px;
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.home__info-icon--purple {
  background: #EEF2FF;
  color: #4F46E5;
}

.home__info-icon--green {
  background: #ECFDF5;
  color: #059669;
}

.home__info-icon--amber {
  background: #FFFBEB;
  color: #D97706;
}

.home__info-content {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.home__info-label {
  font-size: var(--text-xs);
  color: var(--color-gray-400);
  font-weight: var(--font-medium);
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.home__info-value {
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
  color: var(--color-gray-800);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* --- Responsive --- */
@media (max-width: 768px) {
  .home__main {
    padding: var(--space-6) var(--space-4);
  }

  .home__card {
    padding: var(--space-8) var(--space-6);
  }

  .home__user-name {
    display: none;
  }
}
</style>
