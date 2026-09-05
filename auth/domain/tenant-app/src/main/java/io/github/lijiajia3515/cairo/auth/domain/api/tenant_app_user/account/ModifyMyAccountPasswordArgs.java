package io.github.lijiajia3515.cairo.auth.domain.api.tenant_app_user.account;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 修改账号密码参数
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class ModifyMyAccountPasswordArgs implements Serializable {
	/**
	 * 密码
	 */
	@NotNull
	@NotBlank
	private String password;
	/**
	 * 新密码
	 */
	@NotNull
	@NotBlank
	@Size(min = 6, max = 40)
	private String newPassword;
}
