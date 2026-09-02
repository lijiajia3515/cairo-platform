package io.github.lijiajia3515.cairo.auth.modules.app_user_authorization;

import io.github.lijiajia3515.cairo.auth.domain.api.client.app_user_authorization.AppUserAuthorizationModel;
import io.github.lijiajia3515.cairo.auth.domain.api.client.app_user_authorization.GetAppUserAuthorizationArgs;
import io.github.lijiajia3515.cairo.core.exception.ErrorBusinessException;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import org.springframework.http.ResponseEntity;

/**
 * [client/api] app endpoint user fallback feignclient
 */
public class AppUserAuthorizationClientApiFallbackFeignClient implements AppUserAuthorizationClientApiFeignClient {

	public static final RuntimeException EX = new ErrorBusinessException("认证服务-终端用户授权子应用故障");


	@Override
	public ResponseEntity<BusinessResult<AppUserAuthorizationModel>> getAppUserAuthorization(String authorization, GetAppUserAuthorizationArgs args) {
		throw EX;
	}
}
