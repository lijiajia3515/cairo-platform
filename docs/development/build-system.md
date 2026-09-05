# 构建架构

单仓库、单 Gradle 构建，采用 Gradle 官方推荐的现代构建架构：约定插件 + 版本目录 + 原生 BOM。

```
cairo-platform/
├── settings.gradle            # 子应用注册 + includeBuild('build-logic') + 集中仓库声明
├── build.gradle               # 仅全仓版本与 group 分配
├── gradle.properties          # 统一版本号（2.2.0-SNAPSHOT）与构建参数
├── gradle/
│   ├── libs.versions.toml     # 版本目录：所有第三方版本与 BOM 坐标
│   └── wrapper/               # Gradle 8.14.5 wrapper（全仓唯一）
├── docs/                      # 文档中心（分层组织，见 docs/README.md）
│   ├── quickstart.md          # 快速开始
│   ├── architecture.md        # 架构设计
│   ├── deployment.md          # 部署与运维
│   └── development/           # 开发指南
│       ├── standards.md       # 开发规范
│       ├── build-system.md    # 构建架构（本页）
│       └── new-module.md      # 新增子应用 / 模块
└── build-logic/               # 构建逻辑（included build，编译为插件）
    └── src/main/groovy/
        ├── cairo.java-library.gradle      # Java 库约定
        ├── cairo.spring-boot-app.gradle   # Boot 应用约定
        ├── cairo.java-platform.gradle     # BOM 子应用约定
        ├── cairo.maven-publish.gradle     # 发布约定
        └── .../CairoArtifacts.groovy      # artifactId 路径推导工具
```

## 核心机制

**约定插件（build-logic/）**——替代旧的 `apply from` 脚本。以真正插件的形式参与编译（有类型检查、IDE 补全、可在 `plugins {}` 中声明），全仓构建规则集中一处：

| 插件                    | 作用                                                                                     |
|-------------------------|------------------------------------------------------------------------------------------|
| `cairo.java-library`    | java-library + lombok + 17 工具链 + UTF-8 + javadoc/sources jar + JUnit 5 + BOM 平台约束 |
| `cairo.spring-boot-app` | Boot 应用/服务：boot plugin + bootJar（classifier=boot）+ 配置处理器/devtools/测试依赖   |
| `cairo.java-platform`   | BOM 子应用（java-platform），约束用 `api(project(...))` 声明                               |
| `cairo.maven-publish`   | 发布到 Maven 仓库，jar/pom 双形态适配，凭证不入库                                        |

子应用的 `build.gradle` 因此非常薄，通常只有 `plugins {}` + `dependencies {}` 两段。

**版本目录（gradle/libs.versions.toml）**——所有第三方版本与 BOM 坐标的唯一登记处，子应用内以 `libs.arthas`、`libs.lock4j.core` 等类型安全访问器引用。升级版本只改 toml 一处。

**原生 BOM platform()**——不使用 `io.spring.dependency-management` 插件。约定插件统一挂载 `api platform(libs.spring.boot.bom)` 等平台约束，版本随依赖树传导（Boot 子应用的 `annotationProcessor`/`developmentOnly` 配置单独挂载）。

**集中仓库（settings.gradle）**——`dependencyResolutionManagement` + `FAIL_ON_PROJECT_REPOS`：仓库（阿里云镜像优先）只在 settings 声明一次，子应用内出现 `repositories {}` 会直接报错，杜绝各自为政。

**名称推导（CairoArtifacts）**——发布 artifactId 与 jar 文件名统一由子应用路径生成并补 `cairo-` 前缀：`:framework:core` → `cairo-framework-core`、`:auth:domain:core` → `cairo-auth-domain-core`。groupId 按路径前缀分配（framework/starter/auth 各自对应 `io.github.lijiajia3515.cairo.*`）。

**版本管理**——`gradle.properties` 的 `version=2.2.0-SNAPSHOT`（Maven 标准）。发布时去掉 `-SNAPSHOT` 或 CI 上 `-Pversion=2.2.0` 覆盖；仓库端自动为 SNAPSHOT 生成时间戳副本，无需手工拼接。

## 日常操作

```bash
# 给子应用加第三方依赖：先在 gradle/libs.versions.toml 登记，再到子应用 build.gradle 引用
api(libs.minio)

# 新增子应用：建目录 + build.gradle（声明约定插件），并在 settings.gradle 注册一行
include 'auth:domain:new-module'

# 内部子应用互相依赖：一律 project() 引用，改框架代码下游即时生效，无需先发布
implementation(project(':framework:web'))

# 发布（凭证配置在 ~/.gradle/gradle.properties，仓库不保存任何凭证）
#   publishRepository=release|snapshot|central|central-snapshot
./gradlew :framework:core:publish
```

## 相关文档

- 从零新增一个子应用/模块：**[new-module.md](new-module.md)**
- 命名 / 标识 / 权限 / DB / 前端约定：[standards.md](standards.md)
