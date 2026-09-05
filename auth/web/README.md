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
├── axios.js / status.js   # axios 实例与错误码分发（错误码清单见 docs/auth/error-codes.md）
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

## 列表页规范（src/components/list + src/utils/tableColumns.js）

分页列表页统一经 list 壳（`components/list`）+ 列工厂（`utils/tableColumns.js`）组装，新页面禁止手写 `t-table` 列内联（头像/复制/开关等均由列工厂表达；少数非分页小表如菜单编辑、看板小表除外）。

列工厂一览：

| 工厂 | 用途 |
| --- | --- |
| `timeColumn(colKey, title, { width, or })` | 时间列：短格式 `MM-DD HH:mm` + 悬停全量 + 复制；`or` 备选键在主键为空时取备选值 |
| `ellipsisColumn(colKey, title, { width })` | 长文本列：封顶省略 + 悬停 title 全量 |
| `copyColumn(colKey, title, { width, copyKey })` | 悬停复制列：`copyKey` 使展示值与复制值分离 |
| `entityColumn({ colKey, title, iconKey, copyKey, onClick, cap })` | 实体引用列：16px 图标 + 名称、悬停复制实体 ID、可点详情（别名 `avatarCopyColumn`） |
| `userColumn({ colKey, nameKey, avatarKey, idKey, onClick, unbound, width })` | 用户/账号列：头像回退昵称，未绑定文案 |
| `switchColumn({ colKey, api, idKeys, perm, refresh, ... })` | 行内开关直改：确认弹窗 → 状态 API → 刷新 |
| `statusTagColumn(colKey, title, { type, pairs })` | 只读二值彩色标签 |
| `opColumn(buttons, { title, width })` | 操作列：前 3 个平铺 + 「更多」下拉（fixed right）；下拉项按按钮 theme 着色（danger→error 红等） |

列宽三层规则（表格布局为原生 auto，见 `style/design.scss`）：

1. **`minWidth` 是硬下限，`width` 只是建议值**——auto 布局下小屏会压缩未设下限的列（表头逐字竖排）。list 壳对全列兜底：`minWidth ?? width ?? 语义猜值`；工厂列按表头字数（14px/字 + 34）兜底。
2. **nowrap 长文本必须封顶省略**——auto 布局下列宽由内容撑开，长 UA/UUID 会把列顶到数百像素；`cappedText` 按 width 封顶，悬停看全量。
3. **列合计超出视口 = 横向滚动**，操作列 fixed right 钉边；不靠压列凑合，列过多时应精简字段。

列语义规则：

- **主体自身 ID 列排第一**（如账号页账号ID、应用页应用ID、企业页企业ID）。
- **引用实体不设独立 ID 列**——名称单元格悬停即复制实体 ID，查全量去该实体自己的管理页。
- 状态开关/标签一律走 `switchColumn`/`statusTagColumn`；JSX 操作列权限用 `hasPermission` visible 回调（`v-allow` 在 JSX 静默失效）。

## 路由与视图命名

路由 = 静态骨架 + 后端菜单动态注册（`router/getComponent.js` 的 key = DB `auth_menu.component`，种源 `docs/auth/db/data/menu.json`）：

- **资源即路由**，按域加前缀：账号 `/manage/account`、企业 `/manage/tenant/*`、开发 `/manage/develop/*`、通讯录 `/manage/contact/*`、系统设置 `/manage/system/*`；分组节点用伪路由（`/center/*`、`/service/*`、`/manage/contact`、`/manage/system`），不注册成路由。
- **平台页无前缀**：`/home` 首页、`/profile` 个人信息、`/iframe` 外链宿主——静态注册、不挂权限门槛、不占标签位；首页是登录即用的落地页（默认落点/面包屑根/404 归宿），菜单树里的 `/manage/home` 复用同一视图。
- **视图组件名 = 路径 slug**（`/manage/contact/user` → `manage-contact-user`，`defineOptions` 注入），keep-alive 按组件名缓存；views 目录名与路由 key 独立（改目录只需同步 import 路径）。
- 支持跨子应用标签共存：任意子应用已开标签始终可达，路由按 `/子应用前缀` 隔离。

## 依赖补丁

`patches/` + `pnpm-workspace.yaml` `patchedDependencies` 管理第三方补丁（当前：v3-jsoneditor 渲染期 `this.max` 未声明告警）。打完补丁必须**重启 dev server**——内存模块图不会随 node_modules 变化重解析。

## 相关文档

- [auth 账号](../README.md)：后端主体模型、认证体系、错误码
- [auth 文档中枢](../../docs/auth/README.md)：数据库基线数据与导入脚本
- [错误码清单](../../docs/auth/error-codes.md)：`src/api/status.js` 分发的权威码表
- [平台总览](../../README.md)：架构、快速开始、文档导航

## 参考文档

- [Vue Router](https://router.vuejs.org/zh/introduction.html)
- [TDesign Vue Next](https://tdesign.tencent.com/vue-next/overview)
- [Pinia](https://pinia.vuejs.org/zh/)
