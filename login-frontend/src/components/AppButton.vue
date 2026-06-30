<template>
  <button
    :class="['btn', `btn--${variant}`, { 'btn--loading': loading, 'btn--block': block }]"
    :type="type"
    :disabled="disabled || loading"
    @click="$emit('click')"
  >
    <span v-if="loading" class="btn__spinner"></span>
    <span :class="{ 'btn__text--hidden': loading }">
      <slot />
    </span>
  </button>
</template>

<script setup>
defineProps({
  variant: {
    type: String,
    default: 'primary',
    validator: v => ['primary', 'outline', 'ghost'].includes(v)
  },
  type: { type: String, default: 'button' },
  block: { type: Boolean, default: false },
  loading: { type: Boolean, default: false },
  disabled: { type: Boolean, default: false }
})

defineEmits(['click'])
</script>

<style scoped>
.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-2);
  height: 44px;
  padding: 0 var(--space-6);
  font-size: var(--text-sm);
  font-weight: var(--font-semibold);
  border: none;
  border-radius: var(--radius-md);
  cursor: pointer;
  user-select: none;
  white-space: nowrap;
  transition:
    background var(--transition-fast),
    color var(--transition-fast),
    box-shadow var(--transition-fast),
    transform var(--transition-fast);
  position: relative;
}

.btn:active:not(:disabled) {
  transform: scale(0.98);
}

.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* --- Primary --- */
.btn--primary {
  color: white;
  background: var(--color-primary);
  box-shadow: 0 1px 2px rgba(79, 70, 229, 0.2);
}

.btn--primary:hover:not(:disabled) {
  background: var(--color-primary-hover);
  box-shadow: 0 2px 8px rgba(79, 70, 229, 0.35);
}

/* --- Outline --- */
.btn--outline {
  color: var(--color-gray-700);
  background: var(--color-white);
  border: 1.5px solid var(--color-gray-200);
}

.btn--outline:hover:not(:disabled) {
  border-color: var(--color-primary);
  color: var(--color-primary);
  background: var(--color-primary-light);
}

/* --- Ghost --- */
.btn--ghost {
  color: var(--color-primary);
  background: transparent;
}

.btn--ghost:hover:not(:disabled) {
  background: var(--color-primary-light);
}

/* --- Block --- */
.btn--block {
  width: 100%;
}

/* --- Loading --- */
.btn--loading {
  cursor: wait;
}

.btn__spinner {
  position: absolute;
  width: 18px;
  height: 18px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: btn-spin 0.6s linear infinite;
}

.btn--outline .btn__spinner,
.btn--ghost .btn__spinner {
  border-color: rgba(79, 70, 229, 0.2);
  border-top-color: var(--color-primary);
}

.btn__text--hidden {
  visibility: hidden;
}

@keyframes btn-spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
