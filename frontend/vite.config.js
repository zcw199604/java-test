import { resolve } from 'node:path'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': resolve(__dirname, './src')
    }
  },
  build: {
    outDir: resolve(__dirname, '../backend/src/main/resources/static'),
    emptyOutDir: true,
    sourcemap: false,
    rollupOptions: {
      output: {
        manualChunks(id) {
          if (!id.includes('node_modules')) return
          if (id.includes('element-plus')) return 'vendor-element-plus'
          if (id.includes('echarts')) return 'vendor-echarts'
          if (id.includes('xlsx') || id.includes('file-saver')) return 'vendor-export'
          if (id.includes('@element-plus/icons-vue')) return 'vendor-icons'
          if (id.includes('vue-router')) return 'vendor-router'
          if (id.includes('pinia')) return 'vendor-store'
          if (id.includes('axios')) return 'vendor-http'
          if (id.includes('vue')) return 'vendor-vue'
        }
      }
    }
  },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
