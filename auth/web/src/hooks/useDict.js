
import {
  getSysDictDetailInfo_api
} from '@/api';

export default function useDict(dictId) {
  // 返回 Promise 供调用方 await；executor 内 async 逻辑抽出为独立异步函数
  return new Promise((resolve) => {
    (async () => {
      let headers = {
        'app-id': _this.appid
      };
      let res = await getSysDictDetailInfo_api({ dictId }, headers);
      if (res.code == 'Success') {
        resolve(res?.data?.items || []);
      }
    })();
  })
}