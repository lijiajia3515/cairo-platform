package io.github.lijiajia3515.cairo.auth.domain.api.tenant_app.auth_code;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 验证 验证码参数
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerifyCodeAuthCodeArgs {

	/**
	 * 验证码
	 */
	private String verifyCode;

}
