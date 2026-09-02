package io.github.lijiajia3515.cairo.auth.domain.api.client.app_department;


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
 * [client] 获取部门列表参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class GetAppDepartmentArgs extends AbstractPage<GetAppDepartmentArgs> implements Serializable {

	/**
	 * 部门ID
	 */
	private Set<String> departmentIds;

	/**
	 * 上级部门ID
	 */
	private String parentId;

	/**
	 * 插件
	 */
	@Builder.Default
	private Map<String, String> extension = new HashMap<>();
}
