package io.github.lijiajia3515.cairo.auth.modules.app_role;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

@Slf4j
public class AppRoleClientApiFeignClientFallbackFactory implements FallbackFactory<AppRoleClientApiFeignClient> {
	@Override
	public AppRoleClientApiFeignClient create(Throwable cause) {
		log.info("服务异常原因：", cause);
		return new AppRoleClientApiFallbackFeignClient();
	}
}
