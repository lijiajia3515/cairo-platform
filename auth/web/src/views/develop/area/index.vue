<!-- 行政区划 -->
<template>
  <div class="area__wrapper">
    <template v-if="pageType == 'parent'">
      <div v-allow="'area.read'">
        <FilterBar @search="onSearch" @reset="onReset">
          <FilterItem label="关键字">
            <t-input placeholder="请输入关键字" v-model="keyword"></t-input>
          </FilterItem>
          <FilterItem label="状态">
            <t-select v-model="search.enabled" placeholder="状态">
              <t-option :value="true" label="启用"></t-option>
              <t-option :value="false" label="禁用"></t-option>
            </t-select>
          </FilterItem>
          <FilterItem label="热门">
            <t-select v-model="search.hot" placeholder="热门">
              <t-option :value="true" label="是"></t-option>
              <t-option :value="false" label="否"></t-option>
            </t-select>
          </FilterItem>
          <template #actions>
            <t-button @click="onCreate" v-allow="'area.create_area'">创建</t-button>
          </template>
        </FilterBar>
        <div class="empty"></div>
        <List @page-change="configs.onPageChange" :configs="configs"></List>
      </div>
    </template>


    <template v-if="pageType == 'child'">
      <Child @home="goHome" :id="id" :name="name" :shortAreaName="shortAreaName" />
    </template>

    <t-dialog :close-on-overlay-click="false" attach="body" @close="onClose" :on-confirm="onSubmit"
      v-model:visible="state.show" width='30%'>
      <template #header>{{ state.type == 'add' ? '添加' : '编辑' }}</template>
      <t-form ref="formRef" :data="formData" :rules="rules">
        <t-form-item label="区域Id" name="areaId">
          <t-input v-model="formData.areaId" :disabled="state.type == 'edit'"></t-input>
        </t-form-item>
        <t-form-item label="区域名称" name="areaName">
          <t-input v-model="formData.areaName"></t-input>
        </t-form-item>
        <t-form-item label="区域简称" name="shortAreaName">
          <t-input v-model="formData.shortAreaName"></t-input>
        </t-form-item>
        <t-form-item label="拼音" name="pinYin">
          <t-input v-model="formData.pinYin"></t-input>
        </t-form-item>
        <t-form-item label="拼音首字母" name="pinYinPrefix">
          <t-input v-model="formData.pinYinPrefix"></t-input>
        </t-form-item>
        <t-form-item label="热门" name="hot" v-if="state.type == 'add'">
          <t-radio-group v-model="formData.hot">
            <t-radio :value="true">是</t-radio>
            <t-radio :value="false">否</t-radio>
          </t-radio-group>
        </t-form-item>
        <t-form-item label="状态" name="enabled" v-if="state.type == 'add'">
          <t-radio-group v-model="formData.enabled">
            <t-radio :value="true">启用</t-radio>
            <t-radio :value="false">禁用</t-radio>
          </t-radio-group>
        </t-form-item>
        <t-form-item label="排序值" name="sort" v-if="state.type == 'edit'">
          <t-input v-model="formData.sort"></t-input>
        </t-form-item>
      </t-form>
    </t-dialog>

    <t-dialog v-model:visible="visible" width='50%' :confirmBtn='null' :cancelBtn='null'>
      <template #header>详情</template>
      <t-row :gutter="16">
        <t-col :span="4">
          <p> 省份编码：{{ detailInfo.areaId }}</p>
        </t-col>
        <t-col :span="4">
          <p> 省份名称：{{ detailInfo.areaName }}</p>
        </t-col>
        <t-col :span="4">
          <p> 省份简称：{{ detailInfo.shortAreaName }}</p>
        </t-col>
      </t-row>
      <t-row :gutter="16">
        <t-col :span="4">
          <p> 拼音：{{ detailInfo.pinYin }}</p>
        </t-col>
        <t-col :span="4">
          <p> 拼音首字母：{{ detailInfo.pinYinPrefix }}</p>
        </t-col>
        <t-col :span="4">
          <p> 热门：{{ detailInfo.hot === true ? '是' : detailInfo.hot === false ? '否' : '' }}</p>
        </t-col>
      </t-row>
      <t-row :gutter="16">
        <t-col :span="4">
          <p> 状态：{{ detailInfo.enabled === true ? '启用' : detailInfo.enabled === false ? '禁用' : '' }}</p>
        </t-col>
        <t-col :span="4">
          <p> 排序值：{{ detailInfo.sort }}</p>
        </t-col>
        <t-col :span="4">
          <p> 创建人：{{ detailInfo.metadata?.createUser.nickname ? detailInfo.metadata.createUser.nickname : '' }}</p>
        </t-col>
      </t-row>
      <t-row :gutter="16">
        <t-col :span="4">
          <p> 创建时间：{{ detailInfo.metadata?.createTime }}</p>
        </t-col>
        <t-col :span="4">
          <p> 更新人：{{ detailInfo.metadata?.updateUser.nickname ? detailInfo.metadata.updateUser.nickname : '' }}</p>
        </t-col>
        <t-col :span="4">
          <p> 更新时间：{{ detailInfo.metadata?.updateTime }}</p>
        </t-col>
      </t-row>
    </t-dialog>

    <!-- 用户信息 userDetail -->
    <UserInfo @close="onCloseUserDetail" :userId="userDetail.userId" :nickname="userDetail.nickname"
      :accountAvatarUrl="userDetail.accountAvatarUrl" :joinTime="userDetail.joinTime" ref="userInfoRef"></UserInfo>

  </div>
</template>

<script setup lang="jsx">
import Child from './components/child.vue';
import { ref, reactive, onMounted } from 'vue';
import useState from '@/hooks/useState';
import List from '@/components/list';
import FilterBar from '@/components/filterBar';
import FilterItem from '@/components/filterBar/item.vue';
import UserInfo from '@/components/userInfo';
import { timeColumn, opColumn, switchColumn } from '@/utils/tableColumns';
import { hasPermission } from '@/plugins/permission';
import {
  MessagePlugin,
  LoadingPlugin,
  DialogPlugin
} from 'tdesign-vue-next';

import {
  getAreaPageList_api,
  modifyAreaStatus_api,
  modifyAreaHot_api,
  deleteArea_api,
  moveArea_api,
  getAreaList_api,
  createArea_api,
  modifyAreaInfo_api
} from '@/api';

let keyword = ref(null);

let search = ref({
  enabled: null,
  hot: null,
})
let state = reactive({
  type: '',
  show: false
})

let areaList = ref([])

let formRef = ref(null)
let [visible, setVisible] = useState(false)
let [detailInfo, setDetailInfo] = useState(false)

let [formData, setFormData] = useState({
  parentAreaId: '0',
  areaId: '',
  areaName: '',
  shortAreaName: '',
  pinYin: '',
  pinYinPrefix: '',
  hot: false,
  enabled: false,
  sort: '',
})

const rules = {
  areaId: [
    { required: true, message: '区域Id必填' },
    {
      pattern: /^[0-9]*$/, message: '请填写数字'
    },
  ],
  areaName: [{ required: true, message: '区域名称必填' },],
  shortAreaName: [{ required: true, message: '区域简称必填' },],
  pinYin: [
    { required: true, message: '拼音必填' },
    {
      pattern: /^[A-Za-z\s]+$/, message: '必须是字母'
    },
  ],
  pinYinPrefix: [
    { required: true, message: '拼音首字母必填' },
    { pattern: /^[A-Za-z\s]+$/, message: '必须是字母' },
  ],
  hot: [{ required: true, message: '热门必填' },],
  enabled: [{ required: true, message: '状态必填' },],
  sort: [{ required: true, message: '排序值必填' },],
}

const pageType = ref('parent');

const [id, setId] = useState(null); // 父级
const [name, setName] = useState(null); // 父级
const [shortAreaName, setShortAreaName] = useState(null); // 父级

onMounted(() => {
  getTableList()
  getAreaTree()
})

const getAreaTree = async () => {
  let params = {
    parentAreaId: '0',
    enableShort: true
  }
  let res = await getAreaList_api(params)
  if (res.code === "Success") {
    areaList.value = res.data
  }
}
const onCreate = () => {
  state.show = true
  state.type = 'add'
  formRef.value.clearValidate();
}

let page = ref(1);
let size = ref(10);
let total = ref(0);
const [list, setList] = useState([]);
const [loading, setLoading] = useState(false);
const [configs, setConfigs] = useState({
  data: list,
  columns: [
    { colKey: 'areaId', title: '编码' },
    { colKey: 'areaName', title: '名称' },
    { colKey: 'shortAreaName', title: '简称' },
    { colKey: 'pinYin', title: '拼音' },
    { colKey: 'pinYinPrefix', title: '拼音首字母' },
    {
      colKey: 'depth', title: '级别', width: 90, cell: (h, { row }) => {
        return (
          <span>省份</span>
        )
      }
    },
    switchColumn({
      colKey: 'hot',
      title: '热门',
      api: modifyAreaHot_api,
      idKeys: ['areaId'],
      pairs: { true: { label: '热门', theme: 'warning' }, false: { label: '否', theme: 'default' } },
      confirmOf: (value) => value ? '设为热门' : '取消热门',
      perm: 'area.modify_area_hot',
      refresh: () => getTableList(),
    }),
    switchColumn({
      api: modifyAreaStatus_api,
      idKeys: ['areaId'],
      label: '区域',
      perm: 'area.modify_area_status',
      refresh: () => getTableList(),
    }),
    { colKey: 'sort', title: '排序值' },
    {
      colKey: 'metadata.createUser.nickname', title: '创建人', width: 160, cell: (h, { row }) => {
        return (
          <t-space size="small">
            {
              row?.metadata?.createUser?.accountAvatarUrl ?
                <t-avatar class="pick" onClick={() => onWatchUserInfo(row, 'Create')} hideOnLoadFailed={true}
                  alt={row?.metadata?.createUser?.nickname?.slice(0, 2)} size="medium"
                  image={row?.metadata?.createUser?.accountAvatarUrl} /> : (
                  row?.metadata?.createUser?.nickname ? <t-avatar class="pick" onClick={() => onWatchUserInfo(row, 'Create')}
                    size="medium">{row?.metadata?.createUser?.nickname?.slice(0, 2)}</t-avatar> : null
                )
            }
            <div class="pick" onClick={() => onWatchUserInfo(row, 'Create')} style={{
              height: '100%',
              display: 'flex',
              alignItems: 'center'
            }}>{row?.metadata?.createUser?.nickname || null}</div>
          </t-space>
        )
      }
    },
    timeColumn('metadata.createTime', '创建时间'),
    {
      colKey: 'metadata.updateUser.nickname', title: '更新人', width: 160, cell: (h, { row }) => {
        return (
          <t-space size="small">
            {
              row?.metadata?.updateUser?.accountAvatarUrl ?
                <t-avatar class="pick" onClick={() => onWatchUserInfo(row, 'Update')} hideOnLoadFailed={true}
                  alt={row?.metadata?.updateUser?.nickname?.slice(0, 2)} size="medium"
                  image={row?.metadata?.updateUser?.accountAvatarUrl} /> : (
                  row?.metadata?.updateUser?.nickname ? <t-avatar class="pick" onClick={() => onWatchUserInfo(row, 'Update')}
                    size="medium">{row?.metadata?.updateUser?.nickname?.slice(0, 2)}</t-avatar> : null
                )
            }
            <div class="pick" onClick={() => onWatchUserInfo(row, 'Update')} style={{
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
      { content: '下级', onClick: (row) => onNext(row) },
      { content: '详情', onClick: (row) => onDetail(row), visible: () => hasPermission('area.area_detail') },
      { content: '编辑', onClick: (row) => onEdit(row), visible: () => hasPermission('area.modify_area_info') },
      { content: '上移', onClick: (row, rowIndex) => onMoveUp(row, rowIndex), visible: () => hasPermission('area.move_area') },
      { content: '下移', onClick: (row, rowIndex) => onMoveDown(row, rowIndex), visible: () => hasPermission('area.move_area') },
      { content: '删除', theme: 'danger', onClick: (row) => onDelete(row), visible: () => hasPermission('area.delete_area') },
    ], { width: 200 }),
  ],
  rowKey: 'appId',
  loading: loading,
  pagination: {
    current: page,
    pageSize: size,
    total: total,
  },
  onPageChange: (pageInfo) => {
    page.value = pageInfo.current;
    size.value = pageInfo.pageSize;
    getTableList();
  }
});

const getTableList = async () => {
  setLoading(true);
  try {
    let params = {
      parentAreaId: '0',
      enabled: search.value.enabled,
      hot: search.value.hot,
      keyword: keyword.value,
      page: page.value - 1,
      size: size.value
    }
    let res = await getAreaPageList_api(params)
    if (res.code === "Success") {
      setList(res.data.contents || [])
      total.value = Number(res.data.total || 0)
    }
  } finally {
    setLoading(false);
  }
}

const onSearch = () => {
  page.value = 1
  getTableList()
}
const onReset = () => {
  keyword.value = null
  search.value = {
    enabled: null,
    hot: null,
  }
  page.value = 1
  getTableList()
}

let userInfoRef = ref(null);
const [userDetail, setUserDetail] = useState({});
const onWatchUserInfo = (row, type) => {
  userInfoRef.value.open();
  if (type === 'Create') {
    setUserDetail(row.metadata.createUser)

  } else if (type === 'Update') {
    setUserDetail(row.metadata.updateUser)
  }
}

const onCloseUserDetail = () => {
  setUserDetail({});
}

const goHome = () => {
  pageType.value = 'parent';
}
const onNext = (row) => {
  pageType.value = 'child'
  setId(row.areaId)
  setName(row.areaName);
  setShortAreaName(row.shortAreaName)
}
const onDetail = (row) => {
  setVisible(true)
  setDetailInfo(row)
}
const onEdit = (row) => {
  state.show = true
  state.type = 'edit'
  formData.value.areaId = row.areaId
  formData.value.areaName = row.areaName
  formData.value.shortAreaName = row.shortAreaName
  formData.value.pinYin = row.pinYin
  formData.value.pinYinPrefix = row.pinYinPrefix
  formData.value.sort = row.sort
}

const onSubmit = async () => {
  const validate = await formRef.value.validate();
  if (validate == true) {
    let params = {
      ...formData.value
    }
    if (state.type == 'add') {
      delete params['sort']
      let res = await createArea_api(params)
      if (res.code === "Success") {
        MessagePlugin.success('创建成功');
        onClose();
        getTableList();
      }
    } else if (state.type == 'edit') {
      delete params['hot']
      delete params['enabled']
      let res = await modifyAreaInfo_api(params)
      if (res.code === "Success") {
        MessagePlugin.success('编辑成功');
        onClose();
        getTableList();
      }
    }

  }
}
const onClose = () => {
  setFormData({
    parentAreaId: '0',
    areaId: '',
    areaName: '',
    shortAreaName: '',
    pinYin: '',
    pinYinPrefix: '',
    hot: false,
    enabled: false,
  })
  state.show = false
  state.type = ''
}
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
          areaId: row.areaId,
        }
        let res = await deleteArea_api(params);
        if (res.code == 'Success') {
          MessagePlugin.success('删除成功');
          confirmDia.hide();
          getTableList();
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

const handleMove = async (params) => {
  let res = await moveArea_api(params)
  if (res.code === "Success") {
    MessagePlugin.success('移动成功');
    getTableList()
  }
}
const onMoveUp = (row, rowIndex) => {
  if (rowIndex === 0) {
    MessagePlugin.error('无法移动');
    return
  }
  let queryParams = {
    moveAreaId1: row.areaId,
    moveAreaId2: list.value[rowIndex - 1].areaId
  }
  handleMove(queryParams)
}
const onMoveDown = (row, rowIndex) => {
  if (rowIndex === list.value.length - 1) {
    MessagePlugin.error('无法移动');
    return
  }
  let queryParams = {
    moveAreaId1: row.areaId,
    moveAreaId2: list.value[rowIndex + 1].areaId
  }
  handleMove(queryParams)
}

</script>

<style lang="scss" scoped>
.area__wrapper {
  header {
    box-sizing: border-box;
  }

  ::v-deep .t-row {
    margin-bottom: 20px;
  }
}
</style>
