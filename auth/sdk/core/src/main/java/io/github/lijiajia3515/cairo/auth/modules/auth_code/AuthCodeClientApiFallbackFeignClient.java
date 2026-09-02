package io.github.lijiajia3515.cairo.auth.modules.auth_code;

import io.github.lijiajia3515.cairo.auth.framework.auth_code.AuthCodeVerifyStat;
import io.github.lijiajia3515.cairo.auth.framework.auth_code.VerifyAuthCodeArgs;
import io.github.lijiajia3515.cairo.core.exception.ErrorBusinessException;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import org.springframework.http.ResponseEntity;

/**
 * client-api-auth_code fallback feignclient
 */
public class AuthCodeClientApiFallbackFeignClient implements AuthCodeClientApiFeignClient {
	public static final RuntimeException EX = new ErrorBusinessException("认证服务-服务故障");

	@Override
	public ResponseEntity<BusinessResult<AuthCodeVerifyStat>> verifyAuthCode(String authorization,VerifyAuthCodeArgs args) {
		throw EX;
	}
}
