package io.github.lijiajia3515.cairo.auth.domain.api.open.app_user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class RegisterAppUserArgs implements Serializable {

	/**
	 * appId
	 */
	@NotNull
	@NotBlank
	private String appId;


	/**
	 * 账号
	 */
	@NotNull
	@NotBlank
	@Size(min = 11, max = 20)
	private String phoneNumber;

	/**
	 * 验证码
	 */
	@NotNull
	private String verifyCode;

	/**
	 * 密码
	 */
	@NotNull
	@Size(min = 6, max = 40)
	private String password;

	/**
	 * 昵称
	 */
	@NotNull
	private String nickname;

	/**
	 * 头像
	 */
	private String avatarUrl;

}
