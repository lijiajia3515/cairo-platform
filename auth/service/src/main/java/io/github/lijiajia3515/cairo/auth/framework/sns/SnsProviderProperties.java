package io.github.lijiajia3515.cairo.auth.framework.sns;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


/**
 * 微信公众号配置
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SnsProviderProperties {

	/**
	 * 三方认证类型配置
	 */
	List<ProviderTypeProperties> providerTypes;


	/**
	 * 三方厂商配置
	 */
	List<ProviderPartnerProperties> providerPartners;


}
