import type { App, DirectiveBinding } from 'vue'
import { useAppStore } from '../stores/app'
import { hasPermission } from '../utils/access'

const toggleElement = (element: HTMLElement, allowed: boolean) => {
  if (allowed) {
    element.style.removeProperty('display')
    return
  }
  element.style.display = 'none'
}

const checkPermission = (element: HTMLElement, binding: DirectiveBinding<string | string[]>) => {
  const appStore = useAppStore()
  toggleElement(element, hasPermission(appStore.permissions, binding.value))
}

export const permissionDirective = {
  mounted(element: HTMLElement, binding: DirectiveBinding<string | string[]>) {
    checkPermission(element, binding)
  },
  updated(element: HTMLElement, binding: DirectiveBinding<string | string[]>) {
    checkPermission(element, binding)
  }
}

export const registerPermissionDirective = (app: App) => {
  app.directive('permission', permissionDirective)
}
