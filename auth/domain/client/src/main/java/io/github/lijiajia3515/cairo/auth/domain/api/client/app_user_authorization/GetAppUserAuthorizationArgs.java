package io.github.lijiajia3515.cairo.auth.domain.api.client.app_user_authorization;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetAppUserAuthorizationArgs implements Serializable {
	/**
	 * 应用ID
	 */
	@NotNull
	private String appId;

	/**
	 * 终端ID
	 */
	@NotNull
	private String endpointId;

	/**
	 * 访问令牌
	 */
	@NotNull
	private String accessToken;
}
