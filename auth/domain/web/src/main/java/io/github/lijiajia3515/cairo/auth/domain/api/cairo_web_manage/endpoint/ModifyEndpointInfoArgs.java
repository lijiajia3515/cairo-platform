package io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.endpoint;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.List;

/**
 * 修改应用 参数
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class ModifyEndpointInfoArgs implements Serializable {

	@NotNull
	@NotBlank
	private String id;


	/**
	 * 终端ID
	 */
	private String endpointId;

	/**
	 * 终端名称
	 */
	private String endpointName;

	/**
	 * 类型
	 */
	private String type;

	/**
	 * 范围
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

}
