# auth 服务 API 面基线

> 生成于 2026-08-30,基线对应 commit c25cbef5(P0 安全补口之后)。
> 附录清单由 `gen-api-surface.py` 从源码提取,重生成:
> `cd auth/service/src/main/java && python3 ../../../../docs/gen-api-surface.py /tmp/inventory.md`
> 收敛计划(未实施)见 [api-convergence-plan.md](../plans/api-convergence-plan.md)。

## 一、总览

auth 服务对外暴露 **8 个主体面 + 2 个特例面**,166 Controller / 701 端点(api 目录 70,194 行)。

| 面 | URL 前缀 | Ctrl | 端点 | 主体类型 | 方法级防护 | 消费方 |
|---|---|---|---|---|---|---|
| open | `/open_api` | 13 | 36 | 匿名(整体 ignore) | 按设计匿名;关键端点验证码闸 | 管理台 FE 登录前 + 外部应用 |
| client | `/client_api` | 45 | 123 | CLIENT 凭证 | **123/123 @PreAuthorize** | auth/sdk Feign(其他微服务) |
| cairo_web_manage | `/cairo_web_manage_api` | 36 | 181 | CAIRO_WEB_MANAGE_USER | **181/181 @PreAuthorize** | 本仓管理台 FE |
| subapp | `/subapp_user_api` | 22 | 162 | SUBAPP_USER | 161/162(余 1 为自助类) | 管理台 FE(subappPost) |
| endpoint | `/app_user_api` | 13 | 31 | APP_USER | 23/31(余 8 为自助类) | 管理台 FE(appUser) |
| tenant_endpoint | `/tenant_app_user_api` | 13 | 32 | TENANT_APP_USER | 28/32(余 4 为自助类) | 仓外企业应用 |
| tenant_subapp | `/tenant_subapp_user_api` | 17 | 114 | TENANT_SUBAPP_USER | 114/114 | 仓外企业子应用 |
| account | `/account_api` | 5 | 8 | ACCOUNT | 7/8 | **仓内无消费方(待拍板)** |
| 特例 weboffice | `/weboffice/v3/3rd` | 1 | 11 | WPS-2 签名 | 自签名体系(见审计) | WPS 云端 |
| 特例 misc | `/`(IndexController)+ `/access_file_url`×7 | 8 | 10 | — | 视图/文件直链 | 浏览器 |

"自助类"= get_my_* / bind_* / get_current_* 等无 @PreAuthorize 端点:受类型闸 +
`@AuthenticationPrincipal` 限定操作对象为本人,属可接受的"登录即可自助"模式。

## 二、防护模型(三层)

1. **URL 层不设防**——两条资源服务器链均 `anyRequest().permitAll()`
   (`OAuth2WebServerConfig` / `OAuth2ClientServerConfig`);`/open_api/**` 在
   `SecurityConfig#webSecurityCustomizer` 整体 ignore。**授权完全依赖下两层。**
2. **@CairoSecurity 类型闸**(`CairoSecurityInterceptor`)——未登录 401 +
   主体类型必须匹配面类型(如 CLIENT 凭证进不了 cairo_web_manage 面)。
   8 个 typed 面全部 controller 覆盖(逐文件验证)。CAIRO_WEB_MANAGE_USER 额外
   校验 appId=cairo 平台 + subappId=manage 子应用。
3. **@PreAuthorize 权限点**——`资源:动作` 细粒度授权,管理面 100% 覆盖;
   open 面关键端点(登录账号查询、短信发送、可用性查询)以 `@VerifyCaptchaToken`
   图形验证码闸替代(`Captcha-Token` 头,Redis 令牌,IP 绑定)。

## 三、P0 安全补口记录(commit c25cbef5)

| 项 | 处置 |
|---|---|
| A1 账号枚举 | `valid_account_username/phone_number/email` 加 `@VerifyCaptchaToken`。**外部契约变化**:仓外调用方须携带 `Captcha-Token` 头(同 `get_login_account` 既有要求);仓内 FE 不调用这三个端点,不受影响 |
| A1 重置密码 | `reset_account_password_by_phone_number` 维持现状:已受短信验证码闸保护(发送步 `send_verify_code_sms` 有验证码),FE 忘记密码流程无需改动 |
| A2 登录后短信 | 复核撤销:两个 `send_my_account_phone_number_verify_code` 已有 `@VerifyCaptchaToken`(初版分析只扫 @PreAuthorize 漏计) |
| A4 测试端点 | 删除 `/account_api/test`、`/app_user_api/test`、`/subapp_user_api/test` 三组 controller+ApiService(仅 get_area_page_list 测试桩) |
| A5 登录视图 | IndexController `@RestController`→`@Controller`:此前返回字面字符串而非 `templates/{index,login,logout}.html`,OAuth2 formLogin 跳转 `/login` 得不到登录页 |

## 四、WebOffice 面审计

**机制**(`VerifyWebOfficeSignInterceptor`):WPS-2 签名——
`Authorization: WPS-2:{appid}:{SHA1(appSecret + Content-Md5 + ContentType + Date)}`,
共享密钥参与摘要,请求体 MD5 覆盖。

**结论**:算法符合 WPS WebOffice v3 官方对接规范,调用方为 WPS 云端,暴露面有限,风险评级**中**。

**缺口与加固建议**(未实施,列入收敛计划 P2 备选):
- `Date` 头只参与签名、**不校验新鲜度** → 已捕获的合法请求可无限重放;
  建议校验 `|Date - 服务器时间| ≤ 5 分钟`。
- SHA1(secret+data) 前缀 MAC 为弱构造(离线暴力 appSecret 可行),
  官方规范如此,依赖密钥强度;密钥轮换纳入凭据治理。

## 五、已裁决问题(2026-08-30 拍板)

1. **account 面(`/account_api`,5 Ctrl/8 端点)有仓外存量调用** → 保留整面,
   不做 P3 裁撤(仓内零消费方仅为仓外调用所致)。
2. **tenant 两面(`/tenant_app_user_api`、`/tenant_subapp_user_api`)为已交付企业版能力**
   → 镜像面维持现状不合并,逻辑重复靠 P2 共享 CommonService 消除。

## 附录:全量端点清单
### open 面 — `open_api` 主体类型 `匿名(SecurityConfig 整体 ignore)`

Controller 13 个,端点 36 个。

| Controller | 端点 | 方法级防护 |
|---|---|---|
| account/AccountOpenApiController | `POST /valid_account_username` | 验证码 |
| account/AccountOpenApiController | `POST /valid_account_phone_number` | 验证码 |
| account/AccountOpenApiController | `POST /valid_account_email` | 验证码 |
| account/AccountOpenApiController | `POST /register_account` | **无** |
| account/AccountOpenApiController | `POST /logoff_account` | **无** |
| account/AccountOpenApiController | `POST /reset_account_password_by_phone_number` | **无** |
| account/AccountOpenApiController | `POST /get_login_account` | 验证码 |
| app_release/AppReleaseOpenApiController | `POST /get_latest_release_web` | **无** |
| app_release/AppReleaseOpenApiController | `POST /get_latest_release_android` | **无** |
| app_release/AppReleaseOpenApiController | `POST /get_latest_release_ios` | **无** |
| app_release/AppReleaseOpenApiController | `POST /get_latest_preview_web` | **无** |
| app_release/AppReleaseOpenApiController | `POST /get_latest_preview_android` | **无** |
| app_release/AppReleaseOpenApiController | `POST /get_latest_preview_ios` | **无** |
| app_release/AppReleaseOpenApiController | `POST /check_for_updates_android` | **无** |
| app_release/AppReleaseOpenApiController | `POST /check_for_updates_ios` | **无** |
| app_release/AppReleaseOpenApiController | `POST /get_current_app_release_page_list` | **无** |
| app_user/AppUserOpenApiController | `POST /register_app_user` | **无** |
| app_user/AppUserOpenApiController | `POST /logoff_app_user` | **无** |
| area/AreaOpenApiController | `POST /get_area_list` | **无** |
| area/AreaOpenApiController | `POST /get_city_list` | **无** |
| area/AreaOpenApiController | `POST /get_area_tree_list` | **无** |
| area/AreaOpenApiController | `POST /get_area_detail` | **无** |
| captcha/CaptchaOpenApiController | `POST /get_captcha_code` | **无** |
| captcha/CaptchaOpenApiController | `POST /verify_captcha_code` | **无** |
| link/LinkOpenApiController | `GET /access/{linkId}` | **无** |
| oauth2/OAuth2CallbackOpenApiController | `GET /callback` | **无** |
| sns/SnsOpenApiController | `POST /get_sns_info` | **无** |
| sns/SnsOpenApiController | `POST /get_sns_token` | **无** |
| sns/SnsOpenApiController | `POST /verify_sns_token` | **无** |
| sns/SnsOpenApiController | `POST /get_phone_number` | **无** |
| sns_provider/SnsProviderOpenApiController | `POST /get_sns_provider_list` | **无** |
| tenant/TenantOpenApiController | `POST /get_tenant_by_tenant_name` | **无** |
| tenant/TenantOpenApiController | `POST /get_tenant_by_tenant_alias_name` | **无** |
| tenant_app_user/TenantAppUserOpenApiController | `POST /register_tenant_app_user` | **无** |
| tenant_app_user/TenantAppUserOpenApiController | `POST /logoff_tenant_app_user` | **无** |
| verify_code/VerifyCodeOpenApiController | `POST /send_verify_code_sms` | 验证码 |

### client 面 — `client_api` 主体类型 `CLIENT 凭证(服务间)`

Controller 45 个,端点 123 个。

| Controller | 端点 | 方法级防护 |
|---|---|---|
| account/AccountClientApiController | `POST /get_account_auth` | `hasAnyAuthority('account:all', 'account:account_auth')` |
| account/AccountClientApiController | `POST /get_account_info` | `hasAnyAuthority('account:all', 'account:read')` |
| account/AccountClientApiController | `POST /get_account_list` | `hasAnyAuthority('account:all', 'account:read')` |
| account/AccountClientApiController | `POST /get_account_page_list` | `hasAnyAuthority('account:all', 'account:read')` |
| account/AccountClientApiController | `POST /search_account_info` | `hasAnyAuthority('account:all', 'account:read')` |
| account/AccountClientApiController | `POST /create_account` | `hasAnyAuthority('account:all', 'account:create_account')` |
| account/AccountClientApiController | `POST /modify_account_username` | `hasAnyAuthority('account:all', 'account:modify_account_username')` |
| account/AccountClientApiController | `POST /modify_account_phone_number` | `hasAnyAuthority('account:all', 'account:modify_account_phone_number')` |
| account/AccountClientApiController | `POST /get_account_password_status` | `hasAnyAuthority('account:all', 'account:account_password_status')` |
| account/AccountClientApiController | `POST /modify_password` | `hasAnyAuthority('account:all', 'account:modify_account_password')` |
| account/AccountClientApiController | `POST /modify_account_avatar` | `hasAnyAuthority('account:all', 'account:modify_account_avatar')` |
| account_authorization/AccountAuthorizationClientApiController | `POST /get_account_authorization` | `hasAnyAuthority('account_authorization:all', 'account_authorization:get_account_authorization')` |
| account_sns/AccountSnsClientApiController | `POST /get_account_sns_map` | `hasAnyAuthority('account_sns:all', 'account_sns:read')` |
| account_sns/AccountSnsClientApiController | `POST /get_account_sns_list` | `hasAnyAuthority('account_sns:all', 'account_sns:read')` |
| account_sns/AccountSnsClientApiController | `POST /bind_account_sns` | `hasAnyAuthority('account_sns:all', 'account_sns:bind')` |
| account_sns/AccountSnsClientApiController | `POST /unbind_account_sns` | `hasAnyAuthority('account_sns:all', 'account_sns:unbind')` |
| app/AppClientApiController | `POST /get_app_list` | `hasAnyAuthority('app:all', 'app:read')` |
| app/AppClientApiController | `POST /get_app_page_list` | `hasAnyAuthority('app:all', 'app:read')` |
| app_department/AppDepartmentClientApiController | `POST /get_app_department_list` | `hasAnyAuthority('app_department:all', 'app_department:read')` |
| app_department/AppDepartmentClientApiController | `POST /get_app_department_page_list` | `hasAnyAuthority('app_department:all', 'app_department:read')` |
| app_doc/AppDocClientApiController | `POST /get_preview_app_doc_token` | `hasAnyAuthority('app_doc:all', 'app_doc:preview')` |
| app_doc/AppDocClientApiController | `POST /get_edit_app_doc_token` | `hasAnyAuthority('app_doc:all', 'app_doc:edit')` |
| app_role/AppRoleClientApiController | `POST /get_app_role_list` | `hasAnyAuthority('role:all', 'role:read')` |
| app_role/AppRoleClientApiController | `POST /get_app_role_page_list` | `hasAnyAuthority('role:all', 'role:read')` |
| app_user_authorization/AppUserAuthorizationClientApiController | `POST /get_app_user_authorization` | `hasAnyAuthority('app_user_authorization:all', 'app_user_authorization:get_app_user_authorization')` |
| area/AreaClientApiController | `POST /get_area_list` | `hasAnyAuthority('area:all', 'area:read')` |
| area/AreaClientApiController | `POST /get_area_detail` | `hasAnyAuthority('area:all', 'area:read')` |
| area/AreaClientApiController | `POST /get_area_detail_map` | `hasAnyAuthority('area:all', 'area:read')` |
| auth_code/AuthCodeClientApiController | `POST /verify_auth_code` | `hasAnyAuthority('auth_code:all', 'auth_code:verify_auth_code')` |
| captcha/CaptchaClientApiController | `POST /verify_captcha_token` | `hasAnyAuthority('captcha:all', 'captcha:verify_token')` |
| client/ClientClientApiController | `POST /get_basic_client_list` | `hasAnyAuthority('client:all', 'client:read')` |
| client/ClientClientApiController | `POST /get_client_list` | `hasAnyAuthority('app_admin', 'client:all', 'client:read')` |
| dict/biz/BizDictClientApiController | `POST /get_biz_dict_item_map` | `hasAnyAuthority('biz_dict:all', 'biz_dict:read')` |
| dict/biz/BizDictClientApiController | `POST /get_biz_dict_item_id_map` | `hasAnyAuthority('biz_dict:all', 'biz_dict:read')` |
| dict/biz/BizDictClientApiController | `POST /get_path_biz_dict_item_id_map` | `hasAnyAuthority('biz_dict:all', 'biz_dict:read')` |
| dict/biz/BizDictClientApiController | `POST /get_path_biz_dict_item_map` | `hasAnyAuthority('biz_dict:all', 'biz_dict:read')` |
| dict/biz/BizDictClientApiController | `POST /get_biz_dict_detail_info` | `hasAnyAuthority('biz_dict:all', 'biz_dict:read')` |
| dict/system/SystemDictClientApiController | `POST /get_sys_dict_item_map` | `hasAnyAuthority('sys_dict:all', 'sys_dict:read')` |
| dict/system/SystemDictClientApiController | `POST /get_sys_dict_item_id_map` | `hasAnyAuthority('sys_dict:all', 'sys_dict:read')` |
| dict/system/SystemDictClientApiController | `POST /get_sys_dict_detail_info` | `hasAnyAuthority('sys_dict:all', 'sys_dict:read')` |
| dict/system/SystemDictClientApiController | `POST /get_path_sys_dict_item_id_map` | `hasAnyAuthority('sys_dict:all', 'sys_dict:read')` |
| dict/system/SystemDictClientApiController | `POST /get_path_sys_dict_item_map` | `hasAnyAuthority('sys_dict:all', 'sys_dict:read')` |
| dict/system/SystemDictClientApiController | `POST /get_sys_dict_sub_item_list` | `hasAnyAuthority('sys_dict:all', 'sys_dict:read')` |
| endpoint/EndpointClientApiController | `POST /get_endpoint_list` | `hasAnyAuthority('endpoint:all', 'endpoint:read')` |
| endpoint/EndpointClientApiController | `POST /get_endpoint_page_list` | `hasAnyAuthority('endpoint:all', 'endpoint:read')` |
| endpoint/EndpointClientApiController | `POST /get_endpoint_list_by_app` | `hasAnyAuthority('endpoint:all', 'endpoint:read')` |
| file/app_file/AppFileClientApiController | `POST /access_file` | `hasAnyAuthority('app_file:all', 'app_file:access_file')` |
| file/app_file/AppFileClientApiController | `POST /get_file_stat` | `hasAnyAuthority('app_file:all', 'app_file:get_file_stat')` |
| file/app_file/AppFileClientApiController | `POST /upload_file` | `hasAnyAuthority('app_file:all', 'app_file:upload_file')` |
| file/app_file/AppFileClientApiController | `POST /delete_file` | `hasAnyAuthority('app_file:all', 'app_file:delete_file')` |
| file/common_file/CommonFileClientApiController | `POST /access_file` | `hasAnyAuthority('common_file:all', 'common_file:access_file')` |
| file/common_file/CommonFileClientApiController | `POST /get_file_stat` | `hasAnyAuthority('common_file:all', 'common_file:get_file_stat')` |
| file/common_file/CommonFileClientApiController | `POST /upload_file` | `hasAnyAuthority('common_file:all', 'common_file:upload_file')` |
| file/common_file/CommonFileClientApiController | `POST /copy_file` | `hasAnyAuthority('common_file:all', 'common_file:copy_file')` |
| file/common_file/CommonFileClientApiController | `POST /delete_file` | `hasAnyAuthority('app_file:all', 'common_file:delete_file')` |
| file/public_file/PublicFileClientApiController | `POST /access_file` | `hasAnyAuthority('public_file:all', 'public_file:access_file')` |
| file/public_file/PublicFileClientApiController | `POST /get_file_stat` | `hasAnyAuthority('public_file:all', 'public_file:get_file_stat')` |
| file/public_file/PublicFileClientApiController | `POST /upload_file` | `hasAnyAuthority('public_file:all', 'public_file:upload_file')` |
| file/public_file/PublicFileClientApiController | `POST /delete_file` | `hasAnyAuthority('public_file:all', 'public_file:delete_file')` |
| file/temporary_file/TemporaryFileClientApiController | `POST /access_file` | `hasAnyAuthority('temporary_file:all', 'temporary_file:access_file')` |
| file/temporary_file/TemporaryFileClientApiController | `POST /get_file_stat` | `hasAnyAuthority('temporary_file:all', 'temporary_file:get_file_stat')` |
| file/temporary_file/TemporaryFileClientApiController | `POST /upload_file` | `hasAnyAuthority('temporary_file:all', 'temporary_file:upload_file')` |
| file/temporary_file/TemporaryFileClientApiController | `POST /upload_files` | `hasAnyAuthority('temporary_file:all', 'temporary_file:upload_file')` |
| file/temporary_file/TemporaryFileClientApiController | `POST /get_upload_file_sign_url` | `hasAnyAuthority('temporary_file:all', 'temporary_file:upload_file')` |
| file/temporary_file/TemporaryFileClientApiController | `POST /delete_file` | `hasAnyAuthority('temporary_file:all', 'temporary_file:delete_file')` |
| file/tenant_app_file/TenantAppFileClientApiController | `POST /get_folder_list` | `hasAnyAuthority('tenant_app_file:all', 'tenant_app_file:get_folder')` |
| file/tenant_app_file/TenantAppFileClientApiController | `POST /get_folder_tree_list` | `hasAnyAuthority('tenant_app_file:all', 'tenant_app_file:get_folder')` |
| file/tenant_app_file/TenantAppFileClientApiController | `POST /list_file` | `hasAnyAuthority('tenant_app_file:all', 'tenant_app_file:list_file')` |
| file/tenant_app_file/TenantAppFileClientApiController | `POST /mkdir` | `hasAnyAuthority('app_admin', 'tenant_app_file:all', 'tenant_app_file:mkdir')` |
| file/tenant_app_file/TenantAppFileClientApiController | `POST /access_file` | `hasAnyAuthority('tenant_app_file:all', 'tenant_app_file:access_file')` |
| file/tenant_app_file/TenantAppFileClientApiController | `POST /get_file_stat` | `hasAnyAuthority('tenant_app_file:all', 'tenant_app_file:get_file_stat')` |
| file/tenant_app_file/TenantAppFileClientApiController | `POST /upload_file` | `hasAnyAuthority('tenant_app_file:all', 'tenant_app_file:upload_file')` |
| file/tenant_app_file/TenantAppFileClientApiController | `POST /get_upload_file_sign` | `hasAnyAuthority('app_admin', 'tenant_app_file:all', 'tenant_app_file:upload_file')` |
| file/tenant_app_file/TenantAppFileClientApiController | `POST /get_upload_file_sign_url` | `hasAnyAuthority('app_admin', 'tenant_app_file:all', 'tenant_app_file:upload_file')` |
| file/tenant_app_file/TenantAppFileClientApiController | `POST /move_file` | `hasAnyAuthority('tenant_app_file:all', 'tenant_app_file:move_file')` |
| file/tenant_app_file/TenantAppFileClientApiController | `POST /delete_file` | `hasAnyAuthority('tenant_app_file:all', 'tenant_app_file:delete_file')` |
| file/tenant_file/TenantFileClientApiController | `POST /access_file` | `hasAnyAuthority('tenant_file:all', 'tenant_file:access_file')` |
| file/tenant_file/TenantFileClientApiController | `POST /get_file_stat` | `hasAnyAuthority('tenant_file:all', 'tenant_file:get_file_stat')` |
| file/tenant_file/TenantFileClientApiController | `POST /upload_file` | `hasAnyAuthority('tenant_file:all','tenant_file:upload_file')` |
| file/tenant_file/TenantFileClientApiController | `POST /delete_file` | `hasAnyAuthority('tenant_file:all', 'tenant_file:delete_file')` |
| imgproxy/ImgProxyClientApiController | `POST /get_imgproxy_url` | `isAuthenticated()` |
| link/LinkClientApiController | `POST /create_batch_link` | `hasAnyAuthority('link:all', 'link:create_link')` |
| link/LinkClientApiController | `POST /get_link_list_by_short_url` | `hasAnyAuthority('link:all', 'link:read')` |
| link/LinkClientApiController | `POST /get_link_list_by_link_id` | `hasAnyAuthority('link:all', 'link:read')` |
| menu/MenuClientApiController | `POST /get_menu_tree_list` | `hasAnyAuthority('menu:all', 'menu:read')` |
| menu/MenuClientApiController | `POST /get_menu_list` | `hasAnyAuthority('menu:all', 'menu:read')` |
| permission/PermissionClientApiController | `POST /get_permission_list` | `hasAnyAuthority('permission:all', 'permission:read')` |
| permission/PermissionClientApiController | `POST /get_my_permission_list` | `hasAnyAuthority('permission:all', 'permission:read')` |
| sms/message/SmsMsgClientApiController | `POST /send_msg_by_phone_number` | `hasAnyAuthority('sms_msg:all', 'sms_msg:send_msg_by_phone_number')` |
| sms/message/SmsMsgClientApiController | `POST /send_batch_message_by_phone_number` | `hasAnyAuthority('sms_msg:all', 'sms_msg:send_msg_by_phone_number')` |
| sms/message/SmsMsgClientApiController | `POST /send_msg_by_account` | `hasAnyAuthority('sms_msg:all', 'sms_msg:send_msg_by_account')` |
| sms/message/SmsMsgClientApiController | `POST /send_batch_message_by_account` | `hasAnyAuthority('sms_msg:all', 'sms_msg:send_msg_by_account')` |
| sns_provider/SnsProviderClientApiController | `POST /get_sns_provider_list` | `hasAnyAuthority('sns_provider:all', 'sns_provider:read')` |
| subapp/SubappClientApiController | `POST /get_subapp_list` | `hasAnyAuthority('subapp:all', 'subapp:read')` |
| subapp_user_authorization/SubappUserAuthorizationClientApiController | `POST /get_subapp_user_authorization` | `hasAnyAuthority('subapp_user_authorization:all', 'subapp_user_authorization:get_subapp_user_authorization')` |
| subapp_version/SubappVersionClientApiController | `POST /get_subapp_version_list` | `hasAnyAuthority('subapp_version:all', 'subapp_version:read')` |
| tenant/TenantClientApiController | `POST /get_tenant_list` | `hasAnyAuthority('tenant:all', 'tenant:read')` |
| tenant/TenantClientApiController | `POST /get_tenant_info` | `hasAnyAuthority('tenant:all', 'tenant:read')` |
| tenant_app/TenantAppClientApiController | `POST /get_tenant_app_list` | `hasAnyAuthority('tenant_app:all', 'tenant_app:read')` |
| tenant_app/TenantAppClientApiController | `POST /get_tenant_app_page_list` | `hasAnyAuthority('tenant_app:all', 'tenant_app:read')` |
| tenant_app_department/TenantAppDepartmentClientApiController | `POST /get_tenant_app_department_list` | `hasAnyAuthority('tenant_app_department:all', 'tenant_app_department:read')` |
| tenant_app_department/TenantAppDepartmentClientApiController | `POST /get_tenant_app_department_page_list` | `hasAnyAuthority('tenant_app_department:all', 'tenant_app_department:read')` |
| tenant_app_department/TenantAppDepartmentClientApiController | `POST /get_path_tenant_app_department_list` | `hasAnyAuthority('app_admin', 'tenant_app_department:all', 'tenant_app_department:read')` |
| tenant_app_department/TenantAppDepartmentClientApiController | `POST /get_tenant_app_sub_department_list` | `hasAnyAuthority('app_admin', 'tenant_app_department:all', 'tenant_app_department:read')` |
| tenant_app_doc/TenantAppDocClientApiController | `POST /get_preview_tenant_app_doc_token` | `hasAnyAuthority('tenant_app_doc:all', 'tenant_app_doc:preview')` |
| tenant_app_doc/TenantAppDocClientApiController | `POST /get_edit_tenant_app_doc_token` | `hasAnyAuthority('tenant_app_doc:all', 'tenant_app_doc:edit')` |
| tenant_app_role/TenantAppRoleClientApiController | `POST /get_tenant_app_role_list` | `hasAnyAuthority('tenant_app_role:all', 'tenant_app_role:read')` |
| tenant_app_role/TenantAppRoleClientApiController | `POST /get_tenant_app_role_page_list` | `hasAnyAuthority('tenant_app_role:all', 'tenant_app_role:read')` |
| tenant_app_user_authorization/TenantAppUserAuthorizationClientApiController | `POST /get_tenant_app_user_authorization` | `hasAnyAuthority('tenant_app_user_authorization:all', 'tenant_app_user_authorization:get_tenant_app_user_authorization')` |
| tenant_app_user_authorization/TenantAppUserAuthorizationClientApiController | `POST /get_custom_tenant_app_user_authorization` | `hasAnyAuthority('tenant_app_user_authorization:all', 'tenant_app_user_authorization:get_tenant_app_user_authorization')` |
| tenant_app_user_template/TenantAppUserTemplateClientApiController | `POST /get_tenant_app_user_template_list` | `hasAnyAuthority('app_admin', 'tenant_app_user_template:all', 'tenant_app_user_template:read')` |
| tenant_endpoint/TenantEndpointClientApiController | `POST /get_tenant_endpoint_list` | `hasAnyAuthority('app_admin', 'tenant_endpoint:all', 'tenant_endpoint:read')` |
| tenant_subapp/TenantSubappClientApiController | `POST /get_tenant_subapp_list` | `hasAnyAuthority('app_admin', 'tenant_subapp:all', 'tenant_subapp:read')` |
| tenant_subapp_user_authorization/TenantSubappUserAuthorizationClientApiController | `POST /get_tenant_subapp_user_authorization` | `hasAnyAuthority('tenant_subapp_user_authorization:all', 'tenant_subapp_user_authorization:get_tenant_subapp_user_authorization')` |
| verify_code/VerifyCodeClientApiController | `POST /send_account_phone_number_verify_code` | `hasAnyAuthority('verify_code:all', 'verify_code:send_account_phone_number_verify_code')` |
| wxmp/mass_msg/client/WxmpMassMsgClientApiController | `POST /send` | `hasAnyAuthority('wxmass:all', 'wxmass:send')` |
| wxmp/mass_msg/client/WxmpMassMsgClientApiController | `POST /delete` | `hasAnyAuthority('wxmass:all', 'wxmass:send_msg')` |
| wxmp/provider/WxmpProviderClientApiController | `POST /get_provider_info` | `hasAnyAuthority('wxmp_provider:all', 'wxmp_provider:read')` |
| wxmp/provider/WxmpProviderClientApiController | `POST /get_wxmp_openid` | `hasAnyAuthority('wxmp_provider:all', 'wxmp_provider:read')` |
| wxmp/provider/WxmpProviderClientApiController | `POST /js_api_ticket` | `hasAnyAuthority('wxmp_provider:all', 'wxmp_provider:read')` |
| wxmp/send_msg/WxmpSendMsgClientApiController | `POST /send_msg_by_app_user` | `hasAnyAuthority('wxmp_template_msg:all', 'wxmp_message:send_msg')` |
| wxmp/send_msg/WxmpSendMsgClientApiController | `POST /send_msg` | `hasAnyAuthority('wxmp_template_msg:all', 'wxmp_message:send_msg')` |
| wxmp/template_msg/WxmpTemplateMsgClientApiController | `POST /get_wxmp_template_msg` | `hasAnyAuthority('wxmp_template_msg:all', 'wxmp_template_msg:read')` |

### cairo_web_manage 面 — `cairo_web_manage_api` 主体类型 `CAIRO_WEB_MANAGE_USER`

Controller 36 个,端点 181 个。

| Controller | 端点 | 方法级防护 |
|---|---|---|
| account/AccountCairoWebManageApiController | `POST /get_account_list` | `hasAnyAuthority('app_admin', 'account:all', 'account:read')` |
| account/AccountCairoWebManageApiController | `POST /get_account_page_list` | `hasAnyAuthority('app_admin', 'account:all', 'account:read')` |
| account/AccountCairoWebManageApiController | `POST /get_account_info` | `hasAnyAuthority('app_admin', 'account:all', 'account:read')` |
| account/AccountCairoWebManageApiController | `POST /create_account` | `hasAnyAuthority('app_admin', 'account:all', 'account:create_account')` |
| account/AccountCairoWebManageApiController | `POST /reset_account_password` | `hasAnyAuthority('app_admin', 'account:all', 'account:reset_account_password')` |
| account/AccountCairoWebManageApiController | `POST /logoff_account` | `hasAnyAuthority('app_admin', 'account:all', 'account:logoff_account')` |
| account/AccountCairoWebManageApiController | `POST /unlogoff_account` | `hasAnyAuthority('app_admin', 'account:all', 'account:unlogoff_account')` |
| account/AccountCairoWebManageApiController | `POST /delete_account` | `hasAnyAuthority('app_admin', 'account:all', 'account:delete_account')` |
| account/AccountCairoWebManageApiController | `POST /modify_account_info` | `hasAnyAuthority('app_admin', 'account:all', 'account:modify_account_info')` |
| account/AccountCairoWebManageApiController | `POST /modify_account_status` | `hasAnyAuthority('app_admin', 'account:all', 'account:modify_account_status')` |
| account/AccountCairoWebManageApiController | `POST /modify_account_lock_status` | `hasAnyAuthority('app_admin', 'account:all', 'account:modify_account_lock_status')` |
| account_authorization/AccountAuthorizationCairoWebManageApiController | `POST /get_account_authorization_list` | `hasAnyAuthority('app_admin', 'account_authorization:all', 'account_authorization:read')` |
| account_authorization/AccountAuthorizationCairoWebManageApiController | `POST /get_account_authorization_page_list` | `hasAnyAuthority('app_admin', 'account_authorization:all', 'account_authorization:read')` |
| account_authorization/AccountAuthorizationCairoWebManageApiController | `POST /offline_account_authorization` | `hasAnyAuthority('app_admin', 'account_authorization:all', 'account_authorization:offline')` |
| account_authorization/AccountAuthorizationCairoWebManageApiController | `POST /offline_all_account_authorization` | `hasAnyAuthority('app_admin', 'account_authorization:all', 'account_authorization:offline')` |
| app/AppCairoWebManageApiController | `POST /get_app_list` | `hasAnyAuthority('app_admin', 'app:all', 'app:read')` |
| app/AppCairoWebManageApiController | `POST /get_app_page_list` | `hasAnyAuthority('app_admin', 'app:all', 'app:read')` |
| app/AppCairoWebManageApiController | `POST /create_app` | `hasAnyAuthority('app_admin', 'app:all', 'app:create_app')` |
| app/AppCairoWebManageApiController | `POST /modify_app_info` | `hasAnyAuthority('app_admin', 'app:all', 'app:modify_app_info')` |
| app/AppCairoWebManageApiController | `POST /modify_app_status` | `hasAnyAuthority('app_admin', 'app:all', 'app:modify_app_status')` |
| app/AppCairoWebManageApiController | `POST /delete_app` | `hasAnyAuthority('app_admin', 'app:all', 'app:delete_app')` |
| app_release/AppReleaseCairoWebManageApiController | `POST /create_app_release` | `hasAnyAuthority('app_admin', 'app_release:all', 'app_release:create_app_release')` |
| app_release/AppReleaseCairoWebManageApiController | `POST /modify_app_release_info` | `hasAnyAuthority('app_admin', 'app_release:all', 'app_release:modify_app_release_info')` |
| app_release/AppReleaseCairoWebManageApiController | `POST /set_app_release_latest_version` | `hasAnyAuthority('app_admin', 'app_release:all', 'app_release:set_app_relase_latest_version')` |
| app_release/AppReleaseCairoWebManageApiController | `POST /delete_app_release` | `hasAnyAuthority('app_admin', 'app_release:all', 'app_release:delete_app_release')` |
| app_release/AppReleaseCairoWebManageApiController | `POST /get_app_release_list` | `hasAnyAuthority('app_admin', 'app_release:all', 'app_release:read')` |
| app_release/AppReleaseCairoWebManageApiController | `POST /get_app_release_page_list` | `hasAnyAuthority('app_admin', 'app_release:all', 'app_release:read')` |
| area/AreaCairoWebManageApiController | `POST /get_area_page_list` | `hasAnyAuthority('app_admin', 'area:all', 'area:read')` |
| area/AreaCairoWebManageApiController | `POST /get_area_detail` | `hasAnyAuthority('app_admin', 'area:all', 'area:read')` |
| area/AreaCairoWebManageApiController | `POST /create_area` | `hasAnyAuthority('app_admin', 'area:all', 'area:create_area')` |
| area/AreaCairoWebManageApiController | `POST /modify_area_info` | `hasAnyAuthority('app_admin', 'area:all', 'area:modify_area_info')` |
| area/AreaCairoWebManageApiController | `POST /modify_area_hot` | `hasAnyAuthority('app_admin', 'area:all', 'area:modify_area_hot')` |
| area/AreaCairoWebManageApiController | `POST /modify_area_status` | `hasAnyAuthority('app_admin', 'area:all', 'area:read')` |
| area/AreaCairoWebManageApiController | `POST /move_area` | `hasAnyAuthority('app_admin', 'area:all', 'area:move_area')` |
| area/AreaCairoWebManageApiController | `POST /delete_area` | `hasAnyAuthority('app_admin', 'area:all', 'area:read')` |
| biz_log/account_biz_log/AccountBizLogCairoWebManageApiController | `POST /get_account_biz_log_page_list` | `hasAnyAuthority('app_admin', 'account_biz_log:all', 'account_biz_log:read')` |
| biz_log/app_biz_log/AppBizLogCairoWebManageApiController | `POST /get_app_biz_log_page_list` | `hasAnyAuthority('app_admin', 'app_biz_log:all', 'app_biz_log:read')` |
| biz_log/client_biz_log/ClientBizLogCairoWebManageApiController | `POST /get_client_biz_log_page_list` | `hasAnyAuthority('app_admin', 'client_biz_log:all', 'client_biz_log:read')` |
| biz_log/open_biz_log/OpenBizLogCairoWebManageApiController | `POST /get_open_biz_log_page_list` | `hasAnyAuthority('app_admin', 'open_biz_log:all', 'open_biz_log:read')` |
| biz_log/subapp_biz_log/SubappBizLogCairoWebManageApiController | `POST /get_subapp_biz_log_page_list` | `hasAnyAuthority('app_admin', 'subapp_biz_log:all', 'subapp_biz_log:read')` |
| biz_log/tenant_app_biz_log/TenantAppBizLogCairoWebManageApiController | `POST /get_tenant_app_biz_log_page_list` | `hasAnyAuthority('app_admin', 'tenant_app_biz_log:all', 'tenant_app_biz_log:read')` |
| biz_log/tenant_subapp_biz_log/TenantSubappBizLogCairoWebManageApiController | `POST /get_tenant_subapp_biz_log_page_list` | `hasAnyAuthority('app_admin', 'tenant_subapp_biz_log:all', 'tenant_subapp_biz_log:read')` |
| client/ClientCairoWebManageApiController | `POST /get_client_list` | `hasAnyAuthority('app_admin', 'client:all', 'client:read')` |
| client/ClientCairoWebManageApiController | `POST /get_client_page_list` | `hasAnyAuthority('app_admin', 'client:all', 'client:read')` |
| client/ClientCairoWebManageApiController | `POST /create_client` | `hasAnyAuthority('app_admin', 'client:all', 'client:create_client')` |
| client/ClientCairoWebManageApiController | `POST /modify_client_info` | `hasAnyAuthority('app_admin', 'client:all', 'client:modify_client_info')` |
| client/ClientCairoWebManageApiController | `POST /modify_client_status` | `hasAnyAuthority('app_admin', 'client:all', 'client:modify_client_status')` |
| client/ClientCairoWebManageApiController | `POST /modify_client_secret` | `hasAnyAuthority('app_admin', 'client:all', 'client:modify_client_secret')` |
| client/ClientCairoWebManageApiController | `POST /delete_client` | `hasAnyAuthority('app_admin', 'client:all', 'client:delete_client')` |
| dict/system/SystemDictCairoWebManageApiController | `POST /create_sys_dict` | `hasAnyAuthority('app_admin', 'sys_dict:all', 'sys_dict:create_sys_dict')` |
| dict/system/SystemDictCairoWebManageApiController | `POST /modify_sys_dict_info` | `hasAnyAuthority('app_admin', 'sys_dict:all', 'sys_dict:modify_dict_info')` |
| dict/system/SystemDictCairoWebManageApiController | `POST /delete_sys_dict` | `hasAnyAuthority('app_admin', 'sys_dict:all', 'sys_dict:delete_sys_dict')` |
| dict/system/SystemDictCairoWebManageApiController | `POST /get_sys_dict_list` | `hasAnyAuthority('app_admin', 'sys_dict:all', 'sys_dict:read')` |
| dict/system/SystemDictCairoWebManageApiController | `POST /get_sys_dict_page_list` | `hasAnyAuthority('app_admin', 'sys_dict:all', 'sys_dict:read')` |
| dict/system/SystemDictCairoWebManageApiController | `POST /get_sys_dict_info` | `hasAnyAuthority('app_admin', 'sys_dict:all', 'sys_dict:read')` |
| dict/system/SystemDictCairoWebManageApiController | `POST /get_sys_dict_detail_info` | `hasAnyAuthority('app_admin', 'sys_dict:all', 'sys_dict:read')` |
| dict/system/SystemDictCairoWebManageApiController | `POST /put_sys_dict_item` | `hasAnyAuthority('app_admin', 'sys_dict:all', 'sys_dict:put_sys_dict_item')` |
| dict/system/SystemDictCairoWebManageApiController | `POST /modify_sys_dict_item_info` | `hasAnyAuthority('app_admin', 'sys_dict:all', 'sys_dict:modify_sys_dict_item_info')` |
| dict/system/SystemDictCairoWebManageApiController | `POST /modify_sys_dict_item_status` | `hasAnyAuthority('app_admin', 'sys_dict:all', 'sys_dict:modify_sys_dict_item_status')` |
| dict/system/SystemDictCairoWebManageApiController | `POST /move_sys_dict_item` | `hasAnyAuthority('app_admin', 'sys_dict:all', 'sys_dict:move_sys_dict_item')` |
| dict/system/SystemDictCairoWebManageApiController | `POST /delete_sys_dict_item` | `hasAnyAuthority('app_admin', 'sys_dict:all', 'sys_dict:delete_sys_dict_item')` |
| dict/system/SystemDictCairoWebManageApiController | `POST /get_sys_dict_item_info` | `hasAnyAuthority('app_admin', 'sys_dict:all', 'sys_dict:read')` |
| dict/system/SystemDictCairoWebManageApiController | `POST /get_sys_dict_item_page_list` | `hasAnyAuthority('app_admin', 'sys_dict:all', 'sys_dict:read')` |
| dict/system/SystemDictCairoWebManageApiController | `POST /get_sys_dict_sub_item_list` | `hasAnyAuthority('app_admin', 'sys_dict:all', 'sys_dict:read')` |
| dict/system/SystemDictCairoWebManageApiController | `POST /get_sys_dict_sub_item_tree_list` | `hasAnyAuthority('app_admin', 'sys_dict:all', 'sys_dict:read')` |
| dict/system/SystemDictCairoWebManageApiController | `POST /sync_sys_dict` | `hasAnyAuthority('app_admin', 'sys_dict:all', 'sys_dict:sync_sys_dict')` |
| dict/system/SystemDictCairoWebManageApiController | `POST /copy_sys_dict_by_app_id` | `hasAnyAuthority('app_admin', 'sys_dict:all', 'sys_dict:copy_by_app')` |
| dict/system/SystemDictCairoWebManageApiController | `POST /copy_sys_dict_by_dict_id` | `hasAnyAuthority('app_admin', 'sys_dict:all', 'sys_dict:copy_by_dict')` |
| endpoint/EndpointCairoWebManageApiController | `POST /get_endpoint_list` | `hasAnyAuthority('app_admin', 'endpoint:all', 'endpoint:read')` |
| endpoint/EndpointCairoWebManageApiController | `POST /get_endpoint_page_list` | `hasAnyAuthority('app_admin', 'endpoint:all', 'endpoint:read')` |
| endpoint/EndpointCairoWebManageApiController | `POST /create_endpoint` | `hasAnyAuthority('app_admin', 'endpoint:all', 'endpoint:create_endpoint')` |
| endpoint/EndpointCairoWebManageApiController | `POST /modify_endpoint_info` | `hasAnyAuthority('app_admin', 'endpoint:all', 'endpoint:modify_endpoint_info')` |
| endpoint/EndpointCairoWebManageApiController | `POST /modify_endpoint_status` | `hasAnyAuthority('app_admin', 'endpoint:all', 'endpoint:modify_endpoint_status')` |
| endpoint/EndpointCairoWebManageApiController | `POST /delete_endpoint` | `hasAnyAuthority('app_admin', 'endpoint:all', 'endpoint:delete_endpoint')` |
| link/LinkCairoWebManageApiController | `POST /get_link_page_list` | `hasAnyAuthority('app_admin', 'link:all', 'link:read')` |
| link/LinkCairoWebManageApiController | `POST /create_link` | `hasAnyAuthority('app_admin', 'link:all', 'link:create_link')` |
| link/LinkCairoWebManageApiController | `POST /modify_link_status` | `hasAnyAuthority('app_admin', 'link:all', 'link:modify_link_status')` |
| link/LinkCairoWebManageApiController | `POST /delete_link` | `hasAnyAuthority('app_admin', 'link:all', 'link:delete_link')` |
| login_log/account_login_log/AccountLoginLogCairoEndpointUserApiController | `POST /get_account_login_log_page_list` | `hasAnyAuthority('app_admin', 'account_login_log:all', 'account_login_log:read')` |
| login_log/app_user_login_log/AppUserLoginLogCairoWebManageApiController | `POST /get_app_user_login_log_page_list` | `hasAnyAuthority('app_admin', 'app_user_login_log:all', 'app_user_login_log:read')` |
| login_log/client_login_log/ClientLoginLogCairoWebManageApiController | `POST /get_client_login_log_page_list` | `hasAnyAuthority('app_admin', 'client_login_log:all', 'client_login_log:read')` |
| login_log/tenant_app_user_login_log/TenantAppUserLoginLogCairoWebManageApiController | `POST /get_tenant_app_user_login_log_page_list` | `hasAnyAuthority('app_admin', 'tenant_app_user_login_log:all', 'tenant_app_user_login_log:read')` |
| menu/MenuCairoWebManageApiController | `POST /get_menu_tree_list` | `hasAnyAuthority('app_admin', 'menu:all', 'menu:read')` |
| menu/MenuCairoWebManageApiController | `POST /get_menu_list` | `hasAnyAuthority('app_admin', 'menu:all', 'menu:read')` |
| menu/MenuCairoWebManageApiController | `POST /get_menu_page_list` | `hasAnyAuthority('app_admin', 'menu:all', 'menu:read')` |
| menu/MenuCairoWebManageApiController | `POST /create_menu` | `hasAnyAuthority('app_admin', 'menu:all', 'menu:write')` |
| menu/MenuCairoWebManageApiController | `POST /modify_menu` | `hasAnyAuthority('app_admin', 'menu:all', 'menu:write')` |
| menu/MenuCairoWebManageApiController | `POST /move_menu` | `hasAnyAuthority('app_admin', 'menu:all', 'menu:write')` |
| menu/MenuCairoWebManageApiController | `POST /delete_menu` | `hasAnyAuthority('app_admin', 'menu:all', 'menu:write')` |
| notify/NotifyTemplateCairoWebManageApiController | `POST /get_notify_template_list` | `hasAnyAuthority('app_admin', 'notify_template:all', 'notify_template:read')` |
| notify/NotifyTemplateCairoWebManageApiController | `POST /get_notify_template_page_list` | `hasAnyAuthority('app_admin', 'notify_template:all', 'notify_template:read')` |
| notify/NotifyTemplateCairoWebManageApiController | `POST /get_notify_template_info` | `hasAnyAuthority('app_admin', 'notify_template:all', 'notify_template:read')` |
| notify/NotifyTemplateCairoWebManageApiController | `POST /get_notify_template_detail_info` | `hasAnyAuthority('app_admin', 'notify_template:all', 'notify_template:read')` |
| notify/NotifyTemplateCairoWebManageApiController | `POST /create_notify_template` | `hasAnyAuthority('app_admin', 'notify_template:all', 'notify_template:create_notify_template')` |
| notify/NotifyTemplateCairoWebManageApiController | `POST /modify_notify_template_info` | `hasAnyAuthority('app_admin', 'notify_template:all', 'notify_template:modify_notify_template_info')` |
| notify/NotifyTemplateCairoWebManageApiController | `POST /modify_notify_template_status` | `hasAnyAuthority('app_admin', 'notify_template:all', 'notify_template:modify_notify_template_status')` |
| notify/NotifyTemplateCairoWebManageApiController | `POST /delete_notify_template` | `hasAnyAuthority('app_admin', 'notify_template:all', 'notify_template:delete_notify_template')` |
| notify/template/NotifyCategoryCairoWebManageApiController | `POST /get_notify_category_list` | `hasAnyAuthority('app_admin', 'notify_category:all', 'notify_category:read')` |
| notify/template/NotifyCategoryCairoWebManageApiController | `POST /get_notify_category_page_list` | `hasAnyAuthority('app_admin', 'notify_category:all', 'notify_category:read')` |
| notify/template/NotifyCategoryCairoWebManageApiController | `POST /create_notify_category` | `hasAnyAuthority('app_admin', 'notify_category:all', 'notify_category:create')` |
| notify/template/NotifyCategoryCairoWebManageApiController | `POST /modify_notify_category_info` | `hasAnyAuthority('app_admin', 'notify_category:all', 'notify_category:modify_info')` |
| notify/template/NotifyCategoryCairoWebManageApiController | `POST /modify_notify_category_status` | `hasAnyAuthority('app_admin', 'notify_category:all', 'notify_category:modify_status')` |
| notify/template/NotifyCategoryCairoWebManageApiController | `POST /delete_notify_category` | `hasAnyAuthority('app_admin', 'notify_category:all', 'notify_category:delete')` |
| permission/PermissionCairoWebManageApiController | `POST /get_permission_list` | `hasAnyAuthority('app_admin', 'permission:all', 'permission:read')` |
| permission/PermissionCairoWebManageApiController | `POST /get_permission_page_list` | `hasAnyAuthority('app_admin', 'permission:all', 'permission:read')` |
| permission/PermissionCairoWebManageApiController | `POST /create_permission` | `hasAnyAuthority('app_admin', 'permission:all', 'permission:write')` |
| permission/PermissionCairoWebManageApiController | `POST /modify_permission` | `hasAnyAuthority('app_admin', 'permission:all', 'permission:write')` |
| permission/PermissionCairoWebManageApiController | `POST /delete_permission` | `hasAnyAuthority('app_admin', 'permission:all', 'permission:write')` |
| permission/PermissionCairoWebManageApiController | `POST /move_permission` | `hasAnyAuthority('app_admin', 'permission:all', 'permission:move')` |
| sms/message/SmsMsgCairoWebManageApiController | `POST /get_sms_msg_page_list` | `hasAnyAuthority('app_admin', 'sms_msg:all', 'sms_msg:read')` |
| sms/message/SmsMsgCairoWebManageApiController | `POST /retry_sms_msg` | `hasAnyAuthority('app_admin', 'sms_msg:all', 'sms_msg:retry_sms_msg')` |
| sms/template/SmsTemplateCairoWebManageApiController | `POST /create_sms_template` | `hasAnyAuthority('app_admin', 'sms_template:all', 'sms_template:create_sms_template')` |
| sms/template/SmsTemplateCairoWebManageApiController | `POST /modify_sms_template_info` | `hasAnyAuthority('app_admin', 'sms_template:all', 'sms_template:modify_sms_template_info')` |
| sms/template/SmsTemplateCairoWebManageApiController | `POST /modify_sms_template_status` | `hasAnyAuthority('app_admin', 'sms_template:all', 'sms_template:modify_sms_template_status')` |
| sms/template/SmsTemplateCairoWebManageApiController | `POST /delete_sms_template` | `hasAnyAuthority('app_admin', 'sms_template:all', 'sms_template:delete_sms_template')` |
| sms/template/SmsTemplateCairoWebManageApiController | `POST /get_sms_template_list` | `hasAnyAuthority('app_admin', 'sms_template:all', 'sms_template:read')` |
| sms/template/SmsTemplateCairoWebManageApiController | `POST /get_sms_template_page_list` | `hasAnyAuthority('app_admin', 'sms_template:all', 'sms_template:read')` |
| sms/template/SmsTemplateCairoWebManageApiController | `POST /get_sms_template_info` | `hasAnyAuthority('app_admin', 'sms_template:all', 'sms_template:read')` |
| sms/template/SmsTemplateCairoWebManageApiController | `POST /get_sms_template_detail_info` | `hasAnyAuthority('app_admin', 'sms_template:all', 'sms_template:read')` |
| sns_provider/SnsProviderCairoWebManageApiController | `POST /get_sns_provider_list` | `hasAnyAuthority('app_admin', 'sns_provider:all', 'sns_provider:read')` |
| sns_provider/SnsProviderCairoWebManageApiController | `POST /get_sns_provider_page_list` | `hasAnyAuthority('app_admin', 'sns_provider:all', 'sns_provider:read')` |
| sns_provider/SnsProviderCairoWebManageApiController | `POST /create_sns_provider` | `hasAnyAuthority('app_admin', 'sns_provider:all', 'sns_provider:create_sns_provider')` |
| sns_provider/SnsProviderCairoWebManageApiController | `POST /modify_sns_provider` | `hasAnyAuthority('app_admin', 'sns_provider:all', 'sns_provider:modify_sns_provider')` |
| sns_provider/SnsProviderCairoWebManageApiController | `POST /modify_sns_provider_status` | `hasAnyAuthority('app_admin', 'sns_provider:all', 'sns_provider:modify_sns_provider_status')` |
| sns_provider/SnsProviderCairoWebManageApiController | `POST /delete_sns_provider` | `hasAnyAuthority('app_admin', 'sns_provider:all', 'sns_provider:delete_sns_provider')` |
| sns_provider/SnsProviderCairoWebManageApiController | `POST /get_provider_type_list` | `hasAnyAuthority('app_admin', 'sns_provider:all', 'sns_provider:read')` |
| sns_provider/SnsProviderCairoWebManageApiController | `POST /get_provider_partner_list` | `hasAnyAuthority('app_admin', 'sns_provider:all', 'sns_provider:read')` |
| subapp/SubappCairoWebManageApiController | `POST /get_subapp_list` | `hasAnyAuthority('app_admin', 'subapp:all', 'subapp:read')` |
| subapp/SubappCairoWebManageApiController | `POST /get_subapp_page_list` | `hasAnyAuthority('app_admin', 'subapp:all', 'subapp:read')` |
| subapp/SubappCairoWebManageApiController | `POST /create_subapp` | `hasAnyAuthority('app_admin', 'subapp:all', 'subapp:create_subapp')` |
| subapp/SubappCairoWebManageApiController | `POST /modify_subapp_info` | `hasAnyAuthority('app_admin', 'subapp:all', 'subapp:modify_subapp_info')` |
| subapp/SubappCairoWebManageApiController | `POST /modify_subapp_status` | `hasAnyAuthority('app_admin', 'subapp:all', 'subapp:modify_subapp_status')` |
| subapp/SubappCairoWebManageApiController | `POST /move_subapp` | `hasAnyAuthority('app_admin', 'subapp:all', 'subapp:move_subapp')` |
| subapp/SubappCairoWebManageApiController | `POST /delete_subapp` | `hasAnyAuthority('app_admin', 'subapp:all', 'subapp:delete_subapp')` |
| subapp_version/SubappVersionCairoWebManageApiController | `POST /get_subapp_version_list` | `hasAnyAuthority('app_admin', 'subapp_version:all', 'subapp_version:read')` |
| subapp_version/SubappVersionCairoWebManageApiController | `POST /get_subapp_version_page_list` | `hasAnyAuthority('app_admin', 'subapp_version:all', 'subapp_version:read')` |
| subapp_version/SubappVersionCairoWebManageApiController | `POST /create_subapp_version` | `hasAnyAuthority('app_admin', 'subapp_version:all', 'subapp_version:create_subapp_version')` |
| subapp_version/SubappVersionCairoWebManageApiController | `POST /modify_subapp_version_info` | `hasAnyAuthority('app_admin', 'subapp_version:all', 'subapp_version:modify_subapp_version_info')` |
| subapp_version/SubappVersionCairoWebManageApiController | `POST /modify_subapp_version_status` | `hasAnyAuthority('app_admin', 'subapp_version:all', 'subapp_version:modify_subapp_version_status')` |
| subapp_version/SubappVersionCairoWebManageApiController | `POST /delete_subapp_version` | `hasAnyAuthority('app_admin', 'subapp_version:all', 'subapp_version:delete_subapp_version')` |
| subapp_version/SubappVersionCairoWebManageApiController | `POST /sync_subapp_version` | `hasAnyAuthority('app_admin', 'subapp_version:all', 'subapp_version:sync_subapp_version')` |
| tenant/TenantCairoWebManageApiController | `POST /get_tenant_list` | `hasAnyAuthority('app_admin', 'tenant:all', 'tenant:read')` |
| tenant/TenantCairoWebManageApiController | `POST /get_tenant_page_list` | `hasAnyAuthority('app_admin', 'tenant:all', 'tenant:read')` |
| tenant/TenantCairoWebManageApiController | `POST /create_tenant` | `hasAnyAuthority('app_admin', 'tenant:all', 'tenant:create_tenant')` |
| tenant/TenantCairoWebManageApiController | `POST /modify_tenant_info` | `hasAnyAuthority('app_admin', 'tenant:all', 'tenant:modify_tenant_info')` |
| tenant/TenantCairoWebManageApiController | `POST /modify_tenant_owner` | `hasAnyAuthority('app_admin', 'tenant:all', 'tenant:modify_tenant_owner')` |
| tenant/TenantCairoWebManageApiController | `POST /modify_tenant_status` | `hasAnyAuthority('app_admin', 'tenant:all', 'tenant:modify_tenant_status')` |
| tenant/TenantCairoWebManageApiController | `POST /delete_tenant` | `hasAnyAuthority('app_admin', 'tenant:all', 'tenant:delete_tenant')` |
| tenant_app/TenantAppCairoWebManageApiController | `POST /get_tenant_app_list` | `hasAnyAuthority('app_admin', 'tenant_app:all', 'tenant_app:read')` |
| tenant_app/TenantAppCairoWebManageApiController | `POST /get_tenant_app_page_list` | `hasAnyAuthority('app_admin', 'tenant_app:all', 'tenant_app:read')` |
| tenant_app/TenantAppCairoWebManageApiController | `POST /create_tenant_app` | `hasAnyAuthority('app_admin', 'tenant_app:all', 'tenant_app:create_tenant_app')` |
| tenant_app/TenantAppCairoWebManageApiController | `POST /modify_tenant_app_info` | `hasAnyAuthority('app_admin', 'tenant_app:all', 'tenant_app:modify_tenant_app_info')` |
| tenant_app/TenantAppCairoWebManageApiController | `POST /modify_tenant_app_status` | `hasAnyAuthority('app_admin', 'tenant_app:all', 'tenant_app:modify_tenant_app_status')` |
| tenant_app/TenantAppCairoWebManageApiController | `POST /delete_tenant_app` | `hasAnyAuthority('app_admin', 'tenant_app:all', 'tenant_app:delete_tenant_app')` |
| tenant_endpoint/TenantEndpointCairoWebManageApiController | `POST /get_tenant_endpoint_list` | `hasAnyAuthority('app_admin', 'tenant_endpoint:all', 'tenant_endpoint:read')` |
| tenant_endpoint/TenantEndpointCairoWebManageApiController | `POST /get_tenant_endpoint_page_list` | `hasAnyAuthority('app_admin', 'tenant_endpoint:all', 'tenant_endpoint:read')` |
| tenant_endpoint/TenantEndpointCairoWebManageApiController | `POST /create_tenant_endpoint` | `hasAnyAuthority('app_admin', 'tenant_endpoint:all', 'tenant_endpoint:create_tenant_endpoint')` |
| tenant_endpoint/TenantEndpointCairoWebManageApiController | `POST /modify_tenant_endpoint_info` | `hasAnyAuthority('app_admin', 'tenant_endpoint:all', 'tenant_endpoint:modify_tenant_endpoint_info')` |
| tenant_endpoint/TenantEndpointCairoWebManageApiController | `POST /modify_tenant_endpoint_status` | `hasAnyAuthority('app_admin', 'tenant_endpoint:all', 'tenant_endpoint:modify_tenant_endpoint_status')` |
| tenant_endpoint/TenantEndpointCairoWebManageApiController | `POST /delete_tenant_endpoint` | `hasAnyAuthority('app_admin', 'tenant_endpoint:all', 'tenant_endpoint:delete_tenant_endpoint')` |
| tenant_subapp/TenantSubappCairoWebManageApiController | `POST /get_tenant_subapp_list` | `hasAnyAuthority('app_admin', 'tenant_subapp:all', 'tenant_subapp:read')` |
| tenant_subapp/TenantSubappCairoWebManageApiController | `POST /get_tenant_subapp_page_list` | `hasAnyAuthority('app_admin', 'tenant_subapp:all', 'tenant_subapp:read')` |
| tenant_subapp/TenantSubappCairoWebManageApiController | `POST /create_tenant_subapp` | `hasAnyAuthority('app_admin', 'tenant_subapp:all', 'tenant_subapp:create_tenant_subapp')` |
| tenant_subapp/TenantSubappCairoWebManageApiController | `POST /modify_tenant_subapp_status` | `hasAnyAuthority('app_admin', 'tenant_subapp:all', 'tenant_subapp:modify_tenant_subapp_status')` |
| tenant_subapp/TenantSubappCairoWebManageApiController | `POST /delete_tenant_subapp` | `hasAnyAuthority('app_admin', 'tenant_subapp:all', 'tenant_subapp:delete_tenant_subapp')` |
| wxmp/provider/WxmpProviderCairoWebManageApiController | `POST /create_wxmp_provider` | `hasAnyAuthority('app_admin', 'wxmp_provider:all', 'wxmp_provider:create_wxmp_provider')` |
| wxmp/provider/WxmpProviderCairoWebManageApiController | `POST /modify_wxmp_provider` | `hasAnyAuthority('app_admin', 'wxmp_provider:all', 'wxmp_provider:modify_wxmp_provider')` |
| wxmp/provider/WxmpProviderCairoWebManageApiController | `POST /modify_wxmp_provider_status` | `hasAnyAuthority('app_admin', 'wxmp_provider:all', 'wxmp_provider:modify_wxmp_provider_status')` |
| wxmp/provider/WxmpProviderCairoWebManageApiController | `POST /delete_wxmp_provider` | `hasAnyAuthority('app_admin', 'wxmp_provider:all', 'wxmp_provider:delete_wxmp_provider')` |
| wxmp/provider/WxmpProviderCairoWebManageApiController | `POST /get_wxmp_provider_list` | `hasAnyAuthority('app_admin', 'wxmp_provider:all', 'wxmp_provider:read')` |
| wxmp/provider/WxmpProviderCairoWebManageApiController | `POST /get_wxmp_provider_page_list` | `hasAnyAuthority('app_admin', 'wxmp_provider:all', 'wxmp_provider:read')` |
| wxmp/template_msg/WxmpTemplateMsgCairoWebManageApiController | `POST /create_wxmp_template_msg` | `hasAnyAuthority('app_admin', 'wxmp_template_msg:all', 'wxmp_template_msg:create_wxmp_template_msg')` |
| wxmp/template_msg/WxmpTemplateMsgCairoWebManageApiController | `POST /modify_wxmp_template_msg_info` | `hasAnyAuthority('app_admin', 'wxmp_template_msg:all', 'wxmp_template_msg:modify_wxmp_template_msg_info')` |
| wxmp/template_msg/WxmpTemplateMsgCairoWebManageApiController | `POST /modify_wxmp_template_msg_status` | `hasAnyAuthority('app_admin', 'wxmp_template_msg:all', 'wxmp_template_msg:modify_wxmp_template_msg_status')` |
| wxmp/template_msg/WxmpTemplateMsgCairoWebManageApiController | `POST /delete_wxmp_template_msg` | `hasAnyAuthority('app_admin', 'wxmp_template_msg:all', 'wxmp_template_msg:delete_wxmp_template_msg')` |
| wxmp/template_msg/WxmpTemplateMsgCairoWebManageApiController | `POST /get_wxmp_template_msg_list` | `hasAnyAuthority('app_admin', 'wxmp_template_msg:all', 'wxmp_template_msg:read')` |
| wxmp/template_msg/WxmpTemplateMsgCairoWebManageApiController | `POST /get_wxmp_template_msg_page_list` | `hasAnyAuthority('app_admin', 'wxmp_template_msg:all', 'wxmp_template_msg:read')` |
| wxmp/template_msg/WxmpTemplateMsgCairoWebManageApiController | `POST /get_wxmp_template_msg_info` | `hasAnyAuthority('app_admin', 'wxmp_template_msg:all', 'wxmp_template_msg:read')` |
| wxmp/template_msg/WxmpTemplateMsgCairoWebManageApiController | `POST /get_wxmp_template_msg_detail_info` | `hasAnyAuthority('app_admin', 'wxmp_template_msg:all', 'wxmp_template_msg:read')` |
| wxmp/template_msg_record/WxmpTemplateMsgRecordCairoWebManageApiController | `POST /get_wxmp_template_msg_record_page_list` | `hasAnyAuthority('app_admin', 'wxmp_template_msg_record:all', 'wxmp_template_msg_record:read')` |
| wxmp/template_msg_record/WxmpTemplateMsgRecordCairoWebManageApiController | `POST /retry_wxmp_template_msg_record` | `hasAnyAuthority('app_admin', 'wxmp_template_msg_record:all', 'wxmp_template_msg_record:retry_wxmp_template_msg_record')` |

### subapp 面 — `subapp_user_api` 主体类型 `SUBAPP_USER`

Controller 22 个,端点 162 个。

| Controller | 端点 | 方法级防护 |
|---|---|---|
| account/AccountSubappApiController | `POST /get_account_list` | `hasAnyAuthority('app_admin', 'account:all', 'account:read')` |
| account/AccountSubappApiController | `POST /get_account_page_list` | `hasAnyAuthority('app_admin', 'account:all', 'account:read')` |
| account/AccountSubappApiController | `POST /search_account_info` | `hasAnyAuthority('app_admin', 'account:all', 'account:read')` |
| account/AccountSubappApiController | `POST /get_account_info` | `hasAnyAuthority('app_admin', 'account:all', 'account:read')` |
| app_department/AppDepartmentSubappApiController | `POST /get_app_department_list` | `hasAnyAuthority('app_admin', 'app_department:all', 'app_department:read')` |
| app_department/AppDepartmentSubappApiController | `POST /get_app_department_page_list` | `hasAnyAuthority('app_admin', 'app_department:all', 'app_department:read')` |
| app_department/AppDepartmentSubappApiController | `POST /get_path_app_department_list` | `hasAnyAuthority('app_admin', 'app_department:all', 'app_department:read')` |
| app_department/AppDepartmentSubappApiController | `POST /get_path_app_department_page_list` | `hasAnyAuthority('app_admin', 'app_department:all', 'app_department:read')` |
| app_department/AppDepartmentSubappApiController | `POST /get_app_department_tree` | `hasAnyAuthority('app_admin', 'app_department:all', 'app_department:read')` |
| app_department/AppDepartmentSubappApiController | `POST /get_app_department_by_department_id` | `hasAnyAuthority('app_admin', 'app_department:all', 'app_department:read')` |
| app_department/AppDepartmentSubappApiController | `POST /create_app_department` | `hasAnyAuthority('app_admin', 'app_department:all', 'app_department:create_app_department')` |
| app_department/AppDepartmentSubappApiController | `POST /modify_app_department_info` | `hasAnyAuthority('app_admin', 'app_department:all', 'app_department:modify_app_department_info')` |
| app_department/AppDepartmentSubappApiController | `POST /move_app_department` | `hasAnyAuthority('app_admin', 'app_department:all', 'department:move_app_department')` |
| app_department/AppDepartmentSubappApiController | `POST /delete_app_department` | `hasAnyAuthority('app_admin', 'app_department:all', 'app_department:delete_app_department')` |
| app_role/AppRoleSubappApiController | `POST /get_app_role_list` | `hasAnyAuthority('app_admin', 'app_role:all', 'app_role:read')` |
| app_role/AppRoleSubappApiController | `POST /get_app_role_page_list` | `hasAnyAuthority('app_admin', 'app_role:all', 'app_role:read')` |
| app_role/AppRoleSubappApiController | `POST /get_app_role_info` | `hasAnyAuthority('app_admin', 'app_role:all', 'app_role:read')` |
| app_role/AppRoleSubappApiController | `POST /get_app_role_permission` | `hasAnyAuthority('app_admin', 'app_role:all', 'app_role:read')` |
| app_role/AppRoleSubappApiController | `POST /create_app_role` | `hasAnyAuthority('app_admin', 'app_role:all', 'app_role:create_app_role')` |
| app_role/AppRoleSubappApiController | `POST /modify_app_role_info` | `hasAnyAuthority('app_admin', 'app_role:all', 'app_role:modify_app_role_info')` |
| app_role/AppRoleSubappApiController | `POST /modify_app_role_permission` | `hasAnyAuthority('app_admin', 'app_role:all', 'app_role:modify_app_role_permission')` |
| app_role/AppRoleSubappApiController | `POST /modify_app_role_status` | `hasAnyAuthority('app_admin', 'app_role:all', 'app_role:modify_app_role_status')` |
| app_role/AppRoleSubappApiController | `POST /delete_app_role` | `hasAnyAuthority('app_admin', 'app_role:all', 'app_role:delete_app_role')` |
| app_role/AppRoleSubappApiController | `POST /get_app_role_subapp_version` | `hasAnyAuthority('app_admin', 'app_role:all', 'app_role:read')` |
| app_role/AppRoleSubappApiController | `POST /delete_app_role_permission` | `hasAnyAuthority('app_admin', 'app_role:all', 'app_role:delete_app_role_permission')` |
| app_user/AppUserSubappApiController | `POST /get_my_subapp_user_authority` | `isAuthenticated()` |
| app_user/AppUserSubappApiController | `POST /get_my_subapp_user_permission_ids` | `isAuthenticated()` |
| app_user/AppUserSubappApiController | `POST /get_my_subapp_user_menu` | `isAuthenticated()` |
| app_user/AppUserSubappApiController | `POST /get_app_user_list` | `hasAnyAuthority('app_admin', 'app_user:all', 'app_user:read')` |
| app_user/AppUserSubappApiController | `POST /get_app_user_page_list` | `hasAnyAuthority('app_admin', 'app_user:all', 'app_user:read')` |
| app_user/AppUserSubappApiController | `POST /get_app_user_info` | `hasAnyAuthority('app_admin', 'app_user:all', 'app_user:read')` |
| app_user/AppUserSubappApiController | `POST /create_app_user` | `hasAnyAuthority('app_admin', 'app_user:all', 'app_user:create_app_user')` |
| app_user/AppUserSubappApiController | `POST /create_account_and_app_user` | `hasAnyAuthority('app_admin', 'app_user:all', 'app_user:create_app_user')` |
| app_user/AppUserSubappApiController | `POST /modify_app_user_info` | `hasAnyAuthority('app_admin', 'app_user:all', 'app_user:modify_app_user_info')` |
| app_user/AppUserSubappApiController | `POST /modify_app_user_status` | `hasAnyAuthority('app_admin', 'app_user:all', 'app_user:modify_app_user_status')` |
| app_user/AppUserSubappApiController | `POST /transfer_app_user_to_other_account` | `hasAnyAuthority('app_admin', 'app_user:all', 'app_user:transfer_app_user_to_other_account')` |
| app_user/AppUserSubappApiController | `POST /logoff_app_user` | `hasAnyAuthority('app_admin', 'app_user:all', 'app_user:logoff_app_user')` |
| app_user/AppUserSubappApiController | `POST /unlogoff_app_user` | `hasAnyAuthority('app_admin', 'app_user:all', 'app_user:logoff_app_user')` |
| app_user/AppUserSubappApiController | `POST /delete_app_user` | `hasAnyAuthority('app_admin', 'app_user:all', 'app_user:delete_app_user')` |
| app_user_authorization/AppUserAuthorizationSubappApiController | `POST /get_app_user_authorization_list` | `hasAnyAuthority('app_admin','app_user_authorization:all', 'app_user_authorization:get_app_user_authorization')` |
| app_user_authorization/AppUserAuthorizationSubappApiController | `POST /get_app_user_authorization_page_list` | `hasAnyAuthority('app_admin','app_user_authorization:all', 'app_user_authorization:get_app_user_authorization')` |
| app_user_authorization/AppUserAuthorizationSubappApiController | `POST /offline_app_user_authorization` | `hasAnyAuthority('app_admin', 'app_user_authorization:all', 'app_user_authorization:offline')` |
| app_user_authorization/AppUserAuthorizationSubappApiController | `POST /offline_all_app_user_authorization` | `hasAnyAuthority('app_admin', 'app_user_authorization:all', 'app_user_authorization:offline_all')` |
| app_user_tag/AppUserTagSubappApiController | `POST /get_app_user_tag_list` | `hasAnyAuthority('app_admin', 'app_user_tag:all', 'app_user_tag:read')` |
| app_user_tag/AppUserTagSubappApiController | `POST /get_app_user_tag_page_list` | `hasAnyAuthority('app_admin', 'app_user_tag:all', 'app_user_tag:read')` |
| app_user_tag/AppUserTagSubappApiController | `POST /get_app_user_tag_info` | `hasAnyAuthority('app_admin', 'app_user_tag:all', 'app_user_tag:read')` |
| app_user_tag/AppUserTagSubappApiController | `POST /create_app_user_tag` | `hasAnyAuthority('app_admin', 'app_user_tag:all', 'app_user_tag:create_app_user_tag')` |
| app_user_tag/AppUserTagSubappApiController | `POST /modify_app_user_tag_info` | `hasAnyAuthority('app_admin', 'app_user_tag:all', 'app_user_tag:modify_app_user_tag_info')` |
| app_user_tag/AppUserTagSubappApiController | `POST /modify_app_user_tag_status` | `hasAnyAuthority('app_admin', 'app_user_tag:all', 'app_user_tag:modify_app_user_tag_status')` |
| app_user_tag/AppUserTagSubappApiController | `POST /delete_app_user_tag` | `hasAnyAuthority('app_admin', 'app_user_tag:all', 'app_user_tag:delete_app_user_tag')` |
| client/ClientSubappApiController | `POST /get_current_app_client_list` | `isAuthenticated()` |
| dict/system/SystemDictSubappApiController | `POST /get_sys_dict_list` | `hasAnyAuthority('app_admin', 'sys_dict:all', 'sys_dict:read')` |
| dict/system/SystemDictSubappApiController | `POST /get_sys_dict_page_list` | `hasAnyAuthority('app_admin', 'sys_dict:all', 'sys_dict:read')` |
| dict/system/SystemDictSubappApiController | `POST /get_sys_dict_detail_list` | `hasAnyAuthority('app_admin', 'sys_dict:all', 'sys_dict:read')` |
| dict/system/SystemDictSubappApiController | `POST /get_sys_dict_info` | `hasAnyAuthority('app_admin', 'sys_dict:all', 'sys_dict:read')` |
| dict/system/SystemDictSubappApiController | `POST /get_sys_dict_detail_info` | `hasAnyAuthority('app_admin', 'sys_dict:all', 'sys_dict:read')` |
| dict/system/SystemDictSubappApiController | `POST /get_sys_dict_item_info` | `hasAnyAuthority('app_admin', 'sys_dict:all', 'sys_dict:read')` |
| dict/system/SystemDictSubappApiController | `POST /get_sys_dict_sub_item_list` | `hasAnyAuthority('app_admin', 'sys_dict:all', 'sys_dict:read')` |
| dict/system/SystemDictSubappApiController | `POST /get_sys_dict_item_page_list` | `hasAnyAuthority('app_admin', 'sys_dict:all', 'sys_dict:read')` |
| dict/system/SystemDictSubappApiController | `POST /get_sys_dict_sub_item_tree_list` | `hasAnyAuthority('app_admin', 'sys_dict:all', 'sys_dict:read')` |
| dict/system/SystemDictSubappApiController | `POST /modify_sys_dict_info` | `hasAnyAuthority('app_admin', 'sys_dict:all', 'sys_dict:modify_dict_info')` |
| dict/system/SystemDictSubappApiController | `POST /modify_sys_dict_icon` | `hasAnyAuthority('app_admin', 'sys_dict:all', 'sys_dict:modify_dict_icon')` |
| dict/system/SystemDictSubappApiController | `POST /put_sys_dict_item` | `hasAnyAuthority('app_admin', 'sys_dict:all', 'sys_dict:put_sys_dict_item')` |
| dict/system/SystemDictSubappApiController | `POST /modify_sys_dict_item_info` | `hasAnyAuthority('app_admin', 'sys_dict:all', 'sys_dict:modify_sys_dict_item_info')` |
| dict/system/SystemDictSubappApiController | `POST /modify_sys_dict_item_icon` | `hasAnyAuthority('app_admin', 'sys_dict:all', 'sys_dict:modify_sys_dict_item_icon')` |
| dict/system/SystemDictSubappApiController | `POST /modify_sys_dict_item_status` | `hasAnyAuthority('app_admin', 'sys_dict:all', 'sys_dict:modify_sys_dict_item_status')` |
| dict/system/SystemDictSubappApiController | `POST /move_sys_dict_item` | `hasAnyAuthority('app_admin', 'sys_dict:all', 'sys_dict:move_sys_dict_item')` |
| dict/system/SystemDictSubappApiController | `POST /delete_sys_dict` | `hasAnyAuthority('app_admin', 'sys_dict:all', 'sys_dict:delete_sys_dict')` |
| dict/system/SystemDictSubappApiController | `POST /delete_sys_dict_item` | `hasAnyAuthority('app_admin', 'sys_dict:all', 'sys_dict:delete_sys_dict_item')` |
| dict/system/SystemDictSubappApiController | `POST /sync_sys_dict` | `hasAnyAuthority('app_admin', 'sys_dict:all', 'sys_dict:sync_sys_dict')` |
| dict/system/SystemDictSubappApiController | `POST /copy_sys_dict_by_dict_id` | `hasAnyAuthority('app_admin', 'sys_dict:all', 'sys_dict:copy_sys_dict')` |
| endpoint/EndpointSubappApiController | `POST /get_endpoint_list` | `isAuthenticated()` |
| file/app_file/AppFileSubappApiController | `POST /list_file` | `hasAnyAuthority('app_admin', 'app_file:all', 'app_file:list_file')` |
| file/app_file/AppFileSubappApiController | `POST /get_folder_list` | `hasAnyAuthority('app_admin', 'app_file:all', 'app_file:get_folder')` |
| file/app_file/AppFileSubappApiController | `POST /get_folder_tree_list` | `hasAnyAuthority('app_admin', 'app_file:all', 'app_file:get_folder')` |
| file/app_file/AppFileSubappApiController | `POST /access_file` | `hasAnyAuthority('app_admin', 'app_file:all', 'app_file:access_file')` |
| file/app_file/AppFileSubappApiController | `POST /upload_file` | `hasAnyAuthority('app_admin', 'app_file:all', 'app_file:upload_file')` |
| file/app_file/AppFileSubappApiController | `POST /upload_files` | `hasAnyAuthority('app_admin', 'app_file:all', 'app_file:upload_file')` |
| file/app_file/AppFileSubappApiController | `POST /get_upload_file_sign` | `hasAnyAuthority('app_admin', 'app_file:all', 'app_file:upload_file')` |
| file/app_file/AppFileSubappApiController | `POST /get_upload_file_sign_url` | `hasAnyAuthority('app_admin', 'app_file:all', 'app_file:upload_file')` |
| file/app_file/AppFileSubappApiController | `POST /move_file` | `hasAnyAuthority('app_admin', 'app_file:all', 'app_file:move_file')` |
| file/app_file/AppFileSubappApiController | `POST /delete_file` | `hasAnyAuthority('app_admin', 'app_file:all', 'app_file:delete_file')` |
| file/public_file/PublicFileSubappApiController | `POST /access_file` | `hasAnyAuthority('app_admin', 'public_file:all', 'public_file:access_file')` |
| file/public_file/PublicFileSubappApiController | `POST /get_file_stat` | `hasAnyAuthority('app_admin', 'public_file:all', 'public_file:get_file_stat')` |
| file/public_file/PublicFileSubappApiController | `POST /upload_file` | `hasAnyAuthority('app_admin', 'public_file:all', 'public_file:upload_file')` |
| file/public_file/PublicFileSubappApiController | `POST /upload_files` | `hasAnyAuthority('app_admin', 'public_file:all', 'public_file:upload_file')` |
| file/public_file/PublicFileSubappApiController | `POST /get_upload_file_sign` | `hasAnyAuthority('app_admin', 'public_file:all', 'public_file:upload_file')` |
| file/public_file/PublicFileSubappApiController | `POST /get_upload_file_sign_url` | `hasAnyAuthority('app_admin', 'public_file:all', 'public_file:upload_file')` |
| file/public_file/PublicFileSubappApiController | `POST /delete_file` | `hasAnyAuthority('app_admin', 'public_file:all', 'public_file:delete_file')` |
| file/temporary_file/TemporaryFileSubappApiController | `POST /access_file` | `hasAnyAuthority('app_admin', 'temporary_file:all', 'temporary_file:access_file')` |
| file/temporary_file/TemporaryFileSubappApiController | `POST /get_file_stat` | `hasAnyAuthority('app_admin', 'temporary_file:all', 'temporary_file:get_file_stat')` |
| file/temporary_file/TemporaryFileSubappApiController | `POST /upload_file` | `hasAnyAuthority('app_admin', 'temporary_file:all', 'temporary_file:upload_file')` |
| file/temporary_file/TemporaryFileSubappApiController | `POST /upload_files` | `hasAnyAuthority('app_admin', 'temporary_file:all', 'temporary_file:upload_file')` |
| file/temporary_file/TemporaryFileSubappApiController | `POST /get_upload_file_sign` | `hasAnyAuthority('app_admin', 'temporary_file:all', 'temporary_file:upload_file')` |
| file/temporary_file/TemporaryFileSubappApiController | `POST /get_upload_file_sign_url` | `hasAnyAuthority('app_admin', 'temporary_file:all', 'temporary_file:upload_file')` |
| file/temporary_file/TemporaryFileSubappApiController | `POST /delete_file` | `hasAnyAuthority('app_admin', 'temporary_file:all', 'temporary_file:delete_file')` |
| menu/MenuSubappApiController | `POST /get_menu_tree_list` | `hasAnyAuthority('app_admin', 'menu:all', 'menu:read')` |
| menu/MenuSubappApiController | `POST /get_menu_list` | `hasAnyAuthority('app_admin', 'menu:all', 'menu:read')` |
| menu/MenuSubappApiController | `POST /get_menu_page_list` | `hasAnyAuthority('app_admin', 'menu:all', 'menu:read')` |
| permission/PermissionSubappApiController | `POST /get_permission_list` | `hasAnyAuthority('app_admin', 'permission:all', 'permission:read')` |
| permission/PermissionSubappApiController | `POST /get_permission_page_list` | `hasAnyAuthority('app_admin', 'permission:all', 'permission:read')` |
| subapp/SubappSubappApiController | `POST /get_subapp_list` | `hasAnyAuthority('app_admin', 'subapp:all', 'subapp:read')` |
| subapp/SubappSubappApiController | `POST /get_subapp_page_list` | `hasAnyAuthority('app_admin', 'subapp:all', 'subapp:read')` |
| subapp/SubappSubappApiController | `POST /create_subapp` | `hasAnyAuthority('app_admin', 'subapp:all', 'subapp:create_subapp')` |
| subapp/SubappSubappApiController | `POST /modify_subapp_info` | `hasAnyAuthority('app_admin', 'subapp:all', 'subapp:modify_subapp_info')` |
| subapp/SubappSubappApiController | `POST /modify_subapp_status` | `hasAnyAuthority('app_admin', 'subapp:all', 'subapp:modify_subapp_status')` |
| subapp/SubappSubappApiController | `POST /move_subapp` | `hasAnyAuthority('app_admin', 'subapp:all', 'subapp:move_subapp')` |
| subapp/SubappSubappApiController | `POST /delete_subapp` | `hasAnyAuthority('app_admin', 'subapp:all', 'subapp:delete_subapp')` |
| tenant/TenantSubappApiController | `POST /get_tenant_list` | `hasAnyAuthority('app_admin', 'tenant:all', 'tenant:read')` |
| tenant/TenantSubappApiController | `POST /get_tenant_page_list` | `hasAnyAuthority('app_admin', 'tenant:all', 'tenant:read')` |
| tenant/TenantSubappApiController | `POST /create_tenant` | `hasAnyAuthority('app_admin', 'tenant:all', 'tenant:create_tenant')` |
| tenant/TenantSubappApiController | `POST /modify_tenant_info` | `hasAnyAuthority('app_admin', 'tenant:all', 'tenant:modify_tenant_info')` |
| tenant/TenantSubappApiController | `POST /modify_tenant_owner` | `hasAnyAuthority('app_admin', 'tenant:all', 'tenant:modify_tenant_owner')` |
| tenant/TenantSubappApiController | `POST /modify_tenant_status` | `hasAnyAuthority('app_admin', 'tenant:all', 'tenant:modify_tenant_status')` |
| tenant/TenantSubappApiController | `POST /delete_tenant` | `hasAnyAuthority('app_admin', 'tenant:all', 'tenant:delete_tenant')` |
| tenant_app/TenantAppSubappApiController | `POST /get_tenant_app_list` | `hasAnyAuthority('app_admin', 'tenant_app:all', 'tenant_app:read')` |
| tenant_app/TenantAppSubappApiController | `POST /get_tenant_app_page_list` | `hasAnyAuthority('app_admin', 'tenant_app:all', 'tenant_app:read')` |
| tenant_app/TenantAppSubappApiController | `POST /create_tenant_app` | `hasAnyAuthority('app_admin', 'tenant_app:all', 'tenant_app:create_tenant_app')` |
| tenant_app/TenantAppSubappApiController | `POST /modify_tenant_app_info` | `hasAnyAuthority('app_admin', 'tenant_app:all', 'tenant_app:modify_tenant_app_info')` |
| tenant_app/TenantAppSubappApiController | `POST /modify_tenant_app_status` | `hasAnyAuthority('app_admin', 'tenant_app:all', 'tenant_app:modify_tenant_app_status')` |
| tenant_app/TenantAppSubappApiController | `POST /delete_tenant_app` | `hasAnyAuthority('app_admin', 'tenant_app:all', 'tenant_app:delete_tenant_app')` |
| tenant_app_department_template/TenantAppDepartmentTemplateSubappApiController | `POST /get_tenant_app_department_template_list` | `hasAnyAuthority('app_admin', 'tenant_app_department_template:all', 'tenant_app_department_template:read')` |
| tenant_app_department_template/TenantAppDepartmentTemplateSubappApiController | `POST /get_tenant_app_department_template_page_list` | `hasAnyAuthority('app_admin', 'tenant_app_department_template:all', 'tenant_app_department_template:read')` |
| tenant_app_department_template/TenantAppDepartmentTemplateSubappApiController | `POST /get_path_tenant_app_department_template_list` | `hasAnyAuthority('app_admin', 'tenant_app_department_template:all', 'tenant_app_department_template:read')` |
| tenant_app_department_template/TenantAppDepartmentTemplateSubappApiController | `POST /get_path_tenant_app_department_template_page_list` | `hasAnyAuthority('app_admin', 'tenant_app_department_template:all', 'tenant_app_department_template:read')` |
| tenant_app_department_template/TenantAppDepartmentTemplateSubappApiController | `POST /get_tenant_app_department_template_tree` | `hasAnyAuthority('app_admin', 'tenant_app_department_template:all', 'tenant_app_department_template:read')` |
| tenant_app_department_template/TenantAppDepartmentTemplateSubappApiController | `POST /get_tenant_app_department_template_by_department_id` | `hasAnyAuthority('app_admin', 'tenant_app_department_template:all', 'tenant_app_department_template:read')` |
| tenant_app_department_template/TenantAppDepartmentTemplateSubappApiController | `POST /create_tenant_app_department_template` | `hasAnyAuthority('app_admin', 'tenant_app_department_template:all', 'tenant_app_department_template:create_tenant_app_department_template')` |
| tenant_app_department_template/TenantAppDepartmentTemplateSubappApiController | `POST /modify_tenant_app_department_template_info` | `hasAnyAuthority('app_admin', 'tenant_app_department_template:all', 'tenant_app_department_template:modify_tenant_app_department_template_info')` |
| tenant_app_department_template/TenantAppDepartmentTemplateSubappApiController | `POST /move_tenant_app_department_template` | `hasAnyAuthority('app_admin', 'tenant_app_department_template:all', 'department:move_tenant_app_department_template')` |
| tenant_app_department_template/TenantAppDepartmentTemplateSubappApiController | `POST /delete_tenant_app_department_template` | `hasAnyAuthority('app_admin', 'tenant_app_department_template:all', 'tenant_app_department_template:delete_tenant_app_department_template')` |
| tenant_app_department_template/TenantAppDepartmentTemplateSubappApiController | `POST /modify_tenant_app_department_template_status` | `hasAnyAuthority('app_admin', 'tenant_app_department_template:all', 'tenant_app_department_template:modify_tenant_app_department_template_status')` |
| tenant_app_department_template/TenantAppDepartmentTemplateSubappApiController | `POST /get_tenant_app_department_template_status` | `hasAnyAuthority('app_admin', 'tenant_app_department_template:all', 'tenant_app_department_template:read')` |
| tenant_app_role_template/TenantAppRoleTemplateSubappApiController | `POST /get_tenant_app_role_template_list` | `hasAnyAuthority('app_admin', 'tenant_app_role_template:all', 'tenant_app_role_template:read')` |
| tenant_app_role_template/TenantAppRoleTemplateSubappApiController | `POST /get_tenant_app_role_template_page_list` | `hasAnyAuthority('app_admin', 'tenant_app_role_template:all', 'tenant_app_role_template:read')` |
| tenant_app_role_template/TenantAppRoleTemplateSubappApiController | `POST /get_tenant_app_role_template_info` | `hasAnyAuthority('app_admin', 'tenant_app_role_template:all', 'tenant_app_role_template:read')` |
| tenant_app_role_template/TenantAppRoleTemplateSubappApiController | `POST /get_tenant_app_role_template_permission` | `hasAnyAuthority('app_admin', 'tenant_app_role_template:all', 'tenant_app_role_template:read')` |
| tenant_app_role_template/TenantAppRoleTemplateSubappApiController | `POST /create_tenant_app_role_template` | `hasAnyAuthority('app_admin', 'tenant_app_role_template:all', 'tenant_app_role_template:create_tenant_app_role_template')` |
| tenant_app_role_template/TenantAppRoleTemplateSubappApiController | `POST /modify_tenant_app_role_template_info` | `hasAnyAuthority('app_admin', 'tenant_app_role_template:all', 'tenant_app_role_template:modify_tenant_app_role_template_info')` |
| tenant_app_role_template/TenantAppRoleTemplateSubappApiController | `POST /modify_tenant_app_role_template_permission` | `hasAnyAuthority('app_admin', 'tenant_app_role_template:all', 'tenant_app_role_template:modify_tenant_app_role_template_permission')` |
| tenant_app_role_template/TenantAppRoleTemplateSubappApiController | `POST /modify_tenant_app_role_template_status` | `hasAnyAuthority('app_admin', 'tenant_app_role_template:all', 'tenant_app_role_template:modify_tenant_app_role_template_status')` |
| tenant_app_role_template/TenantAppRoleTemplateSubappApiController | `POST /delete_tenant_app_role_template` | `hasAnyAuthority('app_admin', 'tenant_app_role_template:all', 'tenant_app_role_template:delete_tenant_app_role_template')` |
| tenant_app_role_template/TenantAppRoleTemplateSubappApiController | `POST /get_tenant_app_role_template_subapp_version` | `hasAnyAuthority('app_admin', 'tenant_app_role_template:all', 'tenant_app_role_template:read')` |
| tenant_app_role_template/TenantAppRoleTemplateSubappApiController | `POST /delete_tenant_app_role_template_permission` | `hasAnyAuthority('app_admin', 'tenant_app_role_template:all', 'tenant_app_role_template:delete_tenant_app_role_template_permission')` |
| tenant_app_user_template/TenantAppUserTemplateSubappApiController | `POST /get_tenant_app_user_template_list` | `hasAnyAuthority('app_admin', 'tenant_app_user_template:all', 'tenant_app_user_template:read')` |
| tenant_app_user_template/TenantAppUserTemplateSubappApiController | `POST /get_tenant_app_user_template_page_list` | `hasAnyAuthority('app_admin', 'tenant_app_user_template:all', 'tenant_app_user_template:read')` |
| tenant_app_user_template/TenantAppUserTemplateSubappApiController | `POST /create_tenant_app_user_template` | `hasAnyAuthority('app_admin', 'tenant_app_user_template:all', 'tenant_app_user_template:create_tenant_app_user_template')` |
| tenant_app_user_template/TenantAppUserTemplateSubappApiController | `POST /create_account_and_tenant_app_user_template` | `hasAnyAuthority('app_admin', 'tenant_app_user_template:all', 'tenant_app_user_template:create_tenant_app_user_template')` |
| tenant_app_user_template/TenantAppUserTemplateSubappApiController | `POST /modify_tenant_app_user_template_info` | `hasAnyAuthority('app_admin', 'tenant_app_user_template:all', 'tenant_app_user_template:modify_tenant_app_user_template_info')` |
| tenant_app_user_template/TenantAppUserTemplateSubappApiController | `POST /modify_tenant_app_user_template_status` | `hasAnyAuthority('app_admin', 'tenant_app_user_template:all', 'tenant_app_user_template:modify_tenant_app_user_template_status')` |
| tenant_app_user_template/TenantAppUserTemplateSubappApiController | `POST /delete_tenant_app_user_template` | `hasAnyAuthority('app_admin', 'tenant_app_user_template:all', 'tenant_app_user_template:delete_tenant_app_user_template')` |
| tenant_endpoint/TenantEndpointSubappApiController | `POST /get_tenant_endpoint_list` | `hasAnyAuthority('app_admin', 'tenant_endpoint:all', 'tenant_endpoint:read')` |
| tenant_endpoint/TenantEndpointSubappApiController | `POST /get_tenant_endpoint_page_list` | `hasAnyAuthority('app_admin', 'tenant_endpoint:all', 'tenant_endpoint:read')` |
| tenant_endpoint/TenantEndpointSubappApiController | `POST /create_tenant_endpoint` | `hasAnyAuthority('app_admin', 'tenant_endpoint:all', 'tenant_endpoint:create_tenant_endpoint')` |
| tenant_endpoint/TenantEndpointSubappApiController | `POST /modify_tenant_endpoint_info` | `hasAnyAuthority('app_admin', 'tenant_endpoint:all', 'tenant_endpoint:modify_tenant_endpoint_info')` |
| tenant_endpoint/TenantEndpointSubappApiController | `POST /modify_tenant_endpoint_status` | `hasAnyAuthority('app_admin', 'tenant_endpoint:all', 'tenant_endpoint:modify_tenant_endpoint_status')` |
| tenant_endpoint/TenantEndpointSubappApiController | `POST /delete_tenant_endpoint` | `hasAnyAuthority('app_admin', 'tenant_endpoint:all', 'tenant_endpoint:delete_tenant_endpoint')` |
| tenant_subapp/TenantSubappSubappApiController | `POST /get_tenant_subapp_list` | `hasAnyAuthority('app_admin', 'tenant_subapp:all', 'tenant_subapp:read')` |
| tenant_subapp/TenantSubappSubappApiController | `POST /get_tenant_subapp_page_list` | `hasAnyAuthority('app_admin', 'tenant_subapp:all', 'tenant_subapp:read')` |
| tenant_subapp/TenantSubappSubappApiController | `POST /create_tenant_subapp` | `hasAnyAuthority('app_admin', 'tenant_subapp:all', 'tenant_subapp:create_tenant_subapp')` |
| tenant_subapp/TenantSubappSubappApiController | `POST /modify_tenant_subapp_status` | `hasAnyAuthority('app_admin', 'tenant_subapp:all', 'tenant_subapp:modify_tenant_subapp_status')` |
| tenant_subapp/TenantSubappSubappApiController | `POST /delete_tenant_subapp` | `hasAnyAuthority('app_admin', 'tenant_subapp:all', 'tenant_subapp:delete_tenant_subapp')` |

### endpoint 面 — `app_user_api` 主体类型 `APP_USER`

Controller 13 个,端点 31 个。

| Controller | 端点 | 方法级防护 |
|---|---|---|
| account/AccountEndpointApiController | `POST /modify_my_account_username` | `isAuthenticated()` |
| account/AccountEndpointApiController | `POST /modify_my_account_phone_number` | `isAuthenticated()` |
| account/AccountEndpointApiController | `POST /get_my_account_password_status` | `isAuthenticated()` |
| account/AccountEndpointApiController | `POST /modify_my_account_password` | `isAuthenticated()` |
| account/AccountEndpointApiController | `POST /modify_my_account_avatar` | `isAuthenticated()` |
| account_sns/AccountSnsEndpointApiController | `POST /get_my_account_sns_list` | (类型闸/principal 限定) |
| account_sns/AccountSnsEndpointApiController | `POST /bind_account_sns` | (类型闸/principal 限定) |
| account_sns/AccountSnsEndpointApiController | `POST /unbind_account_sns` | (类型闸/principal 限定) |
| app_endpoint/EndpointEndpointApiController | `POST /get_endpoint_list` | `isAuthenticated()` |
| app_user/AppUserEndpointApiController | `POST /get_my_app_user_info` | `isAuthenticated()` |
| app_user/AppUserEndpointApiController | `POST /modify_my_app_user_info` | `isAuthenticated()` |
| app_user/AppUserEndpointApiController | `POST /get_my_app_user_logoff_status` | `isAuthenticated()` |
| app_user/AppUserEndpointApiController | `POST /get_my_app_user_pre_logoff_info` | `isAuthenticated()` |
| app_user/AppUserEndpointApiController | `POST /logoff_my_app_user` | `isAuthenticated()` |
| app_user/AppUserEndpointApiController | `POST /unlogoff_my_app_user` | `isAuthenticated()` |
| app_user_authorization/AppUserAuthorizationEndpointApiController | `POST /get_my_app_user_authorization_list` | `isAuthenticated()` |
| app_user_authorization/AppUserAuthorizationEndpointApiController | `POST /get_my_app_user_authorization_page_list` | `isAuthenticated()` |
| app_user_authorization/AppUserAuthorizationEndpointApiController | `POST /register_my_app_user_device` | `isAuthenticated()` |
| app_user_authorization/AppUserAuthorizationEndpointApiController | `POST /offline_my_app_user_authorization` | `isAuthenticated()` |
| app_user_authorization/AppUserAuthorizationEndpointApiController | `POST /logout_app_user_authorization` | `isAuthenticated()` |
| biz_log/app_biz_log/AppBizLogAppApiController | `POST /get_my_app_biz_log_page_list` | `isAuthenticated()` |
| biz_log/subapp_biz_log/SubappBizLogAppApiController | `POST /get_my_subapp_biz_log_page_list` | `isAuthenticated()` |
| biz_log/subapp_biz_log/SubappBizLogAppApiController | `POST /get_my_subapp_biz_log_list` | `isAuthenticated()` |
| client/ClientEndpointApiController | `POST /get_client_list` | `isAuthenticated()` |
| login_log/app_user_login_log/AppUserLoginLogEndpointApiController | `POST /get_my_app_user_login_log_page_list` | `isAuthenticated()` |
| subapp/SubappEndpointApiController | `POST /get_subapp_list` | `isAuthenticated()` |
| subapp_version/SubappVersionEndpointApiController | `POST /get_subapp_version_list` | `isAuthenticated()` |
| verify_code/VerifyCodeEndpointApiController | `POST /send_my_account_phone_number_verify_code` | 验证码 |
| wxmp/app_user/WxmpAppUserEndpointApiController | `POST /get_my_app_user_wxmp` | (类型闸/principal 限定) |
| wxmp/app_user/WxmpAppUserEndpointApiController | `POST /bind_app_user_wxmp` | (类型闸/principal 限定) |
| wxmp/app_user/WxmpAppUserEndpointApiController | `POST /unbind_app_user_wxmp` | (类型闸/principal 限定) |

### tenant_endpoint 面 — `tenant_app_user_api` 主体类型 `TENANT_APP_USER`

Controller 13 个,端点 32 个。

| Controller | 端点 | 方法级防护 |
|---|---|---|
| account/AccountTenantEndpointApiController | `POST /get_my_account_password_status` | `isAuthenticated()` |
| account/AccountTenantEndpointApiController | `POST /modify_my_account_password` | `isAuthenticated()` |
| account/AccountTenantEndpointApiController | `POST /modify_my_account_avatar` | `isAuthenticated()` |
| account/AccountTenantEndpointApiController | `POST /modify_my_account_phone_number` | `isAuthenticated()` |
| account/AccountTenantEndpointApiController | `POST /modify_my_account_username` | `isAuthenticated()` |
| account_sns/AccountSnsTenantEndpointApiController | `POST /get_my_account_sns_list` | `isAuthenticated()` |
| account_sns/AccountSnsTenantEndpointApiController | `POST /bind_account_sns` | `isAuthenticated()` |
| account_sns/AccountSnsTenantEndpointApiController | `POST /unbind_account_sns` | `isAuthenticated()` |
| auth_code/AuthCodeTenantEndpointApiController | `POST /get_auth_code_by_verify_password` | `isAuthenticated()` |
| biz_log/tenant_app_biz_log/TenantAppBizLogTenantAppApiController | `POST /get_my_tenant_app_biz_log_page_list` | `isAuthenticated()` |
| biz_log/tenant_subapp_biz_log/TenantSubappBizLogTenantAppApiController | `POST /get_my_tenant_subapp_biz_log_page_list` | `isAuthenticated()` |
| biz_log/tenant_subapp_biz_log/TenantSubappBizLogTenantAppApiController | `POST /get_my_tenant_subapp_biz_log_list` | `isAuthenticated()` |
| client/ClientTenantEndpointApiController | `POST /get_current_app_client_list` | `isAuthenticated()` |
| endpoint/EndpointTenantEndpointApiController | `POST /get_endpoint_list` | `isAuthenticated()` |
| login_log/tenant_app_user_login_log/TenantAppUserLoginLogTenantEndpointApiController | `POST /get_my_tenant_app_user_login_log_page_list` | `isAuthenticated()` |
| tenant_app_user/TenantAppUserTenantEndpointApiController | `POST /get_my_tenant_app_user_info` | `isAuthenticated()` |
| tenant_app_user/TenantAppUserTenantEndpointApiController | `POST /get_my_tenant_app_user_authority` | `isAuthenticated()` |
| tenant_app_user/TenantAppUserTenantEndpointApiController | `POST /get_my_tenant_app_user_permission_ids` | `isAuthenticated()` |
| tenant_app_user/TenantAppUserTenantEndpointApiController | `POST /modify_my_tenant_app_user_info` | `isAuthenticated()` |
| tenant_app_user/TenantAppUserTenantEndpointApiController | `POST /get_my_tenant_app_user_logoff_status` | `isAuthenticated()` |
| tenant_app_user/TenantAppUserTenantEndpointApiController | `POST /get_my_tenant_app_user_pre_logoff_info` | `isAuthenticated()` |
| tenant_app_user/TenantAppUserTenantEndpointApiController | `POST /logoff_my_tenant_app_user` | `isAuthenticated()` |
| tenant_app_user/TenantAppUserTenantEndpointApiController | `POST /unlogoff_my_tenant_app_user` | `isAuthenticated()` |
| tenant_app_user_authorization/TenantAppUserAuthorizationTenantEndpointApiController | `POST /get_my_tenant_app_user_authorization_list` | `isAuthenticated()` |
| tenant_app_user_authorization/TenantAppUserAuthorizationTenantEndpointApiController | `POST /get_my_tenant_app_user_authorization_page_list` | `isAuthenticated()` |
| tenant_app_user_authorization/TenantAppUserAuthorizationTenantEndpointApiController | `POST /register_my_tenant_app_user_device` | `isAuthenticated()` |
| tenant_app_user_authorization/TenantAppUserAuthorizationTenantEndpointApiController | `POST /offline_my_tenant_app_user_authorization` | `isAuthenticated()` |
| tenant_app_user_authorization/TenantAppUserAuthorizationTenantEndpointApiController | `POST /logout_tenant_app_user_authorization` | `isAuthenticated()` |
| tenant_endpoint/TenantEndpointTenantEndpointApiController | `POST /get_current_tenant_endpoint_list` | `isAuthenticated()` |
| verify_code/VerifyCodeTenantEndpointApiController | `POST /send_my_account_phone_number_verify_code` | 验证码 |
| wxmp/tenant_app_user/WxmpTenantAppUserTenantEndpointApiController | `POST /bind_tenant_app_user` | (类型闸/principal 限定) |
| wxmp/tenant_app_user/WxmpTenantAppUserTenantEndpointApiController | `POST /unbind_tenant_app_user` | (类型闸/principal 限定) |

### tenant_subapp 面 — `tenant_subapp_user_api` 主体类型 `TENANT_SUBAPP_USER`

Controller 17 个,端点 114 个。

| Controller | 端点 | 方法级防护 |
|---|---|---|
| account/AccountTenantSubappApiController | `POST /get_account_list` | `hasAnyAuthority('app_admin', 'account:all', 'account:read')` |
| account/AccountTenantSubappApiController | `POST /get_account_page_list` | `hasAnyAuthority('app_admin', 'account:all', 'account:read')` |
| account/AccountTenantSubappApiController | `POST /search_account_info` | `hasAnyAuthority('app_admin', 'account:all', 'account:read')` |
| account/AccountTenantSubappApiController | `POST /get_account_info` | `hasAnyAuthority('app_admin', 'account:all', 'account:read')` |
| dict/biz/BizDictTenantSubappApiController | `POST /get_biz_dict_list` | `hasAnyAuthority('app_admin', 'biz_dict:all', 'biz_dict:read')` |
| dict/biz/BizDictTenantSubappApiController | `POST /get_biz_dict_page_list` | `hasAnyAuthority('app_admin', 'biz_dict:all', 'biz_dict:read')` |
| dict/biz/BizDictTenantSubappApiController | `POST /get_biz_dict_detail_list` | `hasAnyAuthority('app_admin', 'biz_dict:all', 'biz_dict:read')` |
| dict/biz/BizDictTenantSubappApiController | `POST /get_biz_dict_info` | `hasAnyAuthority('app_admin', 'biz_dict:all', 'biz_dict:read')` |
| dict/biz/BizDictTenantSubappApiController | `POST /get_biz_dict_detail_info` | `hasAnyAuthority('app_admin', 'biz_dict:all', 'biz_dict:read')` |
| dict/biz/BizDictTenantSubappApiController | `POST /get_biz_dict_item_info` | `hasAnyAuthority('app_admin', 'biz_dict:all', 'biz_dict:read')` |
| dict/biz/BizDictTenantSubappApiController | `POST /get_biz_dict_sub_item_list` | `hasAnyAuthority('app_admin', 'biz_dict:all', 'biz_dict:read')` |
| dict/biz/BizDictTenantSubappApiController | `POST /get_biz_dict_item_page_list` | `hasAnyAuthority('app_admin', 'biz_dict:all', 'biz_dict:read')` |
| dict/biz/BizDictTenantSubappApiController | `POST /get_biz_dict_sub_item_tree_list` | `hasAnyAuthority('app_admin', 'biz_dict:all', 'biz_dict:read')` |
| dict/biz/BizDictTenantSubappApiController | `POST /put_biz_dict_item` | `hasAnyAuthority('app_admin', 'biz_dict:all', 'biz_dict:put_biz_dict_item')` |
| dict/biz/BizDictTenantSubappApiController | `POST /modify_biz_dict_item_info` | `hasAnyAuthority('app_admin', 'biz_dict:all', 'biz_dict:modify_biz_dict_item_info')` |
| dict/biz/BizDictTenantSubappApiController | `POST /modify_biz_dict_item_icon` | `hasAnyAuthority('app_admin', 'biz_dict:all', 'biz_dict:modify_biz_dict_item_icon')` |
| dict/biz/BizDictTenantSubappApiController | `POST /modify_biz_dict_item_status` | `hasAnyAuthority('app_admin', 'biz_dict:all', 'biz_dict:modify_biz_dict_item_status')` |
| dict/biz/BizDictTenantSubappApiController | `POST /delete_biz_dict_item` | `hasAnyAuthority('app_admin', 'biz_dict:all', 'biz_dict:delete_biz_dict_item')` |
| dict/biz/BizDictTenantSubappApiController | `POST /restore_biz_dict` | `hasAnyAuthority('app_admin', 'biz_dict:all', 'biz_dict:restore_biz_dict')` |
| dict/system/SystemDictTenantSubappApiController | `POST /get_sys_dict_list` | `hasAnyAuthority('app_admin', 'biz_dict:all', 'biz_dict:read')` |
| dict/system/SystemDictTenantSubappApiController | `POST /get_sys_dict_page_list` | `hasAnyAuthority('app_admin', 'biz_dict:all', 'biz_dict:read')` |
| dict/system/SystemDictTenantSubappApiController | `POST /get_sys_dict_detail_list` | `hasAnyAuthority('app_admin', 'biz_dict:all', 'biz_dict:read')` |
| dict/system/SystemDictTenantSubappApiController | `POST /get_sys_dict_info` | `hasAnyAuthority('app_admin', 'biz_dict:all', 'biz_dict:read')` |
| dict/system/SystemDictTenantSubappApiController | `POST /get_sys_dict_detail_info` | `hasAnyAuthority('app_admin', 'biz_dict:all', 'biz_dict:read')` |
| dict/system/SystemDictTenantSubappApiController | `POST /get_sys_dict_item_info` | `hasAnyAuthority('app_admin', 'biz_dict:all', 'biz_dict:read')` |
| dict/system/SystemDictTenantSubappApiController | `POST /get_sys_dict_sub_item_list` | `hasAnyAuthority('app_admin', 'biz_dict:all', 'biz_dict:read')` |
| dict/system/SystemDictTenantSubappApiController | `POST /get_sys_dict_sub_item_tree_list` | `hasAnyAuthority('app_admin', 'biz_dict:all', 'biz_dict:read')` |
| file/public_file/PublicFileTenantSubappApiController | `POST /access_file` | `hasAnyAuthority('app_admin', 'public_file:all', 'public_file:access_file')` |
| file/public_file/PublicFileTenantSubappApiController | `POST /get_file_stat` | `hasAnyAuthority('app_admin', 'public_file:all', 'public_file:get_file_stat')` |
| file/public_file/PublicFileTenantSubappApiController | `POST /upload_file` | `hasAnyAuthority('app_admin', 'public_file:all', 'public_file:upload_file')` |
| file/public_file/PublicFileTenantSubappApiController | `POST /upload_files` | `hasAnyAuthority('app_admin', 'public_file:all', 'public_file:upload_file')` |
| file/public_file/PublicFileTenantSubappApiController | `POST /get_upload_file_sign` | `hasAnyAuthority('app_admin', 'public_file:all', 'public_file:upload_file')` |
| file/public_file/PublicFileTenantSubappApiController | `POST /get_upload_file_sign_url` | `hasAnyAuthority('app_admin', 'public_file:all', 'public_file:upload_file')` |
| file/public_file/PublicFileTenantSubappApiController | `POST /delete_file` | `hasAnyAuthority('app_admin', 'public_file:all', 'public_file:delete_file')` |
| file/temporary_file/TemporaryFileTenantSubappApiController | `POST /access_file` | `hasAnyAuthority('app_admin', 'temporary_file:all', 'temporary_file:access_file')` |
| file/temporary_file/TemporaryFileTenantSubappApiController | `POST /get_file_stat` | `hasAnyAuthority('app_admin', 'temporary_file:all', 'temporary_file:get_file_stat')` |
| file/temporary_file/TemporaryFileTenantSubappApiController | `POST /upload_file` | `hasAnyAuthority('app_admin', 'temporary_file:all', 'temporary_file:upload_file')` |
| file/temporary_file/TemporaryFileTenantSubappApiController | `POST /upload_files` | `hasAnyAuthority('app_admin', 'temporary_file:all', 'temporary_file:upload_file')` |
| file/temporary_file/TemporaryFileTenantSubappApiController | `POST /get_upload_file_sign` | `hasAnyAuthority('app_admin', 'temporary_file:all', 'temporary_file:upload_file')` |
| file/temporary_file/TemporaryFileTenantSubappApiController | `POST /get_upload_file_sign_url` | `hasAnyAuthority('app_admin', 'temporary_file:all', 'temporary_file:upload_file')` |
| file/temporary_file/TemporaryFileTenantSubappApiController | `POST /delete_file` | `hasAnyAuthority('app_admin', 'temporary_file:all', 'temporary_file:delete_file')` |
| file/tenant_app_file/TenantAppFileTenantSubappApiController | `POST /list_file` | `hasAnyAuthority('app_admin', 'tenant_app_file:all', 'tenant_app_file:list_file')` |
| file/tenant_app_file/TenantAppFileTenantSubappApiController | `POST /get_folder_list` | `hasAnyAuthority('app_admin', 'tenant_app_file:all', 'tenant_app_file:get_folder')` |
| file/tenant_app_file/TenantAppFileTenantSubappApiController | `POST /get_folder_tree_list` | `hasAnyAuthority('app_admin', 'tenant_app_file:all', 'tenant_app_file:get_folder')` |
| file/tenant_app_file/TenantAppFileTenantSubappApiController | `POST /mkdir` | `hasAnyAuthority('app_admin', 'tenant_app_file:all', 'tenant_app_file:mkdir')` |
| file/tenant_app_file/TenantAppFileTenantSubappApiController | `POST /access_file` | `hasAnyAuthority('app_admin', 'tenant_app_file:all', 'tenant_app_file:access_file')` |
| file/tenant_app_file/TenantAppFileTenantSubappApiController | `POST /get_file_stat` | `hasAnyAuthority('app_admin', 'tenant_app_file:all', 'tenant_app_file:get_file_stat')` |
| file/tenant_app_file/TenantAppFileTenantSubappApiController | `POST /upload_file` | `hasAnyAuthority('app_admin', 'tenant_app_file:all', 'tenant_app_file:upload_file')` |
| file/tenant_app_file/TenantAppFileTenantSubappApiController | `POST /upload_files` | `hasAnyAuthority('app_admin', 'tenant_app_file:all', 'tenant_app_file:upload_file')` |
| file/tenant_app_file/TenantAppFileTenantSubappApiController | `POST /get_upload_file_sign` | `hasAnyAuthority('app_admin', 'tenant_app_file:all', 'tenant_app_file:upload_file')` |
| file/tenant_app_file/TenantAppFileTenantSubappApiController | `POST /get_upload_file_sign_url` | `hasAnyAuthority('app_admin', 'tenant_app_file:all', 'tenant_app_file:upload_file')` |
| file/tenant_app_file/TenantAppFileTenantSubappApiController | `POST /move_file` | `hasAnyAuthority('app_admin', 'tenant_app_file:all', 'tenant_app_file:move_file')` |
| file/tenant_app_file/TenantAppFileTenantSubappApiController | `POST /delete_file` | `hasAnyAuthority('app_admin', 'tenant_app_file:all', 'tenant_app_file:delete_file')` |
| file/tenant_file/TenantFileTenantSubappApiController | `POST /access_file` | `hasAnyAuthority('app_admin', 'tenant_file:all', 'tenant_file:access_file')` |
| file/tenant_file/TenantFileTenantSubappApiController | `POST /get_file_stat` | `hasAnyAuthority('app_admin', 'public_file:all', 'tenant_file:get_file_stat')` |
| file/tenant_file/TenantFileTenantSubappApiController | `POST /upload_file` | `hasAnyAuthority('app_admin', 'tenant_file:all', 'tenant_file:upload_file')` |
| file/tenant_file/TenantFileTenantSubappApiController | `POST /upload_files` | `hasAnyAuthority('app_admin', 'tenant_file:all', 'tenant_file:upload_file')` |
| file/tenant_file/TenantFileTenantSubappApiController | `POST /get_upload_file_sign` | `hasAnyAuthority('app_admin', 'tenant_file:all', 'tenant_file:upload_file')` |
| file/tenant_file/TenantFileTenantSubappApiController | `POST /get_upload_file_sign_url` | `hasAnyAuthority('app_admin', 'tenant_file:all', 'tenant_file:upload_file')` |
| file/tenant_file/TenantFileTenantSubappApiController | `POST /delete_file` | `hasAnyAuthority('app_admin', 'tenant_file:all', 'tenant_file:delete_file')` |
| imgporxy/ImgProxyTenantSubappApiController | `POST /get_proxy_url` | `isAuthenticated()` |
| menu/MenuTenantSubappApiController | `POST /get_menu_tree_list` | `hasAnyAuthority('app_admin', 'menu:all', 'menu:read')` |
| menu/MenuTenantSubappApiController | `POST /get_menu_list` | `hasAnyAuthority('app_admin', 'menu:all', 'menu:read')` |
| menu/MenuTenantSubappApiController | `POST /get_menu_page_list` | `hasAnyAuthority('app_admin', 'menu:all', 'menu:read')` |
| permission/PermissionTenantSubappApiController | `POST /get_permission_list` | `hasAnyAuthority('app_admin', 'permission:all', 'permission:read')` |
| permission/PermissionTenantSubappApiController | `POST /get_permission_page_list` | `hasAnyAuthority('app_admin', 'permission:all', 'permission:read')` |
| tenant_app_department/TenantAppDepartmentTenantSubappApiController | `POST /get_tenant_app_department_list` | `hasAnyAuthority('app_admin', 'tenant_app_department:all', 'tenant_app_department:read')` |
| tenant_app_department/TenantAppDepartmentTenantSubappApiController | `POST /get_tenant_app_department_page_list` | `hasAnyAuthority('app_admin', 'tenant_app_department:all', 'tenant_app_department:read')` |
| tenant_app_department/TenantAppDepartmentTenantSubappApiController | `POST /get_path_tenant_app_department_list` | `hasAnyAuthority('app_admin', 'tenant_app_department:all', 'tenant_app_department:read')` |
| tenant_app_department/TenantAppDepartmentTenantSubappApiController | `POST /get_path_tenant_app_department_page_list` | `hasAnyAuthority('app_admin', 'tenant_app_department:all', 'tenant_app_department:read')` |
| tenant_app_department/TenantAppDepartmentTenantSubappApiController | `POST /get_tenant_app_department_tree` | `hasAnyAuthority('app_admin', 'tenant_app_department:all', 'tenant_app_department:read')` |
| tenant_app_department/TenantAppDepartmentTenantSubappApiController | `POST /get_tenant_app_department_by_department_id` | `hasAnyAuthority('app_admin', 'tenant_app_department:all', 'tenant_app_department:read')` |
| tenant_app_department/TenantAppDepartmentTenantSubappApiController | `POST /create_tenant_app_department` | `hasAnyAuthority('app_admin', 'tenant_app_department:all', 'tenant_app_department:create_tenant_app_department')` |
| tenant_app_department/TenantAppDepartmentTenantSubappApiController | `POST /modify_tenant_app_department_info` | `hasAnyAuthority('app_admin', 'tenant_app_department:all', 'tenant_app_department:modify_tenant_app_department_info')` |
| tenant_app_department/TenantAppDepartmentTenantSubappApiController | `POST /move_tenant_department` | `hasAnyAuthority('app_admin', 'tenant_app_department:all', 'tenant_app_department:move_tenant_app_department')` |
| tenant_app_department/TenantAppDepartmentTenantSubappApiController | `POST /delete_tenant_app_department` | `hasAnyAuthority('app_admin', 'tenant_app_department:all', 'tenant_app_department:delete_tenant_app_department')` |
| tenant_app_role/TenantAppRoleTenantSubappApiController | `POST /get_tenant_app_role_list` | `hasAnyAuthority('app_admin', 'tenant_app_role:all', 'tenant_app_role:read')` |
| tenant_app_role/TenantAppRoleTenantSubappApiController | `POST /get_tenant_app_role_page_list` | `hasAnyAuthority('app_admin', 'tenant_app_role:all', 'tenant_app_role:read')` |
| tenant_app_role/TenantAppRoleTenantSubappApiController | `POST /get_tenant_app_role_info` | `hasAnyAuthority('app_admin', 'tenant_app_role:all', 'tenant_app_role:read')` |
| tenant_app_role/TenantAppRoleTenantSubappApiController | `POST /get_tenant_app_role_permission` | `hasAnyAuthority('app_admin', 'tenant_app_role:all', 'tenant_app_role:read')` |
| tenant_app_role/TenantAppRoleTenantSubappApiController | `POST /create_tenant_app_role` | `hasAnyAuthority('app_admin', 'tenant_app_role:all', 'tenant_app_role:create_tenant_app_role')` |
| tenant_app_role/TenantAppRoleTenantSubappApiController | `POST /modify_tenant_app_role_info` | `hasAnyAuthority('app_admin', 'tenant_app_role:all', 'tenant_app_role:modify_tenant_app_role_info')` |
| tenant_app_role/TenantAppRoleTenantSubappApiController | `POST /modify_tenant_app_role_permission` | `hasAnyAuthority('app_admin', 'tenant_app_role:all', 'tenant_app_role:modify_tenant_app_role_permission')` |
| tenant_app_role/TenantAppRoleTenantSubappApiController | `POST /modify_tenant_app_role_status` | `hasAnyAuthority('app_admin', 'tenant_role:all', 'tenant_app_role:modify_tenant_app_role_status')` |
| tenant_app_role/TenantAppRoleTenantSubappApiController | `POST /delete_tenant_app_role` | `hasAnyAuthority('app_admin', 'tenant_app_role:all', 'tenant_app_role:delete_tenant_app_role')` |
| tenant_app_role/TenantAppRoleTenantSubappApiController | `POST /get_tenant_role_subapp_version` | `hasAnyAuthority('app_admin', 'tenant_app_role:all', 'tenant_app_role:read')` |
| tenant_app_role/TenantAppRoleTenantSubappApiController | `POST /delete_tenant_role_permission` | `hasAnyAuthority('app_admin', 'tenant_app_role:all', 'tenant_app_role:delete_tenant_role_permission')` |
| tenant_app_user/TenantAppUserTenantSubappApiController | `POST /get_my_tenant_subapp_user_menu` | `isAuthenticated()` |
| tenant_app_user/TenantAppUserTenantSubappApiController | `POST /get_my_tenant_subapp_user_authority` | `isAuthenticated()` |
| tenant_app_user/TenantAppUserTenantSubappApiController | `POST /get_my_tenant_subapp_user_permission_ids` | `isAuthenticated()` |
| tenant_app_user/TenantAppUserTenantSubappApiController | `POST /get_tenant_app_user_list` | `hasAnyAuthority('app_admin', 'tenant_app_user:all', 'tenant_app_user:read')` |
| tenant_app_user/TenantAppUserTenantSubappApiController | `POST /get_tenant_app_user_page_list` | `hasAnyAuthority('app_admin', 'tenant_app_user:all', 'tenant_app_user:read')` |
| tenant_app_user/TenantAppUserTenantSubappApiController | `POST /get_tenant_app_user_info` | `hasAnyAuthority('app_admin', 'tenant_app_user:all', 'tenant_app_user:read')` |
| tenant_app_user/TenantAppUserTenantSubappApiController | `POST /create_tenant_app_user` | `hasAnyAuthority('app_admin', 'tenant_app_user:all', 'tenant_app_user:create_tenant_app_user')` |
| tenant_app_user/TenantAppUserTenantSubappApiController | `POST /create_account_and_tenant_app_user` | `hasAnyAuthority('app_admin', 'tenant_app_user:all', 'tenant_app_user:create_tenant_app_user')` |
| tenant_app_user/TenantAppUserTenantSubappApiController | `POST /modify_tenant_app_user_info` | `hasAnyAuthority('app_admin', 'tenant_app_user:all', 'tenant_app_user:modify_tenant_app_user_info')` |
| tenant_app_user/TenantAppUserTenantSubappApiController | `POST /modify_tenant_app_user_status` | `hasAnyAuthority('app_admin', 'tenan_user:all', 'tenant_app_user:modify_tenant_app_user_status')` |
| tenant_app_user/TenantAppUserTenantSubappApiController | `POST /transfer_tenant_app_user_to_other_account` | `hasAnyAuthority('app_admin', 'tenant_app_user:all', 'tenant_app_user:transfer_tenant_app_user_to_other_account')` |
| tenant_app_user/TenantAppUserTenantSubappApiController | `POST /logoff_tenant_app_user` | `hasAnyAuthority('app_admin', 'tenant_app_user:all', 'tenant_app_user:logoff_tenant_app_user')` |
| tenant_app_user/TenantAppUserTenantSubappApiController | `POST /unlogoff_tenant_app_user` | `hasAnyAuthority('app_admin', 'tenant_app_user:all', 'tenant_app_user:unlogoff_tenant_app_user')` |
| tenant_app_user/TenantAppUserTenantSubappApiController | `POST /delete_tenant_app_user` | `hasAnyAuthority('app_admin', 'tenant_app_user:all', 'tenant_app_user:delete_tenant_app_user')` |
| tenant_app_user_authorization/TenantAppUserAuthorizationTenantSubappApiController | `POST /get_tenant_app_user_authorization_list` | `hasAnyAuthority('app_admin','tenant_app_user_authorization:all', 'tenant_app_user_authorization:get_tenant_app_user_authorization')` |
| tenant_app_user_authorization/TenantAppUserAuthorizationTenantSubappApiController | `POST /get_tenant_app_user_authorization_page_list` | `hasAnyAuthority('app_admin','tenant_app_user_authorization:all', 'tenant_app_user_authorization:get_tenant_app_user_authorization')` |
| tenant_app_user_authorization/TenantAppUserAuthorizationTenantSubappApiController | `POST /offline_tenant_app_user_authorization` | `hasAnyAuthority('app_admin', 'tenant_app_user_authorization:all', 'tenant_app_user_authorization:offline')` |
| tenant_app_user_authorization/TenantAppUserAuthorizationTenantSubappApiController | `POST /offline_all_tenant_app_user_authorization` | `hasAnyAuthority('app_admin', 'tenant_app_user_authorization:all', 'tenant_app_user_authorization:offline_all')` |
| tenant_app_user_tag/TenantAppUserTagTenantSubappApiController | `POST /get_tenant_app_user_tag_list` | `hasAnyAuthority('app_admin', 'tenant_app_user_tag:all', 'tenant_app_user_tag:read')` |
| tenant_app_user_tag/TenantAppUserTagTenantSubappApiController | `POST /get_tenant_app_user_tag_page_list` | `hasAnyAuthority('app_admin', 'tenant_app_user_tag:all', 'tenant_app_user_tag:read')` |
| tenant_app_user_tag/TenantAppUserTagTenantSubappApiController | `POST /get_tenant_app_user_tag_info` | `hasAnyAuthority('app_admin', 'tenant_app_user_tag:all', 'tenant_app_user_tag:read')` |
| tenant_app_user_tag/TenantAppUserTagTenantSubappApiController | `POST /create_tenant_app_user_tag` | `hasAnyAuthority('app_admin', 'tenant_app_user_tag:all', 'tenant_app_user_tag:create_tenant_app_user_tag')` |
| tenant_app_user_tag/TenantAppUserTagTenantSubappApiController | `POST /modify_tenant_app_user_tag_info` | `hasAnyAuthority('app_admin', 'tenant_app_user_tag:all', 'tenant_app_user_tag:modify_tenant_app_user_tag_info')` |
| tenant_app_user_tag/TenantAppUserTagTenantSubappApiController | `POST /modify_tenant_app_user_tag_status` | `hasAnyAuthority('app_admin', 'tenant_app_user_tag:all', 'tenant_app_user_tag:modify_tenant_app_user_tag_status')` |
| tenant_app_user_tag/TenantAppUserTagTenantSubappApiController | `POST /delete_tenant_app_user_tag` | `hasAnyAuthority('app_admin', 'tenant_app_user_tag:all', 'tenant_app_user_tag:delete_tenant_app_user_tag')` |
| tenant_endpoint/TenantEndpointTenantSubappApiController | `POST /get_tenant_endpoint_list` | `isAuthenticated()` |
| tenant_subapp/TenantSubappTenantSubappApiController | `POST /get_tenant_subapp_list` | `hasAnyAuthority('app_admin', 'tenant_subapp:all', 'tenant_subapp:read')` |

### account 面 — `account_api` 主体类型 `ACCOUNT`

Controller 5 个,端点 8 个。

| Controller | 端点 | 方法级防护 |
|---|---|---|
| account/AccountAccountApiController | `POST /get_my_account_info` | `isAuthenticated()` |
| account/AccountAccountApiController | `POST /modify_my_account_password` | `isAuthenticated()` |
| account/AccountAccountApiController | `POST /logoff_my_account` | `isAuthenticated()` |
| biz_log/account_biz_log/AccountBizLogAccountApiController | `POST /get_my_account_biz_log_page_list` | (类型闸/principal 限定) |
| login_log/account_login_log/AccountLoginLogAccountApiController | `POST /get_my_account_login_log_list` | `isAuthenticated()` |
| login_log/account_login_log/AccountLoginLogAccountApiController | `POST /get_my_account_login_log_page_list` | `isAuthenticated()` |
| tenant/TenantAccountApiController | `POST /get_my_tenant_list` | `isAuthenticated()` |
| tenant_app_user/TenantAppUserAccountApiController | `POST /get_my_tenant_app_user_list` | `isAuthenticated()` |

### 特例:weboffice — `` WPS-2 签名

Controller 1 个,端点 11 个。

| Controller | 端点 | 方法级防护 |
|---|---|---|
| weboffice/WebOfficeApiController | `GET /files/{fileId}` | **无** |
| weboffice/WebOfficeApiController | `GET /files/{fileId}/download` | **无** |
| weboffice/WebOfficeApiController | `GET /files/{fileId}/permission` | **无** |
| weboffice/WebOfficeApiController | `GET /files/{fileId}/versions` | **无** |
| weboffice/WebOfficeApiController | `GET /files/{fileId}/versions/{fileVersion}` | **无** |
| weboffice/WebOfficeApiController | `GET /files/{fileId}/versions/{fileVersion}/download` | **无** |
| weboffice/WebOfficeApiController | `GET /files/{fileId}/upload/prepare` | **无** |
| weboffice/WebOfficeApiController | `POST /files/{fileId}/upload/address` | **无** |
| weboffice/WebOfficeApiController | `POST /files/{fileId}/upload/complete` | **无** |
| weboffice/WebOfficeApiController | `GET /users` | **无** |
| weboffice/WebOfficeApiController | `GET /files/{fileId}/watermark` | **无** |

