package io.github.lijiajia3515.cairo.auth.framework.auth_code;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * 验证认证码token参数
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerifyAuthCodeArgs {

	/**
	 * 账号id
	 */
	@NotNull
	private String accountId;

	/**
	 * 认证码
	 */
	private String authCode;
}
