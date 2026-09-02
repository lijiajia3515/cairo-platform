// entryTemplate.js
export const entryTpl = (compoenntName) => {
    return `
      import ${compoenntName} from './index.vue'
      export default ${compoenntName}
    `
}
