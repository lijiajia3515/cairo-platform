package io.github.lijiajia3515.cairo.auth.domain.api.open.account;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 账号注册参数
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterAccountArgs {
	/**
	 * 手机号
	 */
	@NotNull
	@NotBlank
	@Size(min = 11, max = 20)
	private String phoneNumber;

	/**
	 * 验证码
	 */
	@NotNull
	@NotBlank
	private String verifyCode;

	/**
	 * 昵称
	 */
	private String nickname;

	/**
	 * 密码
	 */
	@Size(min = 6, max = 40)
	private String password;
}
