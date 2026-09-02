import js from '@eslint/js';
import pluginVue from 'eslint-plugin-vue';
import prettierConfig from 'eslint-config-prettier';
import globals from 'globals';

export default [
  {
    // 构建产物与生成目录不纳入检查
    ignores: ['dist/**', 'dev-dist/**', 'node_modules/**', 'public/**'],
  },

  js.configs.recommended,
  ...pluginVue.configs['flat/recommended'],

  {
    // 浏览器侧源码：src 下的 js 与 vue 组件
    files: ['src/**/*.{js,vue}'],
    languageOptions: {
      ecmaVersion: 2023,
      sourceType: 'module',
      globals: {
        ...globals.browser,
        // page.config.js 在 index.html 中独立加载，运行时挂到 globalThis
        _this: 'readonly',
        // TDesignResolver 自动注入的四个插件式 API（见 unplugin-vue-components 的 pluginList）
        DialogPlugin: 'readonly',
        LoadingPlugin: 'readonly',
        MessagePlugin: 'readonly',
        NotifyPlugin: 'readonly',
      },
      parserOptions: {
        // 部分组件在 script 中书写 JSX（由 @vitejs/plugin-vue-jsx 编译）
        ecmaFeatures: { jsx: true },
      },
    },
    rules: {
      // console 与死变量为存量告警（218 处），不阻断构建，逐步清理
      'no-console': 'warn',
      // 允许 catch (e) {} 之外的空块由人工判断，此处仅放开 catch
      'no-empty': ['error', { allowEmptyCatch: true }],

      // 存量死变量约 560 处，逐步清理，暂不阻断构建；
      // 未使用的函数参数多为对齐接口签名的占位，不报
      'no-unused-vars': ['warn', { args: 'none', caughtErrors: 'none' }],

      // 单文件组件名沿用目录约定，暂不强制多词命名
      'vue/multi-word-component-names': 'off',
      // props 刻意使用 PascalCase 以对齐服务端 DTO 字段
      'vue/prop-name-casing': 'off',
      // 大量 props 由服务端数据驱动，缺省值无意义
      'vue/require-default-prop': 'off',

      // 以下为纯排版偏好，改动量大且无正确性收益，交由 Prettier 与人工把握
      'vue/attributes-order': 'off',
      'vue/attribute-hyphenation': 'off',
      'vue/v-on-event-hyphenation': 'off',
      'vue/first-attribute-linebreak': 'off',
    },
  },

  {
    // Node 侧脚本：构建配置、代码生成模板、运行时配置
    files: ['vite.config.js', 'templates/**/*.js', 'page.config.js', 'eslint.config.js'],
    languageOptions: {
      ecmaVersion: 2023,
      sourceType: 'module',
      globals: { ...globals.node },
    },
    rules: {
      // 模板生成器以 console 作为用户输出通道
      'no-console': 'off',
    },
  },

  {
    // Node 侧 CJS 脚本：scripts 下的检查/对齐工具
    files: ['scripts/**/*.cjs'],
    languageOptions: {
      ecmaVersion: 2023,
      sourceType: 'commonjs',
      globals: { ...globals.node },
    },
    rules: {
      // 脚本以 console 作为用户输出通道
      'no-console': 'off',
    },
  },

  // 关闭与 Prettier 冲突的格式类规则，必须置于末位
  prettierConfig,
];
