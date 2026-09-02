package io.github.lijiajia3515.cairo.auth.framework.sns;

import lombok.Data;


/**
 * 三方认证类型
 *
 * @author someone
 */
@Data
public class ProviderTypeProperties {

	/**
	 * id
	 */
	private String id;

	/**
	 * 名称
	 */
	private String name;

	/**
	 * 启用状态
	 */
	private Boolean enabled;
}
