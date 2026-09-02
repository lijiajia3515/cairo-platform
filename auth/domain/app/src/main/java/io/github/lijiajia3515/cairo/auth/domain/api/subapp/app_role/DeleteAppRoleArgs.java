package io.github.lijiajia3515.cairo.auth.domain.api.subapp.app_role;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.Collection;

/**
 * 删除应用角色参数
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class DeleteAppRoleArgs implements Serializable {

	@NotEmpty
	@NotNull
	private Collection<String> roleIds;
}
