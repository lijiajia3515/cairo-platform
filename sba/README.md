# sba 监控中心

Spring Boot Admin（SBA）可视化监控中心。

## 能力

- Consul 服务发现（`framework/ConsulServiceInstanceConverter` 同步实例元数据）
- 独立管理端口（监控面与业务面隔离，见 `config/SbaConfig`）
- 健康状态告警通知
- 监控面访问控制（`config/SecurityConfig`）

## 运行

依赖 Consul（服务注册 / 发现，启动硬依赖，本地需以 `--spring.cloud.consul.host` 覆盖默认主机名 `consul`）。配置样例见 `src/main/resources/config/application-example.yaml`：可 `--spring.profiles.active=example` 直接启动（敏感项按文件内 `${ENV:}` 清单注入环境变量），或复制为 `application-local.yaml` 填真实值（已被 `.gitignore` 排除，严禁提交凭证）。

```bash
./gradlew :sba:bootJar    # 打包
```

## 相关文档

- [gateway 网关](../gateway/README.md)
- [auth 账号](../auth/README.md)
- [平台总览](../README.md)
