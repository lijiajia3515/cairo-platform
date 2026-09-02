package io.github.lijiajia3515.cairo.auth.framework.security.core;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.List;

/**
 * 部门
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class CairoDepartment implements Serializable {

	/**
	 * 部门ID，层级级由低到高
	 */
	private List<String> departmentIds;

	/**
	 * 部门名称，层级由低到高
	 */
	private List<String> departmentNames;

	/**
	 * 主部门id
	 */
	private String mainDepartmentId;

	/**
	 * 层级
	 */
	private Integer depth;
}
