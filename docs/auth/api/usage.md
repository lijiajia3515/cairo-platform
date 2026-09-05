# auth 服务接口调用指南

面向调用方（前端 / 其他微服务 / 外部企业应用）的手册：怎么拿凭证、怎么带请求头、响应长什么样、错误码怎么处理。**端点全量清单**（166 控制器 / 701 端点 + 防护模型）见 [api-surface.md](api-surface.md)，本页不重复枚举，只讲约定与关键流程。

## 一、认证与调用约定

### 1. 获取令牌（OAuth2 token）

所有受保护接口都要求先拿 token。统一走 token 端点（开放，无需凭证）：

```
POST /open_api/oauth2/token
Content-Type: application/x-www-form-urlencoded
```

表单参数：

| 参数 | 说明 |
|---|---|
| `client_id` / `client_secret` | OAuth2 客户端凭证（如管理台 `cairo_web_v1`） |
| `grant_type` | `主体:方式`（见下表） |
| `username` / `password` | 密码类 grant 的用户名/密码 |
| `code` / `token` 等 | 验证码 / SNS 授权码 / 账号 token 等（按 grant 类型） |

`grant_type` 取值（`主体:方式` 格式，定义见 `auth/core` 的 `framework/security/oauth2/core/OAuth*AuthorizationGrantTypes.java`）：

| grant_type | 主体 | 说明 |
|---|---|---|
| `account:password` / `account:verify_code` / `account:sns_code` | 平台账号 | 账号直接登录 |
| `app_user:password` / `app_user:verify_code` / `app_user:account_sns_code` | 应用级用户 | 应用级用户登录（后两种可经账号第三方） |
| `app_user:account_access_token` | 应用级用户 | 账号 token 置换应用级用户 token |
| `tenant_app_user:password` / `tenant_app_user:verify_code` / `tenant_app_user:account_sns_code` | 企业应用级用户 | 企业应用级用户登录 |
| `tenant_app_user:account_access_token` | 企业应用级用户 | 账号/应用级用户 token 置换 |
| `authorization_code` / `client_credentials` / `refresh_token` | — | Spring Security 标准 grant |

响应（`BusinessResult` 包裹，见[响应结构](#二响应结构)），`data` 形如：

```json
{
  "code": "Success",
  "message": "成功",
  "data": { "access_token": "<JWT>", "token_type": "Bearer", "expires_in": 3600 }
}
```

> token 为 JWT（RSA 密钥对多组轮换），`sub` claim = 授权记录 id（即会话唯一标识），主体身份走 `user_id`/`account_id` 等自定义 claim。不透明凭证（如 refresh token 场景的前缀）见 `auth/README.md` 凭证前缀表。

### 2. 携带凭证（Authorization 头）

受保护接口统一用 Authorization 头传 token，**scheme 为四段式**：

```
Authorization: {auth_type} appId/endpointId/subappId/subappVersion/token
```

| 段 | 说明 | 示例 |
|---|---|---|
| `{auth_type}` | 主体类型 | `subapp_user`（管理台）、`app_user`（应用级用户）、`tenant_app_user`（企业应用级用户）… |
| `appId/endpointId/subappId/subappVersion` | 认证上下文（资源作用域） | `cairo/web/manage/v1` |
| `token` | 上一步拿到的 JWT | `<access_token>` |

完整示例（管理台子应用级用户）：

```
Authorization: subapp_user cairo/web/manage/v1/<token>
```

### 3. 上下文头（多应用管理）

管理/业务 API 除 JWT 外还依赖 4 个请求头，指示「操作哪个应用/终端/子应用的数据」（认证域 token 与上下文域解耦——token 固定，靠头切换管理对象）：

| 头 | 说明 |
|---|---|
| `app-id` | 应用标识（如 `cairo`） |
| `endpoint-id` | 终端标识（如 `web`） |
| `subapp-id` | 子应用标识（如 `manage`） |
| `subapp-version` | 子应用版本（如 `v1`） |

> ⚠️ 上下文头切换是**多应用管理设计**，不是越权：安全边界由「token 认证域绑定」+「`@PreAuthorize` 权限点」+「管理台身份门」三层保证（详见 [api-surface.md](api-surface.md#二防护模型三层)），业务按头读写目标应用数据。

### 4. 验证码闸（open 面关键端点）

open 面部分敏感端点（账号枚举 `valid_account_username/phone_number/email`、`get_login_account`、短信发送等）要求先通过验证码，调用时携带：

```
Captcha-Token: <verify_captcha_code 换取的令牌>
```

流程：`POST /open_api/captcha/get_captcha_code` → `POST /open_api/captcha/verify_captcha_code`（获取 `Captcha-Token`）→ 携带该头调目标端点。

## 二、响应结构

所有接口统一返回 `BusinessResult` 包裹（`framework/core/result/BusinessResult.java`）：

```json
{ "code": "Success", "message": "成功", "data": { } }
```

| 字段 | 类型 | 说明 |
|---|---|---|
| `code` | string | 成功恒为 `"Success"`；失败为错误码（见下） |
| `message` | string | 人类可读消息（中英双语，`Accept-Language` 头控制） |
| `data` | object \| null | 业务数据，失败时多为 null |

**错误处理**：判断 `code != "Success"` 即失败，按 `code` 分发。全量错误码清单（17 枚举类 74 码值 + 前端分发策略）见 [error-codes.md](../snapshots/error-codes.md)，常用：

| 码 | 含义 |
|---|---|
| `Success` | 成功 |
| `Auth.Unauthorized` | 必须进行认证（401） |
| `Auth.TokenInvalid` / `Auth.TokenExpired` | 登录错误 / 登录过期 |
| `Auth.Denied` | 权限不足（403） |
| `Params.ValidationFailed` / `Params.Error` | 参数校验失败 / 参数错误 |
| `Request.NotFound` | 资源不存在 |
| `Request.LimitExceeded` | 请求过于频繁（限流） |

## 三、面速查

同一资源按**调用方视角**拆分 Controller，URL 前缀即视角（端点全量清单见 [api-surface.md](api-surface.md)）：

| 面 | URL 前缀 | 调用方 | 认证 |
|---|---|---|---|
| open | `/open_api` | 登录前前端 + 外部应用 | 匿名（关键端点验证码闸） |
| client | `/client_api` | 其他微服务（auth/sdk Feign） | CLIENT 凭证，全量 `@PreAuthorize` |
| cairo_web_manage | `/cairo_web_manage_api` | 本仓运营平台 | `subapp_user`，全量 `@PreAuthorize` |
| subapp | `/subapp_user_api` | 管理台（subappPost） | `subapp_user` |
| app_user | `/app_user_api` | 应用级用户（个人中心） | `app_user`（自助类限定本人） |
| tenant_app_user | `/tenant_app_user_api` | 仓外企业应用 | `tenant_app_user` |
| tenant_subapp | `/tenant_subapp_user_api` | 仓外企业子应用 | `tenant_subapp_user` |
| account | `/account_api` | 账号端 | ACCOUNT |
| 特例 weboffice | `/weboffice/v3/3rd` | WPS 云端 | WPS-2 签名 |
| 特例 misc | `/`（视图）· `/access_file_url`（文件直链） | 浏览器 | — |

## 四、关键流程示例

### 流程 1：验证码 → 密码登录 → 调管理 API

**① 取验证码**（如需要）：

```bash
curl -X POST http://<host>/open_api/captcha/get_captcha_code
```

**② 密码登录拿 token**：

```bash
curl -X POST http://<host>/open_api/oauth2/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=cairo_web_v1&client_secret=cairo_web_v1&grant_type=app_user:password&username=admin&password=123456"
```

**③ 带上下文头调管理 API**（示例：菜单列表）：

```bash
curl http://<host>/cairo_web_manage_api/menu/get_menu_list \
  -H "Authorization: subapp_user cairo/web/manage/v1/<token>" \
  -H "app-id: cairo" -H "endpoint-id: web" -H "subapp-id: manage" -H "subapp-version: v1" \
  -H "Accept-Language: zh-CN"
```

```json
{ "code": "Success", "message": "成功", "data": [ { "menuId": "...", "parentId": "0", "menuName": "首页", "component": "/manage/home", "icon": "/icons/..." } ] }
```

### 流程 2：客户端凭证（微服务间调用）

```bash
curl -X POST http://<host>/open_api/oauth2/token \
  -d "client_id=<client>&client_secret=<secret>&grant_type=client_credentials"
# 之后带 Authorization: client <appId>/<endpointId>/<subappId>/<subappVersion>/<token> 调 client_api 面
```

## 五、防护注意

- **验证码闸**：open 面账号枚举 / 登录账号查询 / 短信发送等端点必须先过验证码，防撞库与短信轰炸；
- **自助类限定本人**：`get_my_*` / `bind_*` / `get_current_*` 等无 `@PreAuthorize` 端点靠类型闸 + `@AuthenticationPrincipal` 限定操作对象为本人，**不能传别人的 id 操作**；
- **上下文头一致性**：管理面读写的数据 = 4 头指向的应用/终端/子应用，切换目标前先确认级联（应用切换会重置终端/子应用/版本）；
- **响应只看 code**：不要用 HTTP 状态码判断业务成败（统一 200 + `BusinessResult.code`）。

## 相关文档

- [api-surface.md](api-surface.md)：端点全量清单 + 三层防护模型（权威）
- [error-codes.md](../snapshots/error-codes.md)：错误码权威清单
- [auth 账号](../../../auth/README.md)：主体模型、认证体系、token 前缀与 principal 格式
