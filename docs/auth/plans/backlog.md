# 待办（按域分类）

> 维护者工作区，非用户参考文档。
> 状态图例：🚧 进行中 · ⬜ 未开始。**已完成项不在此列**（见 git 历史）。
> 架构侧专项计划另见 [api-convergence-plan.md](api-convergence-plan.md)。

## 前端（auth/web）

- 🚧 **首页改版（部分完成）**：快捷入口已上线；欢迎横幅、统计卡片（当前为占位数据）、趋势图表、最近登录记录、我的会话卡待接入后端 `get_dashboard_statistics` 聚合接口
- ⬜ **iframe 子应用多标签独立缓存**：当前 `/iframe` 不缓存，每次按 query 重载子应用实例；多子应用并行开签时应按 URL 缓存（参考 pig `routerView/iframes.vue`）
- ⬜ **登录页未应用个性化主题**：暗黑/品牌色仅壳内生效（`themeStore.apply()` 在壳 onMounted），登录/注册页仍固定浅色
- ⬜ **PWA 版本清缓存 vs 主题持久化**：`main.js` 版本变更时 `localStorage.clear()` 连带清掉主题配置（`cairo_theme_config`），改为白名单式清理
- ⬜ **外部链接菜单 active 态**：所有外链菜单 `:value` 同为 `'/iframe'`，开签期高亮无法区分是哪个外链
- ⬜ **登录协议同意状态绑定协议版本**：当前 `changeData` / `cairo_login_agree` 永久记住；协议更新后应强制重新确认（存储键绑定协议版本号）
- ⬜ **协议双拷贝构建期同步**：`auth/web/public/agreement/` 与 `auth/service` 两处拷贝可做构建期自动拷贝
- ⬜ **占位页收尾**：`/manage/contact/user_group`、`/manage/notify/record` 视图/路由已备；通知记录的菜单行两端 DB 均未建，接入通知通道时在菜单管理补建
- ⬜ **功能规划**：Excel 导入导出（列表数据级）· WebSocket 实时推送（通知到达/会话下线即时反馈）· 代码生成器（图形化 CRUD，参考 pig-codegen）

## 后端（auth/service）

- ⬜ **`get_dashboard_statistics` 聚合接口**：首页改版前置（`cairo_web_manage` 面 + 菜单/权限点/authority 三层同步）
- ⬜ **OAuth2 授权记录 id 显式 UUIDv7**：provider 显式 `.id(UUIDv7)` 替代 Spring 自动 v4
- ⬜ **AuthBadLoginLogHandler 前缀污染**：历史遗留，用户裁决中（见 [testing/test-plan.md](../testing/test-plan.md) 审计遗留清单）

## 测试

- ⬜ **用例落地**：`test-plan.md` 回归矩阵 → 脚本化 E2E / 接口集合（落地形式待定）

## 架构

- ⬜ **API 面收敛 P2**：ApiService 下沉分批（详见 [api-convergence-plan.md](api-convergence-plan.md)）
- ⬜ **Spring Boot 4.x 迁移评估**：3.5 线 OSS 支持 2026-06-30 已到期
