package io.github.lijiajia3515.cairo.auth.modules.file.public_file;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

@Slf4j
public class PublicFileClientApiFeignClientFallbackFactory implements FallbackFactory<PublicFileClientApiFeignClient> {
	@Override
	public PublicFileClientApiFeignClient create(Throwable cause) {
		log.info("服务异常原因: ", cause);
		return new PublicFileClientApiFallbackFeignClient();
	}
}
