<script setup>
defineOptions({ name: 'dashboard-index' })  // keep-alive 名=路径 slug(/dashboard/index)

import StatCards from './components/StatCards.vue';
import LoginTrendCard from './components/LoginTrendCard.vue';
import RecentLoginsCard from './components/RecentLoginsCard.vue';
import useDashboardData from './composables/useDashboardData';

const { loading, stats, loginTrend, recentLogins } = useDashboardData();
</script>

<template>
  <div v-loading="loading" class="dashboard">
    <StatCards :items="stats"></StatCards>
    <t-row :gutter="[16, 16]">
      <t-col :span="8">
        <LoginTrendCard :data="loginTrend"></LoginTrendCard>
      </t-col>
      <t-col :span="4">
        <RecentLoginsCard :rows="recentLogins"></RecentLoginsCard>
      </t-col>
    </t-row>
  </div>
</template>

<style lang="scss" scoped>
.dashboard {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 4px;
}
</style>
