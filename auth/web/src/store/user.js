// 用户
import { ref, computed } from 'vue';
import { defineStore } from 'pinia';


export const useUserStore = defineStore('user', () => {
  // 菜单列表
  let menuList = ref([]);
  const menuListGetter = computed(() => menuList.value);
  const saveMenuList = (list) => {
    menuList.value = list;
  }


  // 用户信息
  const user = ref({});
  const userGetter = computed(() => user.value);
  const saveUser = (data) => {
    user.value = data;
  }


  // 用户权限集合
  const permissionList = ref([]);
  const permissionListGetter = computed(() => permissionList.value);
  const savePermissionList = (list) => {
    permissionList.value = list;
  }


  return {
    menuList, menuListGetter, saveMenuList,
    user, userGetter, saveUser,
    permissionListGetter, savePermissionList,
  }
})