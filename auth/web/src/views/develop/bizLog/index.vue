<script setup>
import {hasPermission} from '@/plugins/permission';

import useState from '@/hooks/useState';

import OpenComponent from './components/open.vue';
import AccountComponent from './components/account.vue';
import AppComponent from './components/app.vue';
import SubappComponent from './components/subapp.vue';
import TenantAppComponent from './components/tenantApp.vue';
import TenantSubappComponent from './components/tenantSubapp.vue';
import ClientComponent from './components/client.vue';

const [active] = useState(
    hasPermission('biz_log.open_biz_log')
        ? 'open'
        : (hasPermission('biz_log.account_biz_log')
            ? 'account'
            : (hasPermission('biz_log.app_biz_log')
                    ? 'app'
                    : (hasPermission('biz_log.tenant_app_biz_log')
                            ? 'tenantApp'
                            : (hasPermission('biz_log.client_biz_log')
                                    ? 'client'
                                    : 'open'
                            )
                    )
            )
        )
);


</script>


<template>
  <div class="serviceLog__wrapper">
    <t-tabs v-model="active" theme="card">
      <template v-if="hasPermission('biz_log.open_biz_log')">
        <t-tab-panel :destroyOnHide="true" value="open" label="开放级">
          <OpenComponent></OpenComponent>
        </t-tab-panel>
      </template>
      <template v-if="hasPermission('biz_log.account_biz_log')">
        <t-tab-panel :destroyOnHide="true" value="account" label="账号">
          <AccountComponent></AccountComponent>
        </t-tab-panel>
      </template>
      <template v-if="hasPermission('biz_log.app_biz_log')">
        <t-tab-panel :destroyOnHide="true" value="app" label="应用">
          <AppComponent></AppComponent>
        </t-tab-panel>
      </template>
      <template v-if="hasPermission('biz_log.subapp_biz_log')">
        <t-tab-panel :destroyOnHide="true" value="subapp" label="子应用">
          <SubappComponent></SubappComponent>
        </t-tab-panel>
      </template>
      <template v-if="hasPermission('biz_log.tenant_app_biz_log')">
        <t-tab-panel :destroyOnHide="true" value="tenantApp" label="企业应用">
          <TenantAppComponent></TenantAppComponent>
        </t-tab-panel>
      </template>
      <template v-if="hasPermission('biz_log.tenant_subapp_biz_log')">
        <t-tab-panel :destroyOnHide="true" value="tenantSubapp" label="企业子应用">
          <TenantSubappComponent></TenantSubappComponent>
        </t-tab-panel>
      </template>
      <template v-if="hasPermission('biz_log.client_biz_log')">
        <t-tab-panel :destroyOnHide="true" value="client" label="客户端">
          <ClientComponent></ClientComponent>
        </t-tab-panel>
      </template>
    </t-tabs>
  </div>
</template>

<style lang="scss" scoped>
.serviceLog__wrapper {
}
</style>
