package io.github.lijiajia3515.cairo.auth.domain.dto.client;

import io.github.lijiajia3515.cairo.auth.domain.dto.app_user.CairoAppUserMetadata;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppUserMetadataClient {
	/**
	 * 唯一标识
	 */
	private String id;
	/**
	 * 应用标识
	 */
	private String appId;
	/**
	 * 应用名称
	 */
	private String appName;
	/**
	 * 应用图标
	 */
	private String appIcon;
	/**
	 * 终端
	 */
	private String endpointId;
	/**
	 * 应用名称
	 */
	private String endpointName;
	/**
	 * 终端图标
	 */
	private String endpointIcon;
	/**
	 * access_key
	 */
	private String clientId;
	/**
	 * access 到期时间
	 */
	private Instant clientIdIssuedAt;
	/**
	 * access secret 到期时间
	 */
	private Instant clientSecretExpiresAt;
	/**
	 * 客户端标识
	 */
	private String clientName;
	/**
	 * 认证方法
	 */
	private List<String> clientAuthenticationMethods;
	/**
	 * 授权 类型
	 */
	private List<String> authorizationGrantTypes;
	/**
	 * 回跳地址
	 */
	private List<String> redirectUris;
	/**
	 * 权限范围
	 */
	private List<String> scopes;
	/**
	 * client settings
	 */
	private ClientSettings clientSettings;

	/**
	 * token settings
	 */
	private TokenSettings tokenSettings;

	/**
	 * 是否启用
	 */
	private Boolean enabled;

	/**
	 * 身份类型
	 */
	private List<String> authenticationTypes;

	/**
	 * 账号第三方认证供应商ID
	 */
	private List<String> accountSnsProviderIds;


	/**
	 * metadata
	 */
	private CairoAppUserMetadata metadata;
}
