package io.github.lijiajia3515.cairo.auth.modules.weboffice;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 企业应用级用户
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CairoTenantAppUser implements Serializable {

	/**
	 * 企业ID
	 */
	private String tenantId;

	/**
	 * 应用ID
	 */
	private String appId;

	/**
	 * 用户ID
	 */
	private String userId;
}
