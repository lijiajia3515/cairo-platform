package io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.tenant_app_department;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * delete department args
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class DeleteDepartmentUserArgs implements Serializable {
	/**
	 * 部门ID
	 */
	@NotNull
	@NotEmpty
	private String departmentId;
}
