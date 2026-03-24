<template>
  <el-card class="kpi-card" shadow="hover">
    <div class="kpi-top">
      <span class="kpi-label">{{ label }}</span>
      <el-tag size="small" :type="tagType">{{ tagText }}</el-tag>
    </div>
    <div class="kpi-value">{{ value }}</div>
    <div class="kpi-foot">{{ hint }}</div>
  </el-card>
</template>

<script setup>
defineProps({
  label: String,
  value: [String, Number],
  hint: String,
  tagText: { type: String, default: '实时' },
  tagType: { type: String, default: 'success' }
})
</script>
