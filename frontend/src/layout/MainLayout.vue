<template>
  <el-container class="app-shell">
    <el-aside :width="appStore.sidebarCollapsed ? '88px' : '260px'" class="app-aside" v-if="!isMobile">
      <div class="brand-panel">
        <div class="brand-mark">TC</div>
        <div v-show="!appStore.sidebarCollapsed">
          <div class="brand-title">{{ appStore.systemName }}</div>
          <div class="brand-subtitle">智慧采销存 · 合规追溯</div>
        </div>
      </div>
      <MenuTree :groups="appStore.menuGroups" :active-path="route.path" :collapse="appStore.sidebarCollapsed" />
      <div class="aside-footer" v-show="!appStore.sidebarCollapsed">
        <div>{{ appStore.userName }}</div>
        <span>{{ appStore.roleName }}</span>
      </div>
    </el-aside>

    <el-container>
      <el-header class="app-header">
        <div class="header-left">
          <el-button circle @click="toggleSidebar">
            <el-icon><Fold v-if="!isMobile" /><Expand v-else /></el-icon>
          </el-button>
          <div>
            <div class="page-title">{{ String(route.meta?.title || '驾驶舱') }}</div>
            <div class="page-desc">{{ pageDescription }}</div>
          </div>
        </div>
        <div class="header-right">
          <el-switch v-model="darkMode" inline-prompt active-text="暗" inactive-text="亮" @change="appStore.setDarkMode" />
          <el-popover placement="bottom-end" width="360" trigger="click">
            <template #reference>
              <el-badge :value="appStore.unreadCount" :hidden="!appStore.unreadCount">
                <el-button circle>
                  <el-icon><Bell /></el-icon>
                </el-button>
              </el-badge>
            </template>
            <div class="notify-header">
              <span>预警消息</span>
              <el-button link type="primary" @click="markAllRead">全部已读</el-button>
            </div>
            <el-empty v-if="!appStore.notifications.length" description="暂无预警消息" />
            <div v-else class="notify-list">
              <div v-for="item in appStore.notifications" :key="item.id" class="notify-item" @click="markRead(item.id)">
                <strong>{{ item.title }}</strong>
                <p>{{ item.content }}</p>
              </div>
            </div>
          </el-popover>
          <el-dropdown>
            <div class="user-chip">
              <el-avatar :size="36">{{ appStore.userName.slice(0, 1) }}</el-avatar>
              <div>
                <div>{{ appStore.userName }}</div>
                <span>{{ appStore.roleName }}</span>
              </div>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="router.push('/profile')">个人中心</el-dropdown-item>
                <el-dropdown-item @click="handleLogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="app-main">
        <RouterView />
      </el-main>
    </el-container>
  </el-container>

  <el-drawer v-model="drawerVisible" size="260px" :with-header="false" v-if="isMobile">
    <div class="brand-panel mobile">
      <div class="brand-mark">TC</div>
      <div>
        <div class="brand-title">{{ appStore.systemName }}</div>
        <div class="brand-subtitle">智慧采销存 · 合规追溯</div>
      </div>
    </div>
    <MenuTree :groups="appStore.menuGroups" :active-path="route.path" />
  </el-drawer>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute, useRouter, RouterView } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { logout } from '../api/auth'
import { fetchMessages, readMessage } from '../api/message'
import { clearToken } from '../api/http'
import { useAppStore } from '../stores/app'
import MenuTree from '../components/MenuTree.vue'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()
const isMobile = ref(false)
const drawerVisible = ref(false)
const darkMode = computed({
  get: () => appStore.darkMode,
  set: (value: boolean) => appStore.setDarkMode(value)
})

const pageDescription = computed(() => String(route.meta?.description || '围绕采购、销售、库存与合规追溯的业务协同工作台。'))

const handleResize = () => {
  isMobile.value = window.innerWidth < 992
  if (!isMobile.value) drawerVisible.value = false
}

const toggleSidebar = () => {
  if (isMobile.value) {
    drawerVisible.value = !drawerVisible.value
    return
  }
  appStore.toggleSidebar()
}

const loadMessages = async () => {
  const result = await fetchMessages().catch(() => ({ data: [] }))
  appStore.setNotifications((result.data || []).map((item: any) => ({
    id: String(item.id),
    title: item.title,
    content: item.content,
    type: item.messageType,
    read: Boolean(item.isRead)
  })))
}

const markRead = async (id: string) => {
  appStore.markNotificationRead(id)
  await readMessage(id).catch(() => undefined)
}

const markAllRead = async () => {
  const pending = appStore.notifications.filter((item) => !item.read)
  appStore.markAllNotificationsRead()
  await Promise.all(pending.map((item) => readMessage(item.id).catch(() => undefined)))
}

const handleLogout = async () => {
  await ElMessageBox.confirm('确认退出当前账号吗？', '退出登录', { type: 'warning' })
  try {
    await logout()
  } catch (error) {
    // 忽略退出接口异常
  }
  clearToken()
  appStore.clearProfile()
  router.push('/login')
}

onMounted(() => {
  handleResize()
  window.addEventListener('resize', handleResize)
  loadMessages().catch(() => undefined)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
})
</script>
