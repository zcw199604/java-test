<template>
  <DataPanel title="管理中心" description="当前管理中心聚合系统运行、基础管理与报表入口。">
    <div class="admin-grid">
      <RouterLink class="admin-link" to="/system/users">用户管理</RouterLink>
      <RouterLink class="admin-link" to="/system/roles">角色权限</RouterLink>
      <RouterLink class="admin-link" to="/catalog/products">商品管理</RouterLink>
      <RouterLink class="admin-link" to="/supplier/list">供应商管理</RouterLink>
      <RouterLink class="admin-link" to="/customer/list">客户管理</RouterLink>
      <RouterLink class="admin-link" to="/report/center">报表中心</RouterLink>
      <div class="hint">当前版本已完成采销存与报表模块的前后端最小闭环。</div>
    </div>
  </DataPanel>
</template>

<script setup>
import { RouterLink } from 'vue-router'
import DataPanel from '../components/DataPanel.vue'
</script>

<style scoped>
.admin-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}
.admin-link, .hint {
  display: block;
  padding: 18px;
  background: #f8fafc;
  border-radius: 14px;
}
.admin-link {
  color: #2563eb;
  font-weight: 700;
}
</style>
