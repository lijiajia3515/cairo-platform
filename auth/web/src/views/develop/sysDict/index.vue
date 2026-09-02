<script setup lang="jsx">
import { ref, onMounted, watch } from 'vue';
import { MessagePlugin, DialogPlugin, LoadingPlugin } from 'tdesign-vue-next';

import useState from '@/hooks/useState';

import List from '@/components/list';
import FilterBar from '@/components/filterBar';
import FilterItem from '@/components/filterBar/item.vue';
import Dialog from '@/components/dialog';
import AccountInfo from '@/components/accountInfo';
import UploadImage from '@/components/uploadImage';
import Child from './components/child.vue';
import { timeColumn, avatarCopyColumn, opColumn } from '@/utils/tableColumns';
import { hasPermission } from '@/plugins/permission';

import {
  getAppList_api,
  getSysDictPageList_api,
  createSysDict_api,
  modifySysDictInfo_api,
  deleteSysDict_api,
  syncSysDict_api,
  copySysDictByAppId_api,
  copySysDictByDictId_api
} from '@/api';

onMounted(() => {
  getAppList();
});

const pageType = ref('parent'); // child

const [id, setId] = useState(null); // 父级
const [name, setName] = useState(null); // 父级


const keyword = ref(null);
const page = ref(1);
const size = ref(10);
const total = ref(0);
const [list, setList] = useState([]);
const [loading, setLoading] = useState(false);
const [configs, setConfigs] = useState({
  data: list,
  columns: [
    { colKey: 'dictId', title: '字典ID' },
    avatarCopyColumn({ colKey: 'dictName', title: '字典名称', iconKey: 'icon' }),
    {
      colKey: 'dictType', title: '字典类型', width: 110, cell: (h, { row }) => {
        let key = row['dictType'];
        return key == 'system' ? '系统' : (key == 'biz_template' ? '业务模板' : '');
      }
    },
    {
      colKey: 'enabled', title: '启用状态', width: 100, cell: (h, { row }) => {
        return row['enabled'] == true ? '启用' : (row['enabled'] == false ? '禁用' : '')
      }
    },
    {
      colKey: 'metadata.updateAccount.nickname', title: '更新账号', width: 160, cell: (h, { row }) => {
        return (
          <t-space size="small">
            {
              row?.metadata?.updateAccount?.avatarUrl ? <t-avatar imageProps={{ lazy: true }} class="pick" onClick={() => onWatchUserInfo(row)} hideOnLoadFailed={true} alt={row?.metadata?.updateAccount?.nickname?.slice(0, 2)} size="medium" image={row?.metadata?.updateAccount?.avatarUrl} /> : (
                row?.metadata?.updateAccount?.nickname ? <t-avatar imageProps={{ lazy: true }} class="pick" onClick={() => onWatchUserInfo(row)} size="medium" >{row?.metadata?.updateAccount?.nickname?.slice(0, 2)}</t-avatar> : null
              )
            }
            <div class="pick" onClick={() => onWatchUserInfo(row)} style={{ height: '100%', display: 'flex', alignItems: 'center' }}>{row?.metadata?.updateAccount?.nickname || null}</div>
          </t-space>
        )
      }
    },
    timeColumn('metadata.updateTime', '更新时间'),
    opColumn([
      { content: '子项', onClick: (row) => onWatchChild(row), visible: () => hasPermission('sys_dict.find') },
      { content: '同步', onClick: (row) => onSync(row), visible: () => hasPermission('sys_dict.sync') },
      { content: '编辑', onClick: (row) => onEdit(row), visible: () => hasPermission('sys_dict.modify') },
      { content: '拷贝', onClick: (row) => onCopy(row), visible: () => hasPermission('sys_dict.copy_by_dict') },
      { content: '删除', theme: 'danger', onClick: (row) => onDelete(row), visible: () => hasPermission('sys_dict.delete') },
    ], { width: 200 })
  ],
  loading: loading,
  rowKey: 'dictId',
  pagination: {
    current: page,
    pageSize: size,
    total: total,
  },
  onPageChange: (pageInfo) => {
    page.value = pageInfo.current;
    size.value = pageInfo.pageSize;
    getSysDictPage();
  }
});
/**
 **************************************************** 用户详情
 */
let accountInfoRef = ref(null);
const [accountDetail, setAccountDetail] = useState({});
const onWatchUserInfo = (row) => {
  accountInfoRef.value.open();
  setAccountDetail(row.metadata.updateAccount)
}

const onCloseAccountInfo = () => {
  setAccountDetail({});
}

/**
 ********************************************************* 分页列表
 */
const getSysDictPage = async () => {
  setLoading(true);
  try {
    let params = {
      page: page.value - 1,
      size: size.value,
      keyword: keyword.value
    };
    let header = {
      'app-id': appId.value
    }
    let res = await getSysDictPageList_api(params, header);
    if (res.code == 'Success') {
      setList(res?.data?.contents || []);
      total.value = Number(res?.data?.total || 0);
    }
  } finally {
    setLoading(false);
  }
}

const onSearch = () => {
  page.value = 1;
  getSysDictPage();
}

const onReset = () => {
  page.value = 1;
  keyword.value = null;
  getSysDictPage();
}

const goHome = () => {
  pageType.value = 'parent';
}

/**
 * 查看子项
 * @param {Object} row
 * @param {String} row.dictId 父级Id
 */
const onWatchChild = (row) => {
  setId(row.dictId);
  setName(row.dictName);

  pageType.value = 'child';
  // router.push({ path: '/develop/sys_dict/' + row.dictId, query: { appId: appId.value } })
}

/**
 * 同步
 * @param {Object} row
 * @param {String} row.dictId
 */
const onSync = (row) => {
  const confirmDia = DialogPlugin({
    header: '同步系统级字典',
    body: '是否继续同步?',
    confirmBtn: '确定',
    cancelBtn: '取消',
    onConfirm: async ({ e }) => {
      let headers = {
        'app-id': appId.value
      };
      let params = {
        dictId: row.dictId
      };
      let res = await syncSysDict_api(params, headers);
      if (res.code == 'Success') {
        MessagePlugin.success('同步成功');
        confirmDia.hide();
      }
    },
    onClose: ({ e, trigger }) => {
      confirmDia.hide();
    },
  });
}

/**
 * 创建
 */
const rules = {
  dictId: [
    { required: true, message: '字典ID必填', type: 'error', trigger: 'change' },
  ],
  dictName: [
    { required: true, message: '字典名称必填', type: 'error', trigger: 'change' },
  ],
  dictType: [
    { required: true, message: '字典类型必填', type: 'error', trigger: 'change' },
  ]
}
const formRef = ref(null);
const [visible, setVisible] = useState(false);
const [type, setType] = useState('add');
const [editFileList, setEditFileList] = useState([]); // 编辑 显示 icon
const [form, setForm] = useState({
  dictId: null,
  dictName: null,
  // Icon: null,
  enabled: true,
  dictType: null,
  isCreateItem: true
});

// 添加 修改
const onSubmit = async () => {
  const validate = await formRef.value.validate();
  if (validate == true) {
    try {
      let { dictId, dictName, enabled, dictType, isCreateItem } = form.value;
      let header = {
        'app-id': appId.value
      };
      if (type.value == 'add') {
        let params = {
          dictId, dictName, enabled,
          dictType, isCreateItem,
        };
        if (fileList.value && fileList.value.length) {
          params['icon'] = fileList.value[0].url;
        }
        let res = await createSysDict_api(params, header);
        if (res.code == 'Success') {
          MessagePlugin.success('创建成功');
          getSysDictPage();
          onClose();
        }
      } else {
        let params = {
          dictId, dictName, dictType, isCreateItem,
        };
        if (fileList.value && fileList.value.length) {
          params['icon'] = fileList.value[0].url;
        } else {
          params['icon'] = '';
        }
        let res = await modifySysDictInfo_api(params, header);
        if (res.code == 'Success') {
          MessagePlugin.success('编辑成功');
          getSysDictPage();
          onClose();
        }
      }
    } finally {
    // 收尾占位：loading 关闭等统一在调用侧处理
    }
  }
}
const onCreate = () => {
  setVisible(true);
  setType('add');
  formRef.value.clearValidate();
}
const onClose = () => {
  setVisible(false);
  setForm({
    dictId: null,
    dictName: null,
    enabled: true,
    dictType: null,
    isCreateItem: true
  });
  setEditFileList([]);
  setType('add');
}

// 上传图标
const [fileList, setFileList] = useState([]);
const onChangeFiles = (files) => {
  setFileList(files)
}

/**
 * 编辑
 * @param {Object} row
 * @param {String} row.dictId 父级Id
 * @param {String} row.dictName 字典名称
 * @param {String} row.icon 字典图标
 */
const onEdit = (row) => {
  setVisible(true);
  setType('edit');
  formRef.value.clearValidate();
  if (row.icon) {
    setEditFileList([{
      name: row.icon.split('/')[row.icon.split('/').length - 1],
      url: row.icon
    }])
  }
  setForm({
    dictId: row.dictId,
    dictName: row.dictName,
    dictType: row.dictType,
    isCreateItem: row.isCreateItem,
  });
}

/**
 * 删除
 * @param {Object} row
 * @param {String} row.dictId
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
        let header = {
          'app-id': appId.value
        };
        let params = {
          dictId: row.dictId
        };
        let res = await deleteSysDict_api(params, header);
        if (res.code == 'Success') {
          MessagePlugin.success('删除成功');
          confirmDia.hide();
          if (list.value && list.value.length == 1 && page.value > 1) {
            page.value = page.value - 1;
          }
          getSysDictPage();
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

// 应用列表
const [appId, setAppId] = useState(null);
const [appList, setAppList] = useState([]);
const getAppList = async () => {
  let params = {};
  let res = await getAppList_api(params);
  if (res.code == 'Success') {
    setAppList(res?.data || []);

    if (sessionStorage.getItem(_this.storage.systemAppId)) {
      setAppId(sessionStorage.getItem(_this.storage.systemAppId));
    } else {
      setAppId(res?.data[0]?.appId || null);
      sessionStorage.setItem(_this.storage.systemAppId, res?.data[0]?.appId || null);
    }
  }
}

const [cloneDialogVisible, setCloneDialogVisible] = useState(false)
const [cloneForm, setCloneForm] = useState({
  copyAppId: ''
})
const cloneRef = ref(null)

const onClone = () => {
  cloneRef.value.clearValidate()
  setCloneDialogVisible(true)
}

const cloneSubmit = async () => {
  const validate = await cloneRef.value.validate()
  if (validate == true) {
    let params = {
      copyAppId: cloneForm.value.copyAppId
    }
    let header = {
      'app-id': appId.value
    }
    let res = await copySysDictByAppId_api(params, header)
    if (res.code === "Success") {
      MessagePlugin.success('克隆成功');
      cloneClose()
      getSysDictPage()
    }
  }
}

const cloneClose = () => {
  setCloneDialogVisible(false)
  setCloneForm({
    copyAppId: ''
  })
}

const [copyDialogVisible, setCopyDialogVisible] = useState(false)
const [copyForm, setCopyForm] = useState({
  currentDictId: '',
  newDictId: ''
})
const copyRef = ref(null)

const onCopy = (row) => {
  copyRef.value.clearValidate()
  setCopyDialogVisible(true)
  copyForm.value.currentDictId = row.dictId
}

const copySubmit = async () => {
  const validate = await copyRef.value.validate()
  if (validate == true) {
    let params = {
      currentDictId: copyForm.value.currentDictId,
      newDictId: copyForm.value.newDictId
    }

    let header = {
      'app-id': appId.value
    }
    let res = await copySysDictByDictId_api(params, header)
    if (res.code === "Success") {
      MessagePlugin.success('拷贝成功');
      getSysDictPage()
      copyClose()
    }
  }
}

const copyClose = () => {
  setCopyDialogVisible(false)
  setCopyForm({
    copyAppId: ''
  })

}

watch(appId, () => {
  if (appId.value) {
    sessionStorage.setItem(_this.storage.systemAppId, appId.value);
  }
  getSysDictPage();
})
</script>


<template>
  <template v-if="pageType == 'parent'">
    <div v-allow="'sys_dict.find'" class="sysDict__wrapper">
      <FilterBar @search="onSearch" @reset="onReset">
        <FilterItem label="应用">
          <t-select :scroll="{ type: 'virtual' }" filterable v-model="appId" placeholder="应用">
            <t-option :label="item.appName" :value="item.appId" v-for="(item, index) in appList" :key="index">
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
        <FilterItem label="关键字">
          <t-input placeholder="请输入关键字" v-model="keyword"></t-input>
        </FilterItem>
        <template #actions>
          <t-button @click="onClone" v-allow="'sys_dict.copy_by_app'">从应用导入</t-button>
          <t-button v-allow="'sys_dict.create'" @click="onCreate">创建</t-button>
        </template>
      </FilterBar>
      <div class="empty"></div>

      <List @page-change="configs.onPageChange" :configs="configs" />
    </div>
  </template>
  <template v-if="pageType == 'child'">
    <Child @home="goHome" :id="id" :name="name" :appId="appId" />
  </template>

  <!-- 添加 / 编辑 -->
  <Dialog @confirm="onSubmit" @close="onClose" :visible="visible">
    <template #title>{{ type == 'add' ? '添加' : '编辑' }}</template>

    <t-form ref="formRef" :rules="rules" :data="form" label-width="120px">
      <t-row>
        <t-col :span="11">
          <t-form-item name="dictId" label="字典ID">
            <t-input :disabled="type == 'edit'" v-model="form.dictId"></t-input>
          </t-form-item>
        </t-col>
        <div class="empty"></div>
        <t-col :span="11">
          <t-form-item name="dictName" label="字典名称">
            <t-input v-model="form.dictName"></t-input>
          </t-form-item>
        </t-col>
        <!-- <div class="empty"></div> -->
        <!-- <t-col :span="11">
          <t-form-item label="字典图标">
            <t-select v-model="form.icon" :scroll="{ type: 'virtual' }" placeholder="请选择图标">
              <t-option :label="item" :value="item" v-for="(item, index) in icons" :key="index">
                <div class="iconBox">
                  <i :class="['iconfont', item]"></i>
                </div>
              </t-option>
              <template #valueDisplay="{ value, onClose }">
                <i :class="['iconfont', value]"></i>
              </template>
            </t-select>
          </t-form-item>
        </t-col> -->
        <template v-if="type == 'add'">
          <div class="empty"></div>
          <t-col :span="11">
            <t-form-item label="启用状态">
              <t-radio-group :allowUncheck="true" v-model="form.enabled">
                <t-radio :value="true">启用</t-radio>
                <t-radio :value="false">禁用</t-radio>
              </t-radio-group>
            </t-form-item>
          </t-col>
        </template>
        <div class="empty"></div>
        <t-col :span="11">
          <t-form-item name="dictType" label="字典类型">
            <t-select clearable v-model="form.dictType">
              <t-option label="系统" value="system"></t-option>
              <t-option label="业务模板" value="biz_template"></t-option>
            </t-select>
          </t-form-item>
        </t-col>
        <div class="empty"></div>
        <t-col :span="11">
          <t-form-item label="是否允许添加子级">
            <t-radio-group :allowUncheck="true" v-model="form.isCreateItem">
              <t-radio :value="true">允许</t-radio>
              <t-radio :value="false">禁止</t-radio>
            </t-radio-group>
          </t-form-item>
        </t-col>
        <div class="empty"></div>
        <t-col :span="11">
          <t-form-item label="图标">
            <UploadImage key="parent" :appId="appId" :dictId="form.dictId" type="public"
              picType="sys-dict" @change="onChangeFiles" :limit="1" :fileList="editFileList"></UploadImage>
          </t-form-item>
        </t-col>
      </t-row>
    </t-form>
  </Dialog>


  <!-- 克隆 -->
  <Dialog @confirm="cloneSubmit" @close="cloneClose" :visible="cloneDialogVisible">
    <template #title>从应用导入</template>
    <t-form :data="cloneForm" ref="cloneRef">
      <t-form-item name="copyAppId" :rules="[{
    required: true, message: '应用不能为空', trigger: 'change'
  }]" label="应用">
        <t-select :scroll="{ type: 'virtual' }" filterable v-model="cloneForm.copyAppId" placeholder="应用">
          <t-option :label="item.appName" :value="item.appId" v-for="(item, index) in appList" :key="index">
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
    </t-form>
  </Dialog>


  <!-- 拷贝 -->
  <Dialog @confirm="copySubmit" @close="copyClose" :visible="copyDialogVisible">
    <template #title>拷贝</template>
    <t-form :data="copyForm" ref="copyRef">
      <t-form-item name="newDictId" :rules="[{
    required: true, message: '新拷贝字典不能为空', trigger: 'change'
  }]" label="新拷贝字典">
        <t-input v-model="copyForm.newDictId"></t-input>
      </t-form-item>
    </t-form>
  </Dialog>
  <!-- 用户信息 userDetail -->
  <AccountInfo :data="accountDetail" ref="accountInfoRef" @close="onCloseAccountInfo"></AccountInfo>
</template>

<style lang="scss" scoped>
.sysDict__wrapper {}
</style>
