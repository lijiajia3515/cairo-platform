package io.github.lijiajia3515.cairo.auth.modules.permission;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

@Slf4j
public class PermissionClientApiFeignClientFallbackFactory implements FallbackFactory<PermissionClientApiFeignClient> {
	@Override
	public PermissionClientApiFeignClient create(Throwable cause) {
		log.info("服务异常原因：", cause);
		return new PermissionClientApiFallbackFeignClient();
	}
}
