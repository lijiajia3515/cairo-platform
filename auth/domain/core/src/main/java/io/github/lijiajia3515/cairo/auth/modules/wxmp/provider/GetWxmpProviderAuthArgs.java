package io.github.lijiajia3515.cairo.auth.modules.wxmp.provider;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetWxmpProviderAuthArgs {
	/**
	 * 第三方认证提供商ID
	 */
	private String wxmpProviderId;

	/**
	 * 第三方认证授权码
	 */
	private String code;

}
