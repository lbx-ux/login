<template>
  <AuthCard
    brand-title="创建账号"
    brand-desc="加入我们，开启全新的旅程"
  >
    <div class="form-wrapper">
      <!-- 标题区 -->
      <div class="form-wrapper__header">
        <h2 class="form-wrapper__title">注册</h2>
        <p class="form-wrapper__subtitle">
          已有账号？
          <router-link to="/login" class="form-wrapper__link">立即登录</router-link>
        </p>
      </div>

      <!-- 表单 -->
      <form @submit.prevent="handleSubmit" novalidate>
        <FormInput
          id="reg-username"
          v-model="form.username"
          label="用户名"
          placeholder="请输入用户名"
          required
          :error="errors.username"
          @blur="validateUsername"
        />

        <FormInput
          id="reg-email"
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
          id="reg-password"
          v-model="form.password"
          label="密码"
          type="password"
          placeholder="请输入密码（至少6位）"
          required
          autocomplete="new-password"
          :error="errors.password"
          @blur="validatePassword"
        />

        <!-- 验证码行 -->
        <div class="code-row">
          <div class="code-row__input">
            <FormInput
              id="reg-code"
              v-model="form.code"
              label="验证码"
              placeholder="请输入6位验证码"
              required
              maxlength="6"
              :error="errors.code"
              @blur="validateCode"
            />
          </div>
          <div class="code-row__btn">
            <AppButton
              variant="outline"
              :disabled="codeCooldown > 0 || sending"
              :loading="sending"
              block
              @click="handleSendCode"
            >
              {{ codeBtnText }}
            </AppButton>
          </div>
        </div>

        <div class="form-wrapper__actions">
          <AppButton
            type="submit"
            variant="primary"
            block
            :loading="loading"
          >
            注 册
          </AppButton>
        </div>
      </form>
    </div>

    <!-- Toast -->
    <ToastMessage
      v-if="toastMessage"
      :message="toastMessage"
      :type="toastType"
      @close="toastMessage = ''"
    />
  </AuthCard>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import AuthCard from '@/components/AuthCard.vue'
import FormInput from '@/components/FormInput.vue'
import AppButton from '@/components/AppButton.vue'
import { sendCode, register } from '@/api/auth'
import { BusinessError } from '@/api/request'
import ToastMessage from '@/components/ToastMessage.vue'

const router = useRouter()

const loading = ref(false)
const sending = ref(false)
const codeCooldown = ref(0)
const toastMessage = ref('')
const toastType = ref('info')

const form = reactive({
  username: '',
  email: '',
  password: '',
  code: ''
})

const errors = reactive({
  username: '',
  email: '',
  password: '',
  code: ''
})

const codeBtnText = computed(() => {
  if (codeCooldown.value > 0) return `${codeCooldown.value}秒后重发`
  return '发送验证码'
})

// --- 验证 ---
function validateUsername() {
  if (!form.username.trim()) {
    errors.username = '请输入用户名'
  } else if (form.username.trim().length < 2) {
    errors.username = '用户名至少2个字符'
  } else {
    errors.username = ''
  }
}

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
  } else if (form.password.length < 6) {
    errors.password = '密码至少6位'
  } else {
    errors.password = ''
  }
}

function validateCode() {
  if (!form.code) {
    errors.code = '请输入验证码'
  } else if (!/^\d{6}$/.test(form.code)) {
    errors.code = '验证码为6位数字'
  } else {
    errors.code = ''
  }
}

function validateAll() {
  validateUsername()
  validateEmail()
  validatePassword()
  validateCode()
  return !errors.username && !errors.email && !errors.password && !errors.code
}

// --- 发送验证码 ---
async function handleSendCode() {
  validateEmail()
  if (errors.email) return

  sending.value = true
  try {
    await sendCode(form.email.trim())
    toastMessage.value = '验证码已发送，请查看邮箱'
    toastType.value = 'success'
    // 开始倒计时
    codeCooldown.value = 60
    const timer = setInterval(() => {
      codeCooldown.value--
      if (codeCooldown.value <= 0) {
        clearInterval(timer)
      }
    }, 1000)
  } catch (e) {
    if (e instanceof BusinessError) {
      errors.email = e.message
    } else {
      toastMessage.value = '发送验证码失败，请稍后再试'
      toastType.value = 'error'
    }
  } finally {
    sending.value = false
  }
}

// --- 注册 ---
async function handleSubmit() {
  if (!validateAll()) return

  loading.value = true
  try {
    await register({
      username: form.username.trim(),
      email: form.email.trim(),
      password: form.password,
      code: form.code
    })
    toastMessage.value = '注册成功，即将跳转登录页'
    toastType.value = 'success'
    setTimeout(() => {
      router.push({ name: 'Login' })
    }, 1500)
  } catch (e) {
    if (e instanceof BusinessError) {
      if (e.code === 400) errors.code = e.message
      else if (e.code === 409) errors.email = e.message
      else errors.email = e.message
    } else {
      toastMessage.value = '注册失败，请稍后再试'
      toastType.value = 'error'
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.form-wrapper {
  width: 100%;
  max-width: 360px;
  margin: 0 auto;
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

/* --- Code Row --- */
.code-row {
  display: flex;
  gap: var(--space-3);
  align-items: flex-end;
}

.code-row__input {
  flex: 1;
  min-width: 0;
}

.code-row__btn {
  flex-shrink: 0;
  margin-bottom: var(--space-5);
}

.code-row__btn .btn {
  white-space: nowrap;
  font-size: var(--text-xs);
  padding: 0 var(--space-4);
}

/* --- Actions --- */
.form-wrapper__actions {
  margin-top: var(--space-8);
}

/* --- Toast (used programmatically, needs a holder) --- */
</style>
