package io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.tenant_app_department;


import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 部门 保存 请求
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class CreateDepartmentArgs implements Serializable {
	/**
	 * 父级ID
	 */
	@NotNull
	private String parentId;

	/**
	 * 排序id之前
	 */
	private String beforeId;

	/**
	 * 名称
	 */
	@NotNull
	private String departmentName;

	/**
	 * 备注
	 */
	private String remark;
}
