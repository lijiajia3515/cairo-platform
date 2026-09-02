<script setup>
import { ref, watch, reactive, onMounted, } from 'vue';

import {
  MessagePlugin
} from 'tdesign-vue-next';

import UploadImage from '@/components/uploadImage';

import {
  modifyMenu_api
} from '@/api';

const emit = defineEmits(['close', 'finish'])

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  menuId: {
    type: String,
  },
  icon: {
    type: String,
  },
  menuName: {
    type: String,
  },
  component: {
    type: String,
  },
  path: {
    type: String,
  },
  tags: {
    type: Array,
  },
  hiddenMenu: {
    type: Boolean,
  },
  appId: {
    type: String,
  },
  endpointId: {
    type: String,
  },
  subappId: {
    type: String,
  },
  subappVersion: {
    type: String,
  }
});

onMounted(() => {
  form.menuId = props.menuId;
  if (props.icon) {
    form.icon = props.icon;
    editFileList.value = [{ name: props.icon, url: props.icon }]
  }
  form.menuName = props.menuName;
  form.component = props.component;
  form.path = props.path;
  form.tags = props.tags;
  form.hiddenMenu = props.hiddenMenu;
})

watch(() => props.visible, () => {
  visible.value = props.visible;
})

let visible = ref(props.visible);

let form = reactive({
  menuId: null,
  menuName: null,
  path: null,
  tags: [],
  hiddenMenu: null
});


let fileList = ref([]);
let editFileList = ref([]);
const onChangeFiles = (files) => {
  fileList.value = files;
}

// 提交 编辑 菜单
const onSubmit = async () => {
  let params = {
      menuId: form.menuId,
      menuName: form.menuName,
      component: form.component,
      path: form.path,
      tags: form.tags,
      hiddenMenu: form.hiddenMenu,
    };
    let headers = {
      'app-id': props.appId,
      'endpoint-id': props.endpointId,
      'subapp-id': props.subappId,
      'subapp-version': props.subappVersion,
    }
    if (fileList.value && fileList.value.length) {
      params['icon'] = fileList.value[0].url;
    } else {
      params['icon'] = '';
    }
    let res = await modifyMenu_api(params, headers);
    if (res.code == 'Success') {
      MessagePlugin.success('编辑成功');
      close();
      emit('finish');
    }
}


const close = () => {
  form.menuId = null;
  form.menuName = null;
  form.component = null;
  form.path = null;
  form.tags = [];
  form.hiddenMenu = null;
  fileList.value = [];
  editFileList.value = [];
  emit('close');
}


defineExpose({
  close,
})
</script>

<template>
  <t-dialog attach="body" @confirm="onSubmit" @close="close" :visible="visible">
    <template #header>编辑子菜单</template>
    <t-form>
      <t-row>
        <t-col :span="12">
          <t-form-item label="菜单名称">
            <t-input v-model="form.menuName"></t-input>
          </t-form-item>
        </t-col>
        <div class="empty"></div>
        <t-col :span="12">
          <t-form-item label="组件地址">
            <t-input v-model="form.component"></t-input>
          </t-form-item>
        </t-col>
        <div class="empty"></div><t-col :span="12">
          <t-form-item label="外部地址">
            <t-input v-model="form.path"></t-input>
          </t-form-item>
        </t-col>
        <div class="empty"></div>
        <t-col :span="12">
          <t-form-item label="tags">
            <t-select v-model="form.tags" multiple :max="2">
              <t-option label="新功能" value="new" />
              <t-option label="热门" value="hot" />
              <t-option label="旧版本" value="old" />
              <t-option label="废弃功能" value="deprecated" />
            </t-select>
          </t-form-item>
        </t-col>
        <div class="empty"></div>
        <t-col :span="12">
          <t-form-item label="是否显示">
            <t-radio-group v-model="form.hiddenMenu">
              <t-radio :value="false">显示</t-radio>
              <t-radio :value="true">隐藏</t-radio>
            </t-radio-group>
          </t-form-item>
        </t-col>
        <div class="empty"></div>
        <t-col :span="12">
          <t-form-item label="菜单图标">
            <UploadImage :appId="appId" type="public" picType="menu" @change="onChangeFiles" :limit="1"
              :fileList="editFileList"></UploadImage>
          </t-form-item>
        </t-col>

      </t-row>
    </t-form>
  </t-dialog>
</template>
