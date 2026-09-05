package io.github.lijiajia3515.cairo.auth.domain.api.client.tenant_app;


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
 * 企业 查询 参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class GetTenantAppArgs extends AbstractPage<GetTenantAppArgs> implements Serializable {

	/**
	 * tenantId
	 */
	private String tenantId;

	/**
	 * appId
	 */
	private String appId;

	/**
	 * 启用状态
	 */
	private Boolean enabled;

	@Builder.Default
	private Map<String, String> extension = new HashMap<>();
}
