package io.github.lijiajia3515.cairo.auth.domain.api.client.account;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 修改账号手机号参数
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class ModifyAccountPhoneNumberArgs implements Serializable {
	/**
	 * 账号id
	 */
	@NotNull
	@NotBlank
	private String accountId;

	/**
	 * 原手机号验证码
	 */
	private String sourceVerifyCode;

	/**
	 * 手机号
	 */
	@NotNull
	@NotBlank
	private String phoneNumber;

	/**
	 * 验证码
	 */
	@NotNull
	@NotBlank
	private String verifyCode;
}
