<template>
  <div class="layout">
    <aside class="sidebar">
      <div class="brand">{{ appStore.systemName }}</div>
      <div class="user-card" v-if="appStore.profile">
        <strong>{{ appStore.profile.realName || appStore.profile.username }}</strong>
        <span>{{ appStore.profile.roleName }}</span>
      </div>
      <nav class="nav">
        <template v-for="item in menuItems" :key="item.path">
          <RouterLink :to="item.path" class="nav-item">
            <span class="nav-title">{{ item.title }}</span>
            <span class="nav-desc">{{ item.desc }}</span>
          </RouterLink>
        </template>
      </nav>
      <button class="logout-btn" @click="handleLogout">退出登录</button>
    </aside>
    <main class="content">
      <header class="header">
        <div>
          <h1>{{ currentMeta.title }}</h1>
          <p>{{ currentMeta.desc }}</p>
        </div>
      </header>
      <section class="page-body">
        <RouterView />
      </section>
    </main>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter, RouterLink, RouterView } from 'vue-router'
import { useAppStore } from '../stores/app'
import { logout } from '../api/auth'
import { clearToken } from '../api/http'

const appStore = useAppStore()
const route = useRoute()
const router = useRouter()

const menuItems = [
  { path: '/dashboard', title: '首页总览', desc: '概览统计与模块入口' },
  { path: '/system/users', title: '用户管理', desc: '系统账号与启停用状态' },
  { path: '/system/roles', title: '角色权限', desc: '角色与权限分配总览' },
  { path: '/catalog/products', title: '商品管理', desc: '烟草品类、价格与库存阈值' },
  { path: '/supplier/list', title: '供应商管理', desc: '供应商资料与联系人信息' },
  { path: '/customer/list', title: '客户管理', desc: '零售客户资料与销售关联' },
  { path: '/purchase', title: '采购管理', desc: '采购单创建、跟踪与到货入库' },
  { path: '/inventory', title: '库存管理', desc: '库存台账、预警、盘点与调拨' },
  { path: '/sales', title: '销售管理', desc: '销售订单、出库与回款' },
  { path: '/report/center', title: '报表中心', desc: '采购、销售、库存汇总与趋势' },
  { path: '/admin', title: '管理中心', desc: '系统配置与运营概览' }
]

const pageMetaMap = {
  '/dashboard': { title: '首页总览', desc: '查看采购、库存、销售和预警等核心概览数据。' },
  '/system/users': { title: '用户管理', desc: '维护系统账号、角色归属与账号状态。' },
  '/system/roles': { title: '角色权限', desc: '查看系统角色与权限配置。' },
  '/catalog/products': { title: '商品管理', desc: '维护烟草商品、价格与安全库存阈值。' },
  '/supplier/list': { title: '供应商管理', desc: '维护供应商资料与联系人信息。' },
  '/customer/list': { title: '客户管理', desc: '维护零售客户资料并支撑销售业务。' },
  '/purchase': { title: '采购管理', desc: '处理采购创建、状态跟踪与到货入库。' },
  '/inventory': { title: '库存管理', desc: '查看库存台账、流水、预警和盘点调拨。' },
  '/sales': { title: '销售管理', desc: '管理销售订单、出库与回款。' },
  '/report/center': { title: '报表中心', desc: '查看采销存汇总与近 7 天趋势。' },
  '/admin': { title: '管理中心', desc: '查看报表中心与系统运行概况。' }
}

const currentMeta = computed(() => pageMetaMap[route.path] || pageMetaMap['/dashboard'])

const handleLogout = async () => {
  try { await logout() } catch (error) {}
  clearToken()
  appStore.clearProfile()
  router.push('/login')
}
</script>

<style scoped>
.layout { display: grid; grid-template-columns: 300px 1fr; min-height: 100vh; }
.sidebar { background: linear-gradient(180deg, #0f172a, #1e293b); color: #fff; padding: 24px 18px; display: flex; flex-direction: column; }
.brand { font-size: 22px; font-weight: 700; line-height: 1.5; margin-bottom: 16px; }
.user-card { display: flex; flex-direction: column; gap: 4px; margin-bottom: 18px; padding: 14px 16px; border-radius: 14px; background: rgba(255, 255, 255, 0.08); }
.user-card span { color: rgba(255, 255, 255, 0.75); font-size: 13px; }
.nav { display: flex; flex-direction: column; gap: 12px; flex: 1; }
.nav-item { display: flex; flex-direction: column; gap: 4px; padding: 14px 16px; border-radius: 12px; background: rgba(255, 255, 255, 0.07); }
.nav-item.router-link-active { background: rgba(59, 130, 246, 0.35); }
.nav-title { font-size: 16px; font-weight: 600; }
.nav-desc { font-size: 13px; color: rgba(255, 255, 255, 0.75); }
.logout-btn { margin-top: 20px; border: none; background: #ef4444; color: #fff; border-radius: 12px; padding: 12px 16px; cursor: pointer; }
.content { padding: 24px 32px; }
.header { background: #fff; border-radius: 16px; padding: 24px 28px; box-shadow: 0 12px 32px rgba(15, 23, 42, 0.08); }
.header h1 { margin: 0 0 8px; }
.header p { margin: 0; color: #64748b; }
.page-body { margin-top: 24px; }
</style>
