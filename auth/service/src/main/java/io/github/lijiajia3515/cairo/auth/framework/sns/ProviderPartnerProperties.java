package io.github.lijiajia3515.cairo.auth.framework.sns;

import lombok.Data;


/**
 * 三方认证厂商
 *
 * @author someone
 */
@Data
public class ProviderPartnerProperties {

	/**
	 * id
	 */
	private String id;

	/**
	 * 名称
	 */
	private String name;


	/**
	 * 图标
	 */
	private String icon;

	/**
	 * 启用状态
	 */
	private Boolean enabled;
}
