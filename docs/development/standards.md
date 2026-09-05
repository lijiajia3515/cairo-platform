# 开发规范

全仓（后端 + 前端）的现行开发约定。新增与修改代码务必遵守。

## 命名

实体名与代码 / 集合 / 字典 ID 一致——`endpoint`、`subapp`、`permission`；中文术语统一「终端」「子应用」。改名必须走全链路（Java / 集合 / URL / 队列 / authority / 前端 / 种子数据 / 文档）。

## 业务标识

短值 + 复合唯一。`endpointId=web`、`subappId=manage` 依赖 `(appId, endpointId, subappId[, subappVersion])` 复合唯一索引，不做全局唯一；前提是子标识处处与上层标识共存、代码零单独查询。

实体主键（accountId / userId / 集合文档 id 等）统一由 `CoreConstants.nextIdStr()`（`framework/core`）生成 **UUIDv7**（RFC 9562，JUG 库 `timeBasedEpochGenerator`：毫秒时间戳前缀、同毫秒单调递增、免协调、字符串排序即创建时间序）；角色 / 权限等 `sort` 默认值为毫秒时间戳（Long，仅取排序语义）。

## 权限模型（双层）

`permissionId`（`资源.动作`，前端 `v-allow` 指令直接绑定）+ `authorities`（`资源:动作`，`@PreAuthorize` 校验）两层值 1:1；新增管理功能需同时补菜单权限点与后端 authority。

## 字典

值以代码枚举为权威源；DictId 与实体名一致（前端 `useDict(id)` 直接引用）；基线快照在 `docs/auth/db/data/`。

## DB 变更

- `docs/auth/db/` 为唯一权威源；
- 索引名统一 `ix_{字段按 keys 声明序}[_unique]`（点分字段取叶子名）；
- 测试库账号无 `collMod`，验证器变更走「备份 → drop + 重建 → 回填」；
- 嵌套集数据（菜单 / 字典项）必须走 API 注入由服务端计算左右值，禁止直插。

## 前端（auth/web/）

- Node 24 LTS + pnpm 11（corepack 锁定，见 `.nvmrc` / `packageManager`）；
- axios 实例不设默认 `Content-Type`（FormData 会被 JSON 化）；
- MinIO 直传不携带 `withCredentials`；
- 敏感配置仅在运行时 `page.config.js`（不参与 Vite 打包，部署后可直接改）。

## 相关文档

- 构建与发布机制：[build-system.md](build-system.md)
- 新增子应用 / 模块流程：[new-module.md](new-module.md)
