import {
  getDateFormat
} from '@/utils/tips';

import {
  getAppFileUploadSign_api,
  getPublicFileUploadSign_api,
} from '@/api';


function getKeyPrefix(picType, { appId }) {
  const keyObj = {
    'avatar': 'avatar',
    'tenant-icon': 'tenant-icon',
    'app-icon': 'app-icon',
    'endpoint-icon': appId + '/endpoint-icon',
    'permission-icon': appId + '/permission-icon', // 功能权限icon
    'sns-provider-icon': 'sns-provider-icon',
    'menu': appId + '/menu-icon',
    'sys-dict': appId + '/sys-dict-icon',
    'sys-dict-item': appId + '/sys-dict-item-icon',
    'notify-category-icon': appId + '/notify-category-icon',
    'notify-template-icon': appId + '/notify-template-icon',
    'public': appId + '/' + new Date().getTime(),
    // 'app': appId + '/' + new Date().getTime(),
    'app-release': appId + '/app-release'
  }

  return keyObj[picType]
}


/**
 *
 * @param {String} type
 * @param {String} picType  avatar app-icon endpoint-icon permission-icon menu sys-dict sys-dict-item notify-category-icon notify-template-icon app-release
 * @returns
 */
export default function useSign(type, picType, { appId }) {
  return new Promise((resolve, reject) => {
    // executor 内 async 逻辑抽出为独立异步函数
    (async () => {
      try {
        let params = {
          meta: {},
          ttl: 'P1D',
        };
        params['keyPrefix'] = getKeyPrefix(picType, { appId });
        let res = {};
        if (type == 'app') { // 应用文件
          res = await getAppFileUploadSign_api(params);
        }
        if (type == 'public') { // 公开文件
          res = await getPublicFileUploadSign_api(params);
        }

        if (res.code == 'Success') {
          let query = {
            bucket: res?.data?.bucket || null,
            endpoint: res?.data?.endpoint || null,
            expiresTime: res?.data?.expiresTime || null,
            keyPrefix: res?.data?.keyPrefix || null,
            signPostFormData: res?.data?.signPostFormData || null,
          };
          resolve(query);
        }
      } catch (err) {
        reject(false);
      }
    })();
  })
}
