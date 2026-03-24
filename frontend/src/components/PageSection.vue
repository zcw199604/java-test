<template>
  <el-card class="page-section" shadow="never">
    <template #header>
      <div class="section-header">
        <div>
          <div class="section-title">{{ title }}</div>
          <div v-if="description" class="section-description">{{ description }}</div>
        </div>
        <div class="section-actions">
          <slot name="actions" />
        </div>
      </div>
    </template>
    <slot />
  </el-card>
</template>

<script setup>
defineProps({
  title: { type: String, required: true },
  description: { type: String, default: '' }
})
</script>
