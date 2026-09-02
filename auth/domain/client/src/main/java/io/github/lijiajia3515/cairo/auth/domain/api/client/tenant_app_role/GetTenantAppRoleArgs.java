package io.github.lijiajia3515.cairo.auth.domain.api.client.tenant_app_role;

import io.github.lijiajia3515.cairo.core.page.AbstractPage;
import jakarta.validation.constraints.NotNull;
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
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)

public class GetTenantAppRoleArgs extends AbstractPage<GetTenantAppRoleArgs> implements Serializable {
	@NotNull
	private String tenantId;

	private Set<String> roleIds;

	private String keyword;

	@Builder.Default
	private Map<String, String> extension = new HashMap<>();
}
