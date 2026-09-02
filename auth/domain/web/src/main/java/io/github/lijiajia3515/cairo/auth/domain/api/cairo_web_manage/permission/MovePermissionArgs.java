package io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.permission;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 移动功能权限
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class MovePermissionArgs implements Serializable {

	/**
	 * 移动权限ID
	 */
	@NotNull
	private String movePermissionId;

	/**
	 * 被交换权限ID
	 */
	@NotNull
	private String swapPermissionId;


}
