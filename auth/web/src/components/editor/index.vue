<!-- 富文本编辑器 -->
<script setup>
import { ref, shallowRef, reactive, onMounted, onBeforeUnmount, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import '@wangeditor/editor/dist/css/style.css' // 引入 css
import { Editor, Toolbar } from '@wangeditor/editor-for-vue'

import useState from '@/hooks/useState';
onMounted(() => {

});

const props = defineProps({
  content: {
    type: Object,
    default: () => ({
      "appId": "test1",
      "endpointId": "web1",
      "clientId": "test1_web1_client1",
      "clientSecret": "test1_web1_client1",
      "clientName": "test1_web1_client1",
      "clientAuthenticationMethods": [
        "client_secret_basic",
        "client_secret_post",
        "client_secret_jwt",
        "private_key_jwt"
      ],
      "authorizationGrantTypes": [
        "authorization_code",
        "refresh_token",
        "account:password",
        "account:verify_code",
        "user:password",
        "user:verify_code",
        "user:connect",
        "user:account_access_token",
        "endpoint_user:password",
        "endpoint_user:verify_code",
        "endpoint_user:connect"
      ],
      "scopes": [
        "sms:send",
        "file:upload_temporary",
        "file:upload_tenant",
        "file:upload_public",
        "dict:read"
      ],
      "redirectUris": [
        "http://localhost",
        "http://localhost/"
      ],
      "clientSettings": {
        "requireProofKey": false,
        "requireUserConsent": false,
        "jwkSetUrl": "http://127.0.0.1:10010/oauth2/jwks",
        "tokenEndpointAuthenticationSigningAlgorithm": "RS256"
      },
      "tokenSettings": {
        "accessTokenTimeToLive": "PT24H",
        "reuseRefreshTokens": true,
        "refreshTokenTimeToLive": "PT720H",
        "idTokenSignatureAlgorithm": "RS256"
      }
    })
  }
})

const editorRef = shallowRef()
const valueHtml = ref(props.content ? JSON.stringify(props.content, null, '\t'): `{
    "appId": "test1",
    "endpointId": "web1",
    "clientId": "test1_web1_client1",
    "clientSecret": "test1_web1_client1",
    "clientName": "test1_web1_client1",
    "clientAuthenticationMethods": [
        "client_secret_basic",
        "client_secret_post",
        "client_secret_jwt",
        "private_key_jwt"
    ],
    "authorizationGrantTypes": [
        "authorization_code",
        "refresh_token",
        "account:password",
        "account:verify_code",
        "user:password",
        "user:verify_code",
        "user:connect",
        "user:account_access_token",
        "endpoint_user:password",
        "endpoint_user:verify_code",
        "endpoint_user:connect"
    ],
    "scopes": [
        "sms:send",
        "file:upload_temporary",
        "file:upload_tenant",
        "file:upload_public",
        "dict:read"
    ],
    "redirectUris": [
        "http://localhost",
        "http://localhost/"
    ],
    "clientSettings": {
        "requireProofKey": false,
        "requireUserConsent": false,
        "jwkSetUrl": "http://127.0.0.1:10010/oauth2/jwks",
        "tokenEndpointAuthenticationSigningAlgorithm": "RS256"
    },
    "tokenSettings": {
        "accessTokenTimeToLive": "PT24H",
        "reuseRefreshTokens": true,
        "refreshTokenTimeToLive": "PT720H",
        "idTokenSignatureAlgorithm": "RS256"
    }
}`);
const toolbarConfig = {}
const editorConfig = { placeholder: '请输入内容...' }
const handleCreated = (editor) => {
  editorRef.value = editor // 记录 editor 实例，重要！
}


const getContent = () => {
  const editor = editorRef.value;
  const text = editor.getText();
  return text;
}


// 组件销毁时，也及时销毁编辑器
onBeforeUnmount(() => {
  const editor = editorRef.value
  if (editor == null) return
  editor.destroy()
});

onUnmounted(() => {

});


defineExpose({
  getContent
})
</script>


<template>
  <div class="editor__wrapper">
    <Toolbar style="border-bottom: 1px solid #ccc" :editor="editorRef" :defaultConfig="toolbarConfig" :mode="mode" />
    <Editor style="height: 500px; overflow-y: hidden;" v-model="valueHtml" :defaultConfig="editorConfig" :mode="mode"
      @onCreated="handleCreated" />
  </div>
</template>

<style lang="scss" scoped>
.editor__wrapper {}
</style>
