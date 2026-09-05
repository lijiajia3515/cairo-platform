<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import {
  MoonIcon,
  SunnyIcon,
  MenuFoldIcon,
  MenuUnfoldIcon,
  SettingIcon,
} from 'tdesign-icons-vue-next';
import { SearchIcon, NotificationIcon } from 'tdesign-icons-vue-next';

import { resetRouter } from '@/router';
import { clearLoginTraces } from '@/utils/clearLoginTraces';

import SubappSwitcher from '@/components/subappSwitcher';
import Breadcrumb from '@/components/breadcrumb';
import { useUserStore } from '@/store/user';
import { useSubappContextStore } from '@/store/subappContext';
import { useThemeConfigStore } from '@/store/themeConfig';

import SettingDrawer from '@/components/settingDrawer';

import {
  getMyAppUserInfo_api,
  logoutAppUserAuthorization_api
} from '@/api';

const router = useRouter();

const userStore = useUserStore();
const subappStore = useSubappContextStore();
const themeStore = useThemeConfigStore();

const themeConfig = computed(() => themeStore.config);

// 个性化设置抽屉
const settingVisible = ref(false);

// 顶部搜索(菜单导航):本地过滤全部子应用菜单,选中跳转
const searchVisible = ref(false);
const keyword = ref('');
const allMenus = computed(() => {
  const out = [];
  const walk = (nodes, subapp, parents) => {
    for (const n of nodes || []) {
      if (n.hiddenMenu) continue;
      const chain = [...parents, n.menuName].join(' / ');
      if (n.component && (!n.menus || !n.menus.length)) out.push({ title: n.menuName, chain, path: n.component, subapp: subapp });
      if (n.menus?.length) walk(n.menus, subapp, [...parents, n.menuName]);
    }
  };
  subappStore.list.forEach((s2) => walk(s2.menus, s2.subappName, []));
  return out;
});
const searchResults = computed(() => {
  const k = keyword.value.trim().toLowerCase();
  if (!k) return allMenus.value.slice(0, 6);
  return allMenus.value.filter((m) => (m.chain + m.subapp).toLowerCase().includes(k)).slice(0, 8);
});
const goSearch = (item) => {
  searchVisible.value = false;
  keyword.value = '';
  if (item) router.push(item.path);
};

// 消息通知(画页面:面板 UI 与空状态,后端通知通道接入前为静态壳)
const msgVisible = ref(false);
const toggleCollapse = () => {
  themeStore.update({ isCollapse: !themeConfig.value.isCollapse });
};

const user = computed(() => userStore.userGetter);

const options = [
  { content: '个人信息', value: 2 },
  { content: '退出登录', value: 1 },
];
const clickHandler = (data) => {
  switch (data.content) {
    case '退出登录':
      loginOff()
      // router.replace('/login');
      break;
    case '个人信息':
      router.push('/profile');
      break;
    default:
      break;
  }
  // MessagePlugin.success(`选中【${data.content}】`);
};

const loginOff = async () => {
  try {
    await logoutAppUserAuthorization_api({})
  } catch (err) {
    // 服务端登出失败（token 已失效 / 网络异常等）不阻断本地凭证清理
    console.log(err)
  } finally {
    resetRouter(); // 重置路由
    // 无论服务端登出结果如何，强制清除凭证/标签/子应用上下文等全部登录痕迹
    clearLoginTraces();
    window.location.href = window.location.origin + '/login'
  }
}


// 暗黑切换(接管个性化配置并持久化)
const onChangeTheme = () => {
  themeStore.update({ isDark: !themeConfig.value.isDark });
}

// 获取当前用户信息
const getUser = async () => {
  let res = await getMyAppUserInfo_api();
  if (res.code == 'Success') {
    userStore.saveUser(res?.data || {});
  }
}


onMounted(() => {
  getUser();
});



onUnmounted(() => {

})
</script>


<template>
  <div class="header__wrapper">
    <div class="headerContainer">
      <div class="left">
        <div class="item-icon" @click="toggleCollapse" title="折叠/展开菜单">
          <MenuUnfoldIcon v-if="themeConfig.isCollapse" />
          <MenuFoldIcon v-else />
        </div>
        <SubappSwitcher></SubappSwitcher>
        <Breadcrumb v-if="themeConfig.isBreadcrumb" class="header-breadcrumb"></Breadcrumb>
      </div>
      <div class="right">
        <div @click="searchVisible = true" class="item-icon" title="搜索菜单 (Ctrl+K)">
          <SearchIcon />
        </div>
        <t-popup v-model:visible="msgVisible" trigger="click" placement="bottom-right" show-arrow>
          <div class="item-icon" title="消息通知">
            <NotificationIcon />
            <span class="msg-dot"></span>
          </div>
          <template #content>
            <div class="msg-panel">
              <div class="msg-head">
                <span>通知</span>
                <t-button variant="text" size="small" theme="primary" disabled>全部已读</t-button>
              </div>
              <div class="msg-empty">
                <t-icon name="notification" size="32" />
                <div>暂无新通知</div>
              </div>
            </div>
          </template>
        </t-popup>
        <div @click="onChangeTheme" class="item-icon" title="暗黑/浅色切换">
          <MoonIcon v-if="!themeConfig.isDark" />
          <SunnyIcon v-if="themeConfig.isDark" />
        </div>
        <div @click="settingVisible = true" class="item-icon" title="个性化设置">
          <SettingIcon />
        </div>
        <!-- 头像+名称一体下拉,恒在按钮组右侧(最右) -->
        <t-dropdown :options="options" trigger="click" @click="clickHandler">
          <div class="user-chip">
            <t-image lazy error="加载失败" class="headImage" :src="user.accountAvatarUrl" fit="cover" />
            <span class="user-name sl1">{{ user.nickname || user.accountNickname }}</span>
            <t-icon name="chevron-down" size="16" class="user-caret" />
          </div>
        </t-dropdown>
      </div>
    </div>

    <!-- 顶部菜单搜索 -->
    <t-dialog v-model:visible="searchVisible" :footer="false" header="搜索菜单" width="520px" attach="body">
      <div class="search-box">
        <t-input v-model="keyword" placeholder="搜索菜单名称,回车跳转第一个结果" size="large" clearable
          @keydown.enter="goSearch(searchResults[0])">
          <template #prefix-icon><t-icon name="search" /></template>
        </t-input>
        <div class="search-list">
          <div v-for="item in searchResults" :key="item.path" class="search-item" @click="goSearch(item)">
            <span class="search-item-title">{{ item.title }}</span>
            <span class="search-item-chain sl1">{{ item.subapp }} · {{ item.chain }}</span>
          </div>
          <div v-if="!searchResults.length" class="search-empty">未找到匹配的菜单</div>
        </div>
      </div>
    </t-dialog>

    <SettingDrawer v-model:visible="settingVisible" />
  </div>
</template>

<style lang="scss" scoped>
.header__wrapper {
  width: 100%;
  height: 100%;
  border-bottom: 1px solid var(--td-component-stroke);
  box-sizing: border-box;
  background: var(--td-bg-color-container);

  .headerContainer {
    width: 100%;
    height: 100%;
    padding: 0 16px;
    box-sizing: border-box;
    display: flex;
    justify-content: space-between;
    align-items: center;

    .left {
      display: flex;
      align-items: center;
      gap: 8px;
      min-width: 0;

      // 侧栏折叠开关
      .item-icon {
        cursor: pointer;
        color: var(--td-text-color-primary);
        font-size: 20px; // tdesign 图标默认 1em,继承正文 14px 会显小
        padding: 6px;
        border-radius: var(--td-radius-default);
        display: flex;
        align-items: center;
        justify-content: center;

        &:hover {
          background: var(--td-bg-color-container-hover);
          color: var(--td-brand-color);
        }
      }

      // 面包屑自带左侧细分隔(与子应用切换条同一语言),间距交给 .left 的 gap
      .header-breadcrumb {
        padding-left: 12px;
        border-left: 1px solid var(--td-component-stroke);
        min-width: 0;
        overflow: hidden;
      }
    }

    .right {
      display: flex;
      justify-content: flex-end;
      align-items: center;
      gap: 6px;

      .item-icon {
        cursor: pointer;
        color: var(--td-text-color-primary);
        font-size: 20px; // tdesign 图标默认 1em,继承正文 14px 会显小
        padding: 6px;
        border-radius: var(--td-radius-default);
        position: relative; // 消息红点定位锚
        display: flex;
        align-items: center;
        justify-content: center;

        &:hover {
          background: var(--td-bg-color-container-hover);
          color: var(--td-brand-color);
        }
      }

      // 头像+名称一体下拉触发器:紧凑间距,悬停整块反色
      .user-chip {
        display: flex;
        align-items: center;
        gap: 8px;
        margin-left: 6px; // 与按钮组拉开一档
        padding: 3px 10px 3px 3px;
        border-radius: 24px;
        cursor: pointer;
        color: var(--td-text-color-primary);
        font-size: 14px;

        &:hover {
          background: var(--td-bg-color-container-hover);

          .user-caret {
            color: var(--td-brand-color);
          }
        }

        .headImage {
          width: 30px;
          height: 30px;
          border-radius: 50%;
          overflow: hidden;
          flex: none;
        }

        .user-name {
          max-width: 120px;
          line-height: 30px;
        }

        .user-caret {
          color: var(--td-text-color-secondary);
        }
      }
    }
  }
}

  // 消息红点(锚定图标按钮右上角)
  .msg-dot {
    position: absolute;
    top: 4px;
    right: 4px;
    width: 6px;
    height: 6px;
    border-radius: 50%;
    background: var(--td-error-color);
  }

  .msg-panel {
    width: 280px;

    .msg-head {
      display: flex;
      align-items: center;
      justify-content: space-between;
      font-size: 13px;
      font-weight: 600;
      padding-bottom: 8px;
      border-bottom: 1px solid var(--td-component-stroke);
    }

    .msg-empty {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 6px;
      padding: 24px 0;
      color: var(--td-text-color-placeholder);
      font-size: 12px;
    }
  }

  .search-box {
    .search-list {
      margin-top: 10px;
      max-height: 320px;
      overflow-y: auto;
    }

    .search-item {
      display: flex;
      flex-direction: column;
      padding: 8px 10px;
      border-radius: var(--td-radius-default);
      cursor: pointer;

      &:hover {
        background: var(--td-bg-color-container-hover);
      }

      .search-item-title {
        font-size: 13px;
        color: var(--td-text-color-primary);
      }

      .search-item-chain {
        font-size: 12px;
        color: var(--td-text-color-placeholder);
      }
    }

    .search-empty {
      padding: 24px 0;
      text-align: center;
      font-size: 12px;
      color: var(--td-text-color-placeholder);
    }
  }
</style>
