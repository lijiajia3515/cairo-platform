# auth 服务测试用例（安全 / 权限 / 校验 / 合理性 / 边界）

> 将 [test-plan.md](test-plan.md) 的场景矩阵落地为**可执行规格的用例**（每条含 前置/输入/预期）。test-plan.md 仍为安全审计结论的权威源；本页按 安全/权限/校验/合理性/边界 五维组织，用例 ID 交叉引用 C/P/F 场景。
> 用例是行为规格：标注「设计意图」的行为若当前未强制，属待补测试（见 [backlog.md](../plans/backlog.md) 测试域）。
> 判定原则：业务成败看 `BusinessResult.code`（成功= `"Success"`），不依赖 HTTP 状态码。

## 一、安全（SEC）

| ID | 优先级 | 前置 | 输入/步骤 | 预期 |
|---|---|---|---|---|
| SEC-01 | 高 | 未登录 | 不带 `Authorization` 调 `/cairo_web_manage_api/menu/get_menu_list` | `Auth.Unauthorized`（必须认证） |
| SEC-02 | 高 | 已登录 | `Authorization: subapp_user cairo/web/manage/v1/<伪造token>` | `Auth.TokenInvalid`（JWT 签名校验失败） |
| SEC-03 | 高 | token 已过期 | 携带过期 token 调受保护面 | `Auth.TokenExpired` |
| SEC-04 | 高 | 无凭证 | 调 `@CairoContext` 面（不带 authorization） | 401（对应 test-plan C5） |
| SEC-05 | 高 | 未过验证码 | 不带 `Captcha-Token` 调 `get_login_account` / `valid_account_username` 等 | 拒绝（验证码闸，设计意图：防账号枚举） |
| SEC-06 | 高 | 非 WPS 云端 | 调 `/weboffice` 特例面不带 WPS-2 签名 | 拒绝（签名校验失败） |
| SEC-07 | 高 | 已登录管理台 | 上下文头 `subapp-id: ghost`（不存在子应用）调管理面 | `checkParams` 报 subappId 无效（C4） |
| SEC-08 | 高 | 已登录 | 走一遍登录+查询 | 日志/审计不含明文密码与 token（凭证不落日志） |
| SEC-09 | 中 | 已登录 | 登出后复用旧 token 调管理面 | 拒绝（clearLoginTraces 全清，P5） |
| SEC-10 | 中 | 高并发 | 短时间超频请求（触发限流） | `Request.LimitExceeded` |
| SEC-11 | 中 | 已登录 | 头缺失 `app-id` 调管理面 | 业务报「appId不能为空」（C3，前端级联保证必填） |

## 二、权限（PERM）

| ID | 优先级 | 前置 | 输入/步骤 | 预期 |
|---|---|---|---|---|
| PERM-01 | 高 | 无 `menu:write` 权限的账号 | 调 `create_menu` | 403 `Auth.Denied`（@PreAuthorize 拒绝，P1） |
| PERM-02 | 高 | 非 cairo/manage 上下文 token | 调 `/cairo_web_manage_api/*` | CairoSecurityInterceptor 拒绝 NOT_SUPPORTED（P2 / C6） |
| PERM-03 | 高 | CLIENT 凭证 token | 调 `cairo_web_manage` 面 | 拒绝（client 面 token 不能进管理面） |
| PERM-04 | 中 | 自助类端点 | `get_my_*`/`bind_*`/`get_current_*` 传**他人 id** | 操作对象限定本人：返回本人数据或拒绝（不可操作他人） |
| PERM-05 | 中 | 管理台页面 | 操作列按钮可见性 vs 实际 API 权限 | v-allow `hasPermission` 与后端 `@PreAuthorize` 集合一致（P3，JSX 必须 hasPermission 回调） |
| PERM-06 | 高 | 已登录 | 目标子应用级用户不存在（越权目标子应用） | 认证期 loadSubappUserByAppUserId 失败，拒绝（P4） |
| PERM-07 | 中 | 权限刚变更 | 改权限后不退出登录刷新页面 | 立即生效（内存态缓存设计，P6） |
| PERM-08 | 中 | A 企业用户 | 携带 A 企业 token 读 B 企业数据 | 无数据 / 拒绝（企业隔离） |

## 三、校验（VAL）

| ID | 优先级 | 前置 | 输入/步骤 | 预期 |
|---|---|---|---|---|
| VAL-01 | 高 | 已登录管理台 | 创建菜单不传 `menuName` | `Params.ValidationFailed`（必填） |
| VAL-02 | 高 | 已登录管理台 | 传非法枚举值（如 `EndpointType` 不存在值） | 校验失败 |
| VAL-03 | 中 | 已登录 | 传非格式值：非 UUID 的 id / 非法 URL / 非法手机号 | `Params.ValidationFailed` |
| VAL-04 | 中 | 已登录 | 传超长字符串（超字段长度上限） | 校验失败或截断（按字段约定） |
| VAL-05 | 中 | 已登录 | 传字典项不存在的值 | 校验失败（字典值以代码枚举为权威源） |
| VAL-06 | 中 | 已登录 | 嵌套对象缺必填子字段 | `Params.ValidationFailed` |
| VAL-07 | 中 | 已登录 | 类型错误（字符串传给数值字段） | 解析/校验失败 |
| VAL-08 | 低 | 已登录 | 空字符串 vs null（可空字段） | 按字段可空性正确区分（可空字段 null 放行、空串按约定） |

## 四、合理性（REA）

| ID | 优先级 | 前置 | 输入/步骤 | 预期 |
|---|---|---|---|---|
| REA-01 | 高 | 已禁用某应用/子应用 | 用该应用/子应用上下文登录 | 拒绝（状态机：禁用不可用） |
| REA-02 | 高 | 权限点被角色引用 | 删除该权限点 | 拒绝（删除保护：被引用不可删） |
| REA-03 | 高 | 已存在 `(appId, endpointId)` | 重复创建同短值标识（如再建 endpointId=web） | 复合唯一冲突（短值+复合唯一设计） |
| REA-04 | 高 | 菜单树任意状态 | 任意菜单 CRUD 序列后 | 嵌套集不变量：`leftNo` 唯一且父区间包含子区间（F5） |
| REA-05 | 中 | 已登录 | 注销账号流程 | 冷静期/二次确认（注销不是立即删除） |
| REA-06 | 中 | 菜单名与静态路由重名（如「首页」） | 建重名菜单 | 后建者 `name` 以 path 兜底，标签/面包屑仍显示菜单名（F4） |
| REA-07 | 中 | 无根子应用 | 首次创建菜单 | 自动包裹修复 ensureRootMenu（F3） |
| REA-08 | 中 | 密码登录 | 连续错误密码 | 失败计数/锁定（防爆破，设计意图） |

## 五、边界（BND）

| ID | 优先级 | 前置 | 输入/步骤 | 预期 |
|---|---|---|---|---|
| BND-01 | 中 | 已登录 | 分页参数 page=0 / 1 / 超界 / 负 | 归一化或拒绝（不崩、不越界返回脏数据） |
| BND-02 | 中 | 数据为空 | 查询无数据列表 | `data=[]`（空集合，非 null） |
| BND-03 | 中 | 可空字段 | 未填可空字段（如 `metadata.createUserId`） | 正确序列化为 null，不报错 |
| BND-04 | 中 | — | 超大 payload（超 body 上限） | `Request.SizeLimitExceeded` |
| BND-05 | 中 | 并发 | 同父下并发创建同名菜单 | 一方成功、一方冲突（不产生重复脏数据） |
| BND-06 | 高 | 冷启动（无标签新开页） | 访问 `/` | redirect 兜底 `/home`，不落 404（F1，路由 name 同名覆盖回归） |
| BND-07 | 中 | token 将过期 | 恰在过期瞬间/之后调用 | 边界内成功、跨边界 `Auth.TokenExpired` |
| BND-08 | 低 | 已登录 | 超长角色/权限数组 | 正确校验或拒绝，不溢出 |
| BND-09 | 低 | 已登录 | 标识大小写混用（如 appId `Cairo` vs `cairo`） | 按设计保持敏感/不敏感一致（不做静默双标） |

## 覆盖对照

| 本页维度 | test-plan.md 场景 |
|---|---|
| 安全 | C3 / C4 / C5 / P5（登出清凭证） |
| 权限 | P1 / P2 / P3 / P4 / P6 / C6 |
| 合理性 | F3 / F4 / F5 |
| 边界 | F1 |

## 相关文档

- [test-plan.md](test-plan.md)：安全审计结论与场景矩阵（权威源）
- [api-surface.md](../api/api-surface.md)：端点清单与三层防护模型
- [接口调用指南](../api/usage.md)：认证 / 上下文头 / 响应结构约定
- [error-codes.md](../snapshots/error-codes.md)：错误码权威清单
