import { createApp } from 'vue'
import './style.css'
import App from './App.vue'
import Package from '../package.json'
// import { createPinia } from 'pinia';

import { setupStore } from '@/store';

import router from './router';

import './style/index.scss';

// import TDesign from 'tdesign-vue-next'; 1.5.2
// 引入组件库的少量全局样式变量
import 'tdesign-vue-next/es/style/index.css';

import vue3TreeOrg from 'vue3-tree-org';
import "vue3-tree-org/lib/vue3-tree-org.css";

// import Vue3Jsoneditor from 'v3-jsoneditor/src/Vue3Jsoneditor.vue'

// 插件
import setPermisePlugin from './plugins/permission';

// 开发模式下清理历史注册的 Service Worker（vite.config 曾 devOptions.enabled=true，遗留的
// 旧 SW 会拦截导航喂陈旧缓存，导致白屏且服务端无任何请求；当前 dev 已不注册，此处兜底注销）
if (import.meta.env.DEV && 'serviceWorker' in navigator) {
  navigator.serviceWorker.getRegistrations()
    .then((rs) => Promise.all(rs.map((r) => r.unregister())))
    .then(() => caches.keys().then((keys) => Promise.all(keys.map((k) => caches.delete(k)))))
    .catch(() => { /* 清理失败不阻断启动 */ });
}

const version = Package.version
const versionStorage = localStorage.getItem('version')

if (version != versionStorage) {
  localStorage.clear()
  localStorage.setItem('version', version)
  console.log('版本不一致，清除缓存中')
  //location.reload() 方法用来刷新当前页面。该方法只有一个参数，当值为 true 时，将强制浏览器从服务器加载页面资源，
  //当值为 false 或者未传参时，浏览器则可能从缓存中读取页面。
  setTimeout(() => {
    window.location.reload(true)
  }, 1000)
}

const app = createApp(App);

// app.use(TDesign);
setupStore(app);
// app.component('vue3-jsoneditor', Vue3Jsoneditor);
// app.use(createPinia());
app.use(setPermisePlugin);
app.use(vue3TreeOrg);
app.use(router);
app.mount('#app')
