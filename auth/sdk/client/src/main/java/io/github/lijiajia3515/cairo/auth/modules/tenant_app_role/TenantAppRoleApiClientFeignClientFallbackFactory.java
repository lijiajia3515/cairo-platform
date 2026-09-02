package io.github.lijiajia3515.cairo.auth.modules.tenant_app_role;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

@Slf4j
public class TenantAppRoleApiClientFeignClientFallbackFactory implements FallbackFactory<TenantAppRoleApiClientFeignClient> {
	@Override
	public TenantAppRoleApiClientFeignClient create(Throwable cause) {
		log.info("服务异常原因：", cause);
		return new TenantAppRoleApiClientFallbackFeignClient();
	}
}
