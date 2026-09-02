package io.github.lijiajia3515.cairo.auth.domain.api.endpoint.account_sns;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 获取我的账号绑定信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MyAccountSns implements Serializable {

	/**
	 * 第三方账号厂商ID
	 */
	private String snsPartnerId;
	/**
	 * 第三方账号厂商名称
	 */
	private String snsPartnerName;

	/**
	 * 第三方账号厂商图标
	 */
	private String snsPartnerIcon;

	/**
	 * 第三方账号厂商用户ID
	 */
	private String snsPartnerOpenId;

	/**
	 * 第三方认证提供商ID
	 */
	private String snsProviderId;

	/**
	 * 第三方认证clientId
	 */
	private String clientId;

	/**
	 * 绑定时间
	 */
	private LocalDateTime bindTime;

	/**
	 * 昵称
	 */
	private String nickname;

	/**
	 * 头像
	 */
	private String avatarUrl;

	/**
	 * 是否绑定
	 */
	private Boolean isBind;
}
