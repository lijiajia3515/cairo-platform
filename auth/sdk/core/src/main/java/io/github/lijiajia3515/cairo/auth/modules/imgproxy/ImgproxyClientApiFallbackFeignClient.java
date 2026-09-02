package io.github.lijiajia3515.cairo.auth.modules.imgproxy;

import io.github.lijiajia3515.cairo.core.exception.ErrorBusinessException;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import io.github.lijiajia3515.cairo.auth.domain.api.client.imgproxy.GetImgUrlArgs;
import org.springframework.http.ResponseEntity;

import java.util.List;

/**
 * client-api imgproxy fallback feign client
 */
public class ImgproxyClientApiFallbackFeignClient implements ImgproxyClientApiFeignClient {

	public static final RuntimeException EX = new ErrorBusinessException("系统服务-图标代理子应用故障");
	@Override
	public ResponseEntity<BusinessResult<List<String>>> getProxyUrl(String authorization,List<GetImgUrlArgs> params) {
		throw EX;
	}
}
