package io.github.lijiajia3515.cairo.auth.domain.api.subapp.app_role;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 修改应用角色信息参数
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class ModifyAppRoleInfoArgs implements Serializable {

	/**
	 * id
	 */
	@NotNull
	@NotBlank
	private String roleId;



	/**
	 * 名称
	 */
	private String roleName;

	/**
	 * 备注
	 */
	private String remark;

}
