// 壳层折叠状态:窄屏(<1200)强制折叠,宽屏随个性化配置;菜单与侧栏宽度共用
import { computed } from 'vue';
import { useWindowSize } from '@vueuse/core';

import { useThemeConfigStore } from '@/store/themeConfig';

export default function useCollapsed() {
  const { width } = useWindowSize();
  const themeStore = useThemeConfigStore();
  return computed(() => width.value < 1200 || themeStore.config.isCollapse);
}
