<script setup>
import { ref, reactive, onMounted, onUnmounted, computed, watch, nextTick } from 'vue';
import { useRouter } from 'vue-router';
import axios from 'axios';

import useState from '@/hooks/useState';
import useSign from './useSign';
import useUpload from './useUpload';



import {
  accessPublicFile_api,
  accessAppFile_api
} from '@/api';
// https://thoughts.aliyun.com/workspaces/65016de75cd066001b40bdb4/docs/652613064c6050000199c726#3ef51e80-67e4-11ee-a5da-4d82aa7e382b-104737
const props = defineProps({
  disabled: {
    type: Boolean,
    default: false
  },
  tips: {
    type: String,
  },
  limit: {
    type: Number,
    default: 1
  },
  fileList: { // 编辑 {name url}
    type: Array,
    default: () => []
  },
  type: {
    type: String,
    default: 'app', // app 应用  public 公开 tenant 企业
  },
  picType: {
    type: String,
    default: 'app'
  },
  accountId: {
    type: String
  },
  appId: {
    type: String
  },
  endpointId: {
    type: String
  },
  appVersion: {
    type: String
  },
  dictId: {
    type: String
  },
  dictItemId: {
    type: String
  },
  menuId: {
    type: String
  },
  tenantId: {
    type: String
  }
});
// picType
// avatar => accountId 账号头像


// app-icon => appId 应用图标
// endpoint-icon => appId endpointId 终端图标
// menu => appId endpointId menuId 菜单icon



// app-release => appId endpointId appVersion 应用发行版APK下载地址

// permission-icon => randomId  功能权限icon
// sys-dict => appId dictId 系统级字典icon        sys-dict-item
// temporary => appId  yyyy-MM-dd randomId fileName 临时文件上传


onMounted(() => {

});

const emit = defineEmits(['change'])

let uploadRef = ref(null);

const [files, setFiles] = useState([]);
const [editFiles, setEditFiles] = useState();

const [disabled, setDisabled] = useState(props.disabled);

const [autoUpload, setAutoUpload] = useState(true);
const [uploadAllFilesInOneRequest, setUploadAllFilesInOneRequest] = useState(false); // 是否在同一个请求中上传全部文件

const [limit, setLimit] = useState(props.limit);

const requestMethod = computed(() => requestSuccessMethod);

watch(() => props.disabled, () => {
  setDisabled(props.disabled);
})

watch(() => props.fileList, () => { // 显示
  setFiles(props.fileList);
})
watch(files, () => {
  emit('change', files.value);
})


const requestSuccessMethod = (file) => { // 上传多个 这个函数会 依次执行
  return new Promise((resolve) => {
    // executor 内 async 逻辑抽出为独立异步函数
    (async () => {
      // 上传进度控制示例
      let percent = 0;
      const percentTimer = setInterval(() => {
        if (percent + 10 < 99) {
          percent += 10;
          uploadRef.value.uploadFilePercent({ file, percent });
        } else {
          clearInterval(percentTimer);
        }
      }, 100);
      try {
        let signRes = await useSign(props.type, props.picType, {
          appId: props.appId,
        }); // 签名
        const { bucket, endpoint, expiresTime, keyPrefix, signPostFormData } = signRes;
        let uploadRes = await useUpload(props.picType, endpoint, bucket, keyPrefix, signPostFormData, file[0]?.raw, {
          accountId: props.accountId,
          dictId: props.dictId,
          tenantId: props.tenantId,
          appId: props.appId,
          endpointId: props.endpointId,
          appVersion: props.appVersion,
        }); // 上传文件

        const { key, version } = uploadRes;
        nextTick(async () => {
          let urlRes = await getUrl(bucket, key, version, props.type);
          const { url, s3 } = urlRes;
          // + '?v=' + Date.now()
          resolve({ status: 'success', url, response: { url: (props.type == 'public' ? url : url), s3 } });
          clearInterval(percentTimer);
        })
      } catch (err) {
        console.log(err)
        resolve({ status: 'error' });
        clearInterval(percentTimer);
      }
    })();
  });
};


const requestFailMethod = (file) => { // 上传失败 不会存在列表中
  return new Promise((resolve) => {
    // resolve 参数为关键代码
    resolve({ status: 'fail', error: '上传失败，请检查网络或文件是否符合规范' });
  });
};


// /**
//  * 上传文件
//  */
// const uploadFiles = (Endpoint, bucket, keyPrefix, signPostFormData, file) => {

// }



const getUrl = (bucket, key, version, type) => {
  return new Promise((resolve, reject) => {
    // executor 内 async 逻辑抽出为独立异步函数
    (async () => {
      try {
        // 无版本号（CORS 未暴露响应头/桶未版本化）时不拼 version 参数，
        // 按最新对象访问，避免 version=undefined
        let s3Url = 's3://' + bucket + '/' + key + (version ? '?version=' + version : '');
        let params = {
          s3Urls: [s3Url],
          // Ttl: 'P1D',
          enableVersion: Boolean(version)
        };
        let res = {};
        // enableVersion 是否开启version访问
        if (type == 'app') { // 应用文件
          res = await accessAppFile_api(params);
        }
        if (type == 'public') { // 公开文件
          res = await accessPublicFile_api(params);
        }
        if (res.code == 'Success') {
          let query = {
            url: res.data[0],
            s3: s3Url
          };
          resolve(query);
        }
      } catch (err) {
        reject(false);
      }
    })();
  })
}


const onValidate = (params) => {
  console.log(params)
  switch (params.type) {
    case 'FILES_OVER_LENGTH_LIMIT':
      MessagePlugin.error('只能上传' + props.limit + '张图片');
      break;
  }
}



const handleFail = () => {
  console.log('上传失败')
}

const clearFiles = () => {
  setFiles([]);
}

onUnmounted(() => {
  setFiles([]);
})

defineExpose({
  clearFiles
})

</script>


<template>
  <t-upload ref="uploadRef" v-model="files" theme="image" tips="" accept="image/*" :abridge-name="[6, 6]"
    :disabled="disabled" :auto-upload="autoUpload" :upload-all-files-in-one-request="uploadAllFilesInOneRequest"
    multiple :max="limit" @fail="handleFail" @validate="onValidate" :requestMethod="requestMethod"></t-upload>
</template>

<style lang="scss" scoped></style>
