import type { RouteRecordRaw } from 'vue-router'
import MainLayout from '../layout/MainLayout.vue'

export const constantChildren: RouteRecordRaw[] = [
  {
    path: '/dashboard',
    name: 'dashboard',
    component: () => import('../views/dashboard/DashboardView.vue'),
    meta: { title: '驾驶舱', description: '查看核心经营指标、预警和业务入口。', icon: 'Odometer', group: 'workspace', permission: 'dashboard:view' }
  },
  {
    path: '/profile',
    name: 'profile',
    component: () => import('../views/profile/ProfileView.vue'),
    meta: { title: '个人中心', description: '维护个人资料与查看权限范围。', icon: 'User', group: 'workspace', permission: 'profile:view' }
  },
  {
    path: '/403',
    name: 'forbidden',
    component: () => import('../views/common/ForbiddenView.vue'),
    meta: { title: '无权访问', description: '当前账号无权限访问该页面。', icon: 'CircleClose', group: 'workspace', hideInMenu: true }
  },
  { path: '/purchase', redirect: '/purchase/order', meta: { hideInMenu: true } },
  { path: '/inventory', redirect: '/inventory/list', meta: { hideInMenu: true } },
  { path: '/sales', redirect: '/sale/order', meta: { hideInMenu: true } },
  { path: '/admin', redirect: '/admin/account', meta: { hideInMenu: true } },
  { path: '/report/center', redirect: '/report/dashboard', meta: { hideInMenu: true } }
]

export const asyncChildren: RouteRecordRaw[] = [
  {
    path: '/admin/account',
    name: 'admin-account',
    component: () => import('../views/admin/AccountListView.vue'),
    meta: { title: '账号管理', description: '统一维护平台账号、角色和状态。', icon: 'UserFilled', group: 'admin', permission: 'admin:account:view' }
  },
  {
    path: '/admin/role',
    name: 'admin-role',
    component: () => import('../views/admin/RolePermissionView.vue'),
    meta: { title: '角色权限', description: '按角色分配页面与操作权限。', icon: 'Finished', group: 'admin', permission: 'admin:role:view' }
  },
  {
    path: '/admin/base-data',
    name: 'admin-base-data',
    component: () => import('../views/admin/BaseDataView.vue'),
    meta: { title: '基础资料', description: '统一浏览品类、供应商、客户和仓库信息。', icon: 'Tickets', group: 'admin', permission: 'admin:base:view' }
  },
  {
    path: '/admin/log',
    name: 'admin-log',
    component: () => import('../views/admin/LogListView.vue'),
    meta: { title: '操作日志', description: '按时间与模块查看系统操作记录。', icon: 'Document', group: 'admin', permission: 'admin:log:view' }
  },
  {
    path: '/admin/config',
    name: 'admin-config',
    component: () => import('../views/admin/SystemConfigView.vue'),
    meta: { title: '系统配置', description: '维护预警阈值和消息通知规则。', icon: 'Tools', group: 'admin', permission: 'admin:config:view' }
  },
  {
    path: '/purchase/order',
    name: 'purchase-order',
    component: () => import('../views/purchase/PurchaseOrderListView.vue'),
    meta: { title: '采购订单', description: '查看采购订单、到货和入库进度。', icon: 'ShoppingTrolley', group: 'purchase', permission: 'purchase:view' }
  },
  {
    path: '/purchase/order/create',
    name: 'purchase-order-create',
    component: () => import('../views/purchase/PurchaseOrderFormView.vue'),
    meta: { title: '新建采购单', description: '通过步骤表单创建采购订单。', hideInMenu: true, permission: 'purchase:edit' }
  },
  {
    path: '/purchase/order/:id/edit',
    name: 'purchase-order-edit',
    component: () => import('../views/purchase/PurchaseOrderFormView.vue'),
    meta: { title: '编辑采购单', description: '调整采购订单并重新提交。', hideInMenu: true, permission: 'purchase:edit' }
  },
  {
    path: '/purchase/inbound',
    name: 'purchase-inbound',
    component: () => import('../views/purchase/PurchaseInboundView.vue'),
    meta: { title: '采购入库', description: '按待入库订单完成批次登记。', icon: 'Download', group: 'purchase', permission: 'purchase:edit' }
  },
  {
    path: '/purchase/analysis',
    name: 'purchase-analysis',
    component: () => import('../views/purchase/PurchaseAnalysisView.vue'),
    meta: { title: '采购分析', description: '查看采购结构和供应商占比。', icon: 'PieChart', group: 'purchase', permission: 'purchase:view' }
  },
  {
    path: '/sale/order',
    name: 'sale-order',
    component: () => import('../views/sale/SaleOrderListView.vue'),
    meta: { title: '销售订单', description: '管理销售订单、出库与回款。', icon: 'Sell', group: 'sale', permission: 'sale:view' }
  },
  {
    path: '/sale/order/create',
    name: 'sale-order-create',
    component: () => import('../views/sale/SaleOrderFormView.vue'),
    meta: { title: '新建销售单', description: '步骤式创建销售订单并校验库存。', hideInMenu: true, permission: 'sale:edit' }
  },
  {
    path: '/sale/order/:id/edit',
    name: 'sale-order-edit',
    component: () => import('../views/sale/SaleOrderFormView.vue'),
    meta: { title: '编辑销售单', description: '编辑销售订单并重新确认。', hideInMenu: true, permission: 'sale:edit' }
  },
  {
    path: '/sale/outbound',
    name: 'sale-outbound',
    component: () => import('../views/sale/SaleOutboundView.vue'),
    meta: { title: '销售出库', description: '处理待出库销售单。', icon: 'Upload', group: 'sale', permission: 'sale:edit' }
  },
  {
    path: '/sale/performance',
    name: 'sale-performance',
    component: () => import('../views/sale/SalePerformanceView.vue'),
    meta: { title: '销售绩效', description: '查看销售趋势、达成率和区域表现。', icon: 'TrendCharts', group: 'sale', permission: 'sale:view' }
  },
  {
    path: '/inventory/list',
    name: 'inventory-list',
    component: () => import('../views/inventory/InventoryListView.vue'),
    meta: { title: '库存总览', description: '查看实时库存与预警阈值。', icon: 'Box', group: 'inventory', permission: 'inventory:view' }
  },
  {
    path: '/inventory/flow',
    name: 'inventory-flow',
    component: () => import('../views/inventory/InventoryFlowView.vue'),
    meta: { title: '库存流水', description: '查看入库、出库和调拨记录。', icon: 'Connection', group: 'inventory', permission: 'inventory:edit' }
  },
  {
    path: '/inventory/check',
    name: 'inventory-check',
    component: () => import('../views/inventory/InventoryCheckView.vue'),
    meta: { title: '库存盘点', description: '批量盘点并生成盈亏报告。', icon: 'Checked', group: 'inventory', permission: 'inventory:edit' }
  },
  {
    path: '/inventory/ledger',
    name: 'inventory-ledger',
    component: () => import('../views/inventory/InventoryLedgerView.vue'),
    meta: { title: '库存追溯台账', description: '按时间轴查看库存变更轨迹。', icon: 'Memo', group: 'inventory', permission: 'inventory:view' }
  },
  {
    path: '/report/dashboard',
    name: 'report-dashboard',
    component: () => import('../views/report/ReportDashboardView.vue'),
    meta: { title: '经营大屏', description: '集中查看采购、销售和库存经营指标。', icon: 'DataBoard', group: 'report', permission: 'report:view' }
  },
  {
    path: '/trace/query',
    name: 'trace-query',
    component: () => import('../views/trace/TraceQueryView.vue'),
    meta: { title: '合规追溯', description: '按订单号、商品和批次检索链路。', icon: 'Share', group: 'report', permission: 'trace:view' }
  },
  {
    path: '/audit/exception',
    name: 'audit-exception',
    component: () => import('../views/audit/ExceptionAuditView.vue'),
    meta: { title: '异常审核', description: '超级管理员审核异常单据和差异。', icon: 'Warning', group: 'report', permission: 'audit:view' }
  }
]

export const rootRoute: RouteRecordRaw = {
  path: '/',
  name: 'root',
  component: MainLayout,
  redirect: '/dashboard',
  children: constantChildren
}

export const filterAsyncChildren = (permissions: string[] = []): RouteRecordRaw[] => {
  return asyncChildren.filter((route) => {
    const permission = route.meta?.permission as string | string[] | undefined
    if (!permission) return true
    return permissions.includes('*:*') || (Array.isArray(permission)
      ? permission.some((item) => permissions.includes(item))
      : permissions.includes(permission))
  })
}

export const allAllowedChildren = (permissions: string[] = []): RouteRecordRaw[] => [
  ...constantChildren,
  ...filterAsyncChildren(permissions)
]
