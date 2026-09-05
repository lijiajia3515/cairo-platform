package io.github.lijiajia3515.cairo.auth.modules.subapp_user_authorization;

import io.github.lijiajia3515.cairo.auth.domain.api.client.subapp_user.SubappUserAuthorizationModel;
import io.github.lijiajia3515.cairo.auth.domain.api.client.subapp_user.GetSubappUserAuthorizationArgs;
import io.github.lijiajia3515.cairo.core.exception.ErrorBusinessException;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import org.springframework.http.ResponseEntity;

/**
 * [client/api] app subapp user authorization fallback feignclient
 */
public class SubappUserAuthorizationClientApiFallbackFeignClient implements SubappUserAuthorizationClientApiFeignClient {

	public static final RuntimeException EX = new ErrorBusinessException("认证服务-子应用级用户授权子应用故障");

	@Override
	public ResponseEntity<BusinessResult<SubappUserAuthorizationModel>> getSubappUserAuthorization(String authorization, GetSubappUserAuthorizationArgs args) {
		throw EX;
	}
}
