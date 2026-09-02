# cairo-console-web

Cairo 运营平台（auth/web）前端。

## 环境要求

| 项目 | 版本 | 说明 |
| --- | --- | --- |
| Node.js | 24.20.0 | Active LTS，已写入 `.nvmrc` |
| pnpm | 11.24.0 | 由 `packageManager` 字段锁定，通过 corepack 自动启用 |

```bash
nvm use              # 读取 .nvmrc
corepack enable      # 首次需执行一次
pnpm install
```

## 常用命令

| 命令 | 作用 |
| --- | --- |
| `pnpm dev` | 启动开发服务器（端口 5173，自动打开浏览器） |
| `pnpm build` | 生产构建，输出到 `dist/` |
| `pnpm preview` | 本地预览构建产物 |
| `pnpm lint` | ESLint 检查 |
| `pnpm lint:fix` | ESLint 自动修复 |
| `pnpm format` | Prettier 格式化 |
| `pnpm format:check` | 仅检查格式，不写入 |
| `pnpm create` | 按模板生成新页面骨架（`templates/`） |

## 运行时配置

`page.config.js` 由 `index.html` 以独立 `<script type="module">` 加载，
**不参与 Vite 打包**，因此部署后可直接修改而无需重新构建。
其中包含网关地址、应用标识与接口版本号。

## API 层结构（src/api/）

按业务领域拆分为 15 个领域文件 + 5 个基础设施文件，引用方统一 `from '@/api'`：

```
src/api/
├── index.js    # 桶入口：re-export 全部领域文件
├── urls.js     # URL 前缀常量（openApi/appUserApi/subappUserApi/manageApi + 管理子应用标识）
├── fetch.js    # 三个传输方法（见下）
├── axios.js / status.js   # axios 实例与错误码分发（错误码清单见 auth/docs/error-codes.md）
└── 领域文件：open / personal / account / user / app / endpoint / client /
    subapp / snsProvider / log / tenant / sysDict / message / link / area
```

传输上下文三件（真实分层轴是「谁在调用」而非服务来源）：

| fetch 方法 | 请求头注入 | 适用 |
| --- | --- | --- |
| `fetch.post` | 无 | 开放接口（登录/注册/验证码） |
| `fetch.endpointPost` | `app_user` + appId/endpointId/token | 终端用户上下文（个人中心） |
| `fetch.subappPost` | `subapp_user` + 全套子应用标识 | 管理台及其余全部管理操作 |

函数命名约定：动词前置且与 URL 语义一致——`getXxxPage/List`（查询）、`create/modify/delete/move/sync/copy/offline/put/retry Xxx`（写操作），禁止 `get` 前缀包写操作。前后端路由对齐用 `node scripts/check-api-align.cjs` 校验（240 调用 vs 后端 @RequestMapping，唯一豁免 `/oauth2/token` 框架端点）。

## 相关文档

- [auth 账号](../README.md)：后端主体模型、认证体系、错误码
- [auth 文档中枢](../docs/README.md)：数据库基线数据与导入脚本
- [错误码清单](../docs/error-codes.md)：`src/api/status.js` 分发的权威码表
- [平台总览](../../README.md)：架构、快速开始、文档导航

## 参考文档

- [Vue Router](https://router.vuejs.org/zh/introduction.html)
- [TDesign Vue Next](https://tdesign.tencent.com/vue-next/overview)
- [Pinia](https://pinia.vuejs.org/zh/)
