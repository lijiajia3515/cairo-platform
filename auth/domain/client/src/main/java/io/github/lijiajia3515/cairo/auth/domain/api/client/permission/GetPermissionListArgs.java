package io.github.lijiajia3515.cairo.auth.domain.api.client.permission;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * 修改菜单参数
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class GetPermissionListArgs implements Serializable {

	/**
	 * 菜单id
	 */
	private List<String> menuIds;

	/**
	 * 终端ID
	 */
	@NotBlank
	private String endpointId;

	/**
	 * subappId
	 */
	@NotBlank
	private String subappId;

	/**
	 * subappVersion
	 */
	@NotBlank
	private String subappVersion;



	private Boolean defaultPermission;

	private Set<String> permissionIds;
}
