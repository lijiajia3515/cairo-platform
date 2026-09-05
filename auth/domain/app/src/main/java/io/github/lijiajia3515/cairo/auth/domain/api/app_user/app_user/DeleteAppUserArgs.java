package io.github.lijiajia3515.cairo.auth.domain.api.app_user.app_user;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 删除应用级用户参数
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class DeleteAppUserArgs implements Serializable {

	/**
	 * 应用级用户ID
	 */
	@NotNull
	private String userId;
}
