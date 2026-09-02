package io.github.lijiajia3515.cairo.auth.modules.app;

import io.github.lijiajia3515.cairo.auth.domain.dto.app.App;
import io.github.lijiajia3515.cairo.auth.domain.api.client.app.GetAppArgs;
import io.github.lijiajia3515.cairo.core.exception.ErrorBusinessException;
import io.github.lijiajia3515.cairo.core.page.Page;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import org.springframework.http.ResponseEntity;

import java.util.List;
/**
 * client-api-app fallback feignclient
 */
public class AppClientApiFallbackFeignClient implements AppClientApiFeignClient {
	public static final RuntimeException EX = new ErrorBusinessException("认证服务-服务故障");

	@Override
	public ResponseEntity<BusinessResult<List<App>>> getAppList(String authorization, GetAppArgs args) {
		throw EX;
	}

	@Override
	public ResponseEntity<BusinessResult<Page<App>>> getAppPageList(String authorization,GetAppArgs args) {
		throw EX;
	}
}
