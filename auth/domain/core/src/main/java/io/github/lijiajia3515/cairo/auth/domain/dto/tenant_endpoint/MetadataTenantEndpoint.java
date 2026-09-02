package io.github.lijiajia3515.cairo.auth.domain.dto.tenant_endpoint;

import io.github.lijiajia3515.cairo.auth.domain.dto.account.CairoAccountMetadata;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 企业终端
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class MetadataTenantEndpoint implements Serializable {

	/**
	 * 企业ID
	 */
	private String tenantId;

	/**
	 * 企业名称
	 */
	private String tenantName;

	/**
	 * 企业图标
	 */
	private String tenantIcon;

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
	 * 终端ID
	 */
	private String endpointId;

	/**
	 * 终端名称
	 */
	private String endpointName;

	/**
	 * 终端图标
	 */
	private String endpointIcon;

	/**
	 * 启用状态
	 */
	private Boolean enabled;

	/**
	 * metadata
	 */
	private CairoAccountMetadata metadata;

}
