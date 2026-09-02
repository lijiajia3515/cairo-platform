package io.github.lijiajia3515.cairo.auth.domain.dto.app_department;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 应用部门
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class AppDepartment implements Serializable {
	/**
	 * 上级ID
	 */
	private String parentId;

	/**
	 * 根节点
	 */
	private boolean root;

	/**
	 * 应用部门ID
	 */
	private String departmentId;

	/**
	 * 名称
	 */
	private String departmentName;

	/**
	 * 备注
	 */
	private String remark;

	/**
	 * 排序值
	 */
	private Long sort;
}
