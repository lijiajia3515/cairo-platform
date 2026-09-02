package io.github.lijiajia3515.cairo.auth.modules.verify_code;

import io.github.lijiajia3515.cairo.auth.domain.api.client.verify_code.SendAccountPhoneNumberVerifyCodeArgs;
import java.util.Optional;


public interface VerifyCodeClientApiService {


	/**
	 * 发送账号手机号验证码
	 * 需要权限： verify_code:send_account_phone_number_verify_code ｜verify_code:all
	 *
	 * @return 空
	 */
	Optional<String> sendAccountPhoneNumberVerifyCode(SendAccountPhoneNumberVerifyCodeArgs args);
}
