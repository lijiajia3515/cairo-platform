# auth 文档与运维

auth 账号服务的文档中枢：MongoDB 权威源、API 面基线、可读快照、运维脚本与测试计划。

## 目录结构

```
docs/auth/
├── README.md                 # 本页：导航 + 常用操作 + 脚本分工
├── db/                       # MongoDB 结构与数据（唯一权威源）
│   ├── *.js                  #   71 个集合 DDL（验证器 + 索引）
│   ├── init.js               #   从零重建全部集合（mongosh 脚本）
│   └── data/                 #   基础数据快照（camelCase，与库内一致）
├── api/                      # API 面
│   ├── api-surface.md        #   API 面基线（8 主体面 + 2 特例面全量端点 + 防护模型）
│   └── usage.md              #   接口调用指南（认证/上下文头/响应结构/关键流程示例）
├── snapshots/                # 可读快照（人/大模型友好）
│   ├── menus.md              #   菜单权限树（45 菜单 / 169 权限点）
│   ├── dict.md               #   系统字典清单（9 字典 / 48 项）
│   ├── error-codes.md        #   错误码清单（17 枚举类 74 码值）
│   └── sms-templates.md      #   短信模板清单（15 bizId + 阿里云申请/录入指引）
├── scripts/                  # 运维脚本
│   ├── import-menus.cjs      #   菜单/权限导入（Node，读 db/data 基线走 manage API）
│   └── gen-api-surface.py    #   api-surface 附录再生成（从源码提取路由+防护注解）
├── testing/                  # 测试
│   ├── test-plan.md          #   场景矩阵 + 审计结论（权威源）
│   └── test-cases.md         #   五维测试用例（安全/权限/校验/合理性/边界）
└── plans/                    # 计划与待办（维护者工作区，非用户参考）
    ├── backlog.md            #   待办需求
    └── api-convergence-plan.md # API 面收敛计划（P2 下沉分批 / P3 待定，未实施）
```

## 常用操作

| 场景 | 命令 |
|---|---|
| 从零重建库结构（71 集合） | `cd docs/auth/db && mongosh <uri> --file init.js` |
| 导入菜单/权限 | `cd docs/auth && node scripts/import-menus.cjs`（需服务运行 + admin 账号） |
| 清空菜单/权限重灌 | mongosh（在 db/ 下）`load("auth_menu.js"); load("auth_permission.js")` 后重跑导入 |
| 清空字典重灌 | mongosh（在 db/ 下）`load("auth_sys_dict.js"); load("auth_sys_dict_item.js")` 重建结构；数据注入走服务 API（嵌套集左右值由服务端计算，禁止直插），基线快照见 `db/data/sys_dict*.json` |

> **mongosh 脚本必须在 `db/` 目录下执行**：mongosh 的 `load()` 按当前工作目录解析相对路径，`init.js` 里的 `load("auth_account.js")` 只有在 `cd docs/auth/db` 后才找得到文件。

## mongosh / node 分工

两个脚本职责不同，不能互相替代：

| 脚本 | 工具 | 干什么 | 为什么不能用另一个 |
|---|---|---|---|
| `db/init.js` | **mongosh** | 重建 71 个集合的结构（验证器 + 索引） | 纯数据库 DDL 操作 |
| `scripts/import-menus.cjs` | **node** | 导入菜单/权限树 | 本质是 HTTP 操作：登录拿 JWT → 调 manage API 建菜单；mongosh 沙箱无 `fetch`/`http`/`require`，且嵌套集数据（菜单/字典）**必须走服务端 API 注入**（左右值由服务端计算，禁止直插） |

## 基础数据构成（db/data/）

| 文件 | 内容 | 条数 |
|---|---|---|
| `app.json` | 应用（cairo 运营平台、demo 演示应用） | 2 |
| `client.json` | OAuth2 客户端（服务端/网页端） | 2 |
| `endpoint.json` | 终端（web，现挂 demo 应用） | 2 |
| `subapp.json` + `subapp_version.json` | 子应用（manage、demo、dashboard 看板） | 3+2 |
| `menu.json` | 菜单（含根容器 web，树形快照见 [snapshots/menus.md](snapshots/menus.md)） | 45 |
| `permission.json` | 功能权限点 | 169 |
| `sys_dict.json` + `sys_dict_item.json` | 系统字典（含 AccessScope 准入范围） | 9+48 |

约定：`db/data/` 为**库内 camelCase 原样**（去 `_id`、日期 ISO 化），导入脚本负责线格式转换。

## 相关文档

- [auth 账号](../../auth/README.md)：主体模型、认证体系、数据模型、配置
- [平台总览](../../README.md)：架构、快速开始、文档导航
- 可读快照：[menus.md](snapshots/menus.md) · [dict.md](snapshots/dict.md) · [error-codes.md](snapshots/error-codes.md) · [sms-templates.md](snapshots/sms-templates.md)
