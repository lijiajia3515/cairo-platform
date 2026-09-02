package io.github.lijiajia3515.cairo.auth.modules.tenant_app_department;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

/**
 * client-api-department feignclient fallback factory
 */
@Slf4j
public class TenantAppDepartmentApiClientFeignClientFallbackFactory implements FallbackFactory<TenantAppDepartmentApiClientFeignClient> {
	@Override
	public TenantAppDepartmentApiClientFeignClient create(Throwable cause) {
		log.info("服务异常原因：", cause);
		return new TenantAppDepartmentApiClientFallbackFeignClient();
	}
}
