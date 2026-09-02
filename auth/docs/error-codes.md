# auth 错误码清单（权威快照）

> 来源：后端 17 个 `*Business` 枚举类（均实现 `framework/core` 的 `Business` 接口，`code()` + `message()`）。新增码值时同步本表。
> 通用层（framework/core）由 gateway 等其它服务复用；auth 模块码值仅在 auth 服务产生。
> 更新日期：2026-08-30。

## 通用层（framework/core/business/）

| 枚举类 | 码值 | 消息 |
|---|---|---|
| DefaultBusiness | `Success` | 成功 |
| DefaultBusiness | `Conflict` | 出现错误 |
| RequestBusiness | `Request.NotFound` | 资源不存在 |
| RequestBusiness | `Request.SizeLimitExceeded` | 请求payload过大 |
| RequestBusiness | `Request.UriBad` | 请求uri错误 |
| RequestBusiness | `Request.Forbidden` | 请求被拒绝 |
| RequestBusiness | `Request.Timeout` | 请求超时 |
| RequestBusiness | `Request.NotAccepted` | 无法处理请求 |
| RequestBusiness | `Request.NotSupported` | 请求类型不支持 |
| RequestBusiness | `Request.LimitExceeded` | 请求过于频繁 |
| ServiceBusiness | `Service.Error` | 服务端异常,请联系管理员 |
| ServiceBusiness | `Service.NotImplemented` | 服务未实现 |
| ServiceBusiness | `Service.Unavailable` | 服务暂不可用,请稍后重试 |
| ServiceBusiness | `Service.Timeout` | 服务超时,请稍后重试 |
| ServiceBusiness | `Service.NotSupported` | 服务不支持 |
| ParamsBusiness | `Params.Error` | 参数错误 |
| ParamsBusiness | `Params.ValidationFailed` | 参数校验失败 |
| AuthBusiness | `Auth.Error` | 认证错误（CairoAuthBusiness 同码重复定义） |
| AuthBusiness | `Auth.InvalidToken` | 错误凭证 |
| AuthBusiness | `Auth.Denied` | 权限不足（CairoAuthBusiness 同码重复定义） |

## 认证主体（CairoAuthBusiness，auth/core/.../security/）

### 通用认证

| 码值 | 消息 |
|---|---|
| `Auth.Unauthorized` | 必须进行认证 |
| `Auth.TokenInvalid` | 登录错误 |
| `Auth.TokenExpired` | 登录过期 |
| `Auth.PasswordBad` | 密码错误 |
| `Auth.VerifyCodeBad` | 验证码错误 |
| `Auth.SnsCodeBad` | 授权码错误 |
| `Auth.NonceExpired` | Nonce Expired |
| `Auth.NotSupported` | 认证类型不支持 |
| `Auth.Error` | 认证错误 |
| `Auth.Denied` | 权限不足 |

### 主体存在性/状态（NotFound / Locked / Disabled / NotApply）

| 主体 | 码值 |
|---|---|
| 账号 | `Auth.AccountNotFound`、`Auth.AccountLocked`、`Auth.AccountDisabled` |
| 客户端 | `Auth.ClientNotFound`、`Auth.ClientDisabled` |
| 应用 | `Auth.AppNotFound`、`Auth.AppDisabled` |
| 终端 | `Auth.EndpointNotFound`、`Auth.EndpointDisabled` |
| 子应用 | `Auth.SubappNotFound`、`Auth.SubappNotApply`、`Auth.SubappDisabled` |
| 终端用户 | `Auth.AppUserNotFound`、`Auth.AppUserDisabled`（消息为「用户不存在/被禁用」） |
| 企业 | `Auth.TenantNotFound`、`Auth.TenantDisabled` |
| 企业应用 | `Auth.TenantAppNotApply`、`Auth.TenantAppDisabled` |
| 企业终端 | `Auth.TenantEndpointNotApply`、`Auth.TenantEndpointDisabled` |
| 企业子应用 | `Auth.TenantSubappNotApply`、`Auth.TenantSubappDisabled` |
| 企业应用用户 | `Auth.TenantAppUserNotFound`、`Auth.TenantAppUserDisabled`（消息为「用户不存在/被禁用」） |

消息规律：NotFound=「xx不存在」、Disabled=「xx被禁用/已禁用」、NotApply=「xx未开通」、Locked=「账号已锁定」。

## 业务模块（auth 模块内）

| 枚举类 | 码值 | 消息 |
|---|---|---|
| CairoOAuthBusiness | `Auth.OAuthError` | 认证错误（OAuth2 异常统一包装，令牌刷新链路常见） |
| AuthCodeBusiness | `AuthCode.ParamsError` | 认证码参数错误 |
| AuthCodeBusiness | `AuthCode.CodeExpired` | 认证码失效 |
| AuthCodeBusiness | `AuthCode.Bad` | 认证码错误 |
| IdempotentBusiness | `Idempotent.BadToken` | 幂等校验失败 |
| IdempotentBusiness | `Idempotent.RepeatedRequest` | 重复请求 |
| SignBusiness | `Sign.Bad` | 签名参数错误 |
| SignBusiness | `Sign.TimeExpired` | 请求时效过期 |
| SignBusiness | `Sign.RepeatedRequest` | 请求重复 |
| CaptchaTokenBusiness | `CaptchaToken.Bad` | 行为异常 |
| CaptchaCodeBusiness | `CaptchaCode.Bad` | 行为验证码错误 |
| CaptchaCodeBusiness | `CaptchaCode.Expired` | 行为验证码已失效 |
| VerifyCodeBusiness | `VerifyCode.Bad` | 验证码错误 |
| VerifyCodeBusiness | `VerifyCode.Expired` | 验证码已过期 |
| SnsBusiness | `Sns.SnsCodeBad` | 授权码错误（注意与 `Auth.SnsCodeBad` 是两个码） |
| AccountBusiness | `Account.PhoneNumberExists` | 手机号已存在 |
| AccountBusiness | `Account.NotFound` | 账号不存在 |
| TenantAppDepartmentBusiness | `Department.NotExists` | 数据不存在 |
| FileBusiness | `File.UploadFailed` | 文件上传失败 |
| FileBusiness | `File.SignFailed` | 文件签名失败 |

## 前端处理（auth/web/src/api/status.js）

| 错误码 | 前端行为 |
|---|---|
| `Auth.TokenExpired` | 刷新令牌成功则重放原请求；失败清登录态重载 |
| `Auth.OAuthError` | 提示「刷新Token错误」并清登录态 |
| `Auth.Unauthorized` | 提示 + 清登录态 |
| `Auth.TokenInvalid`、`Auth.AccountNotFound/Disabled/Locked`、`Auth.AppUserNotFound/Disabled` | 提示 + 清登录态 |
| `Auth.Denied` | 提示 + reject（不清登录态） |
| `Auth.SnsCodeBad` | 跳转登录页 + 清登录态 |
| `Request.NotFound` | 仅提示「接口请求未找到」 |
| 其余全部 | default 分支：提示 message + reject |

**前端兼容幽灵码（后端从未定义，仅 status.js 存在）**：`Auth.Expired`（`Auth.TokenExpired` 的兼容 fallthrough，仍在生效路径）、`Auth.Bad`、`Auth.Disabled`、`Auth.Locked`、`Auth.OAuth2Error`（纯死分支，后端无对应码）。后端新增同名码前不要复用这些字符串。

## 维护

- 权威源：上述 17 个枚举类；新增/改码值后从源码同步本表（枚举常量 → `code` + `message` 两字段）。
- 刷新链路的码经 `CairoOAuth2ResponseErrorHandler` 把远端 OAuth2 错误翻译回 `CairoAuthBusiness`/`Auth.OAuthError`，故令牌刷新失败可能命中表中任意 Auth.* 码。
