import { MessagePlugin } from 'tdesign-vue-next';
export default function useCopy(value) {
  const textarea = document.createElement('textarea');
  textarea.value = value;
  document.body.appendChild(textarea);

  // 选择并复制文本
  textarea.select();
  document.execCommand('copy');
  // 移除临时元素
  document.body.removeChild(textarea);
  MessagePlugin.success('复制成功');
}