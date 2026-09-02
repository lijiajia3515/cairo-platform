package io.github.lijiajia3515.cairo.auth.domain.dto.account_authorization;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * 账号会话
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class AccountAuthorization implements Serializable {
	/**
	 * token标识
	 */
	private String tokenId;

	/**
	 * 应用ID
	 */
	private String appId;

	/**
	 * 账号ID
	 */
	private String accountId;

	/**
	 * 账号名称
	 */
	private String accountName;

	/**
	 * 登录方式
	 */
	private String loginType;

	/**
	 * 三方登录类型
	 */
	private String snsType;

	/**
	 * 客户端ID
	 */
	private String clientId;

	/**
	 * 客户端名称
	 */
	private String clientName;


	/**
	 * 客户端ID
	 */
	private String registeredClientId;

	/**
	 * 授权类型
	 */
	private String authorizationGrantType;

	/**
	 * 已授权的范围
	 */
	private Set<String> authorizedScopes;

	/**
	 * 访问令牌类型
	 */
	private String accessTokenType;

	/**
	 * 访问令牌范围
	 */
	private Set<String> accessTokenScopes;

	/**
	 * 访问令牌值
	 */
	private String accessTokenValue;

	/**
	 * 访问令牌登录时间
	 */
	private LocalDateTime accessTokenIssuedAt;

	/**
	 * 访问令牌过期时间
	 */
	private LocalDateTime accessTokenExpiresAt;

	/**
	 * 刷新令牌值
	 */
	private String refreshTokenValue;

	/**
	 * 刷新令牌时间
	 */
	private LocalDateTime refreshTokenIssuedAt;

	/**
	 * 刷新令牌过期时间
	 */
	private LocalDateTime refreshTokenExpiresAt;

	/**
	 * 属性
	 */
	private String attributes;

	/**
	 * 状态，参考 AccountAuthorizationStatus
	 */
	private String status;

	/**
	 * 状态
	 */
	private String deviceId;

	/**
	 * 状态
	 */
	private LocalDateTime deviceTime;

	/**
	 * ip
	 */
	private String ip;

	/**
	 * region
	 */
	private String region;

	/**
	 * 原始agent
	 */
	private String agent;

	/**
	 * 操作系统
	 */
	private String os;

	/**
	 * 登录平台
	 */
	private String platform;

	/**
	 * 引擎
	 */
	private String engine;

	/**
	 * 程序名称
	 */
	private String app;

	/**
	 * 登录时间
	 */
	private LocalDateTime loginTime;

	/**
	 * 退出时间
	 */
	private LocalDateTime logoutTime;


	/**
	 * 在线时长(秒)
	 */
	private long onlineDuration;

}
