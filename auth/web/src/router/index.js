import { createRouter, createWebHistory } from 'vue-router';
import { debounce, cloneDeep } from 'lodash';
import {
  MessagePlugin
} from 'tdesign-vue-next';

import { getToken, getRefreshToken } from '@/utils';
import { clearLoginTraces } from '@/utils/clearLoginTraces';

import useRefreshToken from '@/hooks/useRefreshToken';

import { useUserStore } from '@/store/user';
import { useTagsViewStore } from '@/store/tagsView';
import { useSubappContextStore } from '@/store/subappContext';

import getComponent from './getComponent';

const userStore = () => useUserStore();
const tagsStore = () => useTagsViewStore();

// Pinia 需在 app.use(store) 之后才可用，故这些访问器必须延迟到导航守卫内调用
const menus = () => userStore().menuListGetter;
const permissionList = () => userStore().permissionListGetter;

// 根路由'/'静态注册(否则登录成功 replace('/') 时路由表尚无此路径,vue-router 告警 No match)。
// redirect 随菜单注册后经 addRoute 同名覆盖才挂上:vue-router 的 redirect 函数在守卫前执行,
// 菜单未注册时无论返回 undefined(抛 Invalid redirect)还是 /home(未注册,复发 No match 告警)都不行——
// 故静态版无 redirect,守卫注册完菜单 next('/') 重解析时才走恢复逻辑
const createRootRoutes = (withRedirect) => ([
  {
    path: '/', name: '/', component: () => import('@/views/index/index.vue'),
    // 恢复多窗口活动标签(无标签时 getter 兜底 /home)
    redirect: withRedirect ? () => tagsStore().activePath : undefined,
    // 静态 catch-all 兜底:冷启动/刷新直达动态地址时初始导航先于菜单注册解析,
    // 无落点即告警 No match;真实路由注册后按优先级自然覆盖,壳内 404 语义不变
    children: [{ path: '/:pathMatch(.*)*', name: 'notfound', component: () => import('@/views/notfound') }],
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
  // 壳外不再设 404 兜底:404 统一挂壳 children(useMenus 内),已登录输错地址
  // 时壳常驻;未登录由守卫重定向 /login。冷启动直输不存在地址也是守卫先走
  // getMenuList 注册路由后 next(to) 再落壳内 404
]


const router = createRouter({
  // 内部提供了 history 模式的实现。为了简单起见，我们在这里使用 hash 模式。
  history: createWebHistory(),
  routes: [...staticRoutes, ...createRootRoutes(false)],
});

/**
 * 菜单列表
 */
const getMenuList = debounce(async (next, to) => {
  // 子应用清单+菜单+权限的整包数据源(内存态,登录/刷新首拉,切换子应用零请求)。
  // 未就绪先等 load 完成——否则启动直进 /dashboard 等非 manage 前缀时路由表为空落 404
  const subappStore = useSubappContextStore();
  if (!subappStore.loaded) {
    subappStore.initCurrent();
    await subappStore.load();
  }
  // 目标地址带其他子应用前缀时随目标换芯(请求上下文/菜单/权限一并切换)
  const targetSubapp = (to?.path || '').split('/')[1] || '';
  const subappId = subappStore.list.some((s) => s.subappId === targetSubapp)
    ? targetSubapp
    : subappStore.current.subappId;
  subappStore.applyContext(subappId);
  // 菜单树直接用整包缓存(登录/刷新首拉,之后零远端请求;刷新重拉即权限更新)
  const cached = (subappStore.list.find((s) => s.subappId === subappId)?.menus || []).slice();
  cached.push({ component: '/profile', hiddenMenu: true, menuName: '个人信息', path: '/profile' });
  userStore().saveMenuList(cached);
  // 权限表先于组件挂载就绪(next 前 await),否则 v-allow 在空表时误删视图根节点
  await getPermissions();
  initMenu(next, to);
})
// 菜单树拍平成一维路由(layout + 直接子路由)——
// 多级嵌套只用于菜单渲染(userStore.menuList),不参与路由注册;
// 无组件的父级不注册,RouterView matched 恒为两级,不依赖跳过无组件父级的 depth 逻辑
// seen:路由 name 全局唯一登记——DB 菜单名可能与平台静态路由(如「首页」/home)或
// 其他子应用菜单重名,addRoute 同名会静默覆盖(曾致 /home 被 /manage/home 顶掉落 404),
// 重名时以 path 兜底
const flattenMenus = (list, acc, seen) => {
  for (let item of list) {
    let path = item.component || item.path;
    let component = getComponent(path);
    if (path && component) {
      let name = item.menuName;
      if (seen.has(name)) name = path;
      seen.add(name);
      // meta.title=菜单名:展示位(标签/面包屑)以此为准,name 仅作唯一键
      // (重名被 path 兜底替换后,route.name 不再可当显示名用)
      acc.push({ name, path, component, meta: { title: item.menuName } });
    }
    if (item.menus && item.menus.length) {
      flattenMenus(item.menus, acc, seen);
    }
  }
  return acc;
}

const useMenus = async (list) => {
  let arr = cloneDeep(list);
  const seen = new Set(['/iframe', '首页']);
  let newArr = [
    {
      path: '/iframe',
      name: '/iframe',
      component: () => import('@/views/iframe'),
    },
    // 平台首页:静态挂进壳 children,不随子应用菜单重拉消失
    {
      path: '/home',
      name: '首页',
      component: getComponent('/home'),
    },
  ];
  // 壳内 404 兜底:挂在壳 children 下,已登录输错地址时壳常驻、内容区渲染卡片
  // (未登录由守卫先行重定向 /login;壳外 staticRoutes 的 catch-all 仅守菜单注册前的冷启动)
  newArr.push({ path: '/:pathMatch(.*)*', name: 'notfound', component: () => import('@/views/notfound') });
  // 当前子应用菜单 + 其余子应用菜单全部注册(跨子应用标签共存:
  // 任意子应用已开标签始终可达,路由按 /子应用前缀 隔离互不冲突)
  flattenMenus(arr, newArr, seen);
  const subappStore = useSubappContextStore();
  for (const s of subappStore.list) {
    if (s.subappId === subappStore.current.subappId) continue;
    flattenMenus(cloneDeep(s.menus), newArr, seen);
  }
  return newArr;
}

const initMenu = async (next, to) => {
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

    const rootRoutes = createRootRoutes(true);
    rootRoutes[0].children = newList;
    rootRoutes.forEach(route => {
      router.addRoute({ ...route });
    });
    // 直接输 URL 进入时落到目标地址;自然进入 '/' 则由根路由 redirect 恢复活动标签
    next(to?.fullPath || '/');
    // router.replace(router.currentRoute.value.fullPath);
  } catch (err) {
  }
}

// 权限随子应用,走整包缓存(与菜单同源预取,会话内零远端请求)
const getPermissions = async () => {
  if (permissionList().length) return;
  const subappStore = useSubappContextStore();
  const entry = subappStore.list.find((s) => s.subappId === subappStore.current.subappId);
  userStore().savePermissionList(entry?.permissions || []);
};

// 导航后登记多标签(未命名路由如 404 兜底、登录/注册页不开标签)
// 首页/个人中心是菜单之外的平台级页面,不占标签位
router.afterEach((to) => {
  if (!to.name) return;
  if (to.path === '/login' || to.path === '/register') return;
  if (to.path === '/home' || to.path === '/profile') return;
  useTagsViewStore().addTag(to);
});

router.beforeEach((to, from, next) => {
  let token = getToken();
  if (to.fullPath == '/profile') {
    if (to.redirectedFrom != undefined) {
      if (Object.keys(to.redirectedFrom.query).length !== 0) {
        localStorage.setItem('wxCode', to.redirectedFrom.query.code)
        localStorage.setItem('infoType', to.redirectedFrom.query.type)
        localStorage.setItem('snsProviderId', to.redirectedFrom.query.sns_provider_id)
      }
    }
  }
  if (token.value) {
    if (to.path == '/login' || to.path == '/register') {
      next('/');
    } else {
      // 标签驱动换芯:目标是其他子应用前缀的已开标签时,请求上下文/菜单/权限
      // 先随目标子应用切换(路由已全量注册,无需重建即可放行)
      const subappStore = useSubappContextStore();
      const targetSubapp = (to.path.split('/')[1] || '');
      const known = subappStore.list.some((s) => s.subappId === targetSubapp);
      if (known && targetSubapp !== subappStore.current.subappId) {
        subappStore.applyContext(targetSubapp);
      }
      if (!menus().length) {
        getMenuList(next, to);
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
        // 无任何凭证:清掉可能残留的上一用户登录痕迹(标签/子应用上下文)再进登录页
        clearLoginTraces();
        router.replace('/login');
      }
    }
  }
})

// 重置路由
export function resetRouter() {
  let paths = ['登录', '注册', '/'];
  let routers = router.getRoutes();
  routers.map((item) => {
    if (!paths.includes(item.name)) {
      router.removeRoute(item.name);
    }
  });
  // '/' 换回无 redirect 的静态版:菜单/标签已清,带 redirect 的版本会在守卫注册前
  // 解析 redirect 指向未注册地址,复发 No match 告警
  router.addRoute(createRootRoutes(false)[0]);
}

async function refreshTokenFunc(next) {
  let success = await useRefreshToken();
  if (success) {
    let timer = setTimeout(() => {
      location.reload();
      clearTimeout(timer);
    }, 1500)
  } else {
    // 刷新失败:会话终结,清除登录痕迹(标签/子应用上下文)再回登录页
    clearLoginTraces();
    router.replace('/login');
  }
}

export default router;