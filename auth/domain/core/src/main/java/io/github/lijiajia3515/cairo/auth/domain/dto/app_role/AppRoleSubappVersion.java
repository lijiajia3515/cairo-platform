package io.github.lijiajia3515.cairo.auth.domain.dto.app_role;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 应用角色子应用版本
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class AppRoleSubappVersion implements Serializable {

	/**
	 * 子应用ID
	 */
	private String subappId;


	/**
	 * 子应用版本
	 */
	private String subappVersion;

	/**
	 * 子应用备注
	 */
	private String subappRemark;


	/**
	 * 是否启用
	 */
	private Boolean enabled;


}
