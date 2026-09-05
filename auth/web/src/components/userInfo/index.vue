<!-- 人员卡片弹窗:账号/用户详情共用。圆形头像+身份区+信息网格,字段有值才渲染 -->
<script setup>
import { ref, computed, watch } from 'vue';

import useCopy from '@/hooks/useCopy';

const emit = defineEmits(['close']);

const props = defineProps({
  userId: { type: String, default: '' },
  nickname: { type: String, default: '' },
  accountAvatarUrl: { type: String, default: '' },
  joinTime: { type: String, default: '' },
  // 整体对象传入(富字段优先):API 响应的 updateUser/updateAccount 富化结构
  user: { type: Object, default: null },
});

const src = computed(() => props.user || {});
const pick = (...keys) => keys.map((k) => src.value[k]).find((v) => v != null && v !== '') || '';

const name = computed(() => props.nickname || pick('nickname', 'accountNickname') || props.userId || '-');
const avatar = computed(() => props.accountAvatarUrl || pick('accountAvatarUrl', 'avatarUrl') || '');
const id = computed(() => props.userId || pick('userId', 'accountId', 'updateUserId') || '');
const initial = computed(() => String(name.value).slice(0, 1).toUpperCase());

// 信息网格:label/取值键/格式化;有值才出条目
const fields = computed(() => {
  const rows = [
    { label: '用户名', value: pick('accountUsername', 'username') },
    { label: '手机号', value: pick('accountPhoneNumber', 'phoneNumber') },
    { label: '邮箱', value: pick('accountEmail', 'email') },
    { label: '职位', value: pick('position') },
    { label: '加入时间', value: props.joinTime || pick('joinTime') },
  ];
  // 全量展示:空值占位 —,不隐藏字段(用户裁决:不知道有哪些字段比空值更糟)
  return rows;
});

const visible = ref(false);
const open = () => { visible.value = true; };
const close = () => { emit('close'); visible.value = false; };
const copyId = () => id.value && useCopy(String(id.value));

watch(() => props.user, () => {}, {});

defineExpose({ open });
</script>

<template>
  <t-dialog attach="body" :cancelBtn="null" :confirmBtn="null" :close-on-overlay-click="true" width="420px"
    @close="close" :visible="visible">
    <template #header>人员信息</template>

    <div class="person-card">
      <!-- 身份区:圆形头像 + 昵称 + ID 复制 -->
      <div class="person-head">
        <img v-if="avatar" class="person-avatar" :src="avatar" alt="" @error="$event.target.style.visibility = 'hidden'">
        <div v-else class="person-avatar person-avatar--fallback">{{ initial }}</div>
        <div class="person-id">
          <div class="person-name">{{ name }}</div>
          <span v-if="id" class="person-uid sl1" :title="id" @click="copyId">
            <span class="person-uid-label">用户ID</span>{{ id }}
            <i class="iconfont icon-fuzhi person-copy" title="复制用户ID" @click.stop="copyId"></i>
          </span>
        </div>
      </div>

      <!-- 信息网格 -->
      <div class="person-grid">
        <template v-for="f in fields" :key="f.label">
          <span class="person-label">{{ f.label }}</span>
          <span class="person-value sl1" :title="f.value ? String(f.value) : ''">{{ f.value || '—' }}</span>
        </template>
      </div>
    </div>
  </t-dialog>
</template>

<style lang="scss" scoped>
.person-card {
  padding: 4px 0 8px;
}

.person-head {
  display: flex;
  align-items: center;
  gap: 14px;
  padding-bottom: 14px;
  border-bottom: 1px solid var(--td-component-stroke);
}

.person-avatar {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  object-fit: cover;
  flex: none;
  background: var(--td-bg-color-component);

  &--fallback {
    display: flex;
    align-items: center;
    justify-content: center;
    background: var(--td-brand-color-light);
    color: var(--td-brand-color);
    font-size: 22px;
    font-weight: 600;
  }
}

.person-id {
  min-width: 0;

  .person-name {
    font-size: 16px;
    font-weight: 600;
    color: var(--td-text-color-primary);
    line-height: 24px;
  }

  .person-uid {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    max-width: 100%;
    font-size: 12px;
    color: var(--td-text-color-placeholder);
    cursor: pointer;

    .person-uid-label {
      flex: none;
      padding: 0 4px;
      border-radius: 3px;
      background: var(--td-bg-color-component);
      color: var(--td-text-color-secondary);
      line-height: 16px;
    }

    .person-copy {
      font-size: 12px;
      color: var(--td-text-color-placeholder);
    }

    &:hover .person-copy {
      color: var(--td-brand-color);
    }
  }
}

.person-grid {
  display: grid;
  grid-template-columns: 64px 1fr;
  row-gap: 8px;
  column-gap: 12px;
  padding-top: 14px;

  .person-label {
    font-size: 12px;
    color: var(--td-text-color-secondary);
    line-height: 18px;
  }

  .person-value {
    font-size: 12px;
    color: var(--td-text-color-primary);
    line-height: 18px;
  }
}
</style>
