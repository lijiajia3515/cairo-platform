<script setup lang="jsx">
import { ref, onMounted } from 'vue';
import {
  MessagePlugin,
  LoadingPlugin,
  DialogPlugin,
} from 'tdesign-vue-next';
import { useWindowSize } from '@vueuse/core';

import List from '@/components/list';
import FilterBar from '@/components/filterBar';
import FilterItem from '@/components/filterBar/item.vue';
import {timeColumn, copyColumn, opColumn, switchColumn} from '@/utils/tableColumns';
import {hasPermission} from '@/plugins/permission';
import Dialog from '@/components/dialog';
import UploadImage from '@/components/uploadImage';
import AccountInfo from '@/components/accountInfo';

import useState from '@/hooks/useState';

import {
  getAccountPageList_api,
  createAccount_api,
  modifyAccountInfo_api,
  modifyAccountStatus_api,
  resetAccountPassword_api,
  deleteAccount_api,
  logoffAccount_api,
  modifyAccountLockStatus_api,
  unlogoffAccount_api
} from '@/api';

const { width } = useWindowSize(); // 监听窗口大小

onMounted(() => {
  getAccountPage();
});

const [search, setSearch] = useState({
  keyword: null,
  logoffStatuses: [],
  enabled: null,
  locked: null
})

let page = ref(1);
let size = ref(10);
let total = ref(0);
const [list, setList] = useState([]);
const [loading, setLoading] = useState(true);

const [configs, setConfigs] = useState({
  data: list,
  columns: [
    copyColumn('accountId', '账号ID', {width: 150}),
    {
      colKey: '', title: '昵称', width: 120, cell: (h, { row }) => {
        return (
          <t-space size="small" >
            {
              row?.avatarUrl ? <t-avatar onClick={() => onWatchAccountInfo(row)} class="pick" key={Math.random()} hideOnLoadFailed={true} size="medium" image={row?.avatarUrl || row?.nickname} /> :
                <t-avatar onClick={() => onWatchAccountInfo(row)} class="pick" size="medium" >{row?.nickname?.slice(0, 2)}</t-avatar>
            }

            <div class="pick" onClick={() => onWatchAccountInfo(row)} style={{ height: '100%', display: 'flex', alignItems: 'center' }}>{row?.nickname || null}</div>
          </t-space>
        )
      }
    },
    { colKey: 'username', title: '用户名', width: 90, },
    { colKey: 'phoneNumber', title: '手机号', width: 90, },
    switchColumn({
      api: modifyAccountStatus_api,
      idKeys: ['accountId'],
      label: '账号',
      perm: 'account.modify_status',
      refresh: () => getAccountPage(),
    }),
    switchColumn({
      colKey: 'locked',
      title: '锁定状态',
      type: 'lock',
      api: modifyAccountLockStatus_api,
      idKeys: ['accountId'],
      label: '账号',
      perm: 'account.modify_lock_status',
      refresh: () => getAccountPage(),
    }),

    timeColumn('joinTime', '加入时间'),
    timeColumn('loginTime', '登录时间'),
    {
      colKey: 'logoffStatus', title: '注销状态', width: 80, cell: (h, { row }) => {
        return row['logoffStatus'] == 'No' ? '未注销' : (row['logoffStatus'] == 'Pending' ? '注销中' : '注销成功');
      }
    },
    timeColumn('logoffPendingTime', '注销时间'),
    timeColumn('logoffSuccessTime', '注销成功时间'),
    {
      colKey: 'metadata.updateUser.nickname', title: '更新账号', width: 140, cell: (h, { row }) => {
        return (
          <t-space size="small">
            {
              row?.metadata?.updateAccount?.avatarUrl ? <t-avatar onClick={() => onWatchAccountInfo(row?.metadata?.updateAccount)} class="pick" hideOnLoadFailed={true} alt={row?.metadata?.updateAccount?.nickname?.slice(0, 2)} size="medium" image={row?.metadata?.updateAccount?.avatarUrl} /> : (
                row?.metadata?.updateAccount?.nickname ? <t-avatar onClick={() => onWatchAccountInfo(row?.metadata?.updateAccount)} class="pick" size="medium" >{row?.metadata?.updateAccount?.nickname?.slice(0, 2)}</t-avatar> : null
              )
            }
            <div class="pick" onClick={() => onWatchAccountInfo(row?.metadata?.updateAccount)} style={{ height: '100%', display: 'flex', alignItems: 'center' }}>{row?.metadata?.updateAccount?.nickname || null}</div>
          </t-space>
        )
      }
    },
    timeColumn('metadata.updateTime', '更新时间'),
    opColumn([
      {content: '编辑', onClick: (row) => onEdit(row), visible: () => hasPermission('account.modify')},
      {content: '重置', theme: 'danger', onClick: (row) => onResetPassword(row), visible: () => hasPermission('account.reset_account_password')},
      {
        content: '注销',
        theme: 'danger',
        onClick: (row) => onLogOff(row),
        visible: (row) => hasPermission('account.logoff_account') && row.logoffStatus == 'No'
      },
      {
        content: '取消注销',
        theme: 'danger',
        onClick: (row) => onUnlogoff(row),
        visible: (row) => hasPermission('account.unlogoff_account') && row.logoffStatus == 'Pending'
      },
      {
        content: '删除',
        theme: 'danger',
        onClick: (row) => onDelete(row),
        visible: (row) => hasPermission('account.delete') && (row.logoffStatus == 'Pending' || row.logoffStatus == 'Success')
      },
    ], {width: 230})
  ],
  loading: loading,
  rowKey: 'accountId',
  pagination: {
    current: page,
    pageSize: size,
    total: total,
  },
  onPageChange: (pageInfo) => {
    page.value = pageInfo.current;
    size.value = pageInfo.pageSize;
    getAccountPage();
  }
});
/**
 *********************************************** 账号列表
 */
const getAccountPage = async () => {
  setLoading(true);
  try {
    let params = {
      page: page.value - 1,
      size: size.value,
      ...search.value
    }
    let res = await getAccountPageList_api(params);
    if (res.code == 'Success' && res.data && res.data.contents && res.data.contents.length) {
      setList(res?.data?.contents || []);
      total.value = Number(res?.data?.total || 0);
    } else {
      setList([]);
      total.value = 0;
    }
  } finally {
    setLoading(false);
  }
}
// 查询
const onSearch = () => {
  page.value = 1;
  getAccountPage();
}
// 重置
const onReset = () => {
  setSearch({
    keyword: null,
    logoffStatuses: [],
    enabled: null,
    locked: null
  })
  page.value = 1;
  getAccountPage();
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
 ***********************************************  添加/编辑 账号
 */
const validatorPhone = (val) => {
  const regExp = /^1(3\d|4[579]|5[^4\D]|6[67]|7[^249\D]|8\d|9[89])\d{8}$/;
  return new Promise((resolve) => {
    if (!val) {
      resolve(true);
    } else {
      resolve(regExp.test(val))
    }
  });
}
const rules = {
  username: [
    { required: true, message: '登录名必填', type: 'error', trigger: 'change' },
  ],
  phoneNumber: [
    { required: true, message: '登录名必填', type: 'error', trigger: 'change' },
    { validator: validatorPhone, message: '格式错误' },
  ],
  password: [
    { required: true, message: '密码必填', type: 'error', trigger: 'blur' },
    { min: 6, message: '输入密码字数应在6到40之间', type: 'error', trigger: 'blur' },
    { max: 40, message: '输入密码字数应在6到40之间', type: 'error', trigger: 'blur' },
  ]
};
let formRef = ref(null);
const [visible, setVisible] = useState(false);
const [type, setType] = useState('add');
const [fileList, setFileList] = useState([]);
const [editFileList, setEditFileList] = useState([]); // 编辑 过去显示
const [form, setForm] = useState({
  accountId: null,
  username: null,
  phoneNumber: null,
  email: null,
  password: null,
  nickname: null,
});

const onCreate = () => {
  setType('add');
  setVisible(true);
  formRef.value.clearValidate();
}
// 编辑
const onEdit = (row) => {
  setForm({
    ...form.value,
    username: row?.username,
    phoneNumber: row?.phoneNumber,
    email: row?.email,
    nickname: row?.nickname,
    accountId: row?.accountId,
  });
  if (row.avatarUrl) {
    setEditFileList([
      {
        url: row.avatarUrl,
        name: row.avatarUrl.split('/')[row.avatarUrl.split('/').length - 1]
      }
    ])
  }
  setType('edit');
  setVisible(true);
  formRef.value.clearValidate();
}
const onConfirm = async () => {
  const validate = await formRef.value.validate();
  if (validate == true) {
    LoadingPlugin(true);
    try {
      let params = {
        ...form.value
      };
      if (fileList.value.length) {
        params['avatarUrl'] = fileList.value[0].url;
      }
      if (type.value == 'add') {
        delete params['version'];
        let res = await createAccount_api(params);
        if (res.code == 'Success') {
          MessagePlugin.success('创建成功');
          onClose();
          getAccountPage();
        }
      } else {
        delete params['password'];
        let res = await modifyAccountInfo_api(params);
        if (res.code == 'Success') {
          MessagePlugin.success('修改成功');
          onClose();
          getAccountPage();
        }
      }
    } finally {
      LoadingPlugin(false);
    }
  }
}
const onClose = () => {
  setForm({
    accountId: null,
    username: null,
    phoneNumber: null,
    email: null,
    password: null,
    nickname: null,
  });
  setFileList([]);
  setEditFileList([]);
  setType('add');
  setVisible(false);
}

// 重置密码
const onResetPassword = (row) => {
  const confirmDia = DialogPlugin({
    header: '重置密码',
    body: '初始密码为123456, 是否继续操作?',
    confirmBtn: '确定',
    cancelBtn: '取消',
    onConfirm: async ({ e }) => {
      LoadingPlugin(true);
      try {
        let params = {
          accountId: row.accountId,
          password: '123456',
        }
        let res = await resetAccountPassword_api(params);
        if (res.code == 'Success') {
          MessagePlugin.success('重置成功');
          confirmDia.hide();
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

// 删除
const onDelete = (row) => {
  const confirmDia = DialogPlugin({
    header: '删除',
    body: '是否继续操作?',
    confirmBtn: '确定',
    cancelBtn: '取消',
    onConfirm: async ({ e }) => {
      LoadingPlugin(true);
      try {
        let params = {
          accountId: row.accountId,
        }
        let res = await deleteAccount_api(params);
        if (res.code == 'Success') {
          MessagePlugin.success('删除成功');
          confirmDia.hide();
          if (list.value && list.value.length == 1 && page.value > 1) {
            page.value = page.value - 1;
          }
          getAccountPage();
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
 * 注销账号
 * @param {Object} row
 * @param {String} row.accountId 账号Id
 */
const onLogOff = (row) => {
  const confirmDia = DialogPlugin({
    header: '注销账号',
    body: '是否继续操作?',
    confirmBtn: '确定',
    cancelBtn: '取消',
    onConfirm: async ({ e }) => {
      LoadingPlugin(true);
      try {
        let params = {
          accountId: row.accountId,
        }
        let res = await logoffAccount_api(params);
        if (res.code == 'Success') {
          confirmDia.hide();
          MessagePlugin.success('注销成功');
          setList([]);
          getAccountPage();

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


const onUnlogoff = (row) => {
  const confirmDia = DialogPlugin({
    header: '取消注销账号',
    body: '是否继续操作?',
    confirmBtn: '确定',
    cancelBtn: '取消',
    onConfirm: async ({ e }) => {
      LoadingPlugin(true);
      try {
        let params = {
          accountId: row.accountId,
        }
        let res = await unlogoffAccount_api(params);
        if (res.code == 'Success') {
          confirmDia.hide();
          MessagePlugin.success('取消注销成功');
          setList([]);
          getAccountPage();

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
 ****************************************** 图片
 * @param {*} files
 */
const onChangeFiles = (files) => {
  setFileList(files);
}
</script>


<template>
  <div v-allow="'account.find'" class="list__wrapper">
    <FilterBar @search="onSearch" @reset="onReset">
      <FilterItem label="关键字">
        <t-input clearable placeholder="请输入关键字" v-model="search.keyword"></t-input>
      </FilterItem>
      <FilterItem label="注销状态">
        <t-select clearable multiple v-model="search.logoffStatuses" placeholder="请选择注销状态">
          <t-option value="No" label="未注销"></t-option>
          <t-option value="Pending" label="注销中"></t-option>
          <t-option value="success" label="注销成功"></t-option>
        </t-select>
      </FilterItem>
      <FilterItem label="锁定状态">
        <t-select clearable v-model="search.locked" placeholder="请选择锁定状态">
          <t-option value="true" label="锁定"></t-option>
          <t-option value="false" label="未锁定"></t-option>
        </t-select>
      </FilterItem>
      <FilterItem label="启用状态">
        <t-select clearable v-model="search.enabled" placeholder="请选择启用状态">
          <t-option value="true" label="启用"></t-option>
          <t-option value="false" label="禁用"></t-option>
        </t-select>
      </FilterItem>
      <template #actions>
        <t-button v-allow="'account.create'" @click="onCreate">创建</t-button>
      </template>
    </FilterBar>
    <div class="empty"></div>
    <List @page-change="configs.onPageChange" :configs="configs" />
  </div>

  <!--添加 编辑账号 -->
  <Dialog :width="width < 750 ? '100%' : '50%'" :visible="visible" @confirm="onConfirm" @close="onClose" title="添加账号">
    <template #title>
      {{ type == 'add' ? '添加账号' : '编辑账号' }}
    </template>
    <t-form :rules="rules" :data="form" ref="formRef" label-width="88px">
      <t-row>
        <t-col :span="6">
          <t-form-item label="手机号" help="手机号 用户名 邮箱 三选一">
            <t-input v-model="form.phoneNumber"></t-input>
          </t-form-item>
        </t-col>
        <t-col :span="6">
          <t-form-item label="用户名" help="手机号 用户名 邮箱 三选一">
            <t-input v-model="form.username"></t-input>
          </t-form-item>
        </t-col>
      </t-row>
      <div class="empty"></div>
      <t-row>
        <t-col :span="6">
          <t-form-item label="邮箱" help="手机号 用户名 邮箱 三选一">
            <t-input v-model="form.email"></t-input>
          </t-form-item>
        </t-col>
        <t-col v-if="type == 'add'" :span="6">
          <t-form-item name="password" label="密码">
            <t-input v-model="form.password"></t-input>
          </t-form-item>
        </t-col>
      </t-row>
      <div class="empty"></div>
      <t-row>
        <t-col :span="6">
          <t-form-item label="昵称">
            <t-input v-model="form.nickname"></t-input>
          </t-form-item>
        </t-col>
        <t-col :span="6">
          <t-form-item label="头像">
            <UploadImage :accountId="form.accountId" type="public" picType="public" appId="project"
              @change="onChangeFiles" :fileList="editFileList" />
          </t-form-item>
        </t-col>
      </t-row>
    </t-form>
  </Dialog>

  <!-- 账号详情 -->
  <AccountInfo :data="accountDetail" ref="accountInfoRef" @close="onCloseAccountInfo"></AccountInfo>
</template>

<style lang="scss" scoped>
.list__wrapper {
  header {
    box-sizing: border-box;
  }
}
</style>
