<script setup>
// icon 库三合一选择器:内置库选(icon-park 多彩系)/填 URL/自定义上传,值统一为图标 URL 字符串。
// 菜单/权限等 icon 字段直接吃 URL(侧栏 img :src 同构),不强绑存储来源
import { ref, computed, watch } from 'vue';

import { ICON_PRESET_GROUPS, iconUrl } from './preset';
import UploadImage from '@/components/uploadImage';

const props = defineProps({
  visible: { type: Boolean, default: false },
  value: { type: String, default: '' },
  // 上传通道透传(临时文件上传按 appId 归档)
  appId: { type: String, default: '' },
  picType: { type: String, default: 'menu' },
});
const emit = defineEmits(['update:visible', 'update:value', 'select']);

const innerVisible = computed({
  get: () => props.visible,
  set: (v) => emit('update:visible', v),
});

const tab = ref('library');
const urlInput = ref('');
const uploadedUrl = ref('');

// 打开时以现值初始化各 tab
watch(innerVisible, (v) => {
  if (!v) return;
  urlInput.value = props.value || '';
  uploadedUrl.value = '';
  tab.value = 'library';
});

const confirmUrl = (url) => {
  emit('update:value', url);
  emit('select', url);
  innerVisible.value = false;
};

const onUploadChange = (files) => {
  const first = (files || [])[0];
  uploadedUrl.value = first?.url || '';
};

const groups = ICON_PRESET_GROUPS;
</script>

<template>
  <t-dialog v-model:visible="innerVisible" header="选择图标" width="560px" :footer="false">
    <t-tabs v-model="tab">
      <t-tab-panel value="library" label="图标库">
        <div class="icon-library">
          <div v-for="g in groups" :key="g.group" class="icon-group">
            <div class="group-title">{{ g.group }}</div>
            <div class="group-grid">
              <div v-for="name in g.names" :key="name" class="icon-cell" :title="name"
                :class="{ picked: value === iconUrl(name) }" @click="confirmUrl(iconUrl(name))">
                <img :src="iconUrl(name)" :alt="name" loading="lazy">
              </div>
            </div>
          </div>
        </div>
      </t-tab-panel>
      <t-tab-panel value="url" label="填入地址">
        <div class="url-pane">
          <t-input v-model="urlInput" placeholder="https://... 或图标名(按 icon-park 解析)" clearable />
          <t-button theme="primary" :disabled="!urlInput" @click="confirmUrl(urlInput.includes('://') ? urlInput : iconUrl(urlInput.trim()))">确定</t-button>
          <img v-if="urlInput" class="url-preview" :src="urlInput.includes('://') ? urlInput : iconUrl(urlInput.trim())" alt="">
        </div>
      </t-tab-panel>
      <t-tab-panel value="upload" label="自定义上传">
        <div class="upload-pane">
          <UploadImage :appId="appId" type="public" :picType="picType" :limit="1" @change="onUploadChange" />
          <t-button theme="primary" :disabled="!uploadedUrl" @click="confirmUrl(uploadedUrl)">使用上传的图标</t-button>
        </div>
      </t-tab-panel>
    </t-tabs>
  </t-dialog>
</template>

<style lang="scss" scoped>
.icon-library {
  max-height: 420px;
  overflow-y: auto;
  padding: 4px 2px;
}

.icon-group {
  margin-bottom: 10px;

  .group-title {
    font-size: 12px;
    color: var(--td-text-color-secondary);
    margin: 6px 0;
  }

  .group-grid {
    display: grid;
    grid-template-columns: repeat(8, 1fr);
    gap: 6px;
  }
}

.icon-cell {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 46px;
  border-radius: var(--td-radius-default);
  cursor: pointer;
  border: 1px solid transparent;

  &:hover {
    background: var(--td-bg-color-container-hover);
  }

  &.picked {
    border-color: var(--td-brand-color);
    background: var(--td-brand-color-light);
  }

  img {
    width: 22px;
    height: 22px;
    object-fit: contain;
  }
}

.url-pane {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 0;

  .url-preview {
    width: 24px;
    height: 24px;
    object-fit: contain;
    flex: none;
  }
}

.upload-pane {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 8px 0;
}
</style>
