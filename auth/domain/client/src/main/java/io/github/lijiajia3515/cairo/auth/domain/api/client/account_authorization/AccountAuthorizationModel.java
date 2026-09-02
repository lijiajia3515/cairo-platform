package io.github.lijiajia3515.cairo.auth.domain.api.client.account_authorization;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.Collection;
import java.util.Set;

@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class AccountAuthorizationModel {
	/**
	 * 认证状态
	 */
	private String status;

	/**
	 * 错误信息
	 */
	private String errorMessage;

	/**
	 * 授权时间
	 */
	private Instant issuedAt;

	/**
	 * 过期时间
	 */
	private Instant expiresAt;

	/**
	 * 已授权范围
	 */
	private Set<String> authorizedScopes;

	/**
	 * token标识
	 */
	private String tokenId;

	/**
	 * 应用ID
	 */
	private String appId;

	/**
	 * 客户端ID
	 */
	private String clientId;

	/**
	 * 账号ID
	 */
	private String accountId;

	/**
	 * 登录方式
	 */
	private String loginType;

	/**
	 * 第三方认证类型
	 */
	private String snsType;

	/**
	 * 头像
	 */
	private String avatarUrl;

	/**
	 * 昵称
	 */
	private String nickname;

	/**
	 * 手机号
	 */
	private String phoneNumber;

	/**
	 * 用户名
	 */
	private String username;

	/**
	 * 邮箱
	 */
	private String email;

	/**
	 * 账号锁定状态
	 */
	private Boolean locked;

	/**
	 * 启用状态
	 */
	private Boolean enabled;
	/**
	 * 权限信息
	 */
	private Collection<String> authorities;
}
