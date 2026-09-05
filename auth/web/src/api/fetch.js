import request from './axios';
import {getToken, getTokenType, getAuthType, getAppId, getEndpointId} from '@/utils';

const fetch = {
    post(url, params, headers = {}) {
        let config = {
            headers: headers
        };

        config.headers['Accept-Language'] = 'zh-CN,zh;q=0.9,en;q=0.8';

        return request.post(url, params, config);
    },
    appUserPost(url, params, headers = {}) {
        let config = {
            headers: headers
        };

        config.headers['Accept-Language'] = 'zh-CN,zh;q=0.9,en;q=0.8';

        let token = getToken();
        // let auth_type = getAuthType();
        let appId = getAppId();
        let endpointId = getEndpointId();
        config.headers['authorization'] = 'app_user' + ' ' + appId.value + '/' + endpointId.value + '/' + token.value;

        return request.post(url, params, config);
    },

    subappPost(subappId, subappVersion,url, params, headers = {}, ) {
        let config = {
            headers: headers
        };

        config.headers['Accept-Language'] = 'zh-CN,zh;q=0.9,en;q=0.8';

        let token = getToken();
        let appId = getAppId();
        let endpointId = getEndpointId();
        config.headers['authorization'] = 'subapp_user' + ' ' + appId.value + '/' + endpointId.value + '/' + subappId + '/' + subappVersion + '/' + token.value;

        return request.post(url, params, config);
    },
    get() {

    }
}


export default fetch;
