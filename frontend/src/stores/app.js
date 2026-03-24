import { defineStore } from 'pinia'

export const useAppStore = defineStore('app', {
  state: () => ({
    systemName: '烟草采销存协同管理平台',
    sidebarCollapsed: false,
    initialized: false,
    profile: null,
    permissions: [],
    menus: []
  }),
  getters: {
    isLoggedIn: (state) => Boolean(state.profile)
  },
  actions: {
    toggleSidebar() {
      this.sidebarCollapsed = !this.sidebarCollapsed
    },
    setProfile(profile) {
      this.profile = profile
      this.permissions = profile?.permissions || []
      this.menus = profile?.menus || []
      this.initialized = true
    },
    clearProfile() {
      this.profile = null
      this.permissions = []
      this.menus = []
      this.initialized = true
    }
  }
})
