package io.github.lijiajia3515.cairo.auth.domain.api.app_user.account_sns;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 解绑三方账号
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class UnBindAccountSnsArgs implements Serializable {


	/**
	 * 第三方账号厂商ID
	 */
	@NotNull
	private String snsPartnerId;
}
