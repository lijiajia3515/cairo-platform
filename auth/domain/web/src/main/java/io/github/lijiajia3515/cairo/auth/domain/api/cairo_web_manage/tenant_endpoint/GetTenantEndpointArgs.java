package io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.tenant_endpoint;


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
import java.util.Map;

/**
 * 获取企业终端参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class GetTenantEndpointArgs extends AbstractPage<GetTenantEndpointArgs> implements Serializable {

	/**
	 * 企业ID
	 */
	private String tenantId;

	/**
	 * 应用ID
	 */
	private String appId;

	/**
	 * 终端ID
	 */
	private String endpointId;

	/**
	 * 启用状态
	 */
	private Boolean enabled;

	@Builder.Default
	private Map<String, String> extension = new HashMap<>();
}
