package io.github.lijiajia3515.cairo.auth.domain.api.subapp.app_department;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 获取应用部门树请求
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class GetAppDepartmentTreeArgs implements Serializable {
	/**
	 * 上级ID
	 */
	private String parentId;
}
