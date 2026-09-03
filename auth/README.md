# auth 账号

OAuth2 授权服务器（Spring Authorization Server + JWT）与系统功能一体化的账号服务，单服务部署（`cairo-auth`）。多租户 SaaS 的认证、授权与基础系统功能中枢。

## 主体模型

认证体系围绕 6 类主体（Principal）组织，对应多租户层级 `Tenant -订阅-> App -> Endpoint -> Subapp`：

| 主体               | 说明                           |
|--------------------|--------------------------------|
| `Client`           | 服务身份（Client_credentials） |
| `Account`          | 平台账号（个人主体，跨应用）   |
| `AppUser`          | 应用用户（App 维度）           |
| `SubappUser`       | 子应用用户（Token 校验链）     |
| `TenantAppUser`    | 租户应用用户（最细粒度）       |
| `TenantSubappUser` | 租户子应用用户（Token 校验链） |

## 认证体系

认证链分三层（`service` 的 `framework/security/` 与 `core` 的 `framework/security/`）：

**1. 登录认证（`Cairo*AuthenticationProvider`）**--各主体的直接登录认证：

| 主体 \ 方式   | 密码 | 验证码 | SNS | 主体关联登录      |
|---------------|:----:|:------:|:---:|-------------------|
| Account       |  ✓  |   ✓   | ✓  | -                 |
| AppUser       |  ✓  |   ✓   | ✓  | 账号 token 置换   |
| TenantAppUser |  ✓  |   ✓   |  -  | 账号 / AppUser    |

**2. OAuth2 授权认证**--注册在 token 端点（`/oauth2/token`）的**自定义 grant**（`OAuth*AuthenticationProvider` + `*AuthenticationConverter`，`OAuth2ServerConfig` 统一装配）。grant_type 取值为 `主体:方式` 格式，调用方按下表传参：

| grant_type      | Account                         | AppUser                                | TenantAppUser                                                     |
|-----------------|---------------------------------|----------------------------------------|-------------------------------------------------------------------|
| 密码            | `account:password`              | `app_user:password`                    | `tenant_app_user:password`                                        |
| 验证码          | `account:verify_code`           | `app_user:verify_code`                 | `tenant_app_user:verify_code`                                      |
| SNS 授权码      | `account:sns_code`              | `app_user:account_sns_code`            | `tenant_app_user:account_sns_code`                                 |
| 账号 token 置换 | -                               | `app_user:account_access_token`        | `tenant_app_user:account_access_token`                             |
| 刷新令牌        | `account:account_refresh_token` | `app_user:app_user_refresh_token`       | `tenant_app_user:tenant_app_user_refresh_token`                    |

grant 常量定义在 `core` 的 `framework/security/oauth2/core/OAuth*AuthorizationGrantTypes.java`。

**3. Token 校验（`*TokenAuthenticationProvider`，6 个，位于 `core`）**--资源侧 JWT 校验链：Client / Account / AppUser / SubappUser / TenantAppUser / TenantSubappUser。

授权信息（authorization / consent）持久化在 MongoDB（`Mongodb*AuthorizationService`）；token 为 JWT（RSA 密钥对可配置多组轮换，`cairo.auth.oauth2.rsa-keys`），资源侧通过 `JwtAuthenticationConverter` 转换主体。OAuth2 客户端登录回调：`/open_api/oauth2/callback`。

认证错误消息支持中英双语：`service/src/main/resources/i18n/`（默认中文、`zh_CN` 全中文、`en_US` 全英文，按「Spring Security 标准 + Cairo 认证链」分组同序）。

### 认证方法类型常量

项目中认证相关的类型常量分布在以下几处：

**LoginType**（`core` 的 `framework/security/core/LoginType.java`）— 登录方式分类，用于日志与上下文标记：

| 常量          | 值            | 说明         |
|---------------|---------------|--------------|
| `PASSWORD`    | `password`    | 密码登录     |
| `VERIFY_CODE` | `verify_code` | 验证码登录   |
| `SNS`         | `sns`         | 第三方登录   |
| `ACCOUNT`     | `account`     | 账号关联登录 |
| `UNKNOWN`     | `unknown`     | 未知         |

**AuthenticationType**（`core` 的 `framework/security/oauth2/authentication/AuthenticationType.java`）— OAuth2 认证主体类型：

| 常量               | 值                 |
|--------------------|--------------------|
| `CLIENT`           | `client`           |
| `ACCOUNT`          | `account`          |
| `APP_USER`         | `app_user`         |
| `TENANT_APP_USER`  | `tenant_app_user`  |

**ClientAuthenticationMethod**（Spring Security OAuth2 标准类，项目无自定义常量）— 客户端认证方式，值由 `auth_client.clientAuthenticationMethods` 数组存储，通过 `new ClientAuthenticationMethod(stringValue)` 构造。常用值：`client_secret_basic`、`client_secret_post`、`none`。

**OAuth Grant Type 常量**（`core` 的 `framework/security/oauth2/core/OAuth*AuthorizationGrantTypes.java`）— 自定义 `AuthorizationGrantType` 实例，格式为 `主体:方式`：

| 接口                                    | 常量数 | grant_type 前缀    |
|-----------------------------------------|--------|--------------------|
| `OAuthAccountAuthorizationGrantTypes`    | 4      | `account:`         |
| `OAuthAppUserAuthorizationGrantTypes`    | 6      | `app_user:`        |
| `OAuthTenantAppUserAuthorizationTypes`   | 6      | `tenant_app_user:` |

此外 Spring Security 标准 grant：`authorization_code`、`client_credentials`、`refresh_token`。

### 凭证与标识规范

**不透明凭证值前缀**（`TokenKeyGenerator`，前缀见名知意，日志/Redis 键中一眼可辨归属域）：

| 凭证 | 前缀 | 说明 |
|---|---|---|
| 应用用户 AccessToken / RefreshToken | `app_user_at_` / `app_user_rt_` | REFERENCE 格式 |
| 租户应用用户 AccessToken / RefreshToken | `tenant_app_user_at_` / `tenant_app_user_rt_` | REFERENCE 格式 |
| 账号 AccessToken / RefreshToken | `account_at_` / `account_rt_` | REFERENCE 格式 |
| 授权码 / 验证码凭证 | `auth_code_` / `captcha_` | 一次性凭证 |

JWT（SELF_CONTAINED 格式）为标准 JWS 三段式；`sub` claim = 授权记录 id，主体身份走 `user_id`/`account_id` 等自定义 claim。

**principal 四段式**（前置未认证 token 的 `getPrincipal()`，`认证主体:scope:认证方式:唯一标识`）：

- 主体：`account`（无 scope 段）/ `app_user` / `tenant_app_user`
- scope：应用用户带 `appId:endpointId:clientId`，租户应用用户再加 `tenantId` 前置
- 方式：`password` / `verify_code` / `sns_code` / `account` / `user`
- 标识取真唯一值：username / phoneNumber / accountId / userId；SNS 流程认证前无用户标识，用 `snsType_snsProviderId_snsCode` 复合

**ID 生成**：实体主键统一 `CoreConstants.nextIdStr()`（`framework/core`）= RFC 9562 **UUIDv7**（JUG 库 `timeBasedEpochGenerator`，毫秒时间戳前缀、同毫秒单调递增、免协调），字符串排序即创建时间序。角色/权限等 `sort` 字段默认值为毫秒时间戳（Long）。

**会话标识**：一次登录 = 一条授权记录，其 id（授权记录主键，即 JWT `sub`）即会话唯一标识；账号密码表单登录链路的会话标识为 `CairoAuthAccount.id`（`account_` + UUIDv7）。会话管理页展示与下线均以此为准。

## core 框架组件

`core` 的 `framework/` 下除安全链外还提供：

| 组件         | 说明                                            |
|--------------|-------------------------------------------------|
| `context`    | 请求上下文（主体 / 应用 / 租户信息透传）        |
| `idempotent` | 接口幂等                                        |
| `sign`       | 请求签名校验                                    |
| `lock4j`     | 分布式锁（Redisson，key 按主体维度构建）        |
| `auth_code`  | 授权码                                          |
| `sns`        | SNS 第三方登录                                  |
| `audit`      | 审计                                            |
| `mongodb`    | 集合命名策略与元数据（对应 framework 的发号器） |
| `feign`      | 内部服务调用适配                                |

## API 层

Controller 按「**资源 + 调用方视角**」命名（如 `TenantAppUserTenantAppApiController` = 租户应用用户资源、企业应用视角），同一资源对不同视角暴露不同 Controller。URL 前缀即视角（下表为控制器数，端点全量清单 164 控制器 / 698 端点见 [api-surface.md](../docs/auth/api-surface.md)）：

| URL 前缀（主体面）         | 控制器 | 调用方                                               |
|----------------------------|-------:|------------------------------------------------------|
| `/client_api`              |     45 | 客户端管理端（Client）                               |
| `/cairo_web_manage_api`    |     36 | 运营平台（web-console）                              |
| `/subapp_user_api`         |     22 | 子应用管理端（Subapp）                               |
| `/tenant_subapp_user_api`  |     17 | 企业子应用应用端                                     |
| `/app_user_api`            |     13 | 应用用户端（AppUser）                                |
| `/tenant_app_user_api`     |     13 | 企业应用用户端                                       |
| `/open_api`                |     12 | 开放接口（验证码、captcha、SNS、OAuth2 回调、区划等）|
| `/account_api`             |      5 | 账号端                                               |

特例面：`weboffice`（WPS-2 签名，1 控制器 / 11 端点）与 `/`（IndexController 视图）。文件直链访问不是独立前缀——`/access_file_url` 是 7 个文件类控制器共用的方法级子路径。

## 子应用结构

```
auth/
├── core/                  # 框架层（见上节组件表）
├── dependencies/          # BOM：auth 全部子子应用的版本约束
├── domain/                # 业务领域层（DTO / Service / Repository / QueueHandler）
│   ├── core/              #   核心：公共实体、枚举、工具
│   ├── account/           #   账号域：注册、登录、注销、密码、SNS 绑定
│   ├── app/               #   应用域：App / Endpoint / Subapp / AppUser / AppRole
│   ├── client/            #   客户端域：Client 管理
│   ├── open/              #   开放接口域：第三方 Open API
│   ├── tenant-app/        #   企业应用域：TenantApp / TenantEndpoint / TenantSubapp
│   │                      #     / TenantAppUser / TenantAppRole / 登录日志 / 业务日志
│   └── web/               #   Web 运营平台域：菜单、字典、短信、文件、区划、通知等
├── sdk/                   # Feign Client 层（供其他微服务调用）
│   ├── core/              #   核心 SDK：认证、签名
│   └── client/            #   Client 凭证 SDK（服务间调用 client_api 面）
├── service/               # Spring Boot 主应用
│   ├── api/               #   Controller 层（8 个主体面前缀 + weboffice 特例面，见上节）
│   ├── framework/         #   安全框架（认证 Provider / OAuth2 / web 适配）
│   ├── config/            #   Rabbit / Redis / Security / MinIO / SMS / WxMp 等配置
│   └── modules/           #   业务子应用（45 个，见下表）
└── starter/service/       # 自动装配（供其他服务引入 auth starter）
```

**service 的 45 个业务子应用**：

| 类别      | 子应用                                                                                                                                                                                                                                                                                                                   |
|-----------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 认证相关  | `auth_code`、`captcha`、`oauth2`、`sns`、`sns_provider`、`login_log`                                                                                                                                                                                                                                                   |
| 租户/RBAC | `tenant`、`tenant_app`、`tenant_app_user`、`tenant_app_role`、`tenant_app_role_template`、`tenant_app_department`、`tenant_app_department_template`、`tenant_app_user_template`、`tenant_app_user_tag`、`tenant_app_endpoint`、`tenant_subapp`、`tenant_app_user_authorization`、`tenant_subapp_biz_log` 等 |
| 应用/RBAC | `app`、`app_user`、`app_role`、`app_department`、`app_user_tag`、`app_endpoint`、`app_release`、`subapp`、`subapp_version`、`menu`、`permission`、`account_authorization`、`app_user_authorization` 等                                                                     |
| 账号      | `account`、`biz_log`（多主体业务日志）、`link`                                                                                                                                                                                                                                                                         |
| 系统服务  | `file`（MinIO）、`imgproxy`、`weboffice`、`sms`、`notify`、`wxmp`（微信公众号）、`dict`、`area`（行政区划）、`ip2region`、`utils`                                                                                                                                                                  |

## 数据模型

MongoDB 集合 71 个，统一 `auth_` 前缀（初始化脚本见 `docs/auth/db/`），按主体与域组织：`auth_account*`、`auth_client*`、`auth_app*`、`auth_endpoint`、`auth_subapp*`、`auth_tenant_app*`、`auth_tenant_endpoint`、`auth_menu`、`auth_permission`、`auth_sys_dict`、`auth_sms_*`、`auth_biz_log_*`、`auth_wxmp_*` 等。

### 标识与唯一性（短值 + 复合键）

业务标识不追求全局唯一，按层级作用域取**短值**，唯一性由复合唯一索引保证：

| 实体     | 标识示例                        | 唯一索引                                            |
|----------|---------------------------------|-----------------------------------------------------|
| Endpoint | `web`（App `cairo` 下）         | `(appId, endpointId)`                               |
| Subapp   | `manage`（cairo/web 下）        | `(appId, endpointId, subappId)`                     |
| Subapp 版本 | `v1`                         | `(appId, endpointId, subappId, subappVersion)`      |

前提约定：所有携带这些标识的集合**同时存有上层标识**（如 endpointId 处处与 appId 共存），代码中不存在脱离上层的单独按子标识查询。

### 权限模型（双层）

`auth_permission`（功能权限点）同时承载两层语义：

| 层 | 字段            | 格式             | 消费方                                   |
|----|-----------------|------------------|------------------------------------------|
| UI | `permissionId`  | `资源.动作`（点）| 前端 `v-allow` 指令直接绑定（`src/plugins/permission.js`） |
| 服务 | `authorities` | `资源:动作`（冒号）| `@PreAuthorize("hasAnyAuthority(...)")` |

菜单（`auth_menu`）挂权限点，角色绑定权限点后展开为用户 authorities。运营平台身份要求 `subappId == cairo.security.manage-subapp-id`（默认 `manage`）。

### 上下文头与鉴权头

管理/业务 API 除 JWT 外还依赖请求上下文头（`CairoContextFilter` 解析）：`app-id`、`endpoint-id`、`subapp-id`、`subapp-version`。Authorization 头 scheme：`{auth_type} appId/endpointId/subappId/subappVersion/token`。

### 系统字典规范

- DictId 与实体命名一致（`EndpointType`/`EndpointScope`/`EndpointTag`/`SubappTag`…），前端 `useDict(id)` 直接引用
- **字典项值以代码枚举为权威源**（如 `EndpointType` 枚举 6 值）；基线快照见 `docs/auth/db/data/sys_dict*.json`
- 废弃字典直接从基线快照移除

### 基线数据

`docs/auth/db/data/` 为测试库核心集合的数据快照（endpoint / subapp / subapp_version / menu / permission / sys_dict(_item)），可读版见 `docs/auth/menus.md` 与 `docs/auth/dict.md`。菜单/权限注入走 manage API（服务端计算嵌套集左右值）。

## 消息拓扑（RabbitMQ）

交换机声明集中在 `service` 的 `config/RabbitConfig.java`，实际交换机名由 `cairo.rabbitmq.exchange.*` 配置映射：

| 交换机 bean       | 配置名    | 默认值       | 类型           |
|-------------------|-----------|--------------|----------------|
| `cairoAuthExchange` | `auth`    | `cairo_auth` | Topic（durable） |
| `bizLogExchange`    | `biz_log` | `biz_log`    | Topic（durable） |

队列/路由枚举：`CairoAuthRabbitmqExchange`、`CairoAuthRabbitmqRouteKey`、`CairoAuthQueue`（位于 `domain/core` 与 `core`）。各子应用队列声明在 `modules/*/message/*QueueConfig.java`（业务队列 + 绑定两件套），队列名为持久化（`QueueBuilder.durable`），通过 `BindingBuilder` 绑定到对应交换机。

## 配置

配置文件在 `service/src/main/resources/config/`（`bootstrap.yaml` + `application.yaml` + `application-example.yaml`），全部从本地文件加载——**不走 Consul**（`bootstrap.yaml` 中 `consul.config.enabled: false`，Consul 仅作服务注册 / 发现，默认地址 `consul:8500`，本地需以 `--spring.cloud.consul.host` 覆盖；gateway 例外，其 `application.yaml` 开启了 Consul 配置中心 + watch）。`application-example.yaml` 为脱敏样例：可直接以 `--spring.profiles.active=example` 启动（敏感项按文件内 `${ENV:}` 清单注入环境变量），或复制为 `application-local.yaml` 填真实值（已被 `.gitignore` 排除，严禁提交凭证）。OAuth2 签名密钥对见 `config/oauth-jwk/README.md`。关键配置段：

```yaml
cairo:
  auth:
    oauth2:
      issuer: http://cairo-auth            # JWT issuer
      rsa-keys: [ ... ]                    # RSA 密钥对（支持多组，id 标识）
    auto-register: true                    # SNS 首登自动注册
    client: { client-registration-id: cairo-auth-service }
  security:
    cairo-app-id: cairo                    # 平台自身 App 标识
    portal-app-id: portal                  # 门户 App 标识
  redis:
    key-prefix: "cairo:${spring.application.name}:"
```

## 对外输出

| 形态          | 坐标                                                                   | 说明                   |
|---------------|------------------------------------------------------------------------|------------------------|
| domain（DTO） | `io.github.lijiajia3515.cairo.auth.domain:cairo-auth-domain-*`         | 领域模型，供服务间契约 |
| sdk（Feign）  | `io.github.lijiajia3515.cairo.auth.sdk:cairo-auth-sdk-*`               | Feign Client           |
| starter       | `io.github.lijiajia3515.cairo.auth.starter:cairo-auth-starter-service` | 自动装配               |
| BOM           | `io.github.lijiajia3515.cairo.auth:cairo-auth-dependencies`            | 版本约束               |

## 构建

```bash
./gradlew :auth:service:bootJar    # 打包服务
./gradlew :auth:service:test       # 测试
./gradlew :auth:core:publish       # 发布某个子子应用（凭证在 ~/.gradle/gradle.properties）
```

构建约定与版本目录见仓库根 [README 的「构建架构」](../README.md#构建架构)。

## 相关文档

| 文档                                             | 内容                                     |
|--------------------------------------------------|------------------------------------------|
| [平台总览](../README.md)                         | 架构、快速开始、技术栈、开发规范、文档导航 |
| [文档与运维脚本](../docs/auth/README.md)                 | db/ 权威源、初始化 / 导入脚本、基线数据、迁移记录 |
| [API 面基线](../docs/auth/api-surface.md)                | 8 主体面 + 2 特例面全量端点清单 + 防护模型 |
| [菜单与权限快照](../docs/auth/menus.md)                  | 45 菜单 / 169 权限点可读版                |
| [错误码清单](../docs/auth/error-codes.md)                | 17 枚举 74 码值 + 前端分发处理            |
| [运营平台前端](web/README.md)                     | 运营平台前端环境、命令、API 层结构      |
