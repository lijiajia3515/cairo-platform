package io.github.lijiajia3515.cairo.auth.domain.api.tenant_endpoint.tenant_app_user;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 转移用户至其他账号
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class TransferTenantAppUserToOtherAccountArgs implements Serializable {

	/**
	 * 用户ID
	 */
	@NotNull
	private String userId;

	/**
	 * 其他账号ID
	 */
	@NotNull
	private String otherAccountId;
}
