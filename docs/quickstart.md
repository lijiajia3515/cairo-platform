# 快速开始

从零把平台跑起来的完整步骤。先看 [README](../README.md) 了解这是什么，再按本文操作。共四段：**环境 → 中间件 → 初始化 → 启动验证**。

## 一、环境要求

| 项目       | 要求                           | 说明                                |
|------------|--------------------------------|-------------------------------------|
| JDK        | 17+                            | Gradle 工具链统一                   |
| Node.js    | 24.20.0 LTS                    | 见 `auth/web/.nvmrc`；pnpm 11.24.0 由 corepack 锁定 |
| Docker     | 建议                          | 用于起中间件（也可自行安装）        |
| MongoDB    | 5+                            | 核心存储                            |
| Redis      | 任意可用版本                   | 缓存 / 分布式锁（Redisson）         |
| RabbitMQ   | 任意可用版本                   | 消息                                |
| MinIO      | 任意可用版本                   | 文件存储（文件子应用）              |
| Consul     | 任意可用版本                   | 服务注册 / 发现（启动硬依赖）       |
| Zipkin     | 可选                           | 链路追踪                            |

## 二、起中间件

三个服务的注册发现都走 Consul，**不起 Consul 服务无法启动**。其中 gateway 额外开启了 Consul 配置中心（`config.enabled: true` + watch），auth / sba 的配置不走 Consul（全部来自本地 `config/` 目录文件）。

Consul 默认地址是主机名 `consul`（容器网络名），本地运行需先起 Consul 并覆盖主机：

```bash
docker run -d --name consul -p 8500:8500 hashicorp/consul:latest agent -dev -client 0.0.0.0

# 启动服务时覆盖默认主机名（命令行参数或环境变量 SPRING_CLOUD_CONSUL_HOST）
--spring.cloud.consul.host=localhost
```

其余中间件（MongoDB / Redis / RabbitMQ / MinIO）按各自习惯方式启动即可，配置见下文「配置」。

## 三、初始化数据库

账号服务首次启动前，需要从脚本重建 MongoDB 结构并导入菜单/权限基线：

```bash
# 1. 从零重建全部集合（71 个：验证器 + 索引）
#    mongosh 的 load() 按工作目录解析相对路径，必须在 db/ 下执行
cd docs/auth/db && mongosh <uri> --file init.js

# 2. 导入菜单/权限基线（需账号服务已启动 + admin 账号）——走服务 API，需 node
cd docs/auth && node scripts/import-menus.cjs
```

> 菜单 / 权限 / 应用图标与默认头像内置于 `auth/web/public/`（`icons/`、`avatar/`）；DB 基线中的图标 URL 为根相对路径（`/icons/...`），由前端静态服务提供，后端不存储图标文件。基线数据构成与常用运维操作见 [docs/auth/README.md](../auth/README.md)。

## 四、配置

各服务 `src/main/resources/config/` 内置脱敏样例 `application-example.yaml`（auth / gateway / sba 三个服务均有），两种用法：

- **example profile 直接启动**：`--spring.profiles.active=example`，敏感项按文件内 `${ENV:}` 清单用环境变量注入；
- **复制为本地配置**：复制为 `application-local.yaml` 填真实值（已被 `.gitignore` 排除，**严禁提交任何凭证**）。

账号服务首次启动前需生成 OAuth2 令牌签名 RSA 密钥对（见 [oauth-jwk/README.md](../auth/service/src/main/resources/config/oauth-jwk/README.md)）。

## 五、构建与启动

### 后端

```bash
./gradlew build                        # 构建并测试全部 32 个子应用
./gradlew :auth:service:bootJar        # 单独打包账号服务
./gradlew :framework:core:test         # 跑某个子应用的测试
./gradlew :framework:core:publish      # 发布某个子应用（凭证在 ~/.gradle/gradle.properties）
```

### 前端（运营平台）

```bash
cd auth/web
corepack enable
pnpm install
pnpm dev                               # 开发服务器（5173）
```

## 六、验证

1. **Consul**：打开 `http://localhost:8500`，Services 列表应能见到 `gateway`、`auth`、`sba` 三个服务注册成功；
2. **运营平台**：浏览器打开 `http://localhost:5173`，出现登录页即前后端链路打通（admin 账号登录后可进入管理台）；
3. 若页面报错，依次排查：Consul 是否已起且主机名已覆盖、MongoDB 是否已初始化、账号服务日志中的 RSA 密钥对是否生成。

## 相关文档

- [架构设计](architecture.md)：模块划分、项目结构、技术栈
- [部署与运维](deployment.md)：CI、nginx、DB 运维
- [开发指南](development/standards.md)：参与开发前的必读规范
