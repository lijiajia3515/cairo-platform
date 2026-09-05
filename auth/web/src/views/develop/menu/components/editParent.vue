<script setup>
import { ref, watch, reactive, onMounted, } from 'vue';

import {
  MessagePlugin
} from 'tdesign-vue-next';

import IconPicker from '@/components/iconPicker/index.vue';

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
  hiddenMenu: {
    type: Boolean,
  },
  tags: {
    type: Array,
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
  component: null,
  path: null,
  tags: [],
  hiddenMenu: null
});


let iconPickerVisible = ref(false);
const clearIcon = () => { form.icon = ''; };
// 提交 编辑 父菜单
const onSubmit = async () => {
  let params = {
      menuId: form.menuId,
      menuName: form.menuName,
      component: form.component,
      path: form.path,
      tags: form.tags,
      hiddenMenu: form.hiddenMenu
    };
    let headers = {
      'app-id': props.appId,
      'endpoint-id': props.endpointId,
      'subapp-id': props.subappId,
      'subapp-version': props.subappVersion,
    }
    // 图标统一走 iconPicker 三合一(库选/URL/上传),值就是 URL 字符串
    params['icon'] = form.icon || '';
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
  emit('close');
}


defineExpose({
  close,
})
</script>

<template>
  <t-dialog @confirm="onSubmit" @close="close" :visible="visible">
    <template #header>编辑父菜单</template>
    <t-form>
      <t-row>
        <t-col :span="12">
          <t-form-item label="菜单名称">
            <t-input v-model="form.menuName"></t-input>
          </t-form-item>
        </t-col>
        <div class="empty"></div>
        <t-col :span="12">
          <t-form-item label="组件路径">
            <t-input v-model="form.component"></t-input>
          </t-form-item>
        </t-col>
        <div class="empty"></div>
        <t-col :span="12">
          <t-form-item label="外部路径">
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
          <t-form-item label="菜单可见性">
            <t-radio-group v-model="form.hiddenMenu">
              <t-radio :value="false">显示</t-radio>
              <t-radio :value="true">隐藏</t-radio>
            </t-radio-group>
          </t-form-item>
        </t-col>
        <div class="empty"></div>
        <t-col :span="12">
          <t-form-item label="菜单图标">
            <div class="icon-field">
              <img v-if="form.icon" class="icon-preview" :src="form.icon" alt="" @click="iconPickerVisible = true">
              <t-button variant="outline" size="small" @click="iconPickerVisible = true">
                {{ form.icon ? '更换' : '从图标库选择' }}
              </t-button>
              <t-button v-if="form.icon" variant="text" size="small" theme="danger" @click="clearIcon">清除</t-button>
            </div>
          </t-form-item>
        </t-col>

      </t-row>
    </t-form>
    <IconPicker v-model:visible="iconPickerVisible" v-model:value="form.icon" :appId="appId" picType="menu" />
  </t-dialog>
</template>
