package io.github.lijiajia3515.cairo.auth.domain.api.client.client;


import io.github.lijiajia3515.cairo.core.page.AbstractPage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 应用 查询 参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class GetClientArgs extends AbstractPage<GetClientArgs> implements Serializable {

	/**
	 * 客户端ID
	 */
	private List<String> clientIds;

	/**
	 * 身份类型
	 */
	private List<String> authenticationTypes;


	/**
	 * 账号三方认证
	 */
	private List<String> accountSnsProviderIds;


	/**
	 * 关键字
	 */
	private String keyword;

	/**
	 * 应用ID
	 */
	private String appId;

	/**
	 * 终端ID
	 */
	private String endpointId;

	/**
	 * 认证授权类型
	 */
	private List<String> authorizationGrantTypes;

	/**
	 * 启用状态
	 */
	private Boolean enabled;

	@Builder.Default
	private Map<String, String> extension = new HashMap<>();

}
