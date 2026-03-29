export const roleNameMap = {
  SUPER_ADMIN: '超级管理员',
  ADMIN: '普通管理员',
  PURCHASER: '采购专员',
  SELLER: '销售专员',
  KEEPER: '库管人员'
}

export const permissionModuleNameMap = {
  dashboard: '驾驶舱',
  system: '系统管理',
  purchase: '采购管理',
  sales: '销售管理',
  inventory: '库存管理',
  report: '统计报表',
  trace: '合规追溯',
  bulletin: '销售公告',
  audit: '异常审核',
  message: '消息中心',
  auth: '认证鉴权',
  other: '其他'
}

export const operationModuleNameMap = {
  AUTH: '认证鉴权',
  SYSTEM: '系统管理',
  PURCHASE: '采购管理',
  SALES: '销售管理',
  INVENTORY: '库存管理',
  REPORT: '统计报表',
  MESSAGE: '消息中心'
}

export const operationActionNameMap = {
  LOGIN: '登录',
  LOGOUT: '退出登录',
  CREATE: '创建',
  UPDATE: '更新',
  DELETE: '删除',
  AUDIT: '审核',
  CANCEL: '取消',
  RECEIVE: '到货登记',
  INBOUND: '采购入库',
  OUTBOUND: '销售出库',
  PAYMENT: '销售回款',
  CREATE_USER: '新增用户',
  UPDATE_USER: '编辑用户',
  UPDATE_USER_STATUS: '变更用户状态',
  UPDATE_PROFILE: '更新个人信息',
  CHANGE_PASSWORD: '修改密码',
  CREATE_WAREHOUSE: '新增仓库',
  UPDATE_WAREHOUSE: '编辑仓库',
  UPDATE_WAREHOUSE_STATUS: '变更仓库状态'
}

export const bizTypeNameMap = {
  USER: '用户',
  USER_SESSION: '用户会话',
  PURCHASE: '采购单',
  SALES: '销售单',
  INVENTORY: '库存',
  WAREHOUSE: '仓库',
  REPORT: '报表',
  MESSAGE: '消息'
}

export const loginStatusNameMap = {
  SUCCESS: '成功',
  FAILED: '失败',
  LOCKED: '锁定'
}

export const translateRoleName = (roleCode, fallback = '') => roleNameMap[String(roleCode || '').toUpperCase()] || fallback || roleCode || '--'
export const translatePermissionModule = (moduleCode, fallback = '') => permissionModuleNameMap[String(moduleCode || '').toLowerCase()] || fallback || moduleCode || '--'
export const translateOperationModule = (moduleCode, fallback = '') => operationModuleNameMap[String(moduleCode || '').toUpperCase()] || fallback || moduleCode || '--'
export const translateOperationAction = (actionCode, fallback = '') => operationActionNameMap[String(actionCode || '').toUpperCase()] || fallback || actionCode || '--'
export const translateBizType = (bizType, fallback = '') => bizTypeNameMap[String(bizType || '').toUpperCase()] || fallback || bizType || '--'
export const translateLoginStatus = (status, fallback = '') => loginStatusNameMap[String(status || '').toUpperCase()] || fallback || status || '--'
