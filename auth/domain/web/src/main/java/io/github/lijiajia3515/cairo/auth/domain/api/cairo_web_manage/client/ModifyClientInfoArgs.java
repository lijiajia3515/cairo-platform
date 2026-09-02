package io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.client;

import io.github.lijiajia3515.cairo.auth.domain.dto.client.ClientSettings;
import io.github.lijiajia3515.cairo.auth.domain.dto.client.TokenSettings;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModifyClientInfoArgs {
	/**
	 * id
	 */
	@NotNull
	private String id;


	/**
	 * 终端ID
	 */
	private String endpointId;

	/**
	 * 客户端名称
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
	 *
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
}
