package io.github.lijiajia3515.cairo.auth.modules.menu;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

@Slf4j
public class MenuClientApiFeignClientFallbackFactory implements FallbackFactory<MenuClientApiFeignClient> {
	@Override
	public MenuClientApiFeignClient create(Throwable cause) {
		log.info("服务异常原因：", cause);
		return new MenuClientApiFallbackFeignClient();
	}
}
