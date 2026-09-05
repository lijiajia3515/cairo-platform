<!-- 登录日志 -->
<script setup>
defineOptions({ name: 'manage-develop-login_log' })

import { onMounted, onUnmounted } from 'vue';

import { hasPermission } from '@/plugins/permission';

import useState from '@/hooks/useState';

import AccountComponent from './components/account.vue';
import ClientComponent from './components/client.vue';
import AppUserComponent from './components/appUser.vue';
import TenantAppUserComponent from './components/tenantAppUser.vue';

onMounted(() => {

});

const [active] = useState(
  hasPermission('login_log.account_login_log')
    ? 'account'
    : (hasPermission('login_log.client_login_log')
      ? 'client'
      : (hasPermission('login_log.app_user_login_log')
        ? 'appUser'
        : (hasPermission('login_log.tenant_app_user_login_log')
          ? 'tenantAppUser'
          : 'account')
      )
    )
);

onUnmounted(() => {

})
</script>


<template>
  <div class="loginLog__wrapper">
    <t-tabs v-model="active" theme="card">
      <template v-if="hasPermission('login_log.account_login_log')">
        <t-tab-panel :destroyOnHide="true" value="account" label="账号">
          <AccountComponent></AccountComponent>
        </t-tab-panel>
      </template>
      <template v-if="hasPermission('login_log.client_login_log')">
        <t-tab-panel :destroyOnHide="true" value="client" label="客户端">
          <ClientComponent></ClientComponent>
        </t-tab-panel>
      </template>
      <template v-if="hasPermission('login_log.app_user_login_log')">
        <t-tab-panel :destroyOnHide="true" value="appUser" label="应用">
          <AppUserComponent></AppUserComponent>
        </t-tab-panel>
      </template>
      <template v-if="hasPermission('login_log.tenant_app_user_login_log')">
        <t-tab-panel :destroyOnHide="true" value="tenantAppUser" label="企业应用">
          <TenantAppUserComponent></TenantAppUserComponent>
        </t-tab-panel>
      </template>
    </t-tabs>
  </div>
</template>

<style lang="scss" scoped>
.loginLog__wrapper {
}
</style>
