# 部署与运维

## 持续集成（CI）

工作流在 [.github/workflows/ci.yml](../.github/workflows/ci.yml)，标准 GitHub Actions 语法（Gitea Actions 兼容），push / PR 自动触发：

- **后端**：JDK 17 + Gradle 依赖缓存 → `./gradlew test`（全仓测试，含嵌入式 MongoDB 集成测试）→ `:auth:service:bootJar` 打包账号服务。产物：`auth-boot-jar`（boot jar，保留 14 天）；失败时上传测试报告，并将失败用例输出为 Actions 注解；
- **前端**：Node 24 + `pnpm install --frozen-lockfile` → `pnpm build` → `pnpm lint`（错误级阻断，存量 warning 不阻断）。产物：`web-dist`（构建产物，保留 14 天）。

暂无 CD（部署另行处理）。Gitea 接入注意：仓库设置中开启 Actions、部署 act_runner（`runs-on: ubuntu-latest` 需与 runner 标签匹配）；官方 `actions/*` 需 runner 可访问 github.com，或在 Gitea 管理端配置默认 actions 镜像。

## 反向代理（nginx）

`nginx/` 目录为 nginx 部署配置：`Dockerfile` 将配置整体拷贝进 `/etc/nginx/`，`conf.d/` 下含默认站点与前端站点配置（`default.conf`、`frontend.conf`）。

```bash
docker build -t cairo-nginx nginx/
```

## 数据库运维

MongoDB 结构与基线数据的唯一权威源在 [docs/auth/](../auth/README.md)，常用运维入口：

| 场景 | 命令 |
|---|---|
| 从零重建库结构 | `mongosh <uri> --file docs/auth/db/init.js` |
| 导入菜单/权限基线 | `cd docs/auth && node scripts/import-menus.cjs`（需服务运行 + admin 账号） |
| 基线数据构成 | `docs/auth/db/data/`（app / client / endpoint / subapp / menu / permission / sys_dict） |

> 菜单 / 权限等嵌套集数据必须走服务端 API 注入（左右值由服务端计算），禁止直插；验证器变更走「备份 → drop + 重建 → 回填」。

## 相关文档

- [快速开始](quickstart.md)：本地从零跑通
- [架构设计](architecture.md)：模块划分与项目结构
- [auth 运维脚本](../auth/README.md)：初始化 / 导入 / 基线快照
