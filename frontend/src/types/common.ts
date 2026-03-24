export interface UserProfile {
  username?: string
  realName?: string
  roleName?: string
  role_name?: string
  roleCode?: string
  role_code?: string
  role?: string
  permissions?: string[]
}

export interface NotificationItem {
  id: string
  title: string
  content: string
  type?: string
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
