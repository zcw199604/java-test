import { createRouter, createWebHistory, type RouteRecordNameGeneric, type RouteRecordRaw } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAppStore } from '../stores/app'
import { fetchProfile } from '../api/auth'
import { clearToken, getToken } from '../api/http'
import { allAllowedChildren, filterAsyncChildren, rootRoute } from './route-map'
import LoginView from '../views/auth/LoginView.vue'
import ResetPasswordView from '../views/auth/ResetPasswordView.vue'
import { hasPermission } from '../utils/access'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', name: 'login', component: LoginView, meta: { public: true, title: '登录' } },
    { path: '/reset-password', name: 'reset-password', component: ResetPasswordView, meta: { public: true, title: '重置密码' } },
    rootRoute,
    { path: '/:pathMatch(.*)*', redirect: '/dashboard' }
  ]
})

const installAsyncRoutes = (appStore: ReturnType<typeof useAppStore>) => {
  if (appStore.routesReady) return
  const allowed = filterAsyncChildren(appStore.permissions)
  allowed.forEach((route: RouteRecordRaw) => {
    const routeName = route.name as RouteRecordNameGeneric | undefined
    if (routeName && !router.hasRoute(routeName)) {
      router.addRoute('root', route)
    }
  })
  appStore.setRoutes(allAllowedChildren(appStore.permissions))
}

router.beforeEach(async (to) => {
  const appStore = useAppStore()
  const token = getToken()

  if (to.meta.public) {
    if (token && to.path === '/login') return '/dashboard'
    return true
  }

  if (!token) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }

  if (!appStore.profile) {
    try {
      const result = await fetchProfile()
      appStore.bootstrapProfile((result.data || {}) as Record<string, unknown>)
      installAsyncRoutes(appStore)
      return to.name ? true : to.fullPath
    } catch {
      clearToken()
      appStore.clearProfile()
      return { path: '/login', query: { redirect: to.fullPath } }
    }
  }

  installAsyncRoutes(appStore)

  if (!hasPermission(appStore.permissions, to.meta?.permission as string | string[] | undefined)) {
    ElMessage.warning('当前账号无权访问该页面，已跳转到无权限提示页')
    return '/403'
  }

  return true
})

export default router
