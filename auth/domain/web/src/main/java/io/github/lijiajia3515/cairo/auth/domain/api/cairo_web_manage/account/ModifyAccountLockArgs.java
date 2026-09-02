package io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.account;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class ModifyAccountLockArgs {
	/**
	 * 账号ID
	 */
	@NotNull
	private String accountId;

	/**
	 * 锁定状态
	 */
	@NotNull
	private Boolean locked;

}
