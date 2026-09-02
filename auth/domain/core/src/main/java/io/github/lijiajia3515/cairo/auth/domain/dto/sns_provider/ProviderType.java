package io.github.lijiajia3515.cairo.auth.domain.dto.sns_provider;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 第三方认证类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProviderType implements Serializable {
	/**
	 * 第三方认证类型id
	 */
	private String providerTypeId;

	/**
	 * 第三方认证类型名称
	 */
	private String providerTypeName;


	/**
	 * 启用状态
	 */
	private Boolean enabled;


}
