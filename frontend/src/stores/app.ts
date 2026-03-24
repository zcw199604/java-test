import { defineStore } from 'pinia'
import { buildMenuGroups, resolvePermissions, resolveRoleCode } from '../utils/access'
import type { MenuGroup, NotificationItem, UserProfile } from '../types/common'

const SYSTEM_NAME = '烟草采销存协同管理平台'

interface AppState {
  systemName: string
  sidebarCollapsed: boolean
  initialized: boolean
  routesReady: boolean
  profile: UserProfile | null
  roleCode: string
  permissions: string[]
  menuGroups: MenuGroup[]
  notifications: NotificationItem[]
  darkMode: boolean
}

export const useAppStore = defineStore('app', {
  state: (): AppState => ({
    systemName: SYSTEM_NAME,
    sidebarCollapsed: false,
    initialized: false,
    routesReady: false,
    profile: null,
    roleCode: '',
    permissions: [],
    menuGroups: [],
    notifications: [],
    darkMode: false
  }),
  getters: {
    userName: (state): string => state.profile?.realName || state.profile?.username || '未登录用户',
    roleName: (state): string => state.profile?.roleName || state.profile?.role_name || state.roleCode || '访客',
    unreadCount: (state): number => state.notifications.filter((item) => !item.read).length
  },
  actions: {
    toggleSidebar(): void {
      this.sidebarCollapsed = !this.sidebarCollapsed
    },
    setDarkMode(value: boolean): void {
      this.darkMode = Boolean(value)
      document.documentElement.classList.toggle('dark', this.darkMode)
    },
    bootstrapProfile(profile: UserProfile): void {
      this.profile = profile
      this.roleCode = resolveRoleCode(profile)
      this.permissions = resolvePermissions(profile)
      this.initialized = true
    },
    clearProfile(): void {
      this.profile = null
      this.roleCode = ''
      this.permissions = []
      this.menuGroups = []
      this.notifications = []
      this.initialized = true
      this.routesReady = false
    },
    setRoutes(routes: Array<{ path: string; name?: string | symbol; meta?: Record<string, unknown> }>): void {
      this.menuGroups = buildMenuGroups(routes)
      this.routesReady = true
    },
    setNotifications(list: Array<Partial<NotificationItem> & { id?: string }>): void {
      this.notifications = list.map((item, index) => ({
        id: item.id || `${Date.now()}-${index}`,
        title: item.title || '系统消息',
        content: item.content || '请及时处理相关事项',
        type: item.type || 'warning',
        read: Boolean(item.read)
      }))
    },
    markNotificationRead(id: string): void {
      this.notifications = this.notifications.map((item) => item.id === id ? { ...item, read: true } : item)
    },
    markAllNotificationsRead(): void {
      this.notifications = this.notifications.map((item) => ({ ...item, read: true }))
    }
  }
})
