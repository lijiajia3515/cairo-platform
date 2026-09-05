# 新增子应用 / 模块

全仓 32 个 Gradle 子应用共享一套约定插件，新增一个子应用通常只需三步：**建目录 → 写 `build.gradle` → `settings.gradle` 注册一行**。本文以「在 `auth/domain/` 下新增一个业务领域子应用」为例，从零走到发布。

> 机制解释见 [build-system.md](build-system.md)；命名 / 标识等规范见 [standards.md](standards.md)。

## 一、Java 库子应用（最常见）

### 1. 建目录 + `build.gradle`

子应用目录名即子应用名（Spring 风格）：`auth/domain/foo/` 对应 Gradle 子应用 `:auth:domain:foo`。库子应用声明 `cairo.java-library`（要对外发布再加 `cairo.maven-publish`）：

```gradle
// auth/domain/foo/build.gradle
plugins {
  id 'cairo.java-library'
  id 'cairo.maven-publish'
}

dependencies {
  api(project(':auth:core'))                          // 内部依赖用 project() 引用
  compileOnly('org.springframework.data:spring-data-mongodb')
  compileOnly('jakarta.validation:jakarta.validation-api')
}
```

### 2. `settings.gradle` 注册

在 `settings.gradle` 对应分组下追加一行：

```gradle
include 'auth:domain:foo'
```

### 3. 验证

```bash
./gradlew :auth:domain:foo:build     # 编译 + 测试
./gradlew :auth:domain:foo:test      # 只跑测试
```

## 二、Spring Boot 应用/服务子应用

可启动的服务（如 `auth/service`、`starter/example`）改用 `cairo.spring-boot-app` 约定插件：

```gradle
// 目录结构：src/main/java、src/main/resources/config/（application-example.yaml 脱敏样例）
plugins {
  id 'cairo.spring-boot-app'
}

dependencies {
  implementation('org.springframework.boot:spring-boot-starter-web')
  implementation('org.springframework.boot:spring-boot-starter-security')
  testImplementation('org.springframework.boot:spring-boot-starter-test')
}
```

打包产出 boot jar：

```bash
./gradlew :auth:service:bootJar
```

## 三、加第三方依赖

两步走（禁止直接在 build.gradle 里写版本号）：

1. **先在版本目录登记** `gradle/libs.versions.toml`：

```toml
[versions]
my-lib = "1.2.3"

[libraries]
my-lib = { module = "com.example:my-lib", version.ref = "my-lib" }
```

2. **再到 build.gradle 用类型安全访问器引用**（`my-lib` → `libs.my.lib`，`-`/`.` 均映射为 `.`）：

```gradle
api(libs.my.lib)
```

## 四、发布到 Maven 仓库

库子应用发布产物：jar/pom 双形态，artifactId 由路径自动推导（`:auth:domain:foo` → `cairo-auth-domain-foo`）。

```bash
# 凭证配置在 ~/.gradle/gradle.properties，仓库不保存任何凭证
#   publishRepository=release|snapshot|central|central-snapshot
./gradlew :auth:domain:foo:publish
```

发布版本默认取 `gradle.properties` 的 `version`；CI / 手工发布可用 `-Pversion=2.2.0` 覆盖（去掉 `-SNAPSHOT`）。

## 五、改动是否生效

- **内部互依**：子应用互相依赖一律 `project()` 引用，改框架代码下游即时生效，无需先发布；
- **对外发布**：改 `domain`（DTO）/ `sdk`（Feign）后需发布，供其他微服务消费。

## 相关文档

- 构建机制（约定插件 / 版本目录 / BOM / 仓库 / 名称推导）：[build-system.md](build-system.md)
- 开发规范（命名 / 标识 / 权限 / DB / 前端）：[standards.md](standards.md)
- 子应用清单：[架构设计](../architecture.md#项目结构)
