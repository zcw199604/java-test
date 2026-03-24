<template>
  <div class="card">
    <div class="label">{{ label }}</div>
    <div class="value">{{ value }}</div>
    <div class="hint">{{ hint }}</div>
  </div>
</template>

<script setup>
defineProps({
  label: String,
  value: [Number, String],
  hint: String
})
</script>

<style scoped>
.card {
  background: #fff;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 10px 30px rgba(15, 23, 42, 0.06);
}

.label {
  color: #64748b;
  font-size: 14px;
}

.value {
  margin: 12px 0 8px;
  font-size: 32px;
  font-weight: 700;
}

.hint {
  color: #94a3b8;
  font-size: 13px;
}
</style>
