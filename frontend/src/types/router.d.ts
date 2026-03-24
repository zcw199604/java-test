import 'vue-router'

declare module 'vue-router' {
  interface RouteMeta {
    title?: string
    description?: string
    icon?: string
    group?: string
    permission?: string | string[]
    public?: boolean
    hideInMenu?: boolean
  }
}
