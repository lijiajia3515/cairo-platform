# auth 文档与运维脚本

```
docs/auth/
├── menus.md           # 菜单权限树（可读版，人/大模型友好）
├── dict.md            # 系统字典清单（可读版）
├── error-codes.md     # 错误码清单（可读版，17 枚举类 74 码值）
├── api-surface.md     # API 面基线（8 主体面 + 2 特例面全量端点清单 + 防护模型）
├── api-convergence-plan.md # API 面收敛计划（P2 下沉分批 / P3 待定，未实施）
├── sms-templates.md   # 短信模板清单（15 bizId + 阿里云申请/录入指引）
├── gen-api-surface.py # api-surface.md 附录再生成（从源码提取路由+防护注解）
├── import-menus.cjs   # 菜单/权限导入（读 db/data 基线，走 manage API）
└── db/                # MongoDB 结构与数据（唯一权威源）
    ├── *.js           #   71 个集合 DDL（验证器 + 索引）
    ├── init.js        #   从零重建全部集合
    └── data/          #   基础数据快照（camelCase，与库内一致）
```

## 常用操作

| 场景 | 命令 |
|---|---|
| 从零重建库结构 | `mongosh <uri> --file db/init.js`（本目录下执行） |
| 导入菜单/权限 | `node import-menus.cjs`（需服务运行 + admin 账号） |
| 清空菜单/权限重灌 | mongosh 下 `load("db/auth_menu.js"); load("db/auth_permission.js")` 后重跑导入 |
| 清空字典重灌 | mongosh 下 `load("db/auth_sys_dict.js"); load("db/auth_sys_dict_item.js")` 重建结构；数据注入走服务 API（嵌套集左右值由服务端计算，禁止直插），基线快照见 `db/data/sys_dict*.json` |

## 基础数据构成（db/data/）

| 文件 | 内容 | 条数 |
|---|---|---|
| `app.json` | 应用（cairo 运营平台、demo 演示应用） | 2 |
| `client.json` | OAuth2 客户端（服务端/网页端） | 2 |
| `endpoint.json` | 终端（web，现挂 demo 应用） | 2 |
| `subapp.json` + `subapp_version.json` | 子应用（manage、demo、dashboard 看板） | 3+2 |
| `menu.json` | 菜单（含根容器 web，树形快照见 [menus.md](menus.md)） | 45 |
| `permission.json` | 功能权限点 | 169 |
| `sys_dict.json` + `sys_dict_item.json` | 系统字典（含 AccessScope 准入范围） | 9+48 |

约定：`db/data/` 为**库内 camelCase 原样**（去 `_id`、日期 ISO 化），导入脚本负责线格式转换。

## 字典规范

字典项值以代码枚举为权威源；DictId 与实体命名一致（前端 `useDict` 直接引用）；基线快照为 `db/data/sys_dict*.json`，废弃字典直接从快照移除。

## 相关文档

- [auth 账号](../README.md)：主体模型、认证体系、数据模型、配置
- [平台总览](../../README.md)：架构、快速开始、文档导航
- 可读快照：[menus.md](menus.md) · [dict.md](dict.md) · [error-codes.md](error-codes.md) · [api-surface.md](api-surface.md) · [api-convergence-plan.md](api-convergence-plan.md)
