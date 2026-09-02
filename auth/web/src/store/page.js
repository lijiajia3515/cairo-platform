// 页面
import { ref, computed } from 'vue';
import { defineStore } from 'pinia';

const iconList = [

  'icon-kaifataojian',
  'icon-xitongshezhi',

  'icon-user',
  'icon-zhanghaozhongxinzhanghaoguanli',
  'icon-shouye',
  'icon-qiye16',
  'icon-bianji',
  'icon-youshang',
  'icon-fanhui',

]

export const usePageStore = defineStore('page', () => {
  let lastPath = ref(sessionStorage.getItem(_this.storage.lastPath) ? sessionStorage.getItem(_this.storage.lastPath) : '/home');
  const lastPathGetter = computed(() => lastPath.value);
  const updateLastPage = (path) => {
    lastPath.value = path;
  }


  let icons = ref(iconList);
  const getIconsGetter = computed(() => icons.value);



  return {
    lastPathGetter, updateLastPage,
    getIconsGetter,
  }
})