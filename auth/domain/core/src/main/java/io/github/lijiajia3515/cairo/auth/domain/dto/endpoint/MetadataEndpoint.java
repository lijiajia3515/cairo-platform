package io.github.lijiajia3515.cairo.auth.domain.dto.endpoint;

import io.github.lijiajia3515.cairo.auth.domain.dto.app_user.CairoAppUserMetadata;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.List;

/**
 * 应用端口
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class MetadataEndpoint implements Serializable {

	/**
	 * id
	 */
	private String id;
	/**
	 * appId
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
	 * 终端类型
	 */
	private String type;

	/**
	 * 终端范围
	 */
	private String scope;

	/**
	 * 图标值
	 */
	private String icon;

	/**
	 * 网站url
	 */
	private String websiteUrl;

	/**
	 * 是否开启
	 */
	private Boolean enabled;

	/**
	 * metadata
	 */
	private CairoAppUserMetadata metadata;

}
