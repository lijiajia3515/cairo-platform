# 系统级字典（基线快照）

> 来源：测试库基线导出。字典项值以新系统代码枚举为权威源，DictId 与前端 useDict 引用一致。

| 字典 ID | 名称 | 项数 | 值清单 |
|---|---|---|---|
| AccessScope | 准入范围 | 3 | `public`=开放、`app`=平台、`tenant`=企业 |
| AccountAuthType | 账号认证方式 | 2 | `sso`=单点登录、`oauth2`=OAuth2 |
| AppReleaseType | 应用发行类型 | 3 | `web`=web端、`android`=安卓、`ios`=ios |
| AuthenticationType | 身份类型 | 6 | `account`=账号、`client`=客户端、`app_user`=应用用户、`subapp_user`=子应用用户、`tenant_app_user`=企业应用用户、`tenant_subapp_user`=企业子应用用户 |
| AuthorizationGrantType | 认证授权类型 | 18 | `authorization_code`=授权码模式、`client_credentials`=客户端模式、`refresh_token`=刷新令牌、`implicit`=简化模式(废弃)、`account:password`=账号-密码模式、`account:verify_code`=账号-验证码模式、`account:sns_code`=账号-第三方登录(授权码)、`account:account_refresh_token`=账号-账号刷新令牌模式、`app_user:password`=应用用户-密码模式、`app_user:verify_code`=应用用户-验证码模式、`app_user:account_sns_code`=应用用户-账号第三方登录(授权码)模式、`app_user:account_access_token`=应用用户-账号授权模式、`app_user:app_user_refresh_token`=应用用户-应用用户刷新令牌模式、`tenant_app_user:password`=企业应用用户-密码模式、`tenant_app_user:verify_code`=企业应用用户-验证码模式、`tenant_app_user:account_sns_code`=企业应用用户-账号第三方登录(授权码)模式、`tenant_app_user:account_access_token`=企业应用用户-账号授权模式、`tenant_app_user:tenant_app_user_refresh_token`=企业应用用户-企业应用用户刷新令牌模式 |
| EndpointType | 终端类型 | 6 | `web`=网页、`universal_app`=通用手机应用、`android`=安卓、`ios`=IOS、`mini_app`=小程序、`biz`=h5端 |
| LoginType | 登录方式 | 5 | `password`=密码登录、`verify_code`=验证码登录、`sns`=第三方登录、`account`=账号登录、`unknown`=未知 |
| SnsPartner | 第三方认证厂商 | 2 | `default`=默认、`wx`=微信 |
| SnsType | 第三方认证类型 | 3 | `wx_web`=微信开放用户、`wx_mp`=微信公众用户、`wx_ma`=微信小程序用户 |
