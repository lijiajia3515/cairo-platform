package io.github.lijiajia3515.cairo.auth.domain.dto.subapp;

import io.github.lijiajia3515.cairo.auth.domain.dto.app_user.CairoAppUserMetadata;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.List;

/**
 * 子应用 metadata
 */
@SuperBuilder(toBuilder = true)
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class MetadataSubapp implements Serializable {

	/**
	 * 主键ID
	 */
	private String id;

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
	 * 终端名称
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
	 * 准入范围
	 */
	private String scope;

	/**
	 * 是否勾选
	 */
	@Builder.Default
	private Boolean isSelected = false;


	/**
	 * 是否开启
	 */
	private Boolean enabled;

	/**
	 * metadata
	 */
	private CairoAppUserMetadata metadata;

}
