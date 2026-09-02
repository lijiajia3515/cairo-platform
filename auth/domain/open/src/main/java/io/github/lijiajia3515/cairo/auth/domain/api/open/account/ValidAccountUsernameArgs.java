package io.github.lijiajia3515.cairo.auth.domain.api.open.account;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 校验账号用户名
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class ValidAccountUsernameArgs implements Serializable {


	/**
	 * 用户名
	 */
	@NotNull
	@NotBlank
	private String username;
}
