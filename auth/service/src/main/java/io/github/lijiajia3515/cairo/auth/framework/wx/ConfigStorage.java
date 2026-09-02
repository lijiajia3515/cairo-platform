package io.github.lijiajia3515.cairo.auth.framework.wx;

import lombok.Data;

import java.io.Serializable;

@Data
public class ConfigStorage implements Serializable {

	/**
	 * http代理主机.
	 */
	private String httpProxyHost;

	/**
	 * http代理端口.
	 */
	private Integer httpProxyPort;

	/**
	 * http代理用户名.
	 */
	private String httpProxyUsername;

	/**
	 * http代理密码.
	 */
	private String httpProxyPassword;

}
