import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url';
import vueJsx from '@vitejs/plugin-vue-jsx'

import AutoImport from 'unplugin-auto-import/vite';
import Components from 'unplugin-vue-components/vite';
import { TDesignResolver } from 'unplugin-vue-components/resolvers';
import { VitePWA } from 'vite-plugin-pwa'

// https://vitejs.dev/config/
export default defineConfig({
  base: '/',
  plugins: [
    vue(),
    vueJsx(),
    AutoImport({
      resolvers: [TDesignResolver({
        library: 'vue-next'
      })],
    }),
    Components({
      resolvers: [TDesignResolver({
        library: 'vue-next'
      })],
    }),
    VitePWA({
      injectRegister: 'auto',
      registerType: 'autoUpdate',
      devOptions: {
        enabled: false  // 开发模式下不注册 sw，避免缓存干扰热更新与接口调试
      },

      // MANIFEST PWA https://vite-pwa-org.netlify.app/guide/pwa-minimal-requirements.html
      includeAssets: ['logo.svg', 'apple-touch-icon.png', 'mask-icon.svg', 'favicon.png'], // 应该是下面 manifest 中可能用到的文件名字吧
      manifest: {
        name: "Cairo运营平台",
        short_name: "Cairo运营平台",
        theme_color: "#373737",
        start_url: "/",
        display: "standalone",
        background_color: "#373737",
        icons: [
          {
            src: "logo.svg",
            sizes: "512x512",
            type: "image/svg+xml",
            purpose: "any",
          },
          {
            src: "appicon-apple.png",
            sizes: "512x512",
            type: "image/png",
            purpose: "any",
          }
        ],
      },
    })
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  server: {
    port: 5173,
    host: '0.0.0.0',
    // host: '192.0.2.1',
    open: true
  }
})
