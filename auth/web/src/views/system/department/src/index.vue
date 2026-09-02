<!-- 部门 -->
<script setup>
import {
  onMounted
} from 'vue';
import {
  LoadingPlugin,
  MessagePlugin,
  DialogPlugin,
} from 'tdesign-vue-next';
import {
  cloneDeep,
} from 'lodash';

import useState from '@/hooks/useState';
import useCopy from '@/hooks/useCopy';

import {
  getAppDepartmentTree_api,

  createAppDepartment_api,
  modifyAppDepartment_api,
  deleteAppDepartment_api,
} from '@/api';

onMounted(() => {
  getList();
})
const [config, setConfig] = useState({
  value: 'departmentId',
  label: 'departmentName',
  children: 'subs'
});

const [loading, setLoading] = useState(false);
const [list, setList] = useState([]);
const [objData, setObjData] = useState({});
const getList = async () => {
  setLoading(true);
  try {
    let res = await getAppDepartmentTree_api({});
    if (res.code == 'Success' && res.data) {
      setList([res?.data]);
      let arr = filterData(cloneDeep([res.data]));
      if (arr.length == 1) {
        let obj = {
          id: 0,
          label: arr[0].departmentName,
          expand: true,
          children: [],
          noDragging: true
        };
        setObjData(obj)
      }
      if (arr.length > 1) {
        let obj = {
          id: 0,
          label: '组织架构',
          expand: true,
          children: arr,
        };
        setObjData(obj)
      }
      if (arr.length == 0 || !arr.length) {
        setObjData({})
      }
    } else {
      setList([]);
      setObjData({})
    }
  } finally {
    setLoading(false);
  }
}
let filterData = (list) => {
  if (list.length) {
    list.forEach((item) => {
      item.id = item.departmentId;
      item.label = item.departmentName;
      item.expand = false;
      item.noDragging = true;
      delete item.id;
      delete item.name;
      if (item.subs) {
        item.children = item.subs;
        delete item.subs;
        filterData(item.children);
      }
    });
  }
  return list;
}


const [type, setType] = useState('add');
const [addShow, setAddShow] = useState(false);
const [form, setForm] = useState({
  parentId: '0',
  id: '',
  name: ''
});

const onAddFunc = async () => {
  LoadingPlugin(true);
  try {
    let { parentId, name, id } = form.value;
    let params = {
      parentId, departmentName: name
    };
    if (type.value == 'add') {
      let res = await createAppDepartment_api(params);
      if (res.code == 'Success') {
        MessagePlugin.success('添加成功');
        onUserCancel();
        getList();
      }
    } else {
      params.departmentId = id;
      let res = await modifyAppDepartment_api(params);
      if (res.code == 'Success') {
        MessagePlugin.success('编辑成功');
        setType('add');
        onUserCancel();
        getList();
      }
    }
  } finally {
    LoadingPlugin(false);
  }
}

const onAddParent = () => {
  setAddShow(true);
}

let onUserCancel = () => {
  setAddShow(false);
  setForm({
    ...form.value,
    name: '',
    parentId: '0',
  })
}




// 添加
let add = (node) => {
  setAddShow(true);
  setType('add');
  setForm({
    ...form.value,
    parentId: node.value
  });
}
// 编辑
let edit = (node) => {
  console.log(node)
  setAddShow(true);
  setType('edit');
  setForm({
    parentId: node.data ? node.data.parentId : '',
    id: node.data ? node.data.departmentId : '',
    name: node.label,
  });
}

// 删除
let remove = (node) => {
  let departmentId = node.data.departmentId;
  const confirmDia = DialogPlugin({
    header: '部门删除',
    body: '你确定删除吗?',
    confirmBtn: '确定',
    cancelBtn: '取消',
    onConfirm: async ({ e }) => {
      LoadingPlugin(true);
      try {
        let res = await deleteAppDepartment_api({ departmentId });
        if (res.code == 'Success') {
          confirmDia.hide();
          MessagePlugin.success('删除成功');
          getList();
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


const onNodeCopy = (data) => {
  // label
  if (data.label) {
    useCopy(data.label);
  }
}
</script>

<template>
  <div v-allow="'department.find'" class="department__wrapper">
    <t-loading size="small" :loading="loading">
      <div class="main">
        <div class="treeBox">
          <!-- <t-button v-allow="'department.create'" @click="onAddParent">创建</t-button> -->
          <div class="empty"></div>
          <t-tree :keys="config" :data="list" activable hover transition :line="true">
            <template #operations="{ node }">
              <t-space>
                <t-button v-allow="'department.create'" variant="text" size="small" theme="primary" @click="add(node)">
                  添加</t-button>
                <t-button v-allow="'department.modify'" variant="text" size="small" theme="primary" @click="edit(node)">
                  编辑
                </t-button>
                <t-button v-allow="'department.delete'" variant="text" size="small" theme="danger" @click="remove(node)">
                  删除
                </t-button>
              </t-space>
            </template>
          </t-tree>
        </div>

        <div class="treeDraw">
          <!-- https://sangtian152.github.io/vue3-tree-org/ -->
          <vue3-tree-org :disabled="true" :data="objData" center :horizontal="true" :collapsable="true" :label-style="{
    background: 'var(--td-bg-color-container)',
    color: '#5e6d82',
  }" :node-copy="onNodeCopy" />
        </div>
      </div>
    </t-loading>

    <t-dialog attach="body" @close="onUserCancel" :on-confirm="onAddFunc" :visible="addShow">
      <template #header>
        {{ type == 'add' ? '添加部门' : '编辑部门' }}
      </template>
      <t-form>
        <t-row>
          <t-col>
            <t-form-item label="部门名称">
              <t-input size="large" v-model="form.name"></t-input>
            </t-form-item>
          </t-col>
        </t-row>
      </t-form>
    </t-dialog>
  </div>
</template>

<style lang="scss" scoped>
.department__wrapper {
  width: 100%;

  .main {
    display: flex;
    min-height: calc(75vh);

    .treeBox {
      width: 28%;
      background-color: var(--td-bg-color-container);
      box-sizing: border-box;
      padding: 10px;
    }

    .treeDraw {
      width: 71%;
      margin-left: 1%;
    }
  }
}

.zm-tree-org {
  background: var(--td-bg-color-container);
}
</style>
