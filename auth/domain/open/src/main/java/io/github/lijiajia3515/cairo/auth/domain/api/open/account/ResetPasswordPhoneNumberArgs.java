package io.github.lijiajia3515.cairo.auth.domain.api.open.account;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 重置密码参数
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class ResetPasswordPhoneNumberArgs implements Serializable {

	/**
	 * 手机号
	 */
	@NotNull
	private String phoneNumber;

	/**
	 * 验证码
	 */
	@NotNull
	private String verifyCode;

	/**
	 * 密码
	 */
	@Size(min = 6, max = 40)
	private String password;
}
