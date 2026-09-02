package io.github.lijiajia3515.cairo.auth.domain.dto.tenant_subapp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 企业子应用
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class TenantSubapp implements Serializable {

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
	 * 子应用ID
	 */
	private String subappId;

	/**
	 * 子应用名称
	 */
	private String subappName;

	/**
	 * 子应用图标
	 */
	private String subappIcon;

	/**
	 * 启用状态
	 */
	private Boolean enabled;


}
