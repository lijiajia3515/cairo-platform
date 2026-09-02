package io.github.lijiajia3515.cairo.auth.domain.api.subapp.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetCurrentAppClientArgs implements Serializable {
	/**
	 * 终端ID
	 */
	private String endpointId;

	/**
	 * 身份类型
	 */
	private List<String> authenticationTypes;


	/**
	 * 账号三方认证
	 */
	private List<String> accountSnsProviderIds;
}
