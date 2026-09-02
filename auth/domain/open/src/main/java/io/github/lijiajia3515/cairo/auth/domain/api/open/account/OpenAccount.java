package io.github.lijiajia3515.cairo.auth.domain.api.open.account;

import io.github.lijiajia3515.cairo.jackson.desensitize.Desensitize;
import io.github.lijiajia3515.cairo.jackson.desensitize.DesensitizeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import static io.github.lijiajia3515.cairo.jackson.desensitize.DesensitizeType.CHINESE_NAME;

/**
 * 公开的账号信息模型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OpenAccount {
	/**
	 * 账号ID
	 */
	private String accountId;

	/**
	 * 头像
	 */
	private String avatarUrl;

	/**
	 * 昵称
	 */
	@Desensitize(type = CHINESE_NAME)
	private String nickname;

	/**
	 * 用户名
	 */
	private String username;

	/**
	 * 邮箱
	 */
	@Desensitize(type = DesensitizeType.EMAIL)
	private String email;

	/**
	 * 手机号
	 */
	@Desensitize(type = DesensitizeType.MOBILE_PHONE)
	private String phoneNumber;

	/**
	 * 加入时间
	 */
	private LocalDateTime joinTime;
}
