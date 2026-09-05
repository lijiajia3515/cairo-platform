# Cairo 文档中心

全仓文档统一收纳于 `docs/`，按主题分层组织。**根 [README](../README.md) 只做简单概括，细节都在这下面按专题成文。**

## 按你的身份开始

| 我想… | 去这里 |
|---|---|
| 第一次接触，想跑起来 | [快速开始](quickstart.md) |
| 了解整体设计（架构 / 模块 / 技术栈） | [架构设计](architecture.md) |
| 参与开发，先读规范 | [开发规范](development/standards.md) |
| 看懂构建体系 / 想发布 | [构建架构](development/build-system.md) |
| 新增一个子应用 / 模块 | [新增子应用 / 模块](development/new-module.md) |
| 部署上线 / 看 CI | [部署与运维](deployment.md) |

## 文档覆盖域

文档按**架构 / 后端 / 前端 / 测试**四域覆盖，不止后端：

| 域 | 文档 | 说明 |
|---|---|---|
| **架构** | [architecture.md](architecture.md) | 总体架构 / 模块划分 / 项目结构 / 技术栈 |
| **后端** | [auth/README.md](../auth/README.md) · [docs/auth/](../auth/README.md) | 账号服务设计、API 面、DB 权威源 |
| **前端** | [auth/web/README.md](../auth/web/README.md) | 运营平台：环境 / 命令 / 运行时配置 / 列表页规范 |
| **测试** | [docs/auth/testing/test-plan.md](auth/testing/test-plan.md) | 功能 / 权限 / 上下文约束回归矩阵 |
| **构建与部署** | [development/](development/standards.md) · [deployment.md](deployment.md) | 开发规范 / 构建架构 / CI / 运维 |

## 文档地图

```
docs/
├── README.md                    # 本页：文档中心
├── quickstart.md                # 快速开始（从零到跑通）
├── architecture.md              # 架构设计（总览 / 模块 / 项目结构 / 技术栈）
├── deployment.md                # 部署与运维（CI / nginx / DB 运维）
├── development/                 # 开发指南
│   ├── standards.md             # 开发规范
│   ├── build-system.md          # 构建架构
│   └── new-module.md            # 新增子应用 / 模块教程
└── auth/                        # auth 账号专题（当前唯一成规模的服务域）
    ├── README.md                # 运维入口：常用操作 + mongosh/node 脚本分工
    ├── db/                      # MongoDB 结构与数据（唯一权威源，71 集合 DDL + init.js）
    ├── api/                     # API 面
    │   └── api-surface.md       #   API 面基线（8 主体面 + 2 特例面全量端点 + 防护模型）
    ├── snapshots/               # 可读快照
    │   ├── menus.md             #   菜单权限树（45/169）
    │   ├── dict.md              #   系统字典清单
    │   ├── error-codes.md       #   错误码清单
    │   └── sms-templates.md     #   短信模板清单
    ├── scripts/                 # 运维脚本
    │   ├── import-menus.cjs     #   菜单/权限导入（Node + manage API）
    │   └── gen-api-surface.py   #   api-surface 附录再生成
    ├── testing/test-plan.md     # 测试计划（功能 / 权限 / 上下文约束）
    └── plans/                   # 计划与待办（维护者工作区，非用户参考）
        ├── backlog.md           #   待办需求
        └── api-convergence-plan.md  # API 面收敛计划（未实施）
```

## 服务与模块手册

各服务的完整文档跟随其代码所在目录（设计文档与代码同仓共命），下表为入口：

| 域 | 入口 | 内容 |
|---|---|---|
| 账号服务 | [auth/README.md](../auth/README.md) | 主体模型、认证体系、core 组件、API 层、子应用结构、数据模型、消息拓扑、配置、对外输出 |
| 运营平台前端 | [auth/web/README.md](../auth/web/README.md) | 环境要求、常用命令、运行时配置、API 层结构、列表页规范 |
| 网关 | [gateway/README.md](../gateway/README.md) | 限流 / 熔断 / 灰度 / 运行配置 |
| 监控中心 | [sba/README.md](../sba/README.md) | Spring Boot Admin 接入说明 |

## 约定

- **归属规则**：专题文档进 `docs/` 对应分层（快速开始 / 架构 / 部署 / development）；服务专属内容进 `docs/auth/` 等域子目录；跨服务规范放 `docs/` 根；新服务上线时建对应子目录并更新本索引
- **事实核权威源**：版本号 → `gradle/libs.versions.toml`；子应用数 → `settings.gradle`；API 面 → `api-surface.md`；集合数 → `db/` 目录；重写文档时禁止从旧文档搬运数字
- **只留最新规范**：文档记录现状与现行约定，不保留旧命名对照、迁移历史与 git 溯源指引
- **路径引用**：docs 内引用代码用仓库根相对路径；脚本（`.py`/`.cjs`）一律 `__dirname`/同级相对定位，随目录整体迁移可用
