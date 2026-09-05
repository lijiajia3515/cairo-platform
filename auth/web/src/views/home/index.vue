<script setup>
defineOptions({ name: 'home' })

import { computed } from 'vue';
import { useRouter } from 'vue-router';

import { useUserStore } from '@/store/user';

const router = useRouter();
const userStore = useUserStore();
const nickname = computed(() => userStore.userGetter?.nickname || userStore.userGetter?.accountNickname || 'Cairo');

const greeting = computed(() => {
  const h = new Date().getHours();
  return h < 6 ? '夜深了' : h < 12 ? '早上好' : h < 14 ? '中午好' : h < 18 ? '下午好' : '晚上好';
});

const dateText = new Date().toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric', weekday: 'long' });

// 统计卡占位数据,接 dashboard 聚合接口后替换
const stats = [
  { label: '账号总数', value: '—' },
  { label: '应用数', value: '—' },
  { label: '在线会话', value: '—' },
  { label: '今日登录', value: '—' },
];

// 快捷入口按已注册路由过滤——无权限的菜单不会注册(落 catch-all notfound),
// 入口直接隐藏而非点击落 404;首页本身登录即用、不挂权限门槛
const shortcuts = [
  { label: '账号管理', path: '/manage/account' },
  { label: '企业应用', path: '/manage/tenant/app' },
  { label: '菜单权限', path: '/manage/develop/menu' },
  { label: '登录会话', path: '/manage/system/session' },
];
const visibleShortcuts = computed(() => shortcuts.filter((s) => router.resolve(s.path).matched.at(-1)?.name !== 'notfound'));
</script>

<template>
  <div class="home__wrapper">
    <t-card class="welcome" :bordered="false">
      <div class="hello">{{ greeting }}，{{ nickname }}</div>
      <div class="date">{{ dateText }}</div>
    </t-card>

    <t-row :gutter="[16, 16]">
      <t-col :span="3" v-for="s in stats" :key="s.label">
        <t-card class="stat" :bordered="false">
          <div class="stat-value">{{ s.value }}</div>
          <div class="stat-label">{{ s.label }}</div>
        </t-card>
      </t-col>
    </t-row>

    <t-card title="快捷入口" :bordered="false">
      <t-row :gutter="[12, 12]">
        <t-col :span="3" v-for="q in visibleShortcuts" :key="q.path">
          <div class="shortcut" @click="$router.push(q.path)">{{ q.label }}</div>
        </t-col>
      </t-row>
    </t-card>
  </div>
</template>

<style lang="scss" scoped>
.home__wrapper {
  display: flex;
  flex-direction: column;
  gap: 16px;

  .hello {
    font-size: 20px;
    font-weight: 600;
    color: var(--td-text-color-primary);
  }

  .date {
    margin-top: 6px;
    font-size: 13px;
    color: var(--td-text-color-placeholder);
  }

  .stat {
    text-align: center;

    .stat-value {
      font-size: 24px;
      font-weight: 600;
      color: var(--td-brand-color);
    }

    .stat-label {
      margin-top: 4px;
      font-size: 13px;
      color: var(--td-text-color-secondary);
    }
  }

  .shortcut {
    padding: 18px 0;
    text-align: center;
    border: 1px solid var(--td-component-stroke);
    border-radius: var(--td-radius-medium);
    color: var(--td-text-color-primary);
    cursor: pointer;

    &:hover {
      color: var(--td-brand-color);
      border-color: var(--td-brand-color);
    }
  }
}
</style>
