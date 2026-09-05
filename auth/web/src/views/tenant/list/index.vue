<!-- 企业 -->
<script setup lang="jsx">
defineOptions({ name: 'manage-tenant' })

import { ref, onMounted } from 'vue';
import { debounce } from 'lodash';
import { useWindowSize } from '@vueuse/core';
import {
  MessagePlugin,
  LoadingPlugin,
  DialogPlugin,
} from 'tdesign-vue-next';

import useState from '@/hooks/useState';
import useCopy from '@/hooks/useCopy';

import List from '@/components/list';
import FilterBar from '@/components/filterBar';
import FilterItem from '@/components/filterBar/item.vue';
import { timeColumn, copyColumn, opColumn, switchColumn } from '@/utils/tableColumns';
import { hasPermission } from '@/plugins/permission';

import Dialog from '@/components/dialog';
import UploadImage from '@/components/uploadImage';
import AccountInfo from '@/components/accountInfo';


import {
  getTenantPageList_api,
  createTenant_api,
  modifyTenantInfo_api,
  modifyTenantStatus_api,
  deleteTenant_api,
  modifyTenantOwner_api,

  getAccountList_api,
  getAccountPageList_api,
} from '@/api';

const { width } = useWindowSize(); // 监听窗口大小(创建/编辑弹窗宽度)

let keyword = ref(null); // 企业名称
let enabled = ref(null); // 状态
let page = ref(1);
let size = ref(10);
let total = ref(0);
const [list, setList] = useState([])
const [loading, setLoading] = useState(false);

// 预览
const [previewShow, setPreviewShow] = useState(false);
const [previewList, setPreviewList] = useState([]);
const onClosePreview = () => { setPreviewShow(false); setPreviewList([]); }
const [configs, setConfigs] = useState({
  data: list,
  columns: [
    copyColumn('tenantId', '企业ID', { width: 190 }),
    { colKey: 'tenantName', title: '企业名称' },
    { colKey: 'aliasName', title: '企业别名' },
    {
      // 头像+昵称可点击查看账号详情,点击行为无法由列工厂表达,保留自定义 cell
      colKey: 'ownerAccount', title: '拥有人', width: 200, cell: (h, { row }) => {
        let show = ref(false);
        const onMouseEnter = (event) => {
          show.value = true;
        }
        const onMouseLeave = (event) => {
          show.value = false;
        }
        return (
          <t-space onMouseenter={() => onMouseEnter()} onMouseleave={() => onMouseLeave()} size="small">
            {
              row?.ownerAccount?.avatarUrl ? <t-avatar onClick={() => onWatchAccountInfo(row?.ownerAccount || {})} class="pick" imageProps={{ lazy: true }} hideOnLoadFailed={true} size="medium" image={row?.ownerAccount?.avatarUrl || row?.ownerAccount?.nickname} /> : (
                row?.ownerAccount?.nickname ? <t-avatar onClick={() => onWatchAccountInfo(row?.ownerAccount || {})} class="pick" imageProps={{ lazy: true }} size="medium" >{row?.ownerAccount?.nickname?.slice(0, 2)}</t-avatar> : null
              )
            }

            <div onClick={() => onWatchAccountInfo(row?.ownerAccount || {})} style={{ height: '100%', display: 'flex', alignItems: 'center' }} class="pick">{row?.ownerAccount?.nickname || null}</div>
            {
              show.value == true ? <t-icon name="copy" size="12px" class="copyIcon pick" onClick={() => useCopy(row['ownerAccount']?.accountId)}></t-icon> : <t-icon name="copy" size="12px" class="copyIcon" style={{ opacity: 0 }}></t-icon>
            }
          </t-space>
        )
      }
    },
    switchColumn({
      api: modifyTenantStatus_api,
      idKeys: ['tenantId'],
      label: '企业',
      perm: 'tenant.modify_status',
      refresh: () => getTenantPage(),
    }),
    {
      colKey: 'metadata.updateUser.nickname', title: '更新账号', width: 200, cell: (h, { row }) => {
        return (
          <t-space size="small">
            {
              row?.metadata?.updateAccount?.avatarUrl ? <t-avatar onClick={() => onWatchAccountInfo(row?.metadata?.updateAccount || {})} class="pick" hideOnLoadFailed={true} alt={row?.metadata?.updateAccount?.nickname?.slice(0, 2)} size="medium" image={row?.metadata?.updateAccount?.avatarUrl} /> : (
                row?.metadata?.updateAccount?.nickname ? <t-avatar onClick={() => onWatchAccountInfo(row?.metadata?.updateAccount || {})} class="pick" size="medium" >{row?.metadata?.updateAccount?.nickname?.slice(0, 2)}</t-avatar> : null
              )
            }
            <div onClick={() => onWatchAccountInfo(row?.metadata?.updateAccount || {})} style={{ height: '100%', display: 'flex', alignItems: 'center' }} class="pick">{row?.metadata?.updateAccount?.nickname || null}</div>
          </t-space>
        )
      }
    },
    timeColumn('metadata.updateTime', '更新时间'),
    opColumn([
      { content: '编辑', onClick: (row) => onEdit(row), visible: () => hasPermission('tenant.modify') },
      { content: '修改拥有者', onClick: (row) => onEditOwner(row), visible: () => hasPermission('tenant.modify_tenant_owner') },
      { content: '删除', theme: 'danger', onClick: (row) => onDelete(row), visible: () => hasPermission('tenant.delete') },
    ], { width: 200 })
  ],
  rowKey: 'tenantId',
  loading: loading,
  pagination: {
    current: page,
    pageSize: size,
    total: total,
  },
  onPageChange: (pageInfo) => {
    page.value = pageInfo.current;
    size.value = pageInfo.pageSize;
    getTenantPage();
  }
});

onMounted(() => {
  getTenantPage();
});

/**
 *********************************************** 列表
 */
const getTenantPage = async () => {
  setLoading(true);
  try {
    let params = {
      page: page.value - 1,
      size: size.value,
      keyword: keyword.value,
      enabled: enabled.value,
    }
    let res = await getTenantPageList_api(params);
    if (res.code == 'Success') {
      setList(res?.data?.contents || []);
      total.value = Number(res?.data?.total || 0);
    }
  } finally {
    setLoading(false);
  }
}

// 账号列表
const [accountList, setAccountList] = useState([]);
const getAccountList = async (search) => {
  let params = {};
  if (search) {
    params['keyword'] = search
  }
  let res = await getAccountList_api(params);
  if (res.code == 'Success') {
    setAccountList(res?.data || []);
  }
}

// 查询
const onSearch = () => {
  page.value = 1;
  getTenantPage();
}
// 重置
const onReset = () => {
  keyword.value = null;
  enabled.value = null;
  page.value = 1;
  getTenantPage();
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




/**
 * 创建
 */
const validatorValue = (val) => {
  if (val) {
    let t = /^(?![0-9-])(?!.*-$)[a-zA-Z0-9-]{1,20}$/;
    let flag = t.test(val);
    if (flag) {
      return {
        result: true,
        message: '格式正确',
        type: 'success'
      }
    } else {
      return {
        result: false,
        message: '格式错误',
        type: 'error'
      }
    }
  } else {
    return {
      result: true
    }
  }
}
const rules = {
  tenantName: [
    { required: true, message: '企业名称必填', type: 'error', trigger: 'change' },
  ],
  tenantId: [
    { required: false, validator: validatorValue },
  ],
  aliasName: [
    { required: false, validator: validatorValue },
  ],
  ownerAccountId: [
    { required: true, message: '拥有者必选', type: 'error', trigger: 'change' },
  ]
}
let formRef = ref(null);
const [visible, setVisible] = useState(false);
const [type, setType] = useState('add');
const [editFileList, setEditFileList] = useState([]); // 编辑 显示 icon
const [form, setForm] = useState({
  tenantId: null,
  tenantName: null,
  aliasName: null,
  ownerAccountId: null, // 拥有者Id
})
const onCreate = () => {
  // getAccountList();
  getAccountSelectPage();
  setType('add');
  setVisible(true);
  formRef.value.clearValidate();
}
const onEdit = async (row) => {
  setType('edit');
  setVisible(true);
  if (row.icon) {
    setEditFileList([{
      name: row.icon.split('/')[row.icon.split('/').length - 1],
      url: row.icon
    }])
  }
  setForm({
    ...form.value,
    tenantId: row?.tenantId,
    tenantName: row?.tenantName,
    aliasName: row?.aliasName,
    ownerAccountId: row?.ownerAccount?.accountId,
  })
  formRef.value.clearValidate();
}

const onConfirm = async () => {
  const validate = await formRef.value.validate();
  if (validate == true) {
    LoadingPlugin(true);
    try {
      if (type.value == 'add') {
        let params = {
          tenantId: form.value.tenantId,
          tenantName: form.value.tenantName,
          aliasName: form.value.aliasName,
          ownerAccountId: form.value.ownerAccountId,
        };
        if (fileList.value && fileList.value.length) {
          params['icon'] = fileList.value[0].url;
        }
        let res = await createTenant_api(params);
        if (res.code == 'Success') {
          MessagePlugin.success('创建成功');
          onClose();
          getTenantPage();
        }
      } else {
        let params = {
          tenantId: form.value.tenantId,
          tenantName: form.value.tenantName,
          aliasName: form.value.aliasName
        };
        if (fileList.value && fileList.value.length) {
          params['icon'] = fileList.value[0].url;
        } else {
          params['icon'] = '';
        }
        let res = await modifyTenantInfo_api(params);
        if (res.code == 'Success') {
          MessagePlugin.success('编辑成功');
          onClose();
          getTenantPage();
        }
      }
    } finally {
      LoadingPlugin(false);
    }
  }
}
const onClose = () => {
  setType('add');
  setForm({
    tenantId: null,
    tenantName: null,
    aliasName: null,
    ownerAccountId: null,
  });
  setEditFileList([]);
  setVisible(false);
  accountPage.value = 1;
  setAccountList([]);
}

// 上传图标
const [fileList, setFileList] = useState([]);
const onChangeFiles = (files) => {
  setFileList(files)
  console.log(fileList.value, 'fileList.value===');
}



// 修改拥有者
const rules_owner = {
  ownerAccountId: [
    { required: true, message: '拥有者账号必选', type: 'error', trigger: 'change' },
  ]
}
let formOwnerRef = ref(null);
const [visible_owner, setVisibleOwner] = useState(false);
const [formOwner, setFormOwner] = useState({
  tenantId: null,
  ownerAccountId: null,
})
const onEditOwner = (row) => {
  getAccountList(row?.ownerAccount?.accountId || null);
  setVisibleOwner(true);
  setFormOwner({
    tenantId: row.tenantId,
    ownerAccountId: row?.ownerAccount?.accountId || null
  });
}
const onCloseOwner = () => {
  setVisibleOwner(false);
  setFormOwner({
    tenantId: null,
    ownerAccountId: null,
  })
}

const onSubmitOwner = async () => {
  const validate = await formOwnerRef.value.validate();
  if (validate == true) {
    LoadingPlugin(true);
    try {
      let params = {
        ...formOwner.value
      };
      let res = await modifyTenantOwner_api(params);
      if (res.code == 'Success') {
        MessagePlugin.success('修改成功');
        onCloseOwner();
        getTenantPage();
      }
    } finally {
      LoadingPlugin(false);
    }
  }
}

// 删除
const onDelete = async (row) => {
  const confirmDia = DialogPlugin({
    header: '删除',
    body: '是否继续操作?',
    confirmBtn: '确定',
    cancelBtn: '取消',
    onConfirm: async ({ e }) => {
      LoadingPlugin(true);
      try {
        let params = {
          tenantId: row.tenantId,
        }
        let res = await deleteTenant_api(params);
        if (res.code == 'Success') {
          MessagePlugin.success('删除成功');
          confirmDia.hide();
          if (list.value && list.value.length == 1 && page.value > 1) {
            page.value = page.value - 1;
          }
          getTenantPage();
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



let accountPage = ref(1);
let accountSize = ref(10);
let accountTotal = ref(0);
const getAccountSelectPage = async () => {
  let params = {
    page: accountPage.value - 1,
    size: accountSize.value,
  };
  let res = await getAccountPageList_api(params);
  if (res.code == 'Success') {
    setAccountList(accountList.value.concat(res?.data?.contents) || []);
    accountTotal.value = Number(res?.data?.total || 0);
  }
}

// 滚动到底部
let accountFinish = ref(false);
const handleScrollToBottomAccount = () => {
  if (isFilter.value) return; // 处于搜索状态不允许分页查询
  if (accountList.value.length >= accountTotal.value) {
    accountFinish.value = true;
    return;
  }
  loadingAccountSelect.value = true;
  accountPage.value = accountPage.value + 1;
  getAccountSelectPage();
  loadingAccountSelect.value = false;
}

let isFilter = ref(false); // 是否处于搜索状态
const onInputChangeAccount = debounce((value) => {
  if (form.value.ownerAccountId || formOwner.value.ownerAccountId) return; // 已选中 不允许搜索
  if (value) {
    isFilter.value = true;
  } else {
    isFilter.value = false;
    accountPage.value = 1;
    accountTotal.value = 0;
    setAccountList([]);
    getAccountSelectPage();
  }
})

// 搜索账号
let loadingAccountSelect = ref(false);
const remoteMethodAccount = (search) => {
  if (form.value.ownerAccountId || formOwner.value.ownerAccountId) return; // 已选中 不允许搜索
  if (search) {
    loadingAccountSelect.value = true;
    getAccountList(search);
    loadingAccountSelect.value = false;
  }
}
</script>


<template>
  <div v-allow="'tenant.find'" class="list__wrapper">
    <FilterBar @search="onSearch" @reset="onReset">
      <FilterItem label="企业名称">
        <t-input clearable placeholder="请输入企业名称" v-model="keyword"></t-input>
      </FilterItem>
      <FilterItem label="状态">
        <t-select clearable v-model="enabled" placeholder="请选择状态">
          <t-option label="启用" :value="true"></t-option>
          <t-option label="禁用" :value="false"></t-option>
        </t-select>
      </FilterItem>
      <template #actions>
        <t-button v-allow="'tenant.create'" @click="onCreate">创建</t-button>
      </template>
    </FilterBar>
    <div class="empty"></div>

    <List @page-change="configs.onPageChange" :configs="configs"></List>
  </div>

  <Dialog :width="width < 750 ? '100%' : '40%'" :visible="visible" @confirm="onConfirm" @close="onClose">
    <template #title>{{ type == 'add' ? '创建' : '编辑' }}</template>
    <t-form :rules="rules" ref="formRef" :data="form">
      <t-row v-if="type == 'add'">
        <t-col :span="12">
          <t-form-item help="字母、数字、-，数字不能开头，-不能开头和结尾，字符20以内" name="tenantId" label="企业ID">
            <t-input :disabled="type == 'edit'" v-model="form.tenantId"></t-input>
          </t-form-item>
        </t-col>
      </t-row>
      <div class="empty"></div>
      <t-row>
        <t-col :span="12">
          <t-form-item name="tenantName" label="企业名称">
            <t-input v-model="form.tenantName"></t-input>
          </t-form-item>
        </t-col>
        <div class="empty"></div>
        <t-col :span="12">
          <t-form-item name="aliasName" label="企业别名" help="字母、数字、-，数字不能开头，-不能开头和结尾，字符20以内">
            <t-input v-model="form.aliasName"></t-input>
          </t-form-item>
        </t-col>
      </t-row>
      <div class="empty"></div>
      <t-row>
        <t-col v-if="type == 'add'" :span="12">
          <t-form-item name="ownerAccountId" label="拥有者账号">
            <t-select clearable :filterable="!form.ownerAccountId" :loading="loadingAccountSelect" style="width:100%" v-model="form.ownerAccountId"
              :onInputChange="onInputChangeAccount" @search="remoteMethodAccount"
              :popup-props="{ 'on-scroll-to-bottom': handleScrollToBottomAccount }">
              <t-option :label="item.nickname + ' (' + (item?.phoneNumber || item?.username || item?.accountId) + ')'"
                :value="item.accountId" v-for="(item, index) in accountList" :key="index">
                <div style="display: flex;align-items: center;width: 100%;">
                  <t-avatar :imageProps="{ lazy: true }" size="20px" :image="item.avatarUrl" shape="round"></t-avatar>
                  <span style="display: inline-block;marginLeft:10px;">{{ item.nickname + ' (' + (item?.phoneNumber || item?.username || item?.accountId) + ')' }}</span>
                </div>
              </t-option>
               <template #valueDisplay="{ value }">
                <template v-if="value">
                  <t-space>
                    <t-avatar :imageProps="{ lazy: true }" size="20px"
                      :image="accountList.filter(item => item.accountId == value)[0]?.avatarUrl" shape="round"></t-avatar>
                    {{ accountList.filter(item => item.accountId == value)[0]?.nickname + ' (' + (accountList.filter(item => item.accountId == value)[0]?.phoneNumber || accountList.filter(item => item.accountId == value)[0]?.username || accountList.filter(item => item.accountId == value)[0]?.accountId) + ')' }}
                  </t-space>
                </template>
              </template>
            </t-select>
          </t-form-item>
        </t-col>
        <div class="empty"></div>
        <t-col>
          <t-form-item label="图标">
            <UploadImage :disabled="!form.tenantId" :tenantId="form.tenantId" type="public" picType="tenant-icon"
              @change="onChangeFiles" :limit="1" :fileList="editFileList">
            </UploadImage>
          </t-form-item>
        </t-col>
      </t-row>
    </t-form>
  </Dialog>


  <!-- 修改拥有者 -->
  <Dialog @confirm="onSubmitOwner" @close="onCloseOwner" :visible="visible_owner">
    <template #title>修改拥有者</template>
    <t-form :rules="rules_owner" ref="formOwnerRef" :data="formOwner">
      <t-row>
        <t-col :span="11">
          <t-form-item name="ownerAccountId" label="拥有者账号">
            <t-select clearable :filterable="!formOwner.ownerAccountId" :loading="loadingAccountSelect" style="width:100%" v-model="formOwner.ownerAccountId"
                :onInputChange="onInputChangeAccount" @search="remoteMethodAccount"
                :popup-props="{ 'on-scroll-to-bottom': handleScrollToBottomAccount }">
                <t-option :label="item.nickname + ' (' + (item?.phoneNumber || item?.username || item?.accountId) + ')'"
                  :value="item.accountId" v-for="(item, index) in accountList" :key="index">
                  <div style="display: flex;align-items: center;width: 100%;">
                    <t-avatar :imageProps="{ lazy: true }" size="20px" :image="item.avatarUrl" shape="round"></t-avatar>
                    <span style="display: inline-block;marginLeft:10px;">{{ item.nickname + ' (' + (item?.phoneNumber || item?.username || item?.accountId) + ')' }}</span>
                  </div>
                </t-option>
                 <template #valueDisplay="{ value }">
                  <template v-if="value">
                    <t-space>
                      <t-avatar :imageProps="{ lazy: true }" size="20px"
                        :image="accountList.filter(item => item.accountId == value)[0]?.avatarUrl" shape="round"></t-avatar>
                      {{ accountList.filter(item => item.accountId == value)[0]?.nickname + ' (' + (accountList.filter(item => item.accountId == value)[0]?.phoneNumber || accountList.filter(item => item.accountId == value)[0]?.username || accountList.filter(item => item.accountId == value)[0]?.accountId) + ')' }}
                    </t-space>
                  </template>
                </template>
              </t-select>
          </t-form-item>
        </t-col>
      </t-row>
    </t-form>
    <div class="empty"></div>
  </Dialog>

  <!-- 账号详情 -->
  <AccountInfo :data="accountDetail" ref="accountInfoRef" @close="onCloseAccountInfo"></AccountInfo>


  <!-- 预览 -->
  <t-image-viewer :on-close="onClosePreview" v-model:visible="previewShow" :images="previewList">
  </t-image-viewer>
</template>

<style lang="scss" scoped>
.list__wrapper {}
</style>
