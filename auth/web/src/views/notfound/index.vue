<!-- 壳内 404 卡片:已登录但地址不存在时,壳(菜单/标签/子应用切换)常驻,
     内容区渲染本卡片——不丢导航上下文,可直接点菜单离开(阿里云式)。
     未登录场景由路由守卫先行重定向 /login,不进本页 -->
<script setup>
import { useRouter } from 'vue-router';

const router = useRouter();
const goHome = () => router.replace('/home');
</script>

<template>
  <div class="nf-card">
    <div class="nf-emoji">404</div>
    <div class="nf-title">页面不存在</div>
    <div class="nf-desc">地址可能已变更或从未存在，检查链接或从菜单导航</div>
    <t-button theme="primary" variant="base" @click="goHome">返回首页</t-button>
  </div>
</template>

<style lang="scss" scoped>
.nf-card {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 40px 20px;

  .nf-emoji {
    font-size: 56px;
    font-weight: 700;
    line-height: 1.2;
    color: var(--td-text-color-placeholder);
    letter-spacing: 2px;
  }

  .nf-title {
    font-size: 16px;
    font-weight: 600;
    color: var(--td-text-color-primary);
  }

  .nf-desc {
    font-size: 12px;
    color: var(--td-text-color-secondary);
    margin-bottom: 8px;
  }
}
</style>
