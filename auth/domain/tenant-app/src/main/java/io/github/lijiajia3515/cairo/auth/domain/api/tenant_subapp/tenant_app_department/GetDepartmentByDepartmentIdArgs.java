package io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.tenant_app_department;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 根据部门ID获取部门的参数
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetDepartmentByDepartmentIdArgs implements Serializable {

	/**
	 * 部门 id
	 */
	@NotNull
	private String departmentId;
}
