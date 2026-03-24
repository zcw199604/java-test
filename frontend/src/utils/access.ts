import type { MenuGroup, UserProfile } from '../types/common'

type MenuSeed = Omit<MenuGroup, 'children'>

const adminPermissions = [
  '*:*',
  'dashboard:view',
  'profile:view',
  'admin:account:view',
  'admin:role:view',
  'admin:base:view',
  'admin:log:view',
  'admin:config:view',
  'purchase:view',
  'purchase:edit',
  'sale:view',
  'sale:edit',
  'inventory:view',
  'inventory:edit',
  'report:view',
  'trace:view',
  'audit:view'
]

export const rolePermissionMap: Record<string, string[]> = {
  SUPER_ADMIN: adminPermissions,
  ADMIN: adminPermissions,
  PURCHASER: ['dashboard:view', 'profile:view', 'purchase:view', 'purchase:edit', 'report:view', 'trace:view'],
  SELLER: ['dashboard:view', 'profile:view', 'sale:view', 'sale:edit', 'report:view', 'trace:view'],
  KEEPER: ['dashboard:view', 'profile:view', 'inventory:view', 'inventory:edit', 'report:view', 'trace:view']
}

const backendToFrontendPermissionMap: Record<string, string[]> = {
  'system:user:view': ['admin:account:view'],
  'system:user:edit': ['admin:account:view'],
  'system:role:edit': ['admin:role:view'],
  'system:config:edit': ['admin:config:view'],
  'purchase:order:edit': ['purchase:view', 'purchase:edit'],
  'purchase:inbound': ['purchase:edit'],
  'sales:order:edit': ['sale:view', 'sale:edit'],
  'sales:payment': ['sale:edit'],
  'inventory:manage': ['inventory:view', 'inventory:edit'],
  'report:view': ['report:view'],
  'logs:view': ['admin:log:view'],
  'message:view': ['dashboard:view'],
  '*:*': ['*:*']
}

export const menuGroups: MenuSeed[] = [
  { key: 'workspace', title: '工作台', icon: 'House' },
  { key: 'admin', title: '超级管理员', icon: 'Setting' },
  { key: 'purchase', title: '采购管理', icon: 'ShoppingCart' },
  { key: 'sale', title: '销售管理', icon: 'Sell' },
  { key: 'inventory', title: '库存管理', icon: 'Box' },
  { key: 'report', title: '统计与追溯', icon: 'DataAnalysis' }
]

export const resolveRoleCode = (profile: UserProfile = {}): string => {
  const roleCode = profile.roleCode || profile.role_code || profile.role || ''
  if (roleCode) return String(roleCode).toUpperCase()
  if (profile.username === 'admin') return 'SUPER_ADMIN'
  if (profile.username === 'buyer') return 'PURCHASER'
  if (profile.username === 'seller') return 'SELLER'
  if (profile.username === 'keeper') return 'KEEPER'
  return 'SUPER_ADMIN'
}

export const resolvePermissions = (profile: UserProfile = {}): string[] => {
  const normalized = new Set<string>()
  normalized.add('dashboard:view')
  normalized.add('profile:view')

  const raw = Array.isArray(profile.permissions) ? profile.permissions.filter(Boolean) : []
  raw.forEach((item) => {
    normalized.add(item)
    ;(backendToFrontendPermissionMap[item] || []).forEach((mapped) => normalized.add(mapped))
  })

  rolePermissionMap[resolveRoleCode(profile)]?.forEach((item) => normalized.add(item))

  if (normalized.has('report:view')) {
    normalized.add('trace:view')
  }
  if (resolveRoleCode(profile) === 'SUPER_ADMIN') {
    normalized.add('audit:view')
    normalized.add('admin:base:view')
  }

  return Array.from(normalized)
}

export const hasPermission = (ownedPermissions: string[] = [], required?: string | string[]): boolean => {
  if (!required || (Array.isArray(required) && required.length === 0)) return true
  if (ownedPermissions.includes('*:*')) return true
  const requiredList = Array.isArray(required) ? required : [required]
  return requiredList.some((item) => ownedPermissions.includes(item))
}

export const buildMenuGroups = (routes: Array<{ path: string; name?: string | symbol; meta?: Record<string, unknown> }>): MenuGroup[] => {
  const groupMap = new Map<string, MenuGroup>(menuGroups.map((group) => [group.key, { ...group, children: [] }]))
  routes.forEach((route) => {
    if (route.meta?.hideInMenu) return
    const key = String(route.meta?.group || 'workspace')
    const targetGroup = groupMap.get(key)
    if (!targetGroup) return
    targetGroup.children.push({
      path: route.path,
      title: String(route.meta?.title || route.name || route.path),
      icon: String(route.meta?.icon || 'Grid')
    })
  })
  return Array.from(groupMap.values()).filter((group) => group.children.length > 0)
}
