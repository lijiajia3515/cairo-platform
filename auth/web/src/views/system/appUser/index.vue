<script setup lang="jsx">
import {
  ref, onMounted,
  nextTick, watch,
} from 'vue';
import {debounce} from 'lodash';
import {
  MessagePlugin,
  LoadingPlugin,
  DialogPlugin,
} from 'tdesign-vue-next';

import useState from '@/hooks/useState';

import List from '@/components/list';
import FilterBar from '@/components/filterBar';
import FilterItem from '@/components/filterBar/item.vue';
import { timeColumn, opColumn, switchColumn, userColumn } from '@/utils/tableColumns';
import {hasPermission} from '@/plugins/permission';
import Dialog from '@/components/dialog';
import UserInfo from '@/components/userInfo';
import UploadImage from '@/components/uploadImage';

import {
  getAppUserPageList_api,
  createAppUser_api,
  createAccountAndAppUser_api,
  modifyAppUserInfo_api,
  logoffAppUser_api,
  deleteAppUser_api,
  modifyAppUserStatus_api,

  getAccountPageList_api,
  getAppRoleList_api,
  getAppDepartmentTree_api,
  getAppUserTagList_api,

  searchAccountInfo_api,
  transferAppUserToOtherAccount_api,
  unlogoffAppUser_api
} from '@/api';

onMounted(() => {
  getUserPage();

  nextTick(() => {
    // getSearchAccountSelectPage();
    getRoleList();
    getUserTagList();
    getDepartmentTreeFind();
  })
});

const [search, setSearch] = useState({
  keyword: null,
  accountIds: '',
  roleIds: [],
  departmentIds: [],
  tagIds: [],
  enabled: null,
  logoffStatuses: []
})
let page = ref(1);
let size = ref(10);
let total = ref(0);
let list = ref([]);
const [loading, setLoading] = useState(false);
const [configs, setConfigs] = useState({
  data: list,
  columns: [
    {colKey: 'userId', title: 'ID', width: 120, ellipsis: true},
    {colKey: 'nickname', title: '昵称', width: 120, ellipsis: true},
    {colKey: 'phoneNumber', title: '联系方式', width: 120, ellipsis: true},
    {
      colKey: 'roleNames', title: '角色', width: 100, ellipsis: true, cell: (h, {row}) => {
        return row?.roles?.map(item => item.roleName ?? '').join(';') ?? '';
      }
    },
    {
      colKey: 'departmentNames', title: '部门', width: 100, ellipsis: true, cell: (h, {row}) => {
        const dom = row?.departments?.map((item, index) => {
          return <span class={'pick'}
                       onClick={() => onShowDepartmentDetail(row)}>{item?.departmentNames[item?.departmentNames.length - 1]}{item?.departmentIds[item?.departmentIds.length - 1] == row['mainDepartmentId'] ? '(主部门)' : ''}&nbsp;{index < row?.departments.length - 1 ? ';' : ''}&nbsp;</span>
        })
        return (
            <t-tooltip
                class="placement top center"
                content={dom}
                placement="top"
                overlayStyle={{width: '200px'}}
                showArrow
            >
              <div class={'sl1'}>
                {
                  dom
                }
              </div>
            </t-tooltip>
        )
      }
    },
    {
      colKey: 'position', title: '职务'
    },
    // {
    //   colKey: 'appAdmin', title: '是否应用管理员', cell: (h, { row }) => {
    //     let key = row['appAdmin'];
    //     return key == true ? '是' : (key == false ? '否' : '')
    //   }
    // },
    switchColumn({
      api: modifyAppUserStatus_api,
      idKeys: ['userId'],
      label: '用户',
      perm: 'user.modify_status',
      refresh: () => getUserPage(),
    }),
    userColumn({ colKey: 'account', title: '账号', nameKey: 'accountNickname', avatarKey: 'accountAvatarUrl', idKey: 'accountId', onClick: (src, row) => onWatchAccount(row), width: 140 }),

    timeColumn('joinTime', '加入时间'),
    timeColumn('loginTime', '登录时间'),
    {
      colKey: 'logoffStatus', title: '注销状态', width: 80, cell: (h, {row}) => {
        return row['logoffStatus'] == 'No' ? '未注销' : (row['logoffStatus'] == 'Pending' ? '注销中' : '注销成功');
      }
    },
    timeColumn('logoffPendingTime', '注销时间'),
    timeColumn('logoffSuccessTime', '注销成功时间'),
    {
      colKey: 'metadata.updateUser.nickname', title: '更新人', cell: (h, {row}) => {
        return (
            <t-space size="small">
              {
                row?.metadata?.updateUser?.accountAvatarUrl ?
                    <t-avatar class="pick" onClick={() => onWatchUserInfo(row)} hideOnLoadFailed={true}
                              alt={row?.metadata?.updateUser?.nickname?.slice(0, 2)} size="medium"
                              image={row?.metadata?.updateUser?.accountAvatarUrl}/> : (
                        row?.metadata?.updateUser?.nickname ? <t-avatar class="pick" onClick={() => onWatchUserInfo(row)}
                                                                        size="medium">{row?.metadata?.updateUser?.nickname?.slice(0, 2)}</t-avatar> : null
                    )
              }
              <div class="pick" onClick={() => onWatchUserInfo(row)} style={{
                height: '100%',
                display: 'flex',
                alignItems: 'center'
              }}>{row?.metadata?.updateUser?.nickname || null}</div>
            </t-space>
        )
      }
    },
    timeColumn('metadata.updateTime', '更新时间'),
    opColumn([
      {content: '编辑', onClick: (row) => onEdit(row), visible: () => hasPermission('user.modify_info')},
      {content: '转至其他账号', onClick: (row) => onTransfer(row), visible: () => hasPermission('user.transfer_user_to_other_account')},
      {
        content: '强制注销',
        theme: 'danger',
        onClick: (row) => onLogOff(row),
        visible: (row) => hasPermission('user.logoff_user') && (row.logoffStatus == 'No' || row.logoffStatus == 'Pending')
      },
      {
        content: '取消注销',
        theme: 'danger',
        onClick: (row) => onUnlogoff(row),
        visible: (row) => hasPermission('user.unlogoff_user') && row.logoffStatus == 'Pending'
      },
      {
        content: '删除',
        theme: 'danger',
        onClick: (row) => onDelete(row),
        visible: (row) => hasPermission('user.delete') && row.logoffStatus == 'Success'
      },
    ], {width: 230})
  ],
  loading: loading,
  rowKey: 'userId',
  pagination: {
    current: page,
    pageSize: size,
    total: total,
  },
  onPageChange: (pageInfo) => {
    page.value = pageInfo.current;
    size.value = pageInfo.pageSize;
    getUserPage();
  }
});


/**
 * 部门详情
 */
const [visibleDepartmentDetail, setVisibleDepartmentDetail] = useState(false);
const [departmentDetail, setDepartmentDetail] = useState({});
const onShowDepartmentDetail = (row) => {
  setVisibleDepartmentDetail(true);
  setDepartmentDetail(row || {});
}
const onCloseDepartmentDetail = () => {
  setVisibleDepartmentDetail(false);
  setDepartmentDetail({});
}


/**
 **************************************************** 用户详情
 */
let userInfoRef = ref(null);
const [userDetail, setUserDetail] = useState({});
const onWatchUserInfo = (row) => {
  userInfoRef.value.open();
  setUserDetail(row.metadata.updateUser)
}
const onCloseUserDetail = () => {
  setUserDetail({});
}
/**
 **************************************** 用户分页
 */
const getUserPage = async () => {
  setLoading(true);
  try {
    let params = {
      page: page.value - 1,
      size: size.value,
      keyword: search.value.keyword,
      accountIds: search.value.accountIds ? [search.value.accountIds] : [],
      departmentIds: search.value.departmentIds,
      roleIds: search.value.roleIds,
      tagIds: search.value.tagIds,
      enabled: search.value.enabled,
      logoffStatuses: search.value.logoffStatuses
    };
    let res = await getAppUserPageList_api(params);
    if (res.code == 'Success') {
      list.value = res?.data?.contents || [];
      total.value = Number(res?.data?.total || 0)
    }
  } finally {
    setLoading(false);
  }
}
const onSearch = () => {
  page.value = 1;
  getUserPage();
}
const onReset = () => {
  page.value = 1;
  setSearch({
    keyword: null,
    accountIds: '',
    roleIds: [],
    departmentIds: [],
    tagIds: [],
    enabled: null,
    logoffStatuses: []
  })
  getUserPage();
}


/**
 * 创建用户
 */
const rules = {
  accountId: [
    {required: true, message: '账号必选', type: 'error', trigger: 'change'},
  ]
};
const rules2 = {
  // PhoneNumber: [
  //   { required: true, message: '手机号必填', type: 'error', trigger: 'blur' },
  //   { whitespace: true, message: '手机号不能为空' },
  //   { min: 11, message: '手机号字数应在11到20之间', type: 'error', trigger: 'blur' },
  //   { max: 20, message: '手机号字数应在11到20之间', type: 'error', trigger: 'blur' },
  // ],
  password: [
    // { required: true, message: '密码必填', type: 'error', trigger: 'blur' },
    {whitespace: true, message: '密码不能为空'},
    {min: 6, message: '密码个数应在6到40之间', type: 'error', trigger: 'blur'},
    {max: 40, message: '密码个数应在6到40之间', type: 'error', trigger: 'blur'},
  ],
  nickname: [
    {required: true, message: '昵称必填', type: 'error', trigger: 'blur'},
    {whitespace: true, message: '昵称不能为空'},
  ]
}
let createKey = ref(1);
let formRef = ref(null);
let form2Ref = ref(null);
const [visible, setVisible] = useState(false);
// 创建用户
const [form, setForm] = useState({
  accountId: null,
  nickname: null,
  phoneNumber: null,
  roleIds: [],
  departmentIds: [],
  tagIds: [],
  position: '', // 职务
  mainDepartmentId: '',
});
// 创建账号和用户
const [form2, setForm2] = useState({
  phoneNumber: null,
  username: null,
  email: null,
  password: null,
  nickname: null,
  roleIds: [],
  departmentIds: [],
  tagIds: [],
  position: '',
  mainDepartmentId: '', // 主部门ID
});
const [fileList, setFileList] = useState([]);
// 获取头像
const onChangeFiles = (files) => {
  setFileList(files)
}

const onCreate = () => {
  setVisible(true);
  if (createKey.value == 1) {
    formRef.value.clearValidate();
  }
  getAccountSelectPage();
}
const onSubmit = async () => {
  if (createKey.value == 1) { // 创建用户
    const validate = await formRef.value.validate();
    if (validate == true) {
      LoadingPlugin(true);
      try {
        let {accountId, nickname, phoneNumber, roleIds, departmentIds, tagIds, position, mainDepartmentId} = form.value;
        let params = {
          accountId: accountTransferData.value.accountId,
          nickname, phoneNumber, tagIds, position, mainDepartmentId,
        }
        if (roleIds) {
          params['roleIds'] = roleIds;
        }
        if (departmentIds) {
          params['departmentIds'] = departmentIds;
        }
        let res = await createAppUser_api(params);
        if (res.code == 'Success') {
          MessagePlugin.success('创建成功');
          onClose();
          getUserPage();
        }
      } finally {
        LoadingPlugin(false);
      }
    }
  } else { // 创建账号并且创建用户
    let {
      phoneNumber,
      username,
      email,
      password,
      nickname,
      roleIds,
      departmentIds,
      tagIds,
      position,
      mainDepartmentId
    } = form2.value;
    const validate = await form2Ref.value.validate();
    if (validate == true) {
      if (phoneNumber || username || email) {
        LoadingPlugin(true);
        try {
          let params = {
            phoneNumber, username, email, password, nickname, tagIds, position, mainDepartmentId,
          };
          if (roleIds) {
            params['roleIds'] = roleIds;
          }
          if (departmentIds) {
            params['departmentIds'] = departmentIds;
          }
          if (fileList.value.length) {
            params['avatarUrl'] = fileList.value[0].url;
          }
          let res = await createAccountAndAppUser_api(params);
          if (res.code == 'Success') {
            MessagePlugin.success('创建成功');
            onClose();
            getUserPage();
          }
        } finally {
          LoadingPlugin(false);
        }
      } else {
        MessagePlugin.error('手机号 用户名 邮箱 三选一');
      }
    }
  }
}
const onClose = () => {
  setVisible(false);
  if (createKey.value == 1) {
    setForm({
      accountId: null,
      nickname: null,
      phoneNumber: null,
      roleIds: [],
      departmentIds: [],
      tagIds: [],
      position: '', // 职位
      mainDepartmentId: '',
    });
    setAccountList([]);
    setAccountTransferData({})
  } else {
    setForm2({
      phoneNumber: null,
      username: null,
      email: null,
      password: null,
      nickname: null,
      roleIds: [],
      departmentIds: [],
      tagIds: [],
      position: '',
      mainDepartmentId: '',
    });
    setFileList([]);
  }
}


/**
 * 编辑用户
 */
const [visibleEdit, setVisibleEdit] = useState(false);
const [formEdit, setFormEdit] = useState({
  userId: null,
  nickname: null,
  phoneNumber: null,
  tagIds: [],
  roleIds: [],
  departmentIds: [],
  position: '',
  mainDepartmentId: '',
})
const onEdit = (row) => {
  // getAccountList();
  setVisibleEdit(true);
  let departments = [];
  if (row?.departments) {
    row?.departments.forEach((item) => {
      departments.push(item?.departmentIds[item?.departmentIds.length - 1]);
    })
  }
  setFormEdit({
    ...formEdit.value,
    userId: row.userId,
    nickname: row.nickname,
    phoneNumber: row.phoneNumber,
    tagIds: row?.tags && row.tags.length ? row.tags.map(item => item.tagId) : [],
    roleIds: row?.roles ? (row?.roles.map(item => item.roleId)) : [],
    departmentIds: departments,
    position: row?.position,
    mainDepartmentId: row?.mainDepartmentId,
  });
}
const onSubmitEdit = async () => {
  LoadingPlugin(true);
  try {
    let {userId, nickname, phoneNumber, tagIds, roleIds, departmentIds, position, mainDepartmentId,} = formEdit.value;
    let params = {
      userId, nickname, phoneNumber, tagIds, position, mainDepartmentId,
    };
    if (roleIds) {
      params['roleIds'] = roleIds;
    }
    if (departmentIds) {
      params['departmentIds'] = departmentIds;
    }
    let res = await modifyAppUserInfo_api(params);
    if (res.code == 'Success') {
      MessagePlugin.success('编辑成功');
      onCloseEdit();
      getUserPage();
    }
  } finally {
    LoadingPlugin(false);
  }
}
const onCloseEdit = () => {
  setVisibleEdit(false);
  setFormEdit({
    userId: null,
    nickname: null,
    phoneNumber: null,
    tagIds: [],
    roleIds: [],
    departmentIds: [],
    position: '',
    mainDepartmentId: '',
  })
}


// 选择 部门 （编辑用户）
const onChangeDepartmentFormEdit = (value, id) => {
  if (value) {
    let index = formEdit.value.departmentIds.findIndex(item => item == id);
    if (index == -1) {
      let departmentIds = formEdit.value.departmentIds;
      departmentIds.push(id);
      setFormEdit({
        ...formEdit.value,
        departmentIds
      })
    }
  } else {
    let index = formEdit.value.departmentIds.findIndex(item => item == id);
    let departmentIds = formEdit.value.departmentIds;
    if (index != -1) {
      departmentIds.splice(index, 1);
      setFormEdit({
        ...formEdit.value,
        departmentIds
      })
    }
  }
}

// 设置为主部门 （编辑用户）
const onSetMainDepartmentFormEdit = (id) => {
  let index = formEdit.value.departmentIds.findIndex(item => item == id);
  if (index == -1) {
    MessagePlugin.error('请先选择部门');
    return;
  }
  setFormEdit({
    ...formEdit.value,
    mainDepartmentId: id
  })
}
// （编辑用户）
watch(() => formEdit.value.departmentIds, () => {
  if (formEdit.value.departmentIds.length == 0) {
    formEdit.value.mainDepartmentId = '';
    // setFormEdit({
    //   ...formEdit.value,
    //   mainDepartmentId: ''
    // })
  } else {
    let index = formEdit.value.departmentIds.findIndex(item => item == formEdit.value.mainDepartmentId);
    if (index == -1) {
      formEdit.value.mainDepartmentId = '';
    }
  }
}, {
  deep: true
})

/**
 * 注销用户
 * @param {Object} row
 */
const onLogOff = (row) => {
  const confirmDia = DialogPlugin({
    header: '用户强制注销',
    body: '是否继续注销？',
    confirmBtn: '确定',
    cancelBtn: '取消',
    onConfirm: async ({e}) => {
      LoadingPlugin(true);
      try {
        let params = {
          userId: row.userId,
        };
        let res = await logoffAppUser_api(params);
        if (res.code == 'Success') {
          MessagePlugin.success('注销成功');
          confirmDia.hide();
          list.value = [];
          getUserPage();
        }
      } finally {
        LoadingPlugin(false);
      }
    },
    onClose: ({e, trigger}) => {
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
    onConfirm: async ({e}) => {
      LoadingPlugin(true);
      try {
        let params = {
          userId: row.userId
        }
        let res = await unlogoffAppUser_api(params);
        if (res.code == 'Success') {
          confirmDia.hide();
          MessagePlugin.success('取消注销成功');
          list.value = [];
          getUserPage();
        }
      } finally {
        LoadingPlugin(false);
      }
    },
    onClose: ({e, trigger}) => {
      confirmDia.hide();
    },
  });
}


/**
 * 用户删除
 * @param {Object} row
 */
const onDelete = (row) => {
  const confirmDia = DialogPlugin({
    header: '用户删除',
    body: '是否继续删除',
    confirmBtn: '确定',
    cancelBtn: '取消',
    onConfirm: async ({e}) => {
      LoadingPlugin(true);
      try {
        let params = {
          userId: row.userId,
        };
        let res = await deleteAppUser_api(params);
        if (res.code == 'Success') {
          MessagePlugin.success('删除成功');
          confirmDia.hide();
          getUserPage();
        }
      } finally {
        LoadingPlugin(false);
      }
    },
    onClose: ({e, trigger}) => {
      confirmDia.hide();
    },
  });
}


/**
 * 查看账号详情
 */
const [visibleAccount, setVisibleAccount] = useState(false);
const [accountData, setAccountData] = useState({});
const onWatchAccount = (row) => {
  setVisibleAccount(true);
  setAccountData(row);
}
const onCloseAccount = () => {
  setAccountData({});
  setVisibleAccount(false);
}


/**
 ************************************* 账号列表
 */
const [accountList, setAccountList] = useState([]);


/**
 * 角色列表
 */
const [roleList, setRoleList] = useState([]);
const getRoleList = async () => {
  let res = await getAppRoleList_api({});
  if (res.code == 'Success') {
    setRoleList(res?.data || []);
  }
}
/**
 * 部门 树形列表
 */
const [departmentList, setDepartmentList] = useState([]);
const getDepartmentTreeFind = async () => {
  let res = await getAppDepartmentTree_api({});
  if (res.code == 'Success') {
    setDepartmentList(res?.data ? [res.data] : []);
  }
}
/**
 * 用户标签列表
 */
const [tagList, setTagList] = useState([]);
const getUserTagList = async () => {
  let res = await getAppUserTagList_api({});
  if (res.code == 'Success') {
    setTagList(res?.data || []);
  }
}


const onAccountChange = () => {
  setForm({
    ...form.value,
    nickname: accountList.value.filter(item => item.accountId == form.value.accountId)[0]?.nickname
  })
}


// 转移至其他账号
let transferType = ref('phone_number');
const protocolSelect = ref(() => (
    <t-select
        defaultValue="phone_number"
        options={[
          {label: '手机号', value: 'phone_number'},
          {label: '登录名', value: 'username'},
          {label: '邮箱', value: 'email'},
          {label: '账号ID', value: 'account_id'}
        ].map((item) => ({label: item.label, value: item.value}))}
        onChange={onChangeType}
    />
));
const columnsTransfer = [
  {colKey: 'nickname', title: '昵称'},
  {colKey: 'userId', title: '用户ID'},
  {colKey: 'accountId', title: '账号ID'},
]
let keywordTransfer = ref('');
const [visibleTransfer, setVisibleTransfer] = useState(false);
const [userListTransfer, setUserListTransfer] = useState([]);
const [accountTransferData, setAccountTransferData] = useState({});
const onTransfer = (row) => {
  setVisibleTransfer(true);
  setUserListTransfer([row])
}
const onChangeType = (value) => {
  transferType.value = value;
  keywordTransfer.value = '';
}
// 搜索账号
const onSearchAccount = debounce(async () => {
  if (!keywordTransfer.value) {
    setAccountTransferData({});
    return
  }

  try {
    let params = {
      type: transferType.value,
    };
    switch (transferType.value) {
      case 'username':
        params['username'] = keywordTransfer.value;
        break;
      case 'phone_number':
        params['phoneNumber'] = keywordTransfer.value;
        break;
      case 'account_id':
        params['accountId'] = keywordTransfer.value;
        break;
      case 'email':
        params['email'] = keywordTransfer.value;
        break;
    }
    let res = await searchAccountInfo_api(params);
    if (res.code == 'Success') {
      setAccountTransferData(res?.data || {});
    } else {
      setAccountTransferData({});
    }
  } catch (err) {
    console.log(err)
  }
})

const onSearchAccountId = debounce(async () => {
  if (!form.value.accountId) {
    setAccountTransferData({});
    return
  }

  try {
    let params = {
      type: transferType.value,
    };
    switch (transferType.value) {
      case 'username':
        params['username'] = form.value.accountId;
        break;
      case 'phone_number':
        params['phoneNumber'] = form.value.accountId;
        break;
      case 'account_id':
        params['accountId'] = form.value.accountId;
        break;
      case 'email':
        params['email'] = form.value.accountId;
        break;
    }
    let res = await searchAccountInfo_api(params);
    if (res.code == 'Success') {
      setAccountTransferData(res?.data || {});
    } else {
      setAccountTransferData({});
    }
  } catch (err) {
    console.log(err)
  }
})
const onCloseTransfer = () => {
  setVisibleTransfer(false);
  setUserListTransfer([]);
  setAccountTransferData({});
  keywordTransfer.value = '';
  transferType.value = 'phone_number';
}
const onSubmitTransfer = async () => {
  if (!accountTransferData.value.accountId) {
    MessagePlugin.error('未查询到账号信息');
    return;
  }
  LoadingPlugin(true);
  try {
    let params = {
      userId: userListTransfer.value[0].userId,
      otherAccountId: accountTransferData.value.accountId,
    };
    let res = await transferAppUserToOtherAccount_api(params);
    if (res.code == 'Success') {
      MessagePlugin.success('转移成功');
      onCloseTransfer();
      getUserPage();
    }
  } finally {
    LoadingPlugin(false);
  }
}


// 选择 部门 （创建新用户）
const onChangeDepartmentForm2 = (value, id) => {
  if (value) {
    let index = form2.value.departmentIds.findIndex(item => item == id);
    if (index == -1) {
      let departmentIds = form2.value.departmentIds;
      departmentIds.push(id);
      setForm2({
        ...form2.value,
        departmentIds
      })
    }
  } else {
    let index = form2.value.departmentIds.findIndex(item => item == id);
    let departmentIds = form2.value.departmentIds;
    if (index != -1) {
      departmentIds.splice(index, 1);
      setForm2({
        ...form2.value,
        departmentIds
      })
    }
  }
}

// 设置为主部门 （创建新用户）
const onSetMainDepartment = (id) => {
  let index = form2.value.departmentIds.findIndex(item => item == id);
  if (index == -1) {
    MessagePlugin.error('请先选择部门');
    return;
  }
  setForm2({
    ...form2.value,
    mainDepartmentId: id
  })
}
// （创建新用户）
watch(() => form2.value.departmentIds, () => {
  let index = form2.value.departmentIds.findIndex(item => item == form2.value.mainDepartmentId);
  if (index == -1) {
    form2.value.mainDepartmentId = '';
    // setForm2({
    //   ...form2.value,
    //   mainDepartmentId: ''
    // })
  }
}, {
  deep: true
})


// const valueDisplayForm2 = () => {
//   console.log('自定义选中值')
// }

// const showTag = (value) => {
//     console.log('显示标签', value)
//     return '6'
// }


// 选择 部门 （创建用户）
const onChangeDepartmentForm = (value, id) => {
  if (value) {
    let index = form.value.departmentIds.findIndex(item => item == id);
    if (index == -1) {
      let departmentIds = form.value.departmentIds;
      departmentIds.push(id);
      setForm({
        ...form.value,
        departmentIds
      })
    }
  } else {
    let index = form.value.departmentIds.findIndex(item => item == id);
    let departmentIds = form.value.departmentIds;
    if (index != -1) {
      departmentIds.splice(index, 1);
      setForm({
        ...form.value,
        departmentIds
      })
    }
  }
}

// 设置为主部门 （创建用户）
const onSetMainDepartmentForm = (id) => {
  let index = form.value.departmentIds.findIndex(item => item == id);
  if (index == -1) {
    MessagePlugin.error('请先选择部门');
    return;
  }
  setForm({
    ...form.value,
    mainDepartmentId: id
  })
}
// （创建用户）
watch(() => form.value.departmentIds, () => {
  let index = form.value.departmentIds.findIndex(item => item == form.value.mainDepartmentId);
  if (index == -1) {
    form.value.mainDepartmentId = '';
    // setForm({
    //   ...form.value,
    //   mainDepartmentId: ''
    // })
  }
}, {
  deep: true
})


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
</script>


<template>
  <div v-allow="'user.find'" class="appUser__wrapper">
    <FilterBar @search="onSearch" @reset="onReset">
      <FilterItem label="关键字">
        <t-input clearable placeholder="请输入关键字" v-model="search.keyword"></t-input>
      </FilterItem>
      <FilterItem label="角色">
        <t-select clearable filterable multiple v-model="search.roleIds" placeholder="请选择角色">
          <t-option :label="item.roleName" :value="item.roleId" v-for="(item, index) in roleList"
                    :key="index"></t-option>
        </t-select>
      </FilterItem>
      <FilterItem label="部门">
        <t-cascader :keys="{ label: 'departmentName', value: 'departmentId', children: 'subs' }"
                    v-model="search.departmentIds" :options="departmentList" check-strictly multiple clearable
                    filterable
                    placeholder="请选择部门"/>
      </FilterItem>
      <FilterItem label="用户标签">
        <t-select :min-collapsed-num="2" clearable filterable multiple v-model="search.tagIds" placeholder="请选择用户标签">
          <t-option :label="item.tagName" :value="item.tagId" v-for="(item, index) in tagList"
                    :key="index"></t-option>
        </t-select>
      </FilterItem>
      <FilterItem label="状态">
        <t-select clearable v-model="search.enabled" placeholder="请选择状态">
          <t-option label="启用" :value="true"></t-option>
          <t-option label="禁用" :value="false"></t-option>
        </t-select>
      </FilterItem>
      <FilterItem label="注销状态">
        <t-select clearable multiple v-model="search.logoffStatuses" placeholder="请选择注销状态">
          <t-option value="No" label="未注销"></t-option>
          <t-option value="Pending" label="注销中"></t-option>
          <t-option value="success" label="注销成功"></t-option>
        </t-select>
      </FilterItem>
      <template #actions>
        <t-button v-allow="'user.create'" @click="onCreate">创建</t-button>
      </template>
    </FilterBar>
    <div class="empty"></div>
    <List @page-change="configs.onPageChange" :configs="configs"/>
  </div>


  <!-- 创建 用户 -->
  <Dialog @confirm="onSubmit" @close="onClose" top="20" :visible="visible">
    <template #title>创建</template>
    <t-tabs v-model="createKey">
      <t-tab-panel :value="1" label="创建用户">
        <div class="empty"></div>
        <t-form ref="formRef" :rules="rules" :data="form" style="width:calc(100% - 30px)" label-width="90px">
          <p>账号信息</p>
          <div class="empty"></div>
          <t-row>
            <t-col :span="12">
              <t-form-item name="accountId" label="账号">
                <t-input-adornment :prepend="protocolSelect">
                  <t-input v-model="form.accountId" @blur="onSearchAccountId" @enter="onSearchAccountId"
                           style="width:350px" placeholder="请输入内容"/>
                </t-input-adornment>
              </t-form-item>
              <div style="margin:auto;display: flex;justify-content: center;"
                   v-if="Object.keys(accountTransferData).length">
                <div style="width:130px;height:130px;">
                  <t-image style="width:130px;height:130px" :src="accountTransferData.avatarUrl" fit="cover"/>
                </div>
                <div style="padding-left:10px;box-sizing: border-box;">
                  <p style="text-align: center;line-height: 40px;">账号ID：{{ accountTransferData.accountId }}</p>
                  <p style="text-align: center;line-height: 40px;">昵称：{{ accountTransferData.nickname }}</p>
                  <p style="text-align: center;line-height: 40px;">加入时间：{{ accountTransferData.joinTime }}</p>
                </div>
              </div>
            </t-col>
          </t-row>
          <div class="empty"></div>
          <p>用户信息</p>
          <div class="empty"></div>
          <t-row>
            <t-col :span="6">
              <t-form-item label="昵称">
                <t-input v-model="form.nickname"></t-input>
              </t-form-item>
            </t-col>
            <t-col :span="6">
              <t-form-item label="联系方式">
                <t-input v-model="form.phoneNumber"></t-input>
              </t-form-item>
            </t-col>
          </t-row>
          <div class="empty"></div>
          <t-row>
            <t-col :span="12">
              <t-form-item label="角色">
                <t-select multiple clearable filterable v-model="form.roleIds">
                  <t-option :label="item.roleName" :value="item.roleId" v-for="(item, index) in roleList"
                            :key="index"></t-option>
                </t-select>
              </t-form-item>
            </t-col>
          </t-row>
          <div class="empty"></div>
          <t-row>
            <t-col :span="12">
              <t-form-item label="部门">
                <t-cascader :keys="{ label: 'departmentName', value: 'departmentId', children: 'subs' }"
                            v-model="form.departmentIds" :options="departmentList" checkStrictly clearable filterable
                            multiple
                            :popup-props="{ overlayClassName: 'tdesign-demo-select__overlay-option' }">
                  <template #option="{ item }">
                    <div class="tdesign-demo__user-option" style="width: 100%;">
                      <div style="display:flex;">
                        <t-checkbox :checked="form.departmentIds.includes(item.departmentId)"
                                    @change="onChangeDepartmentForm($event, item.departmentId)" label=""></t-checkbox>
                        <span class="sl1" style="display: inline-block;width: 116px;">{{ item.departmentName }}</span>
                        <span v-if="form.mainDepartmentId != item.departmentId"
                              @click="onSetMainDepartmentForm(item.departmentId)"
                              style="color:#0052d9;font-weight: 600;">设为主部门</span>
                        <span v-else style="cursor: default;color:#333">主部门</span>
                      </div>
                    </div>
                  </template>
                </t-cascader>
              </t-form-item>
            </t-col>
          </t-row>
          <div class="empty"></div>
          <t-row>
            <t-col :span="6">
              <t-form-item label="用户标签">
                <t-select :min-collapsed-num="2" clearable filterable multiple v-model="form.tagIds">
                  <t-option :label="item.tagName" :value="item.tagId" v-for="(item, index) in tagList"
                            :key="index"></t-option>
                </t-select>
              </t-form-item>
            </t-col>
            <t-col :span="6">
              <t-form-item label="职务">
                <t-input v-model="form.position"></t-input>
              </t-form-item>
            </t-col>
          </t-row>
        </t-form>
      </t-tab-panel>
      <t-tab-panel :value="2" label="创建新用户">
        <div class="empty"></div>
        <t-form class="userlistForm" ref="form2Ref" :rules="rules2" :data="form2" style="width:calc(100% - 30px)"
                label-width="90px">
          <p>账号信息</p>
          <div class="empty"></div>
          <t-row>
            <t-col :span="6">
              <!-- help="手机号 用户名 邮箱 三选一" -->
              <t-form-item name="phoneNumber" label="手机号">
                <t-input v-model="form2.phoneNumber"></t-input>
              </t-form-item>
            </t-col>
            <t-col :span="6">
              <t-form-item label="用户名" help="">
                <t-input v-model="form2.username"></t-input>
              </t-form-item>
            </t-col>
          </t-row>
          <div class="empty"></div>
          <t-row>
            <t-col :span="6">
              <t-form-item label="邮箱" help="">
                <t-input v-model="form2.email"></t-input>
              </t-form-item>
            </t-col>
            <t-col :span="6">
              <t-form-item name="password" label="密码">
                <t-input v-model="form2.password"></t-input>
              </t-form-item>
            </t-col>
          </t-row>
          <div class="empty"></div>
          <p>用户信息</p>
          <div class="empty"></div>
          <t-row>
            <t-col :span="6">
              <t-form-item name="nickname" label="昵称">
                <t-input v-model="form2.nickname"></t-input>
              </t-form-item>
            </t-col>
          </t-row>
          <div class="empty"></div>
          <t-row>
            <t-col :span="12">
              <t-form-item label="角色">
                <t-select multiple clearable filterable v-model="form2.roleIds">
                  <t-option :label="item.roleName" :value="item.roleId" v-for="(item, index) in roleList"
                            :key="index"></t-option>
                </t-select>
              </t-form-item>
            </t-col>
          </t-row>
          <div class="empty"></div>
          <!-- 主部门ID: {{ form2.mainDepartmentId }} -->
          <t-row>
            <t-col :span="12">
              <t-form-item label="部门">
                <!-- :tagInputProps="{ tag : showTag }" -->
                <t-cascader :keys="{ label: 'departmentName', value: 'departmentId', children: 'subs' }"
                            v-model="form2.departmentIds" :options="departmentList" checkStrictly clearable filterable
                            multiple
                            :popup-props="{ overlayClassName: 'tdesign-demo-select__overlay-option' }">
                  <!-- <template #valueDisplay="{ value, selectedOptions, onClose }">
                    <span>999</span>
                  </template> -->
                  <template #option="{ item }">
                    <div class="tdesign-demo__user-option" style="width: 100%;">
                      <div style="display:flex;">
                        <t-checkbox :checked="form2.departmentIds.includes(item.departmentId)"
                                    @change="onChangeDepartmentForm2($event, item.departmentId)" label=""></t-checkbox>
                        <span class="sl1" style="display: inline-block;width: 116px;">{{ item.departmentName }}</span>
                        <span v-if="form2.mainDepartmentId != item.departmentId"
                              @click="onSetMainDepartment(item.departmentId)"
                              style="color:#0052d9;font-weight: 600;">设为主部门</span>
                        <span v-else style="cursor: default;color:#333">主部门</span>
                      </div>
                    </div>
                  </template>
                </t-cascader>
              </t-form-item>
            </t-col>
          </t-row>
          <div class="empty"></div>
          <t-row>
            <t-col :span="6">
              <t-form-item label="用户标签">
                <t-select :min-collapsed-num="2" clearable filterable multiple v-model="form2.tagIds">
                  <t-option :label="item.tagName" :value="item.tagId" v-for="(item, index) in tagList"
                            :key="index"></t-option>
                </t-select>
              </t-form-item>
            </t-col>
            <t-col :span="6">
              <t-form-item label="职务">
                <t-input v-model="form2.position"></t-input>
              </t-form-item>
            </t-col>
          </t-row>
          <div class="empty"></div>
          <t-row>
            <t-col :span="6">
              <t-form-item label="头像">
                <!-- picType="avatar" -->
                <UploadImage picType="app" type="app" @change="onChangeFiles" :limit="1"></UploadImage>
              </t-form-item>
            </t-col>
          </t-row>
        </t-form>
      </t-tab-panel>
    </t-tabs>
  </Dialog>

  <!-- 编辑用户 -->
  <Dialog width="50%" @confirm="onSubmitEdit" @close="onCloseEdit" :visible="visibleEdit">
    <template #title>编辑用户</template>
    <t-form :data="formEdit">
      <t-row>
        <t-col :span="6">
          <t-form-item label="昵称">
            <t-input v-model="formEdit.nickname"></t-input>
          </t-form-item>
        </t-col>
        <t-col :span="6">
          <t-form-item label="联系方式">
            <t-input v-model="formEdit.phoneNumber"></t-input>
          </t-form-item>
        </t-col>
      </t-row>
      <div class="empty"></div>
      <t-row>
        <t-col :span="12">
          <t-form-item label="角色">
            <t-select multiple clearable filterable v-model="formEdit.roleIds">
              <t-option :label="item.roleName" :value="item.roleId" v-for="(item, index) in roleList"
                        :key="index"></t-option>
            </t-select>
          </t-form-item>
        </t-col>
      </t-row>
      <div class="empty"></div>
      <t-row>
        <t-col :span="12">
          <t-form-item label="部门">
            <t-cascader :keys="{ label: 'departmentName', value: 'departmentId', children: 'subs' }"
                        v-model="formEdit.departmentIds" :options="departmentList" checkStrictly clearable filterable
                        multiple
                        :popup-props="{ overlayClassName: 'tdesign-demo-select__overlay-option' }">
              <template #option="{ item }">
                <div class="tdesign-demo__user-option" style="width: 100%;">
                  <div style="display:flex;">
                    <t-checkbox :checked="formEdit.departmentIds.includes(item.departmentId)"
                                @change="onChangeDepartmentFormEdit($event, item.departmentId)" label=""></t-checkbox>
                    <span class="sl1" style="display: inline-block;width: 116px;">{{ item.departmentName }}</span>
                    <span v-if="formEdit.mainDepartmentId != item.departmentId"
                          @click="onSetMainDepartmentFormEdit(item.departmentId)"
                          style="color:#0052d9;font-weight: 600;">设为主部门</span>
                    <span v-else style="cursor: default;color:#333">主部门</span>
                  </div>
                </div>
              </template>
            </t-cascader>
          </t-form-item>
        </t-col>
      </t-row>
      <div class="empty"></div>
      <t-row>
        <t-col :span="6">
          <t-form-item label="用户标签">
            <t-select :min-collapsed-num="2" clearable filterable multiple v-model="formEdit.tagIds">
              <t-option :label="item.tagName" :value="item.tagId" v-for="(item, index) in tagList"
                        :key="index"></t-option>
            </t-select>
          </t-form-item>
        </t-col>
        <t-col :span="6">
          <t-form-item label="职位">
            <t-input v-model="formEdit.position"></t-input>
          </t-form-item>
        </t-col>
      </t-row>
    </t-form>
  </Dialog>

  <!-- 查看账号 -->
  <Dialog @close="onCloseAccount" :confirmBtn="null" :cancelBtn="null" :visible="visibleAccount">
    <template #title>账号详情</template>
    <t-row>
      <t-col :span="12">
        <t-space size="small">
          <span class="labelWidth">账号头像：</span>
          <t-image v-if="accountData?.accountAvatarUrl" fit="cover" style="width:100px;height:100px"
                   :src="accountData?.accountAvatarUrl"/>
        </t-space>
      </t-col>
      <div class="empty"></div>
      <t-col :span="12">
        <span class="labelWidth">账号ID：</span>
        <span>{{ accountData.accountId }}</span>
      </t-col>
      <div class="empty"></div>
      <t-col :span="12">
        <span class="labelWidth">账号昵称：</span>
        <span>{{ accountData.accountNickname }}</span>
      </t-col>
      <div class="empty"></div>
      <t-col :span="12">
        <span class="labelWidth">账号用户名：</span>
        <span>{{ accountData.accountUsername }}</span>
      </t-col>

      <div class="empty"></div>
      <t-col :span="12">
        <span class="labelWidth">账号手机号：</span>
        <span>{{ accountData.accountPhoneNumber }}</span>
      </t-col>
      <div class="empty"></div>
      <t-col :span="12">
        <span class="labelWidth">账号邮箱：</span>
        <span>{{ accountData.accountEmail }}</span>
      </t-col>
    </t-row>
  </Dialog>

  <!-- 用户信息 userDetail -->
  <UserInfo @close="onCloseUserDetail" :userId="userDetail.userId" :nickname="userDetail.nickname"
            :accountAvatarUrl="userDetail.accountAvatarUrl" :joinTime="userDetail.joinTime"
            ref="userInfoRef"></UserInfo>

  <!-- 部门详情 -->
  <Dialog width="30%" @close="onCloseDepartmentDetail" :visible="visibleDepartmentDetail" :confirmBtn="null"
          :cancelBtn="null">
    <template #title>部门详情</template>
    <div>
      <p style="padding:5px 0;" v-for="(item, index) in departmentDetail?.departments" :key="index">
        <span style="color:#506c88;font-weight: 700;" v-for="(name, i) in item.departmentNames" :key="i">{{ name }} {{
            i
            < item.departmentNames.length - 1 ? '/' : ''
          }}&nbsp;</span>
        <span>{{
            item.departmentIds[item.departmentIds.length - 1] == departmentDetail.mainDepartmentId ? '(主部门)' :
                ''
          }}</span>
      </p>
    </div>
  </Dialog>


  <!-- 转移到其他账号 -->
  <Dialog :visible="visibleTransfer" @confirm="onSubmitTransfer" @close="onCloseTransfer" confirmBtn="提交">
    <template #title>业务转移</template>
    <t-base-table row-key="index" :data="userListTransfer" :columns="columnsTransfer"></t-base-table>
    <div class="empty"></div>
    <p style="line-height: 40px;">搜索需绑定账号</p>
    <t-input-adornment :prepend="protocolSelect">
      <t-input v-model="keywordTransfer" @blur="onSearchAccount" @enter="onSearchAccount" placeholder="请输入内容"/>
    </t-input-adornment>
    <div class="empty"></div>
    <div style="margin:auto;display: flex;justify-content: center;" v-if="Object.keys(accountTransferData).length">
      <div style="width:130px;height:130px;position: relative;">
        <t-image style="width:130px;height:130px" :src="accountTransferData.avatarUrl" fit="cover"/>
      </div>
      <div style="padding-left:10px;box-sizing: border-box;">
        <p style="text-align: center;line-height: 40px;">账号ID：{{ accountTransferData.accountId }}</p>
        <p style="text-align: center;line-height: 40px;">昵称：{{ accountTransferData.nickname }}</p>
        <p style="text-align: center;line-height: 40px;">加入时间：{{ accountTransferData.joinTime }}</p>
      </div>
    </div>
  </Dialog>
</template>

<style lang="scss" scoped>
.appUser__wrapper {
}

.labelWidth {
  display: inline-block;
  min-width: 100px;
  text-align: right;
}
</style>
