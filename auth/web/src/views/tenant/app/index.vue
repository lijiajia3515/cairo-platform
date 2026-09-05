<script setup lang="jsx">
defineOptions({ name: 'manage-tenant-app' })

import {
  ref, onMounted,
  nextTick,
  watch,
} from 'vue';
import {
  MessagePlugin,
  LoadingPlugin,
  DialogPlugin,
} from 'tdesign-vue-next';
import {
  debounce,
} from 'lodash';

import useState from '@/hooks/useState';

import List from '@/components/list';
import FilterBar from '@/components/filterBar';
import FilterItem from '@/components/filterBar/item.vue';
import { timeColumn, avatarCopyColumn, opColumn, switchColumn, entityColumn } from '@/utils/tableColumns';
import { hasPermission } from '@/plugins/permission';

import Dialog from '@/components/dialog';
import UserInfo from '@/components/userInfo'; // 用户详情
import AccountInfo from '@/components/accountInfo';
import Transfer from '@/components/transfer';

import {
  getTenantAppPageList_api,
  createTenantApp_api,
  modifyTenantAppInfo_api,
  modifyTenantAppStatus_api,
  deleteTenantApp_api,

  getTenantList_api,
  getAppList_api,
  getAccountList_api,
  getEndpointList_api,
  getSubappList_api
} from '@/api';

onMounted(() => {
  getTenantAppPage();

  nextTick(() => {
    getAppList();
    getTenantList();
  })
});

let page = ref(1);
let size = ref(10);
let total = ref(0);
const [list, setList] = useState([]);
const [loading, setLoading] = useState(false);
const [search, setSearch] = useState({
  appId: null,
  tenantId: null,
})
const [configs, setConfigs] = useState({
  data: list,
  columns: [
    // 引用实体不设独立 ID 列:名称单元格悬停即复制实体 ID,查全量 ID 去对应实体的管理页
    entityColumn({ colKey: 'tenantName', title: '企业', iconKey: 'tenantIcon' }),
    entityColumn({ colKey: 'appName', title: '应用', iconKey: 'appIcon' }),
    switchColumn({
      api: modifyTenantAppStatus_api,
      idKeys: ['tenantId', 'appId'],
      label: '企业应用',
      perm: 'tenant_app.modify_tenant_status',
      refresh: () => getTenantAppPage(),
    }),
    {
      // 多账号头像+昵称,点击查看账号详情,列工厂无法表达,保留自定义 cell
      colKey: 'adminAccounts', title: '管理员账号', width: 220, cell: (h, { row }) => {
        return (
          <t-space size="small" >
            {
              row['adminAccounts'] ? row['adminAccounts'].map(item => {
                return (
                  <t-space size="small">
                    {
                      item?.avatarUrl ? <t-avatar imageProps={{ lazy: true }} class="pick" onClick={() => onWatchAccountInfo(item)} hideOnLoadFailed={true} size="medium" image={item.avatarUrl || null} /> : (
                        item.nickname ? <t-avatar imageProps={{ lazy: true }} class="pick" onClick={() => onWatchAccountInfo(item)} size="medium" >{item.nickname?.slice(0, 2)}</t-avatar> : null
                      )
                    }
                    <div onClick={() => onWatchAccountInfo(item || {})} style={{ height: '100%', display: 'flex', alignItems: 'center', cursor: 'pointer' }}>{item?.nickname || null}</div>
                  </t-space>
                )
              }) : ''
            }
          </t-space>
        )
      }
    },
    { colKey: 'autoRegister', title: '开通自动注册', width: 120, cell: (h, { row }) => row['autoRegister'] == true ? '是' : (row['autoRegister'] == false ? '否' : '') },
    {
      colKey: 'metadata.updateUser.nickname', title: '更新账号', width: 200, cell: (h, { row }) => {
        return (
          <t-space size="small">
            {
              row?.metadata?.updateAccount?.avatarUrl ? <t-avatar onClick={() => onWatchAccountInfo(row?.metadata?.updateAccount || {})} class="pick" imageProps={{ lazy: true }} hideOnLoadFailed={true} alt={row?.metadata?.updateAccount?.nickname?.slice(0, 2)} size="medium" image={row?.metadata?.updateAccount?.avatarUrl} /> : (
                row?.metadata?.updateAccount?.nickname ? <t-avatar onClick={() => onWatchAccountInfo(row?.metadata?.updateAccount || {})} class="pick" imageProps={{ lazy: true }} size="medium" >{row?.metadata?.updateAccount?.nickname?.slice(0, 2)}</t-avatar> : null
              )
            }
            <div onClick={() => onWatchAccountInfo(row?.metadata?.updateAccount || {})} style={{ height: '100%', display: 'flex', alignItems: 'center' }} class="pick">{row?.metadata?.updateAccount?.nickname || null}</div>
          </t-space>
        )
      }
    },
    timeColumn('metadata.updateTime', '更新时间'),
    opColumn([
      { content: '编辑', onClick: (row) => onEdit(row), visible: () => hasPermission('tenant_app.modify_tenant_app_info') },
      { content: '删除', theme: 'danger', onClick: (row) => onDelete(row), visible: () => hasPermission('tenant_app.delete_tenant_app') },
    ], { width: 160 })
  ],
  loading: loading,
  pagination: {
    current: page,
    pageSize: size,
    total: total,
  },
  onPageChange: (pageInfo) => {
    page.value = pageInfo.current;
    size.value = pageInfo.pageSize;
    getTenantAppPage();
  }
});

/**
 **************************************************** 用户详情
 */
let userInfoRef = ref(null); // 用户详情
const [userDetail, setUserDetail] = useState({});
const onWatchUserInfo = (row) => {
  userInfoRef.value.open();
  setUserDetail(row.metadata.updateUser)
}
const onCloseUserDetail = () => {
  setUserDetail({});
}

let accountInfoRef = ref(null); // 账号详情
const [accountDetail, setAccountDetail] = useState({});
const onWatchAccountInfo = (data) => {
  accountInfoRef.value.open();
  setAccountDetail(data)
}
const onCloseAccountInfo = () => {
  setAccountDetail({});
}



// 企业应用列表
const getTenantAppPage = async () => {
  setLoading(true);
  try {
    let params = {
      page: page.value - 1,
      size: size.value,
      appId: search.value.appId,
      tenantId: search.value.tenantId,
    }
    let res = await getTenantAppPageList_api(params);
    if (res.code == 'Success') {
      setList(res?.data?.contents || []);
      total.value = Number(res?.data?.total || 0);
    }
  } finally {
    setLoading(false);
  }
}

// 搜索
const onSearch = () => {
  page.value = 1;
  getTenantAppPage();
}
// 重置
const onReset = () => {
  page.value = 1;
  setSearch({
    appId: null,
    tenantId: null,
  })
  getTenantAppPage();
}




// 创建
const rules = {
  tenantId: [
    { required: true, message: '企业必选', type: 'error', trigger: 'change' },
  ],
  appId: [
    { required: true, message: '应用必选', type: 'error', trigger: 'change' },
  ],
  endpointIds: [
    { required: true, message: '终端必选', type: 'error', trigger: 'change' },
    { whitespace: true, message: '终端不能为空' },
  ]
}
const formRef = ref(null);
const [visible, setVisible] = useState(false);
const [type, setType] = useState('add');
const [form, setForm] = useState({
  tenantId: null,
  appId: null,
  endpointIds: [],
  adminAccountIds: [],
  subappIds: [],
  autoRegister: true,
  enabled: true
})
const onCreate = () => {
  getAccountList();
  setVisible(true);
  setType('add');
  formRef.value.clearValidate();
}
const onClose = () => {
  setVisible(false);
  setType('add');
  setForm({
    tenantId: null,
    appId: null,
    endpointIds: [],
    adminAccountIds: [],
    subappIds: [],
    autoRegister: true,
    enabled: true
  });
  setEndpointList([]);
  setAccountList([]);
  setAllSubappList([])
}
const onSubmit = async () => {
  const validate = await formRef.value.validate();
  if (validate == true) {
    LoadingPlugin(true);
    try {
      let { tenantId, appId, endpointIds, subappIds, adminAccountIds, autoRegister, enabled } = form.value;
      subappIds = getCheck()
      if (type.value == 'add') {
        let params = {
          tenantId, appId, endpointIds, subappIds, adminAccountIds, autoRegister, enabled
        };
        let res = await createTenantApp_api(params);
        if (res.code == 'Success') {
          MessagePlugin.success('创建成功');
          onClose();
          getTenantAppPage();
        }
      } else {
        let params = {
          tenantId, appId, adminAccountIds, autoRegister
        };
        let res = await modifyTenantAppInfo_api(params);
        if (res.code == 'Success') {
          MessagePlugin.success('编辑成功');
          onClose();
          getTenantAppPage();
        }
      }
    } finally {
      LoadingPlugin(false);
    }
  }
}
/**
 * 编辑
 * @param {Object} row
 * @param {String} row.tenantId
 * @param {String} row.appId
 * @param {Array} row.adminAccounts
 * @param {Boolean} row.autoRegister
 */
const onEdit = (row) => {
  setVisible(true);
  setType('edit');
  formRef.value.clearValidate();
  let adminAccounts = [];
  if (row.adminAccounts && row.adminAccounts.length) {
    row.adminAccounts.forEach(item => {
      adminAccounts.push(item.accountId);
    })
  }
  setForm({
    tenantId: row.tenantId,
    appId: row.appId,
    adminAccountIds: adminAccounts,
    autoRegister: row.autoRegister
  });
  getAccountList();
}

/**
 * 删除
 * @param {Object} row
 * @param {String} row.tenantId
 * @param {String} row.appId
 */
const onDelete = (row) => {
  const confirmDia = DialogPlugin({
    header: '删除',
    body: '是否继续删除?',
    confirmBtn: '确定',
    cancelBtn: '取消',
    onConfirm: async ({ e }) => {
      LoadingPlugin(true);
      try {
        let params = {
          tenantId: row.tenantId,
          appId: row.appId,
        };
        let res = await deleteTenantApp_api(params);
        if (res.code == 'Success') {
          MessagePlugin.success('删除成功');
          confirmDia.hide();
          if (list.value && list.value.length == 1 && page.value > 1) {
            page.value = page.value - 1;
          }
          getTenantAppPage();
        }
      } finally {
        LoadingPlugin(false);
      }
    },
    onClose: ({ e, trigger }) => {
      confirmDia.hide();
    },
  });
}



/**
 * 企业列表
 */
const [tenantList, setTenantList] = useState([]);
const getTenantList = async () => {
  let res = await getTenantList_api({});
  if (res.code == 'Success') {
    setTenantList(res?.data || []);
  }
}
/**
 * 应用列表
 */
const [appList, setAppList] = useState([]);
const getAppList = async () => {
  let res = await getAppList_api({ Scopes: ["tenant"] });
  if (res.code == 'Success') {
    setAppList(res?.data || []);
  }
}

watch(() => form.value.appId, () => {
  setForm({
    ...form.value,
    endpointIds: [],
    subappIds: []
  });
  if (form.value.appId) {
    setEndpointList([]);
    setAllSubappList([])
    getEndpointList();
  }
})

// const onGetEndPointList = () => {
//   setForm({
//     ...form.value,
//     endpointIds: [],
//   })
//   if (form.value.appId) {
//     setEndpointList([]);
//     getEndpointList();
//   }
// }


/**
 * 终端列表
 */
const [endpointList, setEndpointList] = useState([]);
const getEndpointList = async () => {
  let params = {
    appId: form.value.appId,
    scopeIds: ["tenant"]
  };
  let res = await getEndpointList_api(params);
  if (res.code == 'Success') {
    setEndpointList(res?.data || []);
  }
}


/**
 * 账号列表
 */
const [accountList, setAccountList] = useState([]);
const getAccountList = async () => {
  let res = await getAccountList_api({});
  if (res.code == 'Success') {
    setAccountList(res?.data || []);
  }
}


/**
 * 账号详情
 */
const [visibleDetail, setVisibleDetail] = useState(false);
const [detailData, setDetailData] = useState({});
/**
 *
 * @param {Object} item
 * @param {String} item.accountId
 * @param {String} item.avatarUrl
 * @param {String} item.joinTime
 * @param {String} item.nickname
 */
const onShowAccountDetail = (item) => {
  setVisibleDetail(true);
  setDetailData(item)
}
const onCloseAccountDetail = () => {
  setVisibleDetail(false);
  setDetailData({});
}


const [allSubappList, setAllSubappList] = useState([]);
const changeCheckBox = async (val, context) => {
  if (context.type === 'check') {
    let params = {
      enabled: true
    };
    let headers = {};
    if (form.value.appId) {
      headers['app-id'] = form.value.appId;
    }
    if (context.current) {
      headers['endpoint-id'] = context.current;
    }
    let res = await getSubappList_api(params, headers);
    if (res.code == 'Success') {
      if (res?.data) {
        let data = res?.data
        data.forEach(val => {
          val.isSelected = false
        })
        let item = {
          parentName: context.option.label,
          parentId: context.current,
          checkAll: false,
          isIndeterminate: false,
          subappList: JSON.parse(JSON.stringify(data)) || []
        }
        allSubappList.value.push(item)
      }
    }
  } else if (context.type === 'uncheck') {
    allSubappList.value = allSubappList.value.filter((item) => item.parentId !== context.current)
  }
  console.log(context, allSubappList.value, 'allSubappList.value====');
}

// 全选
const onCheckAll = debounce(() => {
  for (let parent of allSubappList.value) {
    if (parent.checkAll == true) { // 是全选
      parent.isIndeterminate = false; // 取消半选
      if (parent.subappList) {
        parent.subappList.forEach(item => {
          item.isSelected = true;
        })
      }
    } else {
      if (parent.subappList) {
        if (parent.isIndeterminate == false) {
          parent.subappList.forEach(item => {
            item.isSelected = false;
          })
        } else {
          let count = 0;
          parent.subappList.forEach(item => {
            if (item.isSelected == false) {
              count++;
            }
          });
          if (count == parent.subappList.length) {
            parent.checkAll = true;
            parent.isIndeterminate = false;
          }
        }
      }
    }
  }
});

// 单选
const onCheckOne = debounce(() => {
  for (let parent of allSubappList.value) {
    if (parent.subappList) {
      let count = 0;
      parent.subappList.forEach(item => {
        if (item.isSelected == true) {
          count++;
        }
      });
      if (count == parent.subappList.length) {
        parent.checkAll = true;
        parent.isIndeterminate = false;
      } else if (count != 0) {
        parent.checkAll = false;
        parent.isIndeterminate = true;
      } else if (count == 0) {
        parent.checkAll = false;
        parent.isIndeterminate = false;
      }
    }
  }
})

//选中
const getCheck = () => {
  let checks = [];
  for (let parent of allSubappList.value) {
    if (parent.subappList) {
      parent.subappList.forEach(item => {
        if (item.isSelected == true) {
          checks.push(item.subappId);
        }
      })
    }
  }

  return checks;
}
</script>


<template>
  <div v-allow="'tenant_app.read'" class="tenantApp__wrapper">
    <FilterBar @search="onSearch" @reset="onReset">
      <FilterItem label="应用">
        <t-select :scroll="{ type: 'virtual' }" filterable clearable v-model="search.appId" placeholder="请选择应用">
          <t-option v-for="(item, index) in appList" :key="index" :value="item.appId" :label="item.appName"
            :style="{ height: '40px', width: '100%' }">
            <div style="display: flex;align-items: center;width: 100%;">
              <t-avatar :imageProps="{ lazy: true }" size="20px" :image="item.icon" shape="round"></t-avatar>
              <span style="display: inline-block;marginLeft:10px;">{{ item.appName }}</span>
            </div>
          </t-option>
          <template #valueDisplay="{ value }">
            <template v-if="value">
              <t-space>
                <t-avatar :imageProps="{ lazy: true }" size="20px"
                  :image="appList.filter(item => item.appId == value)[0]?.icon" shape="round"></t-avatar>
                {{ appList.filter(item => item.appId == value)[0]?.appName }}
              </t-space>
            </template>
          </template>
        </t-select>
      </FilterItem>
      <FilterItem label="企业">
        <t-select :scroll="{ type: 'virtual' }" filterable clearable v-model="search.tenantId" placeholder="请选择企业">
          <t-option v-for="(item, index) in tenantList" :key="index" :value="item.tenantId" :label="item.tenantName"
            :style="{ height: '40px', width: '100%' }">
            <div style="display: flex;align-items: center;width: 100%;">
              <t-avatar :imageProps="{ lazy: true }" size="20px" :image="item.icon" shape="round"></t-avatar>
              <span style="display: inline-block;marginLeft:10px;">{{ item.tenantName }}</span>
            </div>
          </t-option>
          <template #valueDisplay="{ value }">
            <template v-if="value">
              <t-space>
                <t-avatar :imageProps="{ lazy: true }" size="20px"
                  :image="tenantList.filter(item => item.tenantId == value)[0]?.icon" shape="round"></t-avatar>
                {{ tenantList.filter(item => item.tenantId == value)[0]?.tenantName }}
              </t-space>
            </template>
          </template>
        </t-select>
      </FilterItem>
      <template #actions>
        <t-button v-allow="'tenant_app.create'" @click="onCreate">创建</t-button>
      </template>
    </FilterBar>
    <div class="empty"></div>
    <List @page-change="configs.onPageChange" :configs="configs" />
  </div>


  <!-- 添加 修改 信息 -->
  <Dialog @confirm="onSubmit" @close="onClose" :visible="visible" width="45%" top="10vh">
    <template #title>{{ type == 'add' ? '创建' : '编辑' }}</template>
    <t-form :rules="rules" ref="formRef" :data="form">
      <t-row>
        <t-col :span="11">
          <t-form-item name="tenantId" label="企业">
            <t-select :scroll="{ type: 'virtual' }" clearable filterable v-model="form.tenantId">
              <t-option :disabled="type == 'edit'" :label="item.tenantName" :value="item.tenantId"
                v-for="(item, index) in tenantList" :key="index" :style="{ height: '40px', width: '100%' }">
                <div style="display: flex;align-items: center;width: 100%;">
                  <t-avatar :imageProps="{ lazy: true }" size="20px" :image="item.icon" shape="round"></t-avatar>
                  <span style="display: inline-block;marginLeft:10px;">{{ item.tenantName }}</span>
                </div>
              </t-option>
              <template #valueDisplay="{ value }">
                <template v-if="value">
                  <t-space>
                    <t-avatar :imageProps="{ lazy: true }" size="20px"
                      :image="tenantList.filter(item => item.tenantId == value)[0]?.icon" shape="round"></t-avatar>
                    {{ tenantList.filter(item => item.tenantId == value)[0]?.tenantName }}
                  </t-space>
                </template>
              </template>
            </t-select>
          </t-form-item>
        </t-col>
        <div class="empty"></div>
        <t-col :span="11">
          <t-form-item name="appId" label="应用">
            <t-select :scroll="{ type: 'virtual' }" clearable filterable v-model="form.appId">
              <t-option :style="{ background: item.enabled == true ? 'initial' : 'var(--td-bg-color-component-disabled)' }" :disabled="type == 'edit'"
                :label="item.appName" :value="item.appId" v-for="(item, index) in appList" :key="index">
                <div style="display: flex;align-items: center;width: 100%;">
                  <t-avatar :imageProps="{ lazy: true }" size="20px" :image="item.icon" shape="round"></t-avatar>
                  <span style="display: inline-block;marginLeft:10px;">{{ item.appName }}</span>
                </div>
              </t-option>
              <template #valueDisplay="{ value }">
                <template v-if="value">
                  <t-space>
                    <t-avatar :imageProps="{ lazy: true }" size="20px"
                      :image="appList.filter(item => item.appId == value)[0]?.icon" shape="round"></t-avatar>
                    {{ appList.filter(item => item.appId == value)[0]?.appName }}
                  </t-space>
                </template>
              </template>
            </t-select>
          </t-form-item>
        </t-col>
        <div class="empty"></div>
        <t-col v-if="type == 'add'" :span="11">
          <t-form-item name="endpointIds" label="终端">
            <t-checkbox-group v-model="form.endpointIds" @change="changeCheckBox">
              <t-checkbox v-for="(item, index) in endpointList" :key="index" :label="item.endpointName"
                :value="item.endpointId">

                <t-avatar :imageProps="{ lazy: true }" size="20px" :image="item.icon" shape="round"></t-avatar>
                {{ item.endpointName }}
              </t-checkbox>
            </t-checkbox-group>
          </t-form-item>
        </t-col>
        <div class="empty"></div>

        <t-col v-if="type == 'add'" :span="11">
          <t-form-item label="子应用">
            <div class="permisson_Page" v-if="allSubappList.length > 0">
              <div v-for="(item, index) in allSubappList" :key="index" class="row">
                <div class="child">
                  <div class="title"> <t-checkbox :label="item.parentName" :on-change="onCheckAll"
                      :indeterminate="item.isIndeterminate" v-model="item.checkAll" />
                  </div>
                  <div class="permission">
                    <t-checkbox style="marginRight:10px;" :on-change="onCheckOne"
                      v-for="(val, valIndex) in item.subappList" :key="valIndex" v-model="val.isSelected"
                      :label="val.subappName" :value="val.subappId" />
                  </div>
                </div>
              </div>
            </div>
          </t-form-item>
        </t-col>
        <div class="empty"></div>
        <t-col :span="11">
          <t-form-item label="管理员账号">
            <Transfer v-model="form.adminAccountIds" :list="accountList" :actives="form.adminAccountIds"
              v-if="accountList.length"></Transfer>
            <!-- <t-select :scroll="{ type: 'virtual' }" multiple v-model="form.adminAccountIds">
              <t-option :label="item.nickname" :value="item.accountId" v-for="(item, index) in accountList"
                :key="index"></t-option>
            </t-select> -->
          </t-form-item>
        </t-col>
        <div class="empty"></div>
        <t-col :span="11">
          <t-form-item label="开启自动注册">
            <t-radio-group v-model="form.autoRegister">
              <t-radio :value="true">是</t-radio>
              <t-radio :value="false">否</t-radio>
            </t-radio-group>
          </t-form-item>
        </t-col>
        <div class="empty"></div>
        <t-col v-if="type == 'add'" :span="11">
          <t-form-item label="启用状态">
            <t-radio-group v-model="form.enabled">
              <t-radio :value="true">开启</t-radio>
              <t-radio :value="false">关闭</t-radio>
            </t-radio-group>
          </t-form-item>
        </t-col>
      </t-row>
    </t-form>
  </Dialog>

  <!-- 人物详情 -->
  <Dialog :confirmBtn="null" :cancelBtn="null" width="25%" @close="onCloseAccountDetail" :visible="visibleDetail">
    <template #title>账号详情</template>
    <t-form>
      <t-row>
        <t-col :span="11">
          <t-form-item label="头像">
            <t-image :src="detailData.avatarUrl" />
          </t-form-item>
        </t-col>
        <div class="empty"></div>
        <t-col :span="11">
          <t-form-item label="账号Id">
            <span>{{ detailData.accountId }}</span>
          </t-form-item>
        </t-col>
        <div class="empty"></div>
        <t-col :span="11">
          <t-form-item label="账号昵称">
            <span>{{ detailData.nickname }}</span>
          </t-form-item>
        </t-col>
        <div class="empty"></div>
        <t-col :span="11">
          <t-form-item label="加入时间">
            <span>{{ detailData.joinTime }}</span>
          </t-form-item>
        </t-col>
      </t-row>
    </t-form>
  </Dialog>

  <!-- 账号详情 -->
  <AccountInfo :data="accountDetail" ref="accountInfoRef" @close="onCloseAccountInfo"></AccountInfo>



  <!-- 用户信息 userDetail -->
  <UserInfo @close="onCloseUserDetail" :userId="userDetail.userId" :nickname="userDetail.nickname"
    :accountAvatarUrl="userDetail.accountAvatarUrl" :joinTime="userDetail.joinTime" ref="userInfoRef"></UserInfo>
</template>

<style lang="scss" scoped>
.tenantApp__wrapper {}

.permisson_Page {
  width: 100%;
  max-height: 58vh;
  overflow-y: auto;
  box-sizing: border-box;
  border-top: 1px solid var(--td-component-stroke);
  border-left: 1px solid var(--td-component-stroke);
  border-right: 1px solid var(--td-component-stroke);
  border-bottom: 1px solid var(--td-component-stroke);

  .row {
    width: 100%;
    display: flex;
    flex-wrap: wrap;
    box-sizing: border-box;
    border-bottom: 1px solid var(--td-component-stroke);

    &:last-child {
      border-bottom: 0;
    }

    .title {
      width: 120px;
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 5px;
      box-sizing: border-box;

    }

    .child {
      width: 100%;
      display: flex;
      flex-wrap: wrap;
      box-sizing: border-box;
      border-bottom: 1px solid var(--td-component-stroke);

      &:last-child {
        border-bottom: 0;
      }
    }

    .permission {
      width: calc(100% - 120px);
      float: left;
      box-sizing: border-box;
      padding: 5px 10px;
      border-left: 1px solid var(--td-component-stroke);
    }
  }
}
</style>
