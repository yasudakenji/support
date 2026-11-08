import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

const proxy = {
  '/api': {
    target: process.env.SUPPORT_API_URL || 'http://127.0.0.1:8082',
    rewrite: path => path.replace(/^\/api/, ''),
  },
}

export default defineConfig({
  plugins: [vue()],
  server: { host: '127.0.0.1', proxy },
  preview: { host: '127.0.0.1', proxy },
})
