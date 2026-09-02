package io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.endpoint;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.List;

/**
 * 应用创建请求
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class CreateEndpointArgs implements Serializable {

	/**
	 * 应用ID
	 */
	@NotNull
	@NotBlank
	private String appId;

	/**
	 * 终端ID
	 */
	@NotNull
	@NotBlank
	private String endpointId;

	/**
	 * 终端名称
	 */
	@NotNull
	@NotBlank
	private String endpointName;

	/**
	 * 终端类型
	 */
	@NotNull
	@NotBlank
	private String type;

	/**
	 * 终端范围
	 */
	@NotNull
	@NotBlank
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
	 * 启用状态
	 */
	@Builder.Default
	private boolean enabled = false;
}
