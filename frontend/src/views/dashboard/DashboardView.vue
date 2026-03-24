<template>
  <div class="dashboard">
    <div class="stats">
      <StatCard label="采购单数量" :value="summary.purchaseCount" hint="来自采购单真实统计数据" />
      <StatCard label="库存记录数" :value="summary.inventoryCount" hint="基于库存台账的实时统计" />
      <StatCard label="销售单数量" :value="summary.salesCount" hint="来自销售单真实统计数据" />
      <StatCard label="预警数量" :value="summary.warningCount" hint="实时统计低于阈值的库存项目" />
    </div>

    <DataPanel title="快捷模块" description="保留原有业务入口，并补充系统管理相关入口。">
      <div class="modules">
        <div v-for="item in summary.modules" :key="item.key" class="module-card">
          <h3>{{ item.title }}</h3>
          <p>{{ item.description }}</p>
          <RouterLink :to="item.route">进入模块 →</RouterLink>
        </div>
      </div>
    </DataPanel>
  </div>
</template>

<script setup>
import { onMounted, reactive } from 'vue'
import { RouterLink } from 'vue-router'
import StatCard from '../../components/StatCard.vue'
import DataPanel from '../../components/DataPanel.vue'
import { fetchDashboardSummary } from '../../api/dashboard'

const summary = reactive({
  purchaseCount: 0,
  inventoryCount: 0,
  salesCount: 0,
  warningCount: 0,
  modules: []
})

const fallbackModules = [
  { key: 'users', title: '用户管理', description: '系统用户与角色维护。', route: '/system/users' },
  { key: 'roles', title: '角色权限', description: '角色与权限配置。', route: '/system/roles' },
  { key: 'products', title: '商品管理', description: '烟草商品、品类与价格。', route: '/catalog/products' },
  { key: 'suppliers', title: '供应商管理', description: '供应商和联系人资料。', route: '/supplier/list' },
  { key: 'purchase', title: '采购管理', description: '采购创建、到货、入库。', route: '/purchase' },
  { key: 'inventory', title: '库存管理', description: '库存台账、盘点、预警。', route: '/inventory' },
  { key: 'sales', title: '销售管理', description: '销售订单、出库、回款。', route: '/sales' },
  { key: 'admin', title: '管理中心', description: '系统配置与运营概览。', route: '/admin' },
  { key: 'report', title: '报表中心', description: '采购、销售、库存汇总与趋势。', route: '/report/center' }
]

const loadSummary = async () => {
  const response = await fetchDashboardSummary()
  Object.assign(summary, response.data)
  summary.modules = [...response.data.modules, ...fallbackModules.filter(item => !response.data.modules.find(m => m.route === item.route))]
}

onMounted(() => {
  loadSummary().catch(() => {
    summary.modules = fallbackModules
  })
})
</script>

<style scoped>
.dashboard {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.stats {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.modules {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.module-card {
  background: #f8fafc;
  border-radius: 16px;
  padding: 20px;
}

.module-card h3 {
  margin-top: 0;
}

.module-card p {
  color: #64748b;
  min-height: 44px;
}

.module-card a {
  color: #2563eb;
  font-weight: 600;
}
</style>
