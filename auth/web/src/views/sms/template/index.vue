<!-- 短信模板 -->
<script setup lang="jsx">
defineOptions({ name: 'manage-sms-template' })

import { ref, onMounted, watch, } from 'vue';
import { MessagePlugin, LoadingPlugin, DialogPlugin } from 'tdesign-vue-next';

import useState from '@/hooks/useState';

import List from '@/components/list';
import FilterBar from '@/components/filterBar';
import FilterItem from '@/components/filterBar/item.vue';
import Dialog from '@/components/dialog';
import { ellipsisColumn, opColumn, switchColumn } from '@/utils/tableColumns';
import { hasPermission } from '@/plugins/permission';

import {
  getAppList_api,
  getSmsTemplatePageList_api,
  createSmsTemplate_api,
  modifySmsTemplateInfo_api,
  deleteSmsTemplate_api,
  modifySmsTemplateStatus_api,
  getSmsTemplateDetailInfo_api
} from '@/api';

onMounted(() => {
  getAppList();
});

let enabled = ref(null);
let page = ref(1);
let size = ref(10);
let total = ref(0);
let list = ref([]);
const [loading, setLoading] = useState(false);
const [configs, setConfigs] = useState({
  data: list,
  columns: [
    // 不设应用 ID 列:列表本身按应用过滤,应用信息去应用管理页查
    { colKey: 'bizId', title: '业务ID' },
    { colKey: 'templateCode', title: '模板编码' },
    { colKey: 'templateName', title: '模板名称' },
    { colKey: 'templateType', title: '模板类型' },
    { colKey: 'templateSign', title: '模板签名' },
    ellipsisColumn('templateText', '模板内容', { width: 240 }),
    switchColumn({
      api: (params) => modifySmsTemplateStatus_api(params, { 'app-id': headerData.value.appId }),
      idKeys: ['bizId'],
      label: '短信模板',
      perm: 'sms_template.modify_status',
      refresh: () => getSmsTemplatePage(),
    }),
    opColumn([
      { content: '编辑', onClick: (row) => onEdit(row), visible: () => hasPermission('sms_template.modify') },
      { content: '删除', theme: 'danger', onClick: (row) => onDelete(row), visible: () => hasPermission('sms_template.delete') },
    ])
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
    getSmsTemplatePage();
  }
});


/**
 * 获取短信模板列表
 */
const getSmsTemplatePage = async () => {
  setLoading(true);
  try {
    let params = {
      page: page.value - 1,
      size: size.value
    };
    if (enabled.value != null) {
      params['enabled'] = enabled.value;
    }
    let headers = {
      'app-id': headerData.value.appId
    };
    let res = await getSmsTemplatePageList_api(params, headers);
    if (res.code == 'Success') {
      list.value = res?.data?.contents || [];
      total.value = Number(res?.data?.total) || 0;
    }
  } finally {
    setLoading(false);
  }
}

watch(enabled, () => {
  getSmsTemplatePage();
})

const onSearch = () => {
  page.value = 1;
  getSmsTemplatePage();
}
const onReset = () => {
  page.value = 1;
  enabled.value = null;
  getSmsTemplatePage();
}


const [headerData, setHeaderData] = useState({
  appId: null,
});
// 应用列表
const [appList, setAppList] = useState([]);
const getAppList = async () => {
  let params = {};
  let res = await getAppList_api(params);
  if (res.code == 'Success') {
    setAppList(res?.data || []);
    if (appList.value.length > 0) {
      setHeaderData({
        ...headerData.value,
        appId: appList.value[0].appId
      });
    }
  }
}
watch(() => headerData.value.appId, () => {
  if (headerData.value.appId) {
    getSmsTemplatePage();
  }
})



// 创建模板消息
const rules = {
  templateText: [
    { required: true, message: '模板内容必填', type: 'error', trigger: 'blur' },
  ],
  templateType: [
    { required: true, message: '模板类型必填', type: 'error', trigger: 'blur' },
  ],
  templateCode: [
    { required: true, message: '模板编码必填', type: 'error', trigger: 'blur' },
  ],
  bizId: [
    { required: true, message: '业务ID必填', type: 'error', trigger: 'blur' },
  ],
  templateSign: [
    { required: true, message: '模板签名必填', type: 'error', trigger: 'blur' },
  ],
  templateName: [
    { required: true, message: '模板名称必填', type: 'error', trigger: 'blur' },
  ]
}
let formRef = ref(null);
const [type, setType] = useState('add');
const [visible, setVisible] = useState(false);
const [form, setForm] = useState({
  bizId: null, // 业务ID
  templateName: null, // 模板签名
  templateSign: null, // 模板名称
  templateCode: null, // 模板编码
  templateType: null, // 模板类型
  templateText: null, // 模板内容
  args: [{ argCode: null, argName: null, argType: null, templateArgCode: null }], // 模板参数编码
})
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
      let arr = [];
      if (form.value.args.length) {
        form.value.args.forEach(item => {
          if (item.argCode && item.argName && item.argType && item.templateArgCode) {
            arr.push(item);
          }
        })
      }
      params['args'] = arr;
      let headers = {
        'app-id': headerData.value.appId
      };
      if (type.value == 'add') {
        let res = await createSmsTemplate_api(params, headers);
        if (res.code == 'Success') {
          MessagePlugin.success("添加成功");
          onClose();
          getSmsTemplatePage();
        }
      } else {
        let res = await modifySmsTemplateInfo_api(params, headers);
        if (res.code == 'Success') {
          MessagePlugin.success("编辑成功");
          onClose();
          getSmsTemplatePage();
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
    bizId: null, // 业务ID
    templateName: null, // 模板签名
    templateSign: null, // 模板名称
    templateCode: null, // 模板编码
    templateType: null, // 模板类型
    templateText: null, // 模板内容
    args: [{ argCode: null, argName: null, argType: null, templateArgCode: null }], // 模板参数编码
  });
}
// 添加参数列表
const onAddArgs = () => {
  let args = form.value.args;
  args.push({
    argCode: null,
    argName: null,
    argType: null,
    templateArgCode: null
  })
  setForm({
    ...form.value,
    args
  })
}
// 减少参数列表
const onReduceArgs = (index) => {
  let args = form.value.args;
  args.splice(index, 1);
  setForm({
    ...form.value,
    args
  })
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
  let params = {
    bizId: row?.bizId
  };
  let headers = {
    'app-id': headerData.value.appId
  };
  let res = await getSmsTemplateDetailInfo_api(params, headers);
  if (res.code == 'Success') {
    const data = res?.data;
    setForm({
      bizId: data?.bizId, // 业务ID
      templateName: data?.templateName, // 模板签名
      templateSign: data?.templateSign, // 模板名称
      templateCode: data?.templateCode, // 模板编码
      templateType: data?.templateType, // 模板类型
      templateText: data?.templateText, // 模板内容
      args: data?.args ? data.args:[{ argCode: null, argName: null, argType: null, templateArgCode: null }], // 模板参数编码
    });
    console.log(res)
  }
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
          bizId: row?.bizId

        };
        let headers = {
          'app-id': headerData.value.appId
        };
        let res = await deleteSmsTemplate_api(params, headers);
        if (res.code == 'Success') {
          confirmDia.hide();
          MessagePlugin.success('删除成功');
          if (list.value && list.value.length == 1 && page.value > 1) {
            page.value = page.value - 1;
          }
          getSmsTemplatePage();
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
</script>


<template>
  <div v-allow="'sms_template.read'" class="template__wrapper">
    <FilterBar @search="onSearch" @reset="onReset">
      <FilterItem label="应用">
        <t-select :scroll="{ type: 'virtual' }" filterable clearable v-model="headerData.appId" placeholder="应用">
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
        <t-select v-model="enabled" clearable>
          <t-option label="启用" :value="true"></t-option>
          <t-option label="禁用" :value="false"></t-option>
        </t-select>
      </FilterItem>
      <template #actions>
        <t-button v-allow="'sms_template.create'" @click="onCreate">添加</t-button>
      </template>
    </FilterBar>
    <div class="empty"></div>
    <List @page-change="configs.onPageChange" :configs="configs" />
  </div>


  <Dialog @confirm="onConfirm" @close="onClose" width="65%" :visible="visible" top="20">
    <template #title> {{ type == 'add' ? '添加模板消息' : '编辑模板消息' }} </template>
    <t-form :rules="rules" ref="formRef" :data="form">
      <t-row>
        <t-col :span="12">
          <t-form-item name="bizId" label="业务ID">
            <t-input v-model="form.bizId"></t-input>
          </t-form-item>
        </t-col>
      </t-row>
      <div class="empty"></div>
      <t-row>
        <t-col :span="6">
          <t-form-item name="templateName" label="模板名称">
            <t-input v-model="form.templateName"></t-input>
          </t-form-item>
        </t-col>
        <t-col :span="6">
          <t-form-item name="templateSign" label="模板签名">
            <t-input v-model="form.templateSign"></t-input>
          </t-form-item>
        </t-col>
      </t-row>
      <div class="empty"></div>
      <t-row>
        <t-col :span="6">
          <t-form-item name="templateCode" label="模板编码">
            <t-input v-model="form.templateCode"></t-input>
          </t-form-item>
        </t-col>
        <t-col :span="6">
          <t-form-item name="templateType" label="模板类型">
            <t-input v-model="form.templateType"></t-input>
          </t-form-item>
        </t-col>
      </t-row>
      <div class="empty"></div>
      <t-row>
        <t-col :span="12">
          <t-form-item name="templateText" label="模板内容">
            <t-textarea v-model="form.templateText" placeholder="" name="description"
              :autosize="{ minRows: 3, maxRows: 5 }" />
          </t-form-item>
        </t-col>
        <div class="empty"></div>
        <t-col :span="12">
          <t-form-item label="参数列表">
            <div classs="paramsBox" style="width: 100%;">
              <div style="display: flex;width: 100%;marginBottom:10px;" v-for="(item, index) in form.args" :key="index"
                draggable="true" @dragstart="dragstart($event, index)" @dragenter="dragenter($event, index)"
                @dragend="dragend" @dragover="dragover">
                <t-form-item labelAlign="top" label="&nbsp;">
                  <i style="fontSize:24px;cursor: move;" class="iconfont icon-tuodong"></i>
                </t-form-item>
                <t-form-item style="width: calc(((100% - 60px)/4) - 10px);" labelAlign="top" label="参数编码:">
                  <t-input style="width: 100%;" v-model="form.args[index].argCode" placeholder="请输入参数编码"></t-input>
                </t-form-item>
                <t-form-item style="width: calc(((100% - 60px)/4) - 10px);" labelAlign="top" label="参数名称:">
                  <t-input style="width: 100%;" v-model="form.args[index].argName" placeholder="请输入参数名称"></t-input>
                </t-form-item>
                <t-form-item style="width: calc(((100% - 60px)/4) - 10px);" labelAlign="top" label="参数类型:">
                  <t-input style="width: 100%;" v-model="form.args[index].argType" placeholder="请输入参数类型"></t-input>
                </t-form-item>
                <t-form-item style="width: calc(((100% - 60px)/4) - 10px);" labelAlign="top" label="模板参数编码:">
                  <t-input style="width: 100%;" v-model="form.args[index].templateArgCode"
                    placeholder="请输入模板参数编码"></t-input>
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
    </t-form>
  </Dialog>
</template>

<style lang="scss" scoped>
.template__wrapper {}
</style>
