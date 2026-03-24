import { createRouter, createWebHistory } from 'vue-router'
import { useAppStore } from '../stores/app'
import { fetchProfile } from '../api/auth'
import { getToken, clearToken } from '../api/http'
import MainLayout from '../layout/MainLayout.vue'
import LoginView from '../views/auth/LoginView.vue'
import DashboardView from '../views/dashboard/DashboardView.vue'
import PurchaseView from '../views/PurchaseView.vue'
import InventoryView from '../views/InventoryView.vue'
import SalesView from '../views/SalesView.vue'
import AdminView from '../views/AdminView.vue'
import UserListView from '../views/system/UserListView.vue'
import RoleListView from '../views/system/RoleListView.vue'
import ProductListView from '../views/catalog/ProductListView.vue'
import SupplierListView from '../views/supplier/SupplierListView.vue'
import CustomerListView from '../views/customer/CustomerListView.vue'
import ReportCenterView from '../views/report/ReportCenterView.vue'

const routes = [
  {
    path: '/login',
    name: 'login',
    component: LoginView,
    meta: { public: true }
  },
  {
    path: '/',
    component: MainLayout,
    children: [
      { path: '', redirect: '/dashboard' },
      { path: 'dashboard', name: 'dashboard', component: DashboardView },
      { path: 'purchase', name: 'purchase', component: PurchaseView },
      { path: 'inventory', name: 'inventory', component: InventoryView },
      { path: 'sales', name: 'sales', component: SalesView },
      { path: 'admin', name: 'admin', component: AdminView },
      { path: 'report/center', name: 'report-center', component: ReportCenterView },
      { path: 'system/users', name: 'system-users', component: UserListView },
      { path: 'system/roles', name: 'system-roles', component: RoleListView },
      { path: 'catalog/products', name: 'catalog-products', component: ProductListView },
      { path: 'supplier/list', name: 'supplier-list', component: SupplierListView },
      { path: 'customer/list', name: 'customer-list', component: CustomerListView }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to) => {
  const appStore = useAppStore()
  const token = getToken()

  if (to.meta.public) {
    if (token && to.path === '/login') {
      return '/dashboard'
    }
    return true
  }

  if (!token) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }

  if (!appStore.initialized) {
    try {
      const result = await fetchProfile()
      appStore.setProfile(result.data)
    } catch (error) {
      clearToken()
      appStore.clearProfile()
      return { path: '/login', query: { redirect: to.fullPath } }
    }
  }

  return true
})

export default router
