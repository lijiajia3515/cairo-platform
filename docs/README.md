# Cairo 文档中心

全仓文档统一收纳于 `docs/`，**按服务域组织**——服务专属文档放各自子目录，跨服务的平台级规范放本目录。新增文档先确定归属域，再在对应索引登记。

```
docs/
└── auth/                  # 账号服务（平台核心，当前唯一成规模的服务域）
    ├── README.md          # 运维入口：db 权威源、初始化/导入脚本、基线数据构成
    ├── api-surface.md     # API 面基线（8 主体面 + 2 特例面全量端点 + 防护模型）
    ├── api-convergence-plan.md  # API 面收敛计划（未实施）
    ├── menus.md           # 菜单权限树可读快照
    ├── dict.md            # 系统字典清单可读快照
    ├── error-codes.md     # 错误码清单
    ├── sms-templates.md   # 短信模板清单
    ├── backlog.md         # 待办
    ├── gen-api-surface.py # api-surface 附录再生成脚本
    ├── import-menus.cjs   # 菜单/权限导入脚本
    └── db/                # MongoDB 结构与数据（唯一权威源，71 集合）
```

## 文档地图

| 域 | 入口 | 内容 |
|---|---|---|
| 平台总览 | [根 README](../README.md) | 架构、快速开始、构建约定、开发规范 |
| 账号服务 | [auth/README.md](../auth/README.md)（设计）· [auth/](auth/README.md)（文档与运维） | 主体模型、认证体系、API 面、DB、错误码 |
| 网关 | [gateway/README.md](../gateway/README.md) | 限流 / 熔断 / 灰度 |
| 监控中心 | [sba/README.md](../sba/README.md) | Spring Boot Admin |
| 运营平台前端 | [auth/web/README.md](../auth/web/README.md) | Vue3 + TDesign |

## 约定

- **归属规则**：文档描述哪个服务就进哪个域子目录（如 `docs/auth/`）；跨服务规范（如统一错误处理、部署拓扑）放 `docs/` 根；新服务上线时建对应子目录并更新本索引
- **事实核权威源**：版本号 → `gradle/libs.versions.toml`；子应用数 → `settings.gradle`；API 面 → `api-surface.md`；集合数 → `db/` 目录；重写文档时禁止从旧文档搬运数字
- **只留最新规范**：文档记录现状与现行约定，不保留旧命名对照、迁移历史与 git 溯源指引
- **路径引用**：docs 内引用代码用仓库根相对路径；脚本（`.py`/`.cjs`）一律 `__dirname`/同级相对定位，随目录整体迁移可用
