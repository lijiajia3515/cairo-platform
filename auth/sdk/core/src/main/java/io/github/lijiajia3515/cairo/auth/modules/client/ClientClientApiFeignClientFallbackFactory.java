package io.github.lijiajia3515.cairo.auth.modules.client;



import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

/**
 * client-api-app feignclient fallback factory
 */

@Slf4j
public class ClientClientApiFeignClientFallbackFactory implements FallbackFactory<ClientClientApiFeignClient> {
	@Override
	public ClientClientApiFeignClient create(Throwable cause) {
		log.info("服务异常原因：", cause);
		return new ClientClientApiFallbackFeignClient();
	}
}
