import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import 'element-plus/dist/index.css'
import App from './App.vue'
import router from './router'
import { registerPermissionDirective } from './directives/permission'
import './assets/main.css'

const app = createApp(App)
const pinia = createPinia()

Object.entries(ElementPlusIconsVue).forEach(([key, component]) => {
  app.component(key, component)
})

app.use(pinia)
registerPermissionDirective(app)
app.use(router)
app.use(ElementPlus)
app.mount('#app')
