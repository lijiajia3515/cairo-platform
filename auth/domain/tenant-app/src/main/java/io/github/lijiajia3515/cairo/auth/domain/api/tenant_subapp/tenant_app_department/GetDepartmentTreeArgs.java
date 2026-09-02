package io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.tenant_app_department;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 部门树 查询参数
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class GetDepartmentTreeArgs implements Serializable {
	/**
	 * 上级ID
	 */
	private String parentId;
}
