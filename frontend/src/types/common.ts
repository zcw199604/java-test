export interface UserProfile {
  id?: number | string
  username?: string
  realName?: string
  roleName?: string
  role_name?: string
  roleCode?: string
  role_code?: string
  role?: string
  status?: string
  permissions?: string[]
}

export interface NotificationItem {
  id: string
  title: string
  content: string
  type?: string
  bizType?: string
  bizId?: string | number
  createdAt?: string
  read?: boolean
}

export interface MenuItem {
  path: string
  title: string
  icon: string
}

export interface MenuGroup {
  key: string
  title: string
  icon: string
  children: MenuItem[]
}

export interface RouteMetaConfig {
  title?: string
  description?: string
  icon?: string
  group?: string
  permission?: string | string[]
  public?: boolean
  hideInMenu?: boolean
}
