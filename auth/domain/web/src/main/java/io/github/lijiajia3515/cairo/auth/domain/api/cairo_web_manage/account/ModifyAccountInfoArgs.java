package io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.account;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class ModifyAccountInfoArgs {

	/**
	 * 账号id
	 */
	@NotNull
	private String accountId;


	/**
	 * 登录名
	 */
	private String username;
	/**
	 * 手机号
	 */
	private String phoneNumber;
	/**
	 * 邮箱
	 */
	@Email
	private String email;
	/**
	 * 昵称
	 */
	private String nickname;

	/**
	 * 头像url
	 */
	private String avatarUrl;

}
