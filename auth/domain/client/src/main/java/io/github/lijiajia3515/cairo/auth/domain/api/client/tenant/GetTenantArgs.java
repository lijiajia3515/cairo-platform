package io.github.lijiajia3515.cairo.auth.domain.api.client.tenant;


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
 * 租户 查询 参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class GetTenantArgs extends AbstractPage<GetTenantArgs> implements Serializable {

	private Boolean enabled;

	@Builder.Default
	private Map<String, String> extension = new HashMap<>();
}
