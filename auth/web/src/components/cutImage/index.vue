<script setup>
import {
  ref, reactive, onMounted, onUnmounted, watch, nextTick,
  computed,
} from 'vue';
import { useRouter } from 'vue-router';
import Cropper from 'cropperjs';
import 'cropperjs/dist/cropper.css'


import useState from '@/hooks/useState';
import useSign from '@/components/uploadImage/useSign';
import useUpload from '@/components/uploadImage/useUpload';

import { useUserStore } from '@/store/user';

import {
  getDateFormat,
  dataURLtoFile,
  randomString,
} from '@/utils/tips';


const userStore = useUserStore();

const user = computed(() => userStore.userGetter);

const props = defineProps({
  type: {
    type: String,
    default: 'public', //   public 公开 tenant 企业  app应用
  },
  accountId: {
    type: String
  },
  picType: {
    type: String
  }
});

const emit = defineEmits(['confirm', 'close']);

let cropper = ref(null);

onMounted(() => {

});

let fileRef = ref(null);
let visible = ref(null);
let imgurl = ref(null);

const imageContainer = ref(null);
const image = ref(null);

watch(visible, () => {
  if (visible.value == true) {
    nextTick(() => {
      cropper.value = new Cropper(image.value, {
        aspectRatio: 1,
        viewMode: 0,
        dragMode: 'move',
        preview: '.preview_Url',
        guides: true,
        highlight: true,
        background: true, // 是否显示网格
        autoCropArea: 0.8, // 裁剪区大小
        cropBoxResizable: false, // 剪裁框不可以调整大小
        cropBoxMovable: false, // 裁剪框不可以移动

        zoomOnWheel: true, // 设置图片是否可以进行收缩功能
        center: true,
      });
    })
  } else {
    cropper.value = null;
  }
})


let chooseImg = async () => {
  let reader = new FileReader();
  const fileObj = fileRef.value.files[0];
  // emit('confirm', fileObj[0]);
  console.log(fileObj, 'fileObj=====');
  reader.readAsDataURL(fileObj);
  reader.onload = function(e) {
    imgurl.value = reader.result; // base64
    visible.value = true;
  };

}

const onConfirm = async () => {
  const croppedImageDataUrl = cropper.value.getCroppedCanvas({
    width: 100,
    height: 100,
    imageSmoothingQuality: 'low'
    // minWidth、minHeight、maxWidth、maxHeight、fillColor、imageSmoothingEnabled（图片是否是光滑的 默认true）、
    // imageSmoothingQuality（图片的质量 默认low 还有medium、high）
  }).toDataURL('image/jpeg');
  let strArr = croppedImageDataUrl.split('image/');
  let str2 = strArr[1];
  let typeArr = str2.split(';base64');
  let type = typeArr[0];
  let newFile = dataURLtoFile(croppedImageDataUrl, 'avatar_' + randomString(5) + '.' + type);
  // let list = await filterUpload([newFile]);
  emit('confirm', newFile);
  cropper.value = null;
}
const onClose = () => {
  emit('close')
  close();
}

// const filterUpload = async (fileList) => {
//   return new Promise(async (resolve, reject) => {
//     let arr = [];
//     for (let file of fileList) {
//       console.log(file, 'file')
//       let signRes = await useSign(props.type, 'avatar', {}); // 签名
//       const { bucket, endpoint, expiresTime, keyPrefix, signPostFormData } = signRes;
//       let uploadRes = await useUpload('avatar', endpoint, bucket, keyPrefix, signPostFormData, file, {
//         accountId: props.accountId
//       }); // 上传文件
//       const { key } = uploadRes;

//       let version = localStorage.getItem('versionId')
//       console.log(version, 'version=====');
//       nextTick(async () => {
//         let urlRes = await getUrl(Bucket, key, version);
//         const { url, s3 } = urlRes;
//         let obj = {
//           url, s3,
//         };
//         arr.push(obj);
//         emit('confirm', arr[0]);
//       })

//     }
//     resolve(arr);
//   })
// }


// const getUrl = (Bucket, key, version) => {
//   return new Promise(async (resolve) => {
//     let s3Url = 's3://' + Bucket + '/' + key + '?' + `version=${version}`;
//     let params = {
//       S3Urls: [s3Url],
//       enableVersion: true
//     };
//     // enableVersion 是否开启version访问
//     let res = await getPublicSignUrl_api(params);
//     if (res.code == 'Success') {
//       let query = {
//         url: res.data[0],
//         s3: s3Url
//       };
//       resolve(query);
//     }
//   })
// }


/**
 * 获取签名
 */
// const getSign = async () => {
//   return new Promise(async (resolve, reject) => {
//     let params = {
//       Meta: {},
//       Ttl: 'P1D',
//     };
//     if (props.picType == 'temporary') {
//       params['keyPrefix'] = getDateFormat('-') + '/';
//     } else {
//       params['keyPrefix'] = '';
//     }
//     let res = {};
//     if (props.type == 'temporary') { // 临时文件
//       res = await getTemporaryUploadSign_api(params);
//     }
//     if (props.type == 'public') { // 公开文件
//       res = await getPublicFileUploadSign_api(params);
//     }
//     if (props.type == 'tenant') { // 企业文件
//     }

//     if (res.code == 'Success') {
//       let query = {
//         Bucket: res?.data?.bucket || null,
//         Endpoint: res?.data?.endpoint || null,
//         expiresTime: res?.data?.expiresTime || null,
//         keyPrefix: res?.data?.keyPrefix || null,
//         signPostFormData: res?.data?.signPostFormData || null,
//       };
//       resolve(query);
//     }
//   })
// }






const open = () => {
  fileRef.value.click();
}
const close = () => {
  cropper.value = null;
  visible.value = false;
}

defineExpose({
  open,
  close,
})


onUnmounted(() => {

})
</script>


<template>
  <div class="cutImage__wrapper">
    <input @change="chooseImg" accept="image/*" type="file" name="file" class="fileClass" ref="fileRef" />
    <t-Dialog @confirm="onConfirm" @close="onClose" width="730px" :visible="visible">
      <template #header>裁剪图片</template>
      <div class="mainCanvas">
        <div ref="imageContainer" class="img_main">
          <img ref="image" :src="imgurl" alt="">
        </div>
        <div class="preview">
          <div class="preview_Url"></div>
          <div class="empty"></div>
          <p>裁切：100px 100px</p>
        </div>
      </div>
    </t-Dialog>
  </div>
</template>

<style lang="scss" scoped>
.cutImage__wrapper {
  .fileClass {
    display: none;
  }

  .mainCanvas {
    // width: 450px;
    height: 500px;
    display: flex;
    box-sizing: border-box;

    .img_main {
      width: 300px;
      height: 300px;

      img {
        display: block;
        max-width: 100%;
      }
    }

    .preview {
      // width: 140px;
      height: 500px;
      margin-left: 60px;

      .preview_Url {
        width: 240px;
        height: 240px;
        overflow: hidden;
        border: 1px solid #ccc;
        box-sizing: border-box;
      }
    }
  }
}
</style>
