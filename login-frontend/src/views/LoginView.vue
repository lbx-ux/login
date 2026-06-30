<template>
  <AuthCard
    brand-title="欢迎回来"
    brand-desc="登录您的账号，继续使用我们的服务"
  >
    <div class="form-wrapper">
      <!-- 登录过期提示 -->
      <div v-if="showExpiredTip" class="form-wrapper__alert form-wrapper__alert--warning">
        <svg viewBox="0 0 20 20" fill="currentColor" width="16" height="16">
          <path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a.75.75 0 000 1.5h.253a.25.25 0 01.244.304l-.459 2.066A1.75 1.75 0 0010.747 15H11a.75.75 0 000-1.5h-.253a.25.25 0 01-.244-.304l.459-2.066A1.75 1.75 0 009.253 9H9z" clip-rule="evenodd"/>
        </svg>
        <span>登录已过期，请重新登录</span>
      </div>

      <!-- 标题区 -->
      <div class="form-wrapper__header">
        <h2 class="form-wrapper__title">登录</h2>
        <p class="form-wrapper__subtitle">
          还没有账号？
          <router-link to="/register" class="form-wrapper__link">立即注册</router-link>
        </p>
      </div>

      <!-- 表单 -->
      <form @submit.prevent="handleSubmit" novalidate>
        <FormInput
          id="login-email"
          v-model="form.email"
          label="邮箱地址"
          type="email"
          placeholder="请输入邮箱地址"
          required
          autocomplete="email"
          :error="errors.email"
          @blur="validateEmail"
        />

        <FormInput
          id="login-password"
          v-model="form.password"
          label="密码"
          type="password"
          placeholder="请输入密码"
          required
          autocomplete="current-password"
          :error="errors.password"
          @blur="validatePassword"
        />

        <div class="form-wrapper__actions">
          <AppButton
            type="submit"
            variant="primary"
            block
            :loading="loading"
          >
            登 录
          </AppButton>
        </div>
      </form>
    </div>
  </AuthCard>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import AuthCard from '@/components/AuthCard.vue'
import FormInput from '@/components/FormInput.vue'
import AppButton from '@/components/AppButton.vue'
import { useAuthStore } from '@/stores/auth'
import { BusinessError } from '@/api/request'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const loading = ref(false)
const showExpiredTip = computed(() => route.query.expired === '1')

const form = reactive({
  email: '',
  password: ''
})

const errors = reactive({
  email: '',
  password: ''
})

function validateEmail() {
  if (!form.email.trim()) {
    errors.email = '请输入邮箱地址'
  } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) {
    errors.email = '请输入正确的邮箱格式'
  } else {
    errors.email = ''
  }
}

function validatePassword() {
  if (!form.password) {
    errors.password = '请输入密码'
  } else {
    errors.password = ''
  }
}

function validateAll() {
  validateEmail()
  validatePassword()
  return !errors.email && !errors.password
}

async function handleSubmit() {
  if (!validateAll()) return

  loading.value = true
  try {
    await authStore.loginAction({
      email: form.email.trim(),
      password: form.password
    })
    router.replace({ name: 'Home' })
  } catch (e) {
    if (e instanceof BusinessError) {
      errors.email = e.message
    } else {
      errors.email = '登录失败，请稍后再试'
    }
  } finally {
    loading.value = false
  }
}

// 清理过期参数
onMounted(() => {
  if (route.query.expired) {
    router.replace({ name: 'Login' })
  }
})
</script>

<style scoped>
.form-wrapper {
  width: 100%;
  max-width: 360px;
  margin: 0 auto;
}

/* --- Alert --- */
.form-wrapper__alert {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-3) var(--space-4);
  border-radius: var(--radius-md);
  font-size: var(--text-sm);
  margin-bottom: var(--space-6);
}

.form-wrapper__alert--warning {
  color: var(--color-warning);
  background: var(--color-warning-light);
  border: 1px solid #FDE68A;
}

/* --- Header --- */
.form-wrapper__header {
  margin-bottom: var(--space-8);
}

.form-wrapper__title {
  font-size: var(--text-2xl);
  font-weight: var(--font-bold);
  color: var(--color-gray-900);
  margin-bottom: var(--space-2);
  letter-spacing: -0.02em;
}

.form-wrapper__subtitle {
  font-size: var(--text-sm);
  color: var(--color-gray-500);
}

.form-wrapper__link {
  font-weight: var(--font-medium);
  color: var(--color-primary);
  transition: color var(--transition-fast);
}

.form-wrapper__link:hover {
  color: var(--color-primary-hover);
}

/* --- Actions --- */
.form-wrapper__actions {
  margin-top: var(--space-8);
}
</style>
