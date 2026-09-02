package io.github.lijiajia3515.cairo.auth.modules.app_department;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

/**
 * client-api-department feignclient fallback factory
 */
@Slf4j
public class AppDepartmentClientApiFeignClientFallbackFactory implements FallbackFactory<AppDepartmentClientApiFallbackFeignClient> {

	@Override
	public AppDepartmentClientApiFallbackFeignClient create(Throwable cause) {
		log.info("服务异常原因：", cause);
		return new AppDepartmentClientApiFallbackFeignClient();
	}
}
