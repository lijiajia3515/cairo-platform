# gateway 网关

Spring Cloud Gateway 服务网关，平台统一入口。

## 能力

- 统一业务返回包装（`framework/webflux/CairoWebfluxResponseHandler`）
- Redis 限流（key 序列化与配置见 `framework/redis/`）
- Resilience4j 熔断与重试（`config/Resilience4jConfig`）
- 标签灰度路由（自定义 LoadBalancer：`framework/loadbalancer/`，按服务实例标签路由）
- 追踪响应头透传（`framework/sleuth/TraceResponseHeaderWebFilter`）
- 兜底与错误页（`controller/error/`：Fallback / 维护中 / 版本不匹配 / 功能未开放）
- CORS 跨域配置（`config/CorsConfig`）

## 运行

依赖 Consul（服务注册 / 发现 + 配置中心——`application.yaml` 开启 `consul.config.enabled: true` 并 watch，是三个服务中唯一走 Consul 配置的；本地需以 `--spring.cloud.consul.host` 覆盖默认主机名 `consul`）。配置样例见 `src/main/resources/config/application-example.yaml`：可 `--spring.profiles.active=example` 直接启动（敏感项按文件内 `${ENV:}` 清单注入环境变量），或复制为 `application-local.yaml` 填真实值（已被 `.gitignore` 排除，严禁提交凭证）。

```bash
./gradlew :gateway:bootJar    # 打包
```

## 相关文档

- [auth 账号](../auth/README.md)
- [sba 监控中心](../sba/README.md)
- [平台总览](../README.md)
