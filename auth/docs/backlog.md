# 待办需求

## 登录协议同意状态绑定协议版本

三端（auth/web 两种登录方式、注册页、auth/service 服务端登录页）协议门已实现「同意并继续」后持久记住，下次进入默认勾选、不再弹门。但同意状态是**永久**的——协议更新（隐私政策头部版本号，当前 v20231015）后用户不会重新确认，合规上有风险。

**方案**：存储键绑定协议版本号，协议更新即强制全量用户重新同意。

- 前端：localStorage `changeData` → `changeData_v<版本>`
- 服务端登录页：localStorage `cairo_login_agree` → `cairo_login_agree_v<版本>`
- 版本号来源：`auth/web/public/agreement/privacy.html` 的版本标记

涉及：`auth/web/src/views/login/index.vue`、`loginByCode.vue`、`loginByPassword.vue`、`register.vue`、`auth/service/src/main/resources/templates/login.html`

## 协议双拷贝同步

协议文件现存两份拷贝：`auth/web/public/agreement/`（前端）与 `auth/service/src/main/resources/static/agreement/`（服务端登录页），修改协议需手动同步两处。可做构建期自动拷贝。
