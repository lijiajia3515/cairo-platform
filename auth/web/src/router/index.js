import { createRouter, createWebHashHistory, createWebHistory } from 'vue-router';
import { nextTick } from 'vue';
import { debounce, cloneDeep } from 'lodash';
import {
  MessagePlugin
} from 'tdesign-vue-next';

import { getToken, getRefreshToken } from '@/utils';

import useRefreshToken from '@/hooks/useRefreshToken';

import { useUserStore } from '@/store/user';
import { usePageStore } from '@/store/page';

import getComponent from './getComponent';

import {
  getMySubappUserMenu_api,
  getMySubappUserPermissionIds_api,
} from '@/api';

const userStore = () => useUserStore();
const pageStore = () => usePageStore();

// Pinia 需在 app.use(store) 之后才可用，故这些访问器必须延迟到导航守卫内调用
const menus = () => userStore().menuListGetter;
const lastPath = () => pageStore().lastPathGetter;
const permissionList = () => userStore().permissionListGetter;

// 动态根路由，children 由服务端菜单填充
const createRootRoutes = () => ([
  {
    path: '/', component: () => import('@/views/index/index.vue'),
    redirect: lastPath(),
    children: []
  },
]);

let staticRoutes = [
  {
    path: '/login',
    name: '登录',
    component: () => import('@/views/login/index.vue')
  },
  {
    path: '/register',
    name: '注册',
    component: () => import('@/views/register/index.vue')
  },
  // {
  //   path: '/404',
  //   name: '404',
  //   component: () => import('@/views/notfound'),
  // },
  {
    path: '/:pathMatch(.*)*',
    component: () => import('@/views/notfound'),
  },
]


const router = createRouter({
  // 内部提供了 history 模式的实现。为了简单起见，我们在这里使用 hash 模式。
  history: createWebHistory(),
  routes: staticRoutes,
});

// 获取所有路径
const getPaths = (list) => {
  let arr = [];
  for (let parent of list) {
    if (parent.menus) {
      for (let child of parent.menus) {
        arr.push(child.component);
      }
    } else {
      if (parent.component) {
        arr.push(parent.component);
      }
    }
  }
  return arr;
}

/**
 * 菜单列表
 */
const getMenuList = debounce(async (next) => {
  let routeData = []
  try {
    let res = await getMySubappUserMenu_api({});
    if (res.code == 'Success') {
      routeData = res?.data || []
      routeData.push(
        {
          component: "/main/userInfo",
          hiddenMenu: true,
          menuName: "个人信息",
          path: "/main/userInfo"
        }
      )
     
    }
  } catch (error) {
    console.log(error, 'error');
    routeData.push(
      {
        component: "/main/userInfo",
        hiddenMenu: true,
        menuName: "个人信息",
        path: "/main/userInfo"
      }
    )
  }finally{
    userStore().saveMenuList(routeData);
    let paths = getPaths(routeData);
    if (paths.length) {
      const index = paths.findIndex(item => item == lastPath());
      if (index == -1) { // 不存在这个路径
        pageStore().updateLastPage(paths[0]);
        sessionStorage.setItem(_this.storage.lastPath, paths[0]);
      } else {
        // pageStore().updateLastPage('/home');
        // sessionStorage.setItem(_this.storage.lastPath, '/home')
        pageStore().updateLastPage(lastPath());
        sessionStorage.setItem(_this.storage.lastPath, lastPath());
      }
    } else { // 没有路径
      pageStore().updateLastPage('/home');
      sessionStorage.setItem(_this.storage.lastPath, '/home')
    }
    initMenu(next);
  }
})
const useMenus = async (list) => {
  let arr = cloneDeep(list);
  let newArr = [
    {
      path: '/iframe',
      name: '/iframe',
      component: () => import('@/views/iframe'),
    }
  ];
  for (let item of arr) {
    let row = {};
    if (item.menus) {
      row = { name: item.menuName, path: item.path, component: getComponent(item.path) }
      let childItems = [];
      for (let child of item.menus) {
        // childItems.push({ name: child.menuName, path: child.path, component: getComponent(child.path) })
        let row1 = {}
        if (child.menus) {
          row1 = { name: child.menuName, path: child.path, component: getComponent(child.path) }
          let childItems1 = [];
          for (let val of child.menus) {
            childItems1.push({ name: val.menuName, path: val.path, component: getComponent(val.path) })
          }
          row1.children = childItems1;
        } else {
          if (child.path) {
            row1 = { name: child.menuName, path: child.path, component: getComponent(child.path) };
          }
        }
        childItems.push(row1)
      }
      row.children = childItems
    } else {
      if (item.path) {
        row = { name: item.menuName, path: item.path, component: getComponent(item.path) };
      }
    }
    if (row && Object.keys(row).length) {
      newArr.push(row)
    }
  }

  return newArr;
}

const initMenu = async (next) => {
  try {
    let menuList = menus();
    if (menuList && menuList.length > 0) {
      menuList.forEach(item => {
        if (item.component) {
          item.path = item.component
        }
        if (item.menus && item.menus.length > 0) {
          item.menus.forEach(val => {
            if (val.component) {
              val.path = val.component
            }
          })
        }
      })
    }
    let newList = await useMenus(menuList);

    const rootRoutes = createRootRoutes();
    rootRoutes[0].children = newList;
    rootRoutes.forEach(route => {
      router.addRoute({ ...route });
    });
    next(lastPath() || '/');
    // router.replace(router.currentRoute.value.fullPath);
  } catch (err) {
  }
}

const getPermissions = async () => {
  if (!permissionList().length) {
    let res = await getMySubappUserPermissionIds_api();
    if (res.code == 'Success') {
      userStore().savePermissionList(res?.data || [])
    }
  }
};

router.beforeEach((to, from, next) => {
  let token = getToken();
  if (to.fullPath == '/main/userInfo') {
    if (to.redirectedFrom != undefined) {
      if (Object.keys(to.redirectedFrom.query).length !== 0) {
        localStorage.setItem('wxCode', to.redirectedFrom.query.code)
        localStorage.setItem('infoType', to.redirectedFrom.query.type)
        localStorage.setItem('snsProviderId', to.redirectedFrom.query.sns_provider_id)
        console.log(to.redirectedFrom, 'redirectedFrom====');
      }
    }
  }
  if (token.value) {
    if (to.path == '/login' || to.path == '/register') {
      let path = sessionStorage.getItem(_this.storage.lastPath);
      next(path || '/home');
    } else {
      if (!menus().length) {
        getMenuList(next);
      } else {
        next();
      }
    }
    getPermissions();
  } else {
    // 判断 refreshToken 是否存在
    let refreshToken = getRefreshToken();
    if (refreshToken.value) {
      MessagePlugin.error('登录失效');
      // 尝试去刷新token
      refreshTokenFunc(next);
    } else {
      if (to.path == '/login' || to.path == '/register') {
        next();
      } else {
        router.replace('/login');
      }
    }
  }
  // else {
  //   if (to.path == '/login' || to.path == '/register') {
  //     next();
  //   } else {
  //     router.replace('/login')
  //   }
  // }
})

//全局后置钩子 - 这里不会修改路由
// || to.fullPath == '/'
router.afterEach((to, from) => {
  if (to.fullPath == '/login' || to.fullPath == '/register' || to.fullPath == '/') return;
  // console.log('全局后置', to, from)
  nextTick(() => {
    // pageStore.updateLastPage(to.fullPath);
    sessionStorage.setItem(_this.storage.lastPath, to.fullPath);
  })
});

// 重置路由
export function resetRouter() {
  let paths = ['登录', '注册', '/'];
  let routers = router.getRoutes();
  routers.map((item) => {
    if (!paths.includes(item.name)) {
      router.removeRoute(item.name);
    }
  });
}

async function refreshTokenFunc(next) {
  let success = await useRefreshToken();
  if (success) {
    let path = sessionStorage.getItem(_this.storage.lastPath);
    console.log(path);
    // next(path || '/home');
    // router.replace(path);
    let timer = setTimeout(() => {
      location.reload();
      clearTimeout(timer);
    }, 1500)
  } else {
    router.replace('/login');
  }
}

export default router;