# 测试计划（功能 / 权限 / 上下文约束）

> 2026-09-04 起。本轮安全审计结论与回归矩阵的权威源；用例落地形式待定（脚本化 E2E / 接口集合）。

## 一、上下文头切换（多应用管理设计，2026-09-04 用户裁决）

**设计定案**：cairo 管理台是平台级运营控制台。认证域固定（token=cairo/manage，与切换无关），管理域靠请求头切换——`app-id`/`endpoint-id`/`subapp-id`/`subapp-version` 四头指定「管理哪个应用/终端/子应用的数据」。菜单页有应用→终端→子应用→版本四级级联选择器；企业子应用/通知/微信模板/登录日志等 8+ 页面同样按此传参。**曾误判为越权面并加钉死校验（00ff3613），确认设计后撤回**——钉死会把选中的目标应用静默覆写回 cairo，多应用管理失效。

真实安全边界（不依赖 header 一致性）：
1. `CairoSecurityInterceptor` CAIRO_WEB_MANAGE_USER 门——必须是 cairo 应用的 manage 子应用上下文 token
2. 各面 `@PreAuthorize` 权限点（menu:write 等，来自 manage 子应用级用户权限）
3. 认证期 `findByToken(appId, endpointId, token)` 已绑定 token 与认证域 app/endpoint

| # | 场景 | 请求 | 预期 |
|---|---|---|---|
| C1 | 四头齐全指向目标应用 | 选应用 X → 菜单/权限 CRUD | 业务读写应用 X 的数据（多应用管理核心回归） |
| C2 | 级联切换 | 应用切换后重置终端/子应用/版本 | 不残留上一应用的同名终端/子应用（历史坑：watch 不触发） |
| C3 | 头缺失 | 不带 app-id | 业务报「appId不能为空」（现状约束，前端级联选择器保证必填） |
| C4 | subapp-id 指向不存在子应用 | 头 `subapp-id: ghost` | checkParams 报 subappId 无效 |
| C5 | 无凭证调 @CairoContext 面 | 不带 authorization | 401 |
| C6 | 非 cairo/manage token 调管理面 | 其他应用 token | CairoSecurityInterceptor 拒绝（NOT_SUPPORTED） |

## 二、权限测试面

| # | 场景 | 预期 |
|---|---|---|
| P1 | 无 menu:write 权限的用户调 create_menu | PreAuthorize 拒绝（403） |
| P2 | 非 manage 上下文 token 调 cairo_web_manage 面 | CairoSecurityInterceptor 拒绝（NOT_SUPPORTED） |
| P3 | 操作列按钮 vs 实际 API 权限 | v-allow visible 与 PreAuthorize 集合一致（JSX 必须 hasPermission 回调，v-allow 在 JSX 静默失效——历史坑） |
| P4 | 子应用级用户不存在的目标子应用 | 认证期 loadSubappUserByAppUserId 失败，拒绝 |
| P5 | 登出后凭证/标签/子应用上下文/用户态 | clearLoginTraces 全清（四路：主动登出/过期被踢/无凭证/刷新失败） |
| P6 | 权限变更生效时延 | 刷新页面即重拉生效（内存态缓存设计），不需退出登录 |

## 三、功能测试面

| # | 场景 | 预期 |
|---|---|---|
| F1 | 冷启动（无标签新标签页） | '/' redirect 兜底 /home，不落 404（路由 name 同名覆盖回归） |
| F2 | 切换子应用 | 零菜单/权限请求；菜单树/权限/首页即时切换；跨子应用标签共存 |
| F3 | 菜单 CRUD（含目标子应用） | 建/改/移/删成功；无根子应用首建自动包裹修复（ensureRootMenu 回归） |
| F4 | 菜单名与静态路由重名（如「首页」） | 后建者 name 以 path 兜底，标签/面包屑仍显示菜单名（meta.title） |
| F5 | 嵌套集不变量 | 任意 CRUD 序列后 leftNo 唯一且父区间包含子区间 |
| F6 | 标签页 | 图标=所属菜单 icon（无则圆点）；右键五项；刷新只影响当前标签 |

## 四、审计遗留清单（待裁决/跟进）

- **AuthBadLoginLogHandler 前缀污染**（历史遗留，用户裁决中）
- **CairoContextFilter 为死代码**（从未注册，真实灌入者是 CairoContextInterceptor）——建议删除防误改
- **subapp 头客户端可控**：个人面读作用域随之变化（菜单/权限读取按 holder subapp）。当前授权门=principal 子应用级用户存在性+权限面；若未来个人面出现敏感数据需收敛为 principal 上下文
- 前端遗留：user_group 死菜单、notify record 空壳
