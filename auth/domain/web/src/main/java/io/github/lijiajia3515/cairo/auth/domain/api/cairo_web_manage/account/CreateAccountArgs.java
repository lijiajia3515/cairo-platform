package io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.account;

import jakarta.validation.constraints.Email;
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
public class CreateAccountArgs implements Serializable {

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
	 * 重置的密码
	 */
	@Size(min = 6, max = 40)
	private String password;

	/**
	 * 昵称
	 */
	private String nickname;

	/**
	 * 头像url
	 */
	private String avatarUrl;
}
