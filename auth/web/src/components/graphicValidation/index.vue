<!--图形验证 -->
<script setup>
import { onMounted, watch } from 'vue';
import { MessagePlugin } from 'tdesign-vue-next';

import useState from '@/hooks/useState';

import {
  getCaptchaCode_api,
  verifyCaptchaCode_api,
} from '@/api';

const props = defineProps({
  show: {
    type: Boolean,
    default: false
  }
});

const emit = defineEmits(['close', 'confirm'])

const [visible, setVisible] = useState(props.show);

// 验证码有效期 5 分钟,提前 1 分钟视为过期,给用户留出输入时间
const CAPTCHA_FRESH_MS = 4 * 60 * 1000;
let fetchedAt = 0;
let fetching = false;

// 获取图形验证码
const [captchaImageUrl, setCaptchaImageUrl] = useState('');
const [captchaKey, setCaptchaKey] = useState('');
const [captchaType, setCaptchaType] = useState('');
const [expireTime, setExpireTime] = useState('');
const getCodeImage = async () => {
  if (fetching) return; // 上一次请求还在途,不重复获取
  fetching = true;
  let params = {
    width: 160,
    height: 40
  }
  try {
    let res = await getCaptchaCode_api(params);
    if (res.code == 'Success') {
      setCaptchaImageUrl(res?.data?.captchaImageUrl);
      setCaptchaKey(res?.data?.captchaKey);
      setCaptchaType(res?.data?.captchaType);
      setExpireTime(res?.data?.expireTime);
      fetchedAt = Date.now();
      // 预热图片,展示时直接命中浏览器缓存
      new Image().src = res?.data?.captchaImageUrl;
    }
  } finally {
    fetching = false;
  }
}

// 挂载即预取验证码:后端生成+上传临时文件+浏览器下载都在弹窗打开前完成
onMounted(() => {
  getCodeImage();
});

watch(() => props.show, (val) => {
  setVisible(val)
  if (val) {
    // 打开时验证码缺失或临近过期才重新获取,正常情况下立即可见
    if (!captchaKey.value || Date.now() - fetchedAt > CAPTCHA_FRESH_MS) {
      getCodeImage();
    }
  } else {
    // 关闭后预取下一张,再次打开无需等待
    getCodeImage();
  }
});

const onClose = () => {
  setCode('');
  emit('close')
}


//
const [code, setCode] = useState(null);
const [captchaToken, setCaptchaToken] = useState(null);

// 验证
const onSubmit = async () => {
  if (!code.value) {
    MessagePlugin.error('请输入验证码')
    return;
  }
  let params = {
    captchaKey: captchaKey.value,
    captchaCode: code.value,
  }
  let res = await verifyCaptchaCode_api(params);
  if (res.code == 'Success') {
    setCaptchaToken(res?.data?.captchaToken);
    emit('confirm', captchaToken.value)
  }
}
</script>


<template>
  <t-dialog :close-on-overlay-click="false" width="30%" :zIndex="1000" attach="body" @confirm="onSubmit" @close="onClose" :visible="visible">
    <template #header>图形验证</template>
    <div class="codeBox">
      <div class="left">
        <t-input placeholder="请输入验证码" v-model="code" style="height: 50px;" size="large"></t-input>
      </div>
      <div class="right">
        <t-image @click="getCodeImage" :src="captchaImageUrl" fit="cover"
          :style="{ width: '160px', height: '40px', cursor: 'pointer' }" error="验证码加载失败" />
      </div>
    </div>
  </t-dialog>
</template>

<style lang="scss" scoped>
.codeBox {
  width: 100%;
  height: 41px;
  display: flex;
  justify-content: space-between;

  .left {
    width: calc(100% - 180px);
  }

  .right {
    width: 161px;
    height: 40px;
    border: 1px solid #ededed;
  }
}
</style>
