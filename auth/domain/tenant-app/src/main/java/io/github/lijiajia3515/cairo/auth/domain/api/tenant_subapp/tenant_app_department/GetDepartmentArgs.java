package io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.tenant_app_department;


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
import java.util.Set;

/**
 * 获取部门的参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class GetDepartmentArgs extends AbstractPage<GetDepartmentArgs> implements Serializable {

	/**
	 * 上级ID
	 */
	private String parentId;

	/**
	 * 部门ID
	 */
	private Set<String> departmentIds;


	@Builder.Default
	private Map<String, String> extension = new HashMap<>();
}
