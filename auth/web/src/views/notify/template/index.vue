<!-- 短信模板 -->
<script setup lang="jsx">
import { ref, onMounted, watch, } from 'vue';
import { MessagePlugin, LoadingPlugin, DialogPlugin } from 'tdesign-vue-next';

import useState from '@/hooks/useState';
import UploadImage from '@/components/uploadImage';
import List from '@/components/list';
import FilterBar from '@/components/filterBar';
import FilterItem from '@/components/filterBar/item.vue';
import Dialog from '@/components/dialog';
import { avatarCopyColumn, opColumn, switchColumn } from '@/utils/tableColumns';
import { hasPermission } from '@/plugins/permission';

import {
  getAppList_api,
  getNotifyTemplatePageList_api,
  createNotifyTemplate_api,
  modifyNotifyTemplateInfo_api,
  getNotifyTemplateDetailInfo_api,
  deleteNotifyTemplate_api,
  modifyNotifyTemplateStatus_api,
  getNotifyCategoryList_api
} from '@/api';


onMounted(() => {
  getAppList();
  // getCategoryIdList()
});

const search = ref({
  keyword: '',
  enabled: null,
  categoryIds: [],
  messageTypes: [],
  linkTypes: []
})
const [appId, setAppId] = useState('')
const [editFileList, setEditFileList] = useState([]);
const messageType = ref({
  "0": "提醒消息",
  "1": "内容消息",
  "2": "模板消息",
})
let page = ref(1);
let size = ref(10);
let total = ref(0);
let list = ref([]);
const [loading, setLoading] = useState(false);
const [configs, setConfigs] = useState({
  data: list,
  columns: [
    { colKey: 'templateId', title: '业务ID' },
    { colKey: 'templateName', title: '模板名称' },
    // {colKey: 'templateType', title: '模板类型'},
    avatarCopyColumn({ colKey: 'categoryName', title: '分类名称', iconKey: 'categoryIcon' }),
    { colKey: 'messageCode', title: '消息编码' },
    avatarCopyColumn({ colKey: 'messageTitle', title: '消息标题', iconKey: 'messageIcon' }),
    {
      colKey: 'messageType', title: '消息类型', width: 100,
      cell: (h, { row }) => {
        return (
          <span>{messageType.value[row?.messageType]}</span>
        )
      }
    },
    switchColumn({
      api: (params) => modifyNotifyTemplateStatus_api(params, { 'app-id': appId.value }),
      idKeys: ['templateId'],
      label: '通知模板',
      perm: 'notify_template.modify_status',
      refresh: () => getTableList(),
    }),
    opColumn([
      { content: '编辑', onClick: (row) => onEdit(row), visible: () => hasPermission('notify_template.modify_info') },
      { content: '详情', onClick: (row) => onDetail(row), visible: () => hasPermission('notify_template.read') },
      { content: '删除', theme: 'danger', onClick: (row) => onDelete(row), visible: () => hasPermission('notify_template.delete') },
    ], { width: 200 })
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
    getTableList();
  }
});
/**
 * 获取短信模板列表
 */
const getTableList = async () => {
  setLoading(true);
  try {
    let params = {
      page: page.value - 1,
      size: size.value,
      ...search.value
    };
    let headers = {
      'app-id': appId.value
    };
    let res = await getNotifyTemplatePageList_api(params, headers);
    if (res.code == 'Success') {
      list.value = res?.data?.contents || [];
      total.value = Number(res?.data?.total) || 0;
    }
  } finally {
    setLoading(false);
  }
}

watch(() => appId.value, () => {
  getTableList();
  getCategoryIdList()
})

const onSearch = () => {
  page.value = 1
  getTableList()
}
const onReset = () => {
  search.value.keyword = ''
  search.value.enabled = null
  search.value.categoryIds = []
  search.value.messageTypes = []
  search.value.linkTypes = []
  getTableList()
}

// 应用列表
const [appList, setAppList] = useState([]);
const getAppList = async () => {
  let params = {};
  let res = await getAppList_api(params);
  if (res.code == 'Success') {
    setAppList(res?.data || []);
    if (appList.value.length > 0) {
      setAppId(appList.value[0].appId);
    }
  }
}

const [categoryIdList, setCategoryIdList] = useState([])
const getCategoryIdList = async () => {
  let headers = {
    'app-id': appId.value
  };
  let res = await getNotifyCategoryList_api({ Enabled: true }, headers)
  if (res.code === "Success") {
    setCategoryIdList(res.data || [])
  }
}


// 创建模板消息
const rules = {
  categoryId: [
    { required: true, message: '请选择', type: 'error', trigger: 'change' },
  ],
  templateName: [
    { required: true, message: '请输入', type: 'error', trigger: 'blur' },
  ],
  messageCode: [
    { required: true, message: '请输入', type: 'error', trigger: 'blur' },
  ],
  linkType: [
    { required: true, message: '请选择', type: 'error', trigger: 'change' },
  ],
  messageTitle: [
    { required: true, message: '请输入', type: 'error', trigger: 'blur' },
  ],
  messageType: [
    { required: true, message: '请选择', type: 'error', trigger: 'change' },
  ],
  messageAlert: [
    { required: true, message: '请输入', type: 'error', trigger: 'blur' },
  ]
}
let formRef = ref(null);
const [type, setType] = useState('add');
const [visible, setVisible] = useState(false);
const [form, setForm] = useState({
  templateId: null,
  categoryId: null, // 分类ID
  categoryName: null,
  templateName: null,
  messageCode: null,
  messageIcon: null,
  messageTitle: null,
  messageType: null,
  messageAlert: null,
  messageContent: null,
  linkType: null,
  pageUrl: null,
  linkUrl: null,
})
const args = ref(
  [{ argsCode: null, argsName: null, dataType: null, defaultValue: null }]
)
const onCreate = () => {
  setVisible(true);
  formRef.value.clearValidate();
}
const onConfirm = async () => {
  let validate = await formRef.value.validate();
  if (validate == true) {
    LoadingPlugin(true);
    try {
      let params = {
        ...form.value,
      };
      delete params['categoryName']
      if (fileList.value && fileList.value.length) {
        params['messageIcon'] = fileList.value[0].url;
      }
      let arr = [];
      if (args.value.length) {
        args.value.forEach(item => {
          if (item.argsCode && item.argsName && item.dataType && item.defaultValue) {
            arr.push(item);
          }
        })
      }
      switch (form.value.messageType) {
        case '0':
          params['alertArgs'] = arr;
          break;
        case '1':
          params['contentArgs'] = arr;
          break;
        case '2':
          params['templateArgs'] = arr;
          break;
        default:
          break;
      }
      let headers = {
        'app-id': appId.value
      };
      if (type.value == 'add') {
        let res = await createNotifyTemplate_api(params, headers);
        if (res.code == 'Success') {
          MessagePlugin.success("添加成功");
          onClose();
          getTableList();
        }
      } else {
        let res = await modifyNotifyTemplateInfo_api(params, headers);
        if (res.code == 'Success') {
          MessagePlugin.success("编辑成功");
          onClose();
          getTableList();
        }
      }
    } finally {
      LoadingPlugin(false);
    }
  }
}
const onClose = () => {
  setVisible(false);
  setType('add');
  setForm({
    templateId: null,
    categoryId: null, // 分类ID
    categoryName: null,
    templateName: null,
    messageCode: null,
    messageIcon: null,
    messageTitle: null,
    messageType: null,
    messageAlert: null,
    messageContent: null,
    linkType: null,
    pageUrl: null,
    linkUrl: null,
  });
  setEditFileList([]);
  args.value = [{ argsCode: null, argsName: null, dataType: null, defaultValue: null }]
}

// 添加参数列表
const onAddArgs = () => {
  args.value.push({
    argsCode: null,
    argsName: null,
    dataType: null,
    defaultValue: null
  })
}
// 减少参数列表
const onReduceArgs = (index) => {
  args.value.splice(index, 1);
}

// 上传图标
const [fileList, setFileList] = useState([]);
const onChangeFiles = (files) => {
  setFileList(files)
}

const getDetailInfo = async (info) => {
  let params = {
    templateId: info?.templateId
  };
  let headers = {
    'app-id': appId.value
  };
  let res = await getNotifyTemplateDetailInfo_api(params, headers);
  if (res.code == 'Success') {
    const data = res?.data;
    setForm({
      templateId: data?.templateId,
      categoryId: data?.categoryId, // 分类ID
      categoryName: data?.categoryName,
      templateName: data?.templateName,
      messageCode: data?.messageCode,
      messageIcon: data?.messageIcon,
      messageTitle: data?.messageTitle,
      messageType: data?.messageType,
      messageAlert: data?.messageAlert,
      messageContent: data?.messageContent,
      linkType: data?.linkType,
      pageUrl: data?.pageUrl,
      linkUrl: data?.linkUrl,
    });

    if (data.messageIcon) {
      setEditFileList([{
        name: data.messageIcon.split('/')[data.messageIcon.split('/').length - 1],
        url: data.messageIcon
      }])
    }
    args.value = data?.alertArgs || data?.contentArgs || data?.templateArgs || [{ argsCode: null, argsName: null, dataType: null, defaultValue: null }]
    console.log(args, 'args====')
  }
}
/**
 * 编辑
 * @param {Object} row
 * @param {String} row.bizId 业务ID
 */
const onEdit = async (row) => {
  setVisible(true);
  setType('edit');
  // 获取详情
  getDetailInfo(row)
}

// 详情
const [detailVisible, setDetailVisible] = useState(false);
const onDetail = (row) => {
  setDetailVisible(true)
  getDetailInfo(row)
}

// 删除
const onDelete = (row) => {
  const confirmDia = DialogPlugin({
    header: '提示',
    body: '是否继续删除?',
    confirmBtn: '确定',
    cancelBtn: '取消',
    onConfirm: async ({ e }) => {
      LoadingPlugin(true);
      try {
        let params = {
          templateId: row?.templateId
        };
        let headers = {
          'app-id': appId.value
        };
        let res = await deleteNotifyTemplate_api(params, headers);
        if (res.code == 'Success') {
          confirmDia.hide();
          MessagePlugin.success('删除成功');
          if (list.value && list.value.length == 1 && page.value > 1) {
            page.value = page.value - 1;
          }
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


// 拖拽排序
let dragIndex = 0
const dragstart = (e, index) => {
  e.stopPropagation();
  dragIndex = index
  setTimeout(() => {
    e.target.classList.add('moveing')
  }, 0)
}
const dragenter = (e, index) => {
  e.preventDefault();
  // 拖拽到原位置时不触发
  if (dragIndex !== index) {
    let args = form.value.args
    const source = args[dragIndex];

    args.splice(dragIndex, 1);
    args.splice(index, 0, source);
    setForm({
      ...form.value,
      args
    })
    // 更新节点位置
    dragIndex = index
  }
}
const dragover = (e) => {
  e.preventDefault()
  e.dataTransfer.dropEffect = 'move'
}
const dragend = (e) => {
  e.target.classList.remove('moveing')
}
const goClick = (url) => {
  window.open(url)
}
</script>


<template>
  <div v-allow="'notify_template.read'" class="template__wrapper">
    <FilterBar @search="onSearch" @reset="onReset">
      <FilterItem label="关键字">
        <t-input placeholder="关键字" v-model="search.keyword"></t-input>
      </FilterItem>
      <FilterItem label="应用">
        <t-select :scroll="{ type: 'virtual' }" filterable clearable v-model="appId" placeholder="应用">
          <t-option v-for="(item, index) in appList" :key="index" :value="item.appId" :label="item.appName">
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
      <FilterItem label="状态">
        <t-select placeholder="状态" v-model="search.enabled">
          <t-option label="启用" :value="true" />
          <t-option label="禁用" :value="false" />
        </t-select>
      </FilterItem>
      <FilterItem label="消息分类">
        <t-select clearable placeholder="消息分类" v-model="search.categoryIds" multiple>
          <t-option :label="item.categoryName" :value="item.categoryId" v-for="(item, index) in categoryIdList"
            :key="index"></t-option>
        </t-select>
      </FilterItem>
      <FilterItem label="消息类型">
        <t-select clearable placeholder="消息类型" v-model="search.messageTypes" multiple>
          <t-option label="提醒消息" value="0" />
          <t-option label="内容消息" value="1" />
          <t-option label="模板消息" value="2" />
        </t-select>
      </FilterItem>
      <FilterItem label="跳转方式">
        <t-select clearable placeholder="跳转方式" v-model="search.linkTypes" multiple>
          <t-option label="不跳转" value="0" />
          <t-option label="页面地址" value="1" />
          <t-option label="内部链接地址" value="2" />
          <t-option label="外部链接地址" value="3" />
        </t-select>
      </FilterItem>
      <template #actions>
        <t-button @click="onCreate" v-allow="'notify_template.create'">创建</t-button>
      </template>
    </FilterBar>
    <div class="empty"></div>
    <List @page-change="configs.onPageChange" :configs="configs" />
  </div>

  <Dialog @confirm="onConfirm" @close="onClose" width="65%" :visible="visible" top="20">
    <template #title> {{ type == 'add' ? '添加模板消息' : '编辑模板消息' }}</template>
    <t-form :rules="rules" ref="formRef" :data="form">
      <t-row>
        <t-col :span="6">
          <t-form-item name="categoryId" label="分类ID">
            <t-select clearable v-model="form.categoryId">
              <t-option :label="item.categoryName" :value="item.categoryId" v-for="(item, index) in categoryIdList"
                :key="index"></t-option>
            </t-select>
          </t-form-item>
        </t-col>
        <t-col :span="6">
          <t-form-item name="templateName" label="模板名称">
            <t-input v-model="form.templateName"></t-input>
          </t-form-item>
        </t-col>
      </t-row>
      <div class="empty"></div>
      <t-row>
        <t-col :span="6">
          <t-form-item name="messageCode" label="消息编码">
            <t-input v-model="form.messageCode"></t-input>
          </t-form-item>
        </t-col>
        <t-col :span="6">
          <t-form-item name="messageTitle" label="消息标题">
            <t-input v-model="form.messageTitle"></t-input>
          </t-form-item>
        </t-col>
      </t-row>
      <div class="empty"></div>
      <t-row>
        <t-col :span="6">
          <t-form-item name="messageType" label="消息类型">
            <t-select v-model="form.messageType">
              <t-option label="提醒消息" value="0" />
              <t-option label="内容消息" value="1" />
              <t-option label="模板消息" value="2" />
            </t-select>
          </t-form-item>
        </t-col>
      </t-row>

      <t-row v-if="form.messageType == '0'">
        <div class="empty"></div>
        <t-col :span="12">
          <t-form-item name="messageAlert" label="消息提醒">
            <t-textarea v-model="form.messageAlert" placeholder="" name="description"
              :autosize="{ minRows: 3, maxRows: 5 }" />
          </t-form-item>
        </t-col>
      </t-row>
      <div class="empty"></div>
      <t-row>
        <t-col :span="12" v-if="form.messageType == '1'">
          <t-form-item name="templateText" label="模板内容">
            <t-textarea v-model="form.templateText" placeholder="" name="description"
              :autosize="{ minRows: 3, maxRows: 5 }" />
          </t-form-item>
        </t-col>
        <t-col :span="12">
          <t-form-item label="参数列表">
            <div classs="paramsBox" style="width: 100%;">
              <div style="display: flex;width: 100%;marginBottom:10px;" v-for="(item, index) in args" :key="index"
                draggable="true" @dragstart="dragstart($event, index)" @dragenter="dragenter($event, index)"
                @dragend="dragend" @dragover="dragover">
                <t-form-item labelAlign="top" label="&nbsp;">
                  <i style="fontSize:24px;cursor: move;" class="iconfont icon-tuodong"></i>
                </t-form-item>
                <t-form-item style="width: calc((100%/4) - 10px);" labelAlign="top" label="参数编码:">
                  <t-input style="width: 100%;" v-model="args[index].argsCode" placeholder="请输入参数编码"></t-input>
                </t-form-item>
                <t-form-item style="width: calc((100%/4) - 10px);" labelAlign="top" label="参数名称:">
                  <t-input style="width: 100%;" v-model="args[index].argsName" placeholder="请输入参数名称"></t-input>
                </t-form-item>
                <t-form-item style="width: calc((100% /4) - 10px);" labelAlign="top" label="参数类型:">
                  <t-input style="width: 100%;" v-model="args[index].dataType" placeholder="请输入参数类型"></t-input>
                </t-form-item>
                <t-form-item style="width: calc((100% /4) - 10px);" labelAlign="top" label="默认值:">
                  <t-input style="width: 100%;" v-model="args[index].defaultValue" placeholder="请输入默认值"></t-input>
                </t-form-item>
                <t-form-item labelAlign="top" label="&nbsp;">
                  <t-button v-if="index == 0" @click="onAddArgs" style="width: 60px;">+</t-button>
                  <t-button v-else @click="onReduceArgs(index)" style="width: 60px;">-</t-button>
                </t-form-item>
              </div>
            </div>
          </t-form-item>
        </t-col>
      </t-row>
      <div class="empty"></div>
      <t-row>
        <t-col :span="6">
          <t-form-item name="linkType" label="跳转方式">
            <t-select v-model="form.linkType">
              <t-option label="不跳转" value="0" />
              <t-option label="页面地址" value="1" />
              <t-option label="内部链接地址" value="2" />
              <t-option label="外部链接地址" value="3" />
            </t-select>
          </t-form-item>
        </t-col>
      </t-row>
      <div class="empty"></div>
      <t-row>
        <t-col :span="6" v-if="form.linkType == '1'">
          <t-form-item name="pageUrl" label="页面地址">
            <t-input v-model="form.pageUrl"></t-input>
          </t-form-item>
        </t-col>
        <t-col :span="6" v-if="form.linkType == '2' || form.linkType == '3'">
          <t-form-item name="linkUrl" label="链接地址">
            <t-input v-model="form.linkUrl"></t-input>
          </t-form-item>
        </t-col>
      </t-row>
      <div class="empty"></div>
      <t-row>
        <t-col :span="6">
          <t-form-item name="messageIcon" label="消息图标">
            <UploadImage :appId="appId" type="public" picType="notify-template-icon" @change="onChangeFiles"
              :limit="1" :fileList="editFileList">
            </UploadImage>
          </t-form-item>
        </t-col>
      </t-row>
    </t-form>
  </Dialog>
  <!-- 详情 -->
  <t-dialog v-model:visible="detailVisible" :header="form.templateName" width="700" :confirmBtn="null">
    <t-card>
      <div class="remindBox">
        <div>
          <t-avatar :image="form.messageIcon" size="60px" shape="round" />
        </div>
        <div style="margin-left:10px;">
          <p style="font-size: 16px;font-weight:600;">{{ form.messageTitle }}</p>
          <p style="font-size: 14px;font-weight:600;margin-top:5px;">{{ form.messageAlert }}</p>
        </div>
      </div>
    </t-card>
    <div class="empty"></div>
    <t-card v-if="form.messageType == '1'">
      <div>
        <div style="margin:15px 0;display:flex;align-items: center;">
          <div>
            <t-avatar :image="form.messageIcon" size="40px" />
          </div>
          <p style="font-size: 16px;font-weight:600;margin-left:10px;">{{ form.categoryName }}</p>
        </div>
        <div style="margin-left:10px;">
          <p style="font-size: 16px;font-weight:600;">{{ form.messageTitle }}</p>
          <p style="font-size: 14px;margin-top:5px;">{{ form.messageContent }}</p>
        </div>
        <div class="empty"></div>
        <hr style="border:1px solid #eee">
        <div style="display:flex;align-items: center;justify-content: space-between" v-if="form.linkUrl">
          <span style="font-size: 16px;">查看详情</span>
          <t-icon name="chevron-right-s" size="32px" style="color:darkgrey" @click="goClick(form.linkUrl)" />
        </div>
      </div>
    </t-card>
    <div class="empty"></div>
    <t-card v-if="form.messageType == '2'">
      <div>
        <div style="margin:15px 0;display:flex;align-items: center;">
          <div>
            <t-avatar :image="form.messageIcon" size="40px" />
          </div>
          <p style="font-size: 16px;font-weight:600;margin-left:10px;">{{ form.categoryName }}</p>
        </div>
        <hr style="border:1px solid #eee">
        <h3>{{ form.messageTitle }}</h3>
        <p style="font-size: 14px;line-height: 25px;">00:55</p>
        <div v-for="(item, index) in args" :key="index">
          <p style="font-size: 14px;line-height: 25px;">{{ item.argsName }} &nbsp; {{ item.defaultValue
            }}</p>
        </div>

      </div>
    </t-card>


  </t-dialog>
</template>

<style lang="scss" scoped>
.template__wrapper {}

.remindBox {
  display: flex;
  align-items: center;
}
</style>
