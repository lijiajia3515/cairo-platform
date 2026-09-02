
import {ref} from 'vue';
export default function(value) {
  const state = ref(value); // 初始值
  const setState = (newVal) => {
    state.value = newVal
  }
  return [state, setState];
}

