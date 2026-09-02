package io.github.lijiajia3515.cairo.auth.domain.api.client.tenant;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 查询企业信息参数
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class GetTenantInfoArgs implements Serializable {

	private String tenantId;

	@Builder.Default
	private Map<String, String> extension = new HashMap<>();
}
