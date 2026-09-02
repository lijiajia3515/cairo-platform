package io.github.lijiajia3515.cairo.auth.domain.dto.sns_provider;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;

/**
 * 第三方认证提供方
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BasicSnsProvider implements Serializable {
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
	private String snsProviderType;

	/**
	 * 厂商
	 */
	private String snsProviderPartner;

	/**
	 * clientId
	 */
	private String clientId;

	/**
	 * 启用状态
	 */
	private Boolean enabled;



}
