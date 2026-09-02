package io.github.lijiajia3515.cairo.auth.domain.dto.sns_provider;

import io.github.lijiajia3515.cairo.auth.domain.dto.app_user.CairoAppUserMetadata;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 第三方认证提供方
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetadataSnsProvider implements Serializable {

	/**
	 * 应用ID
	 */
	private String appId;

	/**
	 * 应用名称
	 */
	private String appName;

	/**
	 * 应用图标
	 */
	private String appIcon;

	/**
	 * 第三方认证ID
	 */
	private String snsProviderId;

	/**
	 * 名称
	 */
	private String snsProviderName;

	/**
	 * 类型
	 */
	private String snsProviderTypeId;

	/**
	 * 类型名称
	 */
	private String snsProviderTypeName;

	/**
	 * 厂商
	 */
	private String snsProviderPartnerId;

	/**
	 * 厂商名称
	 */
	private String snsProviderPartnerName;

	/**
	 * 厂商图标
	 */
	private String snsProviderPartnerIcon;

	/**
	 * clientId
	 */
	private String clientId;

	/**
	 * clientSecret
	 */
	private String clientSecret;

	/**
	 * 启用状态
	 */
	private Boolean enabled;

	/**
	 * 是否自动注册
	 */
	private Boolean isAutoRegister;


	/**
	 * metadata
	 */
	private CairoAppUserMetadata metadata;
}
