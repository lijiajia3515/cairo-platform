package io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.permission;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.Set;

/**
 * 删除权限参数
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class DeletePermissionArgs implements Serializable {

	/**
	 * 功能权限ID
	 */
	@NotNull
	@NotEmpty
	private Set<String> permissionIds;
}
