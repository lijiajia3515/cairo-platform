package io.github.lijiajia3515.cairo.auth.domain.api.client.account_sns;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 绑定三方账号
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class BindAccountSnsArgs implements Serializable {
	/**
	 * 账号id
	 */
	@NotEmpty
	private String accountId;

	/**
	 * 第三方账号厂商ID
	 */
	@NotNull
	private String snsToken;
}
