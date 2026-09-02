package io.github.lijiajia3515.cairo.auth.domain.dto.app_department;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.List;

/**
 * 应用部门 路径模式 对象
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class PathAppDepartment implements Serializable {

	/**
	 * 应用部门ID，层级级由低到高
	 */
	private List<String> departmentIds;

	/**
	 * 部门名称，层级由低到高
	 */
	private List<String> departmentNames;

	/**
	 * 层级
	 */
	private Integer depth;
}
