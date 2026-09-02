package io.github.lijiajia3515.cairo.auth.domain.api.client.account;

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
public class GetAccountAuthArgs implements Serializable {
	/**
	 * 应用id
	 */
	@NotNull
	private String appId;

	/**
	 * 端id
	 */
	@NotNull
	private String clientId;

	/**
	 * 账号id
	 */
	@NotNull
	private String accountId;
}
