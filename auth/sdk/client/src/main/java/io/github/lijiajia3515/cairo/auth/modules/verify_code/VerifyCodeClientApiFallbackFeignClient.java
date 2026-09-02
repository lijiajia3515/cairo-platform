package io.github.lijiajia3515.cairo.auth.modules.verify_code;

import io.github.lijiajia3515.cairo.auth.domain.api.client.verify_code.SendAccountPhoneNumberVerifyCodeArgs;
import io.github.lijiajia3515.cairo.core.exception.ErrorBusinessException;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

/**
 * client-api-user_connect fallback feignclient
 */
public class VerifyCodeClientApiFallbackFeignClient implements VerifyCodeClientApiFeignClient {

	public static final RuntimeException EX = new ErrorBusinessException("认证服务-服务故障");

	@Override
	public ResponseEntity<BusinessResult<Optional<String>>> sendAccountPhoneNumberVerifyCode(String authorization, SendAccountPhoneNumberVerifyCodeArgs args) {
		throw EX;
	}
}
