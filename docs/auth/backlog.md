# 待办需求

## 壳层(P1 多标签/个性化/折叠)遗留

- ~~**暗黑模式覆盖面**：自定义样式硬编码浅色清扫~~（已完成：边框 `#ededed`→`var(--td-component-stroke)`、禁用选项底→`var(--td-bg-color-component-disabled)`、白底→`var(--td-bg-color-container)`；品牌色/遮罩上的白字白点等 6 处合法保留）
- **iframe 子应用多标签独立缓存**：当前 `/iframe` 不缓存（每次按 query 重载子应用）；多子应用并行开签时应按 URL 缓存 iframe 实例（参考 pig `routerView/iframes.vue` 按标签缓存）
- **登录页未应用个性化主题**：暗黑/品牌色仅壳内生效（`themeStore.apply()` 在壳 onMounted），登录/注册页仍固定浅色
- **PWA 版本清缓存 vs 主题持久化**：`main.js` 版本变更时 `localStorage.clear()` 会连带清掉主题配置（`cairo_theme_config`），可改为白名单式清理
- **外部链接菜单 active 态**：所有外链菜单 `:value` 同为 `'/iframe'`，开签期高亮无法区分是哪个外链

## P2 首页改版（已定方案待实施）

欢迎横幅（问候/账号/日期）+ 快捷入口网格（权限过滤）+ 统计卡片行（需新增 `cairo_web_manage` 面 `get_dashboard_statistics` 聚合接口 + 菜单/权限点/authority 三层同步）+ 趋势图表（近 7/30 天登录趋势、登录方式分布，需引 echarts）+ 最近登录记录表（复用现有 API）+ 我的会话卡（Qoder 式 is_current/下线）。

## 远期

- Excel 导入导出（列表数据级，EasyExcel 或等价方案）
- WebSocket 实时推送（通知到达/会话下线即时反馈）
- 代码生成器（图形化 CRUD，参考 pig-codegen）
- sessionId 分层改名（对外 `sessionId`/对内 `authorizationId`，111 处 + DB 迁移 + 仓外消费方协调）+ provider 显式 `.id(UUIDv7)` 替代 Spring 自动 v4
- Spring Boot 4.x 迁移评估（3.5 线 OSS 支持 2026-06-30 已到期）

## 登录协议同意状态绑定协议版本

三端（auth/web 两种登录方式、注册页、auth/service 服务端登录页）协议门已实现「同意并继续」后持久记住，下次进入默认勾选、不再弹门。但同意状态是**永久**的——协议更新（隐私政策头部版本号，当前 v20231015）后用户不会重新确认，合规上有风险。

**方案**：存储键绑定协议版本号，协议更新即强制全量用户重新同意。

- 前端：localStorage `changeData` → `changeData_v<版本>`
- 服务端登录页：localStorage `cairo_login_agree` → `cairo_login_agree_v<版本>`
- 版本号来源：`auth/web/public/agreement/privacy.html` 的版本标记

涉及：`auth/web/src/views/login/index.vue`、`loginByCode.vue`、`loginByPassword.vue`、`register.vue`、`auth/service/src/main/resources/templates/login.html`

## 协议双拷贝同步

协议文件现存两份拷贝：`auth/web/public/agreement/`（前端）与 `auth/service/src/main/resources/static/agreement/`（服务端登录页），修改协议需手动同步两处。可做构建期自动拷贝。

## 占位页收尾

- 用户组 `/manage/contact/user_group`、通知消息记录 `/manage/notify/record` 均为占位空页（路由/视图已就绪）；通知记录的菜单行两端 DB 均未建，接入通知通道时在菜单管理补建
