import axios from 'axios';

export default function useUpload(picType, endpoint, bucket, keyPrefix, signPostFormData, file,
  { accountId, dictId, tenantId, appId, endpointId, appVersion }
) {
  let formData = new FormData();
  formData.append('x-amz-date', signPostFormData['x-amz-date']);
  formData.append('x-amz-signature', signPostFormData['x-amz-signature']);
  formData.append('x-amz-algorithm', signPostFormData['x-amz-algorithm']);
  formData.append('x-amz-credential', signPostFormData['x-amz-credential']);
  formData.append('policy', signPostFormData['policy']);
  formData.append('key', getKey(picType, file, keyPrefix,
    { accountId, dictId, tenantId, appId, endpointId, appVersion }
  ));
  formData.append('file', file);
  return new Promise((resolve, reject) => {
    // executor 内 async 逻辑抽出为独立异步函数
    (async () => {
      try {
        // MinIO 直传走 policy 签名，无需携带凭据；跨域若带凭据，
        // 要求服务端 CORS 返回精确 Origin + Allow-Credentials，
        // 通配符 * 会导致响应被浏览器拦截、响应头读不到。
        // 禁用 withCredentials 并保持纯 FormData 简单请求（无预检）。
        let res = await axios({
          url: endpoint + '/' + bucket,
          method: 'POST',
          withCredentials: false,
          data: formData,
        });
        if (res.status == 204) {
          let query = {
            key: formData.get('key')
          }
          // 版本号从上传响应头透传；浏览器 CORS 未暴露 x-amz-version-id
          // 或桶未开版本化时为空，由调用方退化为无版本访问
          query.version = res.headers['x-amz-version-id'] || null;
          resolve(query);
        } else {
          reject(new Error('上传失败: HTTP ' + res.status));
        }
      } catch (err) {
        console.log(err, 'upload')
        reject(false);
      }
    })();
  })
}



function getKey(picType, file, keyPrefix,
  { accountId, dictId, tenantId, appId, endpointId, appVersion }
) {
  let imageTypeArr = file.name.split('.');
  let imageType = imageTypeArr[imageTypeArr.length - 1]; // 获取图片后缀


  // 文件重命名
  switch (picType) {
    case 'avatar':
      return keyPrefix + '/' + accountId + '.' + imageType;

    case 'tenant-icon':
      return keyPrefix + '/' + tenantId + '.' + imageType;

    case 'sns-provider-icon':
      return keyPrefix + '/' + new Date().getTime()  + '.' + imageType;

    case 'app-icon':
      return keyPrefix + '/' + appId + '.' + imageType;

    case 'endpoint-icon':
      return keyPrefix + '/' + endpointId + '.' + imageType;

    case 'app-release': // 应用软件包
      return keyPrefix + '/' + appId + '-' + endpointId + '-' + appVersion + '.' + imageType;

    case 'permission-icon':
      return keyPrefix + '/' + new Date().getTime() + '.' + imageType;

    case 'sys-dict':
      return keyPrefix + '/' + dictId + '.' + imageType;

    case 'sys-dict-item':
      return keyPrefix + '/' + new Date().getTime() + '.' + imageType;

    case 'menu':
      return keyPrefix + '/' + new Date().getTime() + '.' + imageType;

    default: // app
      return keyPrefix + '/' + new Date().getTime() + '.' + imageType;
  }
}
