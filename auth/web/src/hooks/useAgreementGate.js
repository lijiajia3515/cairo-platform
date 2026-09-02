// 协议确认门(大厂登录惯例):未勾选协议时弹窗展示《用户协议》《隐私政策》,
// 「同意并继续」回调放行——调用侧负责勾选协议并继续原流程
import { h } from 'vue';
import { DialogPlugin } from 'tdesign-vue-next';

const useAgreementGate = () => (onAgree) => {
  const link = (url, text) => h('a', {
    href: url,
    target: '_blank',
    rel: 'noopener noreferrer',
    style: 'color: #0052d9;',
  }, text);
  const dia = DialogPlugin({
    header: '服务协议及隐私保护',
    body: () => h('div', { style: 'line-height: 1.8;' }, [
      '为了更好地保障您的合法权益，请阅读并同意',
      link(globalThis._this?.userAgreement, '《用户协议》'),
      '与',
      link(globalThis._this?.privacyPolicy, '《隐私政策》'),
      '。点击「同意并继续」视为您已阅读并同意上述协议。',
    ]),
    confirmBtn: '同意并继续',
    cancelBtn: '不同意',
    onConfirm: () => {
      dia.destroy();
      onAgree && onAgree();
    },
    onClose: () => dia.destroy(),
  });
};

export default useAgreementGate;
