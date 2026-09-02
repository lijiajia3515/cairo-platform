package io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.client;

import io.github.lijiajia3515.cairo.auth.domain.dto.client.ClientSettings;
import io.github.lijiajia3515.cairo.auth.domain.dto.client.TokenSettings;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 创建客户端参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateClientArgs {
	/**
	 * 应用标识
	 */
	@NotNull
	private String appId;
	/**
	 * 端标识
	 */
	private String endpointId;
	/**
	 * clientId
	 */
	@NotNull
	private String clientId;

	/**
	 * client secret
	 */
	@NotNull
	private String clientSecret;
	/**
	 * 客户端标识
	 */
	@NotNull
	private String clientName;
	/**
	 * 认证方法
	 */
	@NotNull
	@NotEmpty
	private List<String> clientAuthenticationMethods;
	/**
	 * 授权 类型
	 */
	@NotNull
	@NotEmpty
	private List<String> authorizationGrantTypes;
	/**
	 * 回跳地址
	 */
	private List<String> redirectUris;
	/**
	 * scopes
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
	 * 身份类型
	 */
	private List<String> authenticationTypes;

	/**
	 * 账号第三方认证供应商ID
	 */
	private List<String> accountSnsProviderIds;

	/**
	 * 启用
	 */
	@Builder.Default
	private Boolean enabled = true;
}
