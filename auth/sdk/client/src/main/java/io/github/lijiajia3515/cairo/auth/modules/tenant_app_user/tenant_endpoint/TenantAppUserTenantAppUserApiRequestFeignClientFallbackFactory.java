package io.github.lijiajia3515.cairo.auth.modules.tenant_app_user.tenant_endpoint;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

/**
 * [tenant_app_user]
 */
@Slf4j
public class TenantAppUserTenantAppUserApiRequestFeignClientFallbackFactory implements FallbackFactory<TenantAppUserTenantAppUserApiRequestFeignClient> {
	@Override
	public TenantAppUserTenantAppUserApiRequestFeignClient create(Throwable cause) {
		log.info("服务异常原因：", cause);
		return new TenantAppUserTenantAppUserApiRequestFallbackFeignClient();
	}
}
