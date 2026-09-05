package io.github.lijiajia3515.cairo.auth.domain.api.tenant_app_user.tenant_app_user_authorization;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 下线企业应用级用户会话
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class OfflineMyTenantAppUserAuthorizationArgs implements Serializable {

	/**
	 * 会话id
	 */
	@NotBlank
	private String tokenId;
}
