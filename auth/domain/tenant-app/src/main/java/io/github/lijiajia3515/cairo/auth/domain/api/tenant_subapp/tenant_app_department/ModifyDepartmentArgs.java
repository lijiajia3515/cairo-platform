package io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.tenant_app_department;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 部门 修改 请求
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class ModifyDepartmentArgs implements Serializable {
	/**
	 * 部门 id
	 */
	@NotNull
	private String departmentId;
	/**
	 * 上级 id
	 */
	private String parentId;
	/**
	 * 名称
	 */
	private String departmentName;
	/**
	 * 备注
	 */
	private String remark;
}
