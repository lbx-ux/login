<template>
  <div :class="['form-group', { 'form-group--error': error }]">
    <label v-if="label" :for="id" class="form-group__label">
      {{ label }}
      <span v-if="required" class="form-group__required">*</span>
    </label>
    <div class="form-group__input-wrap">
      <slot name="prefix" />
      <input
        :id="id"
        :type="type"
        :value="modelValue"
        :placeholder="placeholder"
        :disabled="disabled"
        :maxlength="maxlength"
        :autocomplete="autocomplete"
        class="form-group__input"
        @input="$emit('update:modelValue', $event.target.value)"
        @blur="$emit('blur')"
      />
      <slot name="suffix" />
    </div>
    <p v-if="error" class="form-group__error">{{ error }}</p>
    <p v-else-if="hint" class="form-group__hint">{{ hint }}</p>
  </div>
</template>

<script setup>
defineProps({
  modelValue: { type: String, default: '' },
  id: { type: String, required: true },
  label: { type: String, default: '' },
  type: { type: String, default: 'text' },
  placeholder: { type: String, default: '' },
  error: { type: String, default: '' },
  hint: { type: String, default: '' },
  required: { type: Boolean, default: false },
  disabled: { type: Boolean, default: false },
  maxlength: { type: [Number, String], default: undefined },
  autocomplete: { type: String, default: 'off' }
})

defineEmits(['update:modelValue', 'blur'])
</script>

<style scoped>
.form-group {
  margin-bottom: var(--space-5);
}

.form-group__label {
  display: block;
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
  color: var(--color-gray-700);
  margin-bottom: var(--space-2);
}

.form-group__required {
  color: var(--color-error);
  margin-left: 2px;
}

.form-group__input-wrap {
  position: relative;
  display: flex;
  align-items: center;
}

.form-group__input {
  width: 100%;
  height: 44px;
  padding: 0 var(--space-4);
  font-size: var(--text-sm);
  color: var(--color-gray-800);
  background: var(--color-gray-50);
  border: 1.5px solid var(--color-gray-200);
  border-radius: var(--radius-md);
  outline: none;
  transition:
    border-color var(--transition-fast),
    box-shadow var(--transition-fast),
    background var(--transition-fast);
}

.form-group__input::placeholder {
  color: var(--color-gray-400);
}

.form-group__input:hover:not(:disabled) {
  border-color: var(--color-gray-300);
}

.form-group__input:focus {
  border-color: var(--color-primary);
  background: var(--color-white);
  box-shadow: var(--shadow-focus);
}

.form-group__input:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.form-group--error .form-group__input {
  border-color: var(--color-error);
  background: var(--color-error-light);
}

.form-group--error .form-group__input:focus {
  box-shadow: 0 0 0 3px rgba(220, 38, 38, 0.12);
}

.form-group__error {
  margin-top: var(--space-2);
  font-size: var(--text-xs);
  color: var(--color-error);
  line-height: var(--leading-normal);
}

.form-group__hint {
  margin-top: var(--space-2);
  font-size: var(--text-xs);
  color: var(--color-gray-400);
  line-height: var(--leading-normal);
}
</style>
