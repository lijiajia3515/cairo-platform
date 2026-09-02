package io.github.lijiajia3515.cairo.auth.domain.api.subapp.app_department;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 获取应用部门请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetAppDepartmentByDepartmentIdArgs implements Serializable {

	/**
	 * 应用部门 id
	 */
	@NotNull
	private String departmentId;
}
