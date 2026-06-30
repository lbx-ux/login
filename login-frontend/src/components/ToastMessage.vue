<template>
  <transition name="toast-slide" appear>
    <div v-if="visible" :class="['toast', `toast--${type}`]">
      <span class="toast__icon">
        <template v-if="type === 'success'">
          <svg viewBox="0 0 20 20" fill="currentColor" width="18" height="18">
            <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.857-9.809a.75.75 0 00-1.214-.882l-3.483 4.79-1.88-1.88a.75.75 0 10-1.06 1.061l2.5 2.5a.75.75 0 001.137-.089l4-5.5z" clip-rule="evenodd"/>
          </svg>
        </template>
        <template v-else-if="type === 'error'">
          <svg viewBox="0 0 20 20" fill="currentColor" width="18" height="18">
            <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.28 7.22a.75.75 0 00-1.06 1.06L8.94 10l-1.72 1.72a.75.75 0 101.06 1.06L10 11.06l1.72 1.72a.75.75 0 101.06-1.06L11.06 10l1.72-1.72a.75.75 0 00-1.06-1.06L10 8.94 8.28 7.22z" clip-rule="evenodd"/>
          </svg>
        </template>
        <template v-else>
          <svg viewBox="0 0 20 20" fill="currentColor" width="18" height="18">
            <path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a.75.75 0 000 1.5h.253a.25.25 0 01.244.304l-.459 2.066A1.75 1.75 0 0010.747 15H11a.75.75 0 000-1.5h-.253a.25.25 0 01-.244-.304l.459-2.066A1.75 1.75 0 009.253 9H9z" clip-rule="evenodd"/>
          </svg>
        </template>
      </span>
      <span class="toast__message">{{ message }}</span>
      <button v-if="closable" class="toast__close" @click="close">&times;</button>
    </div>
  </transition>
</template>

<script setup>
import { ref, watch, onUnmounted } from 'vue'

const props = defineProps({
  message: { type: String, default: '' },
  type: { type: String, default: 'info', validator: v => ['success', 'error', 'info', 'warning'].includes(v) },
  duration: { type: Number, default: 4000 },
  closable: { type: Boolean, default: true }
})

const emit = defineEmits(['close'])

const visible = ref(false)
let timer = null

function close() {
  visible.value = false
  clearTimeout(timer)
  emit('close')
}

function show() {
  visible.value = true
  if (props.duration > 0) {
    timer = setTimeout(close, props.duration)
  }
}

watch(() => props.message, (val) => {
  if (val) show()
}, { immediate: true })

onUnmounted(() => clearTimeout(timer))

defineExpose({ show, close })
</script>

<style scoped>
.toast {
  position: fixed;
  top: var(--space-6);
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-3) var(--space-5);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-lg);
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
  z-index: 9999;
  max-width: 420px;
  min-width: 280px;
}

.toast--success {
  color: var(--color-success);
  background: var(--color-success-light);
  border: 1px solid #A7F3D0;
}

.toast--error {
  color: var(--color-error);
  background: var(--color-error-light);
  border: 1px solid #FECACA;
}

.toast--warning {
  color: var(--color-warning);
  background: var(--color-warning-light);
  border: 1px solid #FDE68A;
}

.toast--info {
  color: var(--color-primary);
  background: var(--color-primary-light);
  border: 1px solid #C7D2FE;
}

.toast__icon {
  flex-shrink: 0;
  display: flex;
}

.toast__message {
  flex: 1;
  line-height: var(--leading-normal);
}

.toast__close {
  flex-shrink: 0;
  background: none;
  border: none;
  font-size: var(--text-lg);
  cursor: pointer;
  opacity: 0.5;
  padding: 0;
  line-height: 1;
  transition: opacity var(--transition-fast);
}

.toast__close:hover {
  opacity: 1;
}

/* --- Transition --- */
.toast-slide-enter-active {
  transition: all var(--transition-slow);
}

.toast-slide-leave-active {
  transition: all var(--transition-fast);
}

.toast-slide-enter-from {
  opacity: 0;
  transform: translateX(-50%) translateY(-12px);
}

.toast-slide-leave-to {
  opacity: 0;
  transform: translateX(-50%) translateY(-12px);
}
</style>
