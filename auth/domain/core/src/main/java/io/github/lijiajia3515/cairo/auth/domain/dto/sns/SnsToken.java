package io.github.lijiajia3515.cairo.auth.domain.dto.sns;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 联接信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SnsToken {
	/**
	 * token
	 */
	private String token;

	/**
	 * 状态
	 */
	private SnsTokenStatus status;

	/**
	 * 过期时间
	 */
	private LocalDateTime expireTime;

	/**
	 * 厂商ID
	 */
	private String partnerId;

	/**
	 * 供应商ID
	 */
	private String providerId;

	/**
	 * 厂商OpenId
	 */
	private String partnerOpenId;

	/**
	 * 供应商OpenId(微信公众号OpenId,小程序OpenId,网站OpenId)
	 */
	private String providerOpenId;

	/**
	 * 昵称
	 */
	private String nickName;

	/**
	 * 头像
	 */
	private String avatarUrl;

	/**
	 * 性别，0-未知，1-男，2-女
	 */
	private String sex;
}
