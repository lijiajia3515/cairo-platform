<script setup>
import {
  ref, reactive, onMounted, onUnmounted,
  watch
} from 'vue';
import { useRouter } from 'vue-router';
import useState from '@/hooks/useState';

const props = defineProps({
  width: {
    type: String,
    default: '40%'
  },
  top: {
    type: String
  },
  visible: {
    type: Boolean,
    default: false
  },
  attach:{
    type:String,
    default:null
  },
  zIndex:{
    type:Number
  },
  cancelBtn:{
    type:String
  },
  confirmBtn:{
    type:String
  }
});

const emit = defineEmits(['confirm', 'close']);

const [visible, setVisible] = useState(props.visible);

watch(() => props.visible, () => {
  setVisible(props.visible)
})


const onConfirm = () => {
  emit('confirm')
}

const onClose = () => {
  emit('close')
}



onMounted(() => {

});



onUnmounted(() => {

})
</script>


<template>
  <t-dialog :confirmBtn="confirmBtn" :cancelBtn="cancelBtn" :zIndex="zIndex" :attach="attach" :top="props.top" :width="props.width" :close-on-overlay-click="false" @confirm="onConfirm" @close="onClose"
    :visible="visible">
    <template #header>
      <slot name="title"></slot>
    </template>
    <slot></slot>
  </t-dialog>
</template>

<style lang="scss" scoped></style>
