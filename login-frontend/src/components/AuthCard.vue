<template>
  <div class="auth-container">
    <div class="auth-card">
      <!-- 左侧装饰面板 -->
      <div class="auth-card__brand">
        <div class="auth-card__brand-inner">
          <div class="brand-logo">
            <svg viewBox="0 0 40 40" fill="none" xmlns="http://www.w3.org/2000/svg">
              <rect width="40" height="40" rx="10" fill="white" fill-opacity="0.2"/>
              <path d="M12 20L18 26L28 14" stroke="white" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </div>
          <h1 class="brand-title">{{ brandTitle }}</h1>
          <p class="brand-desc">{{ brandDesc }}</p>
        </div>
        <div class="brand-bg-shape"></div>
      </div>

      <!-- 右侧表单面板 -->
      <div class="auth-card__form">
        <slot />
      </div>
    </div>

    <!-- 底部版权 -->
    <footer class="auth-footer">
      <p>&copy; {{ year }} 登录系统. All rights reserved.</p>
    </footer>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  brandTitle: { type: String, required: true },
  brandDesc: { type: String, default: '' }
})

const year = computed(() => new Date().getFullYear())
</script>

<style scoped>
.auth-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--space-6);
  background:
    radial-gradient(ellipse 80% 60% at 50% -20%, rgba(79, 70, 229, 0.08), transparent),
    radial-gradient(ellipse 60% 50% at 80% 80%, rgba(99, 102, 241, 0.05), transparent),
    var(--color-bg);
}

.auth-card {
  display: flex;
  width: 860px;
  min-height: 520px;
  background: var(--color-white);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-xl);
  overflow: hidden;
}

/* --- 左侧品牌区 --- */
.auth-card__brand {
  position: relative;
  width: 360px;
  flex-shrink: 0;
  background: linear-gradient(135deg, #4F46E5 0%, #7C3AED 50%, #6366F1 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.auth-card__brand-inner {
  position: relative;
  z-index: 2;
  text-align: center;
  padding: var(--space-12);
}

.brand-logo {
  width: 56px;
  height: 56px;
  margin: 0 auto var(--space-6);
}

.brand-logo svg {
  width: 100%;
  height: 100%;
}

.brand-title {
  font-size: var(--text-2xl);
  font-weight: var(--font-bold);
  color: white;
  margin-bottom: var(--space-3);
  letter-spacing: -0.02em;
}

.brand-desc {
  font-size: var(--text-sm);
  color: rgba(255, 255, 255, 0.8);
  line-height: var(--leading-relaxed);
}

.brand-bg-shape {
  position: absolute;
  bottom: -60px;
  right: -60px;
  width: 280px;
  height: 280px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.06);
}

.brand-bg-shape::before {
  content: '';
  position: absolute;
  top: 50px;
  left: 50px;
  width: 180px;
  height: 180px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.05);
}

/* --- 右侧表单区 --- */
.auth-card__form {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: var(--space-12) var(--space-10);
  min-width: 0;
}

/* --- 底部 --- */
.auth-footer {
  margin-top: var(--space-8);
  text-align: center;
}

.auth-footer p {
  font-size: var(--text-xs);
  color: var(--color-gray-400);
}

/* ========================================
   Responsive
   ======================================== */

@media (max-width: 768px) {
  .auth-container {
    padding: var(--space-4);
  }

  .auth-card {
    flex-direction: column;
    width: 100%;
    max-width: 420px;
    min-height: auto;
  }

  .auth-card__brand {
    width: 100%;
    padding: var(--space-8) var(--space-6);
  }

  .auth-card__brand-inner {
    padding: var(--space-4);
  }

  .brand-title {
    font-size: var(--text-lg);
  }

  .brand-logo {
    width: 40px;
    height: 40px;
    margin-bottom: var(--space-4);
  }

  .brand-bg-shape {
    width: 160px;
    height: 160px;
    bottom: -40px;
    right: -40px;
  }

  .auth-card__form {
    padding: var(--space-8) var(--space-6);
  }
}
</style>
