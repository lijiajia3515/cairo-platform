package io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.account;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 重置密码参数
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class ResetAccountPasswordArgs implements Serializable {

	/**
	 * 账号ID
	 */
	@NotNull
	private String accountId;


	/**
	 * 重置的密码
	 */
	@Size(min = 6, max = 40)
	private String password;
}
