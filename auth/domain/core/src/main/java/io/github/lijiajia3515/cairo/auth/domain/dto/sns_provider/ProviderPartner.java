package io.github.lijiajia3515.cairo.auth.domain.dto.sns_provider;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 第三方认证厂商
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProviderPartner implements Serializable {
	/**
	 * 第三方厂商id
	 */
	private String providerPartnerId;

	/**
	 * 第三方厂商名称
	 */
	private String providerPartnerName;

	/**
	 * 第三方厂商图标
	 */
	private String providerPartnerIcon;


	/**
	 * 启用状态
	 */
	private Boolean enabled;


}
