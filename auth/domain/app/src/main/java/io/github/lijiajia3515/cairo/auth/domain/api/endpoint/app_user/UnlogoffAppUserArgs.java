package io.github.lijiajia3515.cairo.auth.domain.api.endpoint.app_user;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 删除企业用户参数
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class UnlogoffAppUserArgs implements Serializable {

	/**
	 * 应用用户ID
	 */
	@NotNull
	private String userId;
}
