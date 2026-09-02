package io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.tenant_subapp;


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
 * 获取企业子应用参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class GetTenantSubappArgs extends AbstractPage<GetTenantSubappArgs> implements Serializable {
	/**
	 * 终端ID
	 */
	private String endpointId;

	/**
	 * 子应用ID
	 */
	private String subappId;


	/**
	 * 启用状态
	 */
	@Builder.Default
	private Boolean enabled = true;

	@Builder.Default
	private Map<String, String> extension = new HashMap<>();
}
