<script setup>
import { ref, reactive, onMounted, onUnmounted, computed, watch } from 'vue';
import { useRouter } from 'vue-router';
import { MessagePlugin } from 'tdesign-vue-next';
import axios from 'axios';

import useState from '@/hooks/useState';

import useSign from '../uploadImage/useSign';
import useUpload from '../uploadImage/useUpload';

import {
  getDateFormat
} from '@/utils/tips';


import {
  getAppFileUploadSign_api,
  accessPublicFile_api,
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
// app-release =>  appId endpointId appVersion

onMounted(() => {

});

const emit = defineEmits(['change'])

let uploadRef = ref(null);

const [disabled, setDisabled] = useState(props.disabled);
const [files, setFiles] = useState([]);
const [editFiles, setEditFiles] = useState();


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
        console.log(uploadRes, 'uploadRes=====');
        const { key } = uploadRes;
        let urlRes = await getUrl(bucket, key);
        const { url, s3 } = urlRes;

        resolve({ status: 'success', response: { url, s3 } });
        clearInterval(percentTimer);
      } catch (err) {
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
//  * 获取签名
//  */
// const getSign = async () => {
//   return new Promise(async (resolve, reject) => {
//     try {
//       let params = {
//         Meta: {},
//         Ttl: 'P1D',
//       };
//       if (props.picType == 'temporary') {
//         params['keyPrefix'] = getDateFormat('-') + '/';
//       } else {
//         params['keyPrefix'] = '';
//       }
//       let res = {};
//       if (props.type == 'temporary') { // 临时文件
//         res = await getTemporaryUploadSign_api(params);
//       }
//       if (props.type == 'public') { // 公开文件
//         res = await getPublicFileUploadSign_api(params);
//       }
//       if (props.type == 'tenant') { // 企业文件
//       }

//       if (res.code == 'Success') {
//         let query = {
//           Bucket: res?.data?.bucket || null,
//           Endpoint: res?.data?.endpoint || null,
//           expiresTime: res?.data?.expiresTime || null,
//           keyPrefix: res?.data?.keyPrefix || null,
//           signPostFormData: res?.data?.signPostFormData || null,
//         };
//         resolve(query);
//       }
//     } catch (err) {
//       reject(false);
//     }
//   })
// }

// /**
//  * 上传文件
//  */
// const uploadFiles = (Endpoint, bucket, keyPrefix, signPostFormData, file) => {
//   let formData = new FormData();
//   formData.append('x-amz-date', signPostFormData['x-amz-date']);
//   formData.append('x-amz-signature', signPostFormData['x-amz-signature']);
//   formData.append('x-amz-algorithm', signPostFormData['x-amz-algorithm']);
//   formData.append('x-amz-credential', signPostFormData['x-amz-credential']);
//   formData.append('policy', signPostFormData['policy']);
//   let imageTypeArr = file.name.split('.');
//   let imageType = imageTypeArr[imageTypeArr.length - 1];
//   if (props.picType == 'avatar') {
//     formData.append('key', 'avatar' + '/' + props.accountId + '.' + imageType);
//   }
//   if (props.picType == 'sys-dict') {
//     formData.append('key', props.appId + '/sys-dict-icon/' + props.dictId + '.' + imageType);
//   }
//   if (props.picType == 'menu') {
//     formData.append('key', keyPrefix + 'menu' + '/' + props.appId + '-' + props.endpointId + '-' + props.menuId + '.' + imageType);
//   }
//   if (props.picType == 'app-release') {
//     formData.append('key', props.appId + '/app-release' + '/' + props.appId + '-' + props.endpointId + '-' + props.appVersion + '.' + imageType);
//   }
//   if (props.picType == 'temporary') {
//     formData.append('key', keyPrefix + new Date().getTime() + '/' + file.name);
//   }

//   formData.append('file', file.raw);
//   return new Promise(async (resolve, reject) => {
//     try {
//       axios.defaults.crossDomain = true;
//       axios.defaults.withCredentials = true;
//       let res = await axios({
//         url: Endpoint + '/' + Bucket,
//         method: 'POST',
//         headers: {
//           Authorization: null,
//         },
//         data: formData,
//       });
//       if (res.status == 204) {
//         let query = {
//           key: formData.get('key')
//         }
//         resolve(query);
//       }
//     } catch (err) {
//       reject(false);
//     }
//   })
// }



const getUrl = (bucket, key) => {
  return new Promise((resolve, reject) => {
    // executor 内 async 逻辑抽出为独立异步函数
    (async () => {
      try {
        let s3Url = 's3://' + bucket + '/' + key;
        let params = {
          // Paths
          s3Urls: [s3Url],
          ttl: 'P1D'
        };
        console.log(key, 'key=====');
        // enableVersion 是否开启version访问
        let res = await accessPublicFile_api(params);
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
      MessagePlugin.error('只能上传' + props.limit + '个文件');
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
  <t-upload ref="uploadRef" v-model="files" theme="file" :tips="tips" accept=".apk" :abridge-name="[20, 20]"
    :disabled="disabled" :auto-upload="autoUpload" :upload-all-files-in-one-request="uploadAllFilesInOneRequest"
    multiple :max="limit" @fail="handleFail" @validate="onValidate" :requestMethod="requestMethod"></t-upload>
</template>

<style lang="scss" scoped></style>
