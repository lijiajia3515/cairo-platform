package io.github.lijiajia3515.cairo.auth.domain.api.subapp.app_department;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 删除应用部门请求
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class DeleteAppDepartmentArgs implements Serializable {
	/**
	 * 应用部门ID
	 */
	@NotNull
	@NotEmpty
	private String departmentId;
}
