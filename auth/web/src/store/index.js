import { createPinia } from 'pinia'

const store = createPinia(); // 创建pinia实例

// 全局注册 store
export function setupStore(app) {
  app.use(store); // 挂载pinia
}
export { store };
