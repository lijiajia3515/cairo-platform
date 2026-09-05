# 短信模板清单（阿里云 dysms）

全仓 SMS 发送统一走阿里云 dysmsapi（`modules/sms/msg/send_msg/SendMsgSmsService`）。代码不写死模板，只传 `bizId`（定义见 `constants/CairoAuthSmsConstants`），实际按 `appId + bizId` 查 `auth_sms_template` 集合取 `templateSign`（签名）、`templateCode`（阿里云模板 CODE）、`templateText`，变量经 `auth_sms_template_arg` 做名称映射（代码参数名 → 阿里云模板变量名）。

共 **15 个模板**，全部有真实调用链，一个都不能少。

## 1. 验证码类（阿里云模板类型：验证码）

| bizId | 触发场景 | 代码传参 | 建议模板内容 |
|---|---|---|---|
| `VerifyCode` | 登录/注册等所有验证码下发 | `code` | 您的验证码为${code}，5分钟内有效，请勿泄露。 |

## 2. 账号级（通知短信）

| bizId | 触发场景 | 代码传参 | 建议模板内容 |
|---|---|---|---|
| `RegisterAccountSuccess` | 注册账号成功 | `name` `username` `password` | 尊敬的${name}，您的账号${username}已注册成功。 |
| `LogoffAccount` | 注销账号通知 | `Account` `Day`（固定"3天"） | 您的账号${Account}已申请注销，${Day}后将永久删除。 |
| `LogoffAccountSuccess` | 注销账号成功通知 | `Account` | 您的账号${Account}已注销完成。 |
| `UnlogoffAccount` | 取消注销成功 | `Account` | 您的账号${Account}已恢复，注销申请已取消。 |

## 3. 应用级（通知短信）

| bizId | 触发场景 | 代码传参 | 建议模板内容 |
|---|---|---|---|
| `RegisterAppUserSuccess` | 注册应用级用户成功 | `Account` `User` | 账号${Account}已开通应用级用户${User}。 |
| `LogoffAppUser` | 注销应用级用户通知 | `Account` `User` `Day`（固定"3天"） | 账号${Account}的应用级用户${User}已申请注销，${Day}后删除。 |
| `LogoffAppUserSuccess` | 注销应用级用户成功 | `Account` `User` | 账号${Account}的应用级用户${User}已注销完成。 |
| `UnlogoffAppUser` | 取消注销应用级用户 | `Account` `User` | 账号${Account}的应用级用户${User}已恢复。 |

## 4. 企业级 + 企业应用级（通知短信）

| bizId | 触发场景 | 代码传参 | 建议模板内容 |
|---|---|---|---|
| `RegisterTenantSuccess` | 注册企业成功 | `Account` `Tenant` | 账号${Account}的企业${Tenant}注册成功。 |
| `ApplyNewTenantAppSuccess` | 申请企业应用成功 | `Account` `Tenant` | 账号${Account}的企业${Tenant}应用开通成功。 |
| `RegisterTenantAppUserSuccess` | 注册企业应用级用户成功 | `Account` `Tenant` `User` | 账号${Account}在企业${Tenant}开通用户${User}成功。 |
| `LogoffTenantAppUser` | 注销企业应用级用户通知 | `Account` `Tenant` `User` `Day` | 账号${Account}在企业${Tenant}的用户${User}申请注销，${Day}后删除。 |
| `LogoffTenantAppUserSuccess` | 注销企业应用级用户成功 | `Account` `Tenant` `User` | 账号${Account}在企业${Tenant}的用户${User}已注销完成。 |
| `UnlogoffTenantAppUser` | 取消注销企业应用级用户 | `Account` `Tenant` `User` | 账号${Account}在企业${Tenant}的用户${User}已恢复。 |

## 初始化步骤

1. **先申请签名，再申请模板**。签名建议 `cairo数据`（与品牌前缀一致）；签名与模板一对多绑定，`templateText` 不含签名内容。
2. **变量名可自由定**：阿里云模板里的 `${xxx}` 不必和代码参数名一致，录入 DB 时通过 `auth_sms_template_arg` 配置映射（如代码 `Account` → 模板 `account`）。
3. 模板审批通过后，在管理页「短信模板」逐条录入 `auth_sms_template`（`bizId` + `templateCode` + `templateSign` + `templateText`），`enabled` 置 `true` 才会真正发送。
4. DB 中 `templateText` 与阿里云最终审核通过的模板内容保持一致（发送记录渲染按 `${var}` 替换）。

> `db/data/` 无 `auth_sms_template` 种子数据，模板需人工录入，不随 `init.js` 重建。

## 注意事项（申请前必读）

- `RegisterAccountSuccess` 的 `password` 参数——短信明文传密码，阿里云审核大概率拒；代码已有兜底（无密码时传 `******`），建议模板内容不含 `${password}`，只申请 name/username 两个变量的版本。
- `VerifyCode` 建议文案写死「5 分钟有效」——如与实际过期时间不符，先改文案再申请，模板审核通过后改动要重审。
- 阿里云接入配置见 `framework/aliyunsms/AliyunDysmsProperties`（endpoint/region/accessKey/accessSecret），凭据走环境变量，勿入库。
