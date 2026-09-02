package io.github.lijiajia3515.cairo.auth.framework.wx.ma;

import io.github.lijiajia3515.cairo.auth.framework.wx.ConfigStorage;
import lombok.Data;
import me.chanjar.weixin.mp.config.WxMpHostConfig;

import java.io.Serializable;


/**
 * 微信接入相关配置属性.
 *
 * @author someone
 */
@Data
public class WxMaProperties implements Serializable {
	public static final String PREFIX = "wx.ma";

	/**
	 * 设置微信公众号的appid.
	 */
	private String appId;

	/**
	 * 设置微信公众号的app secret.
	 */
	private String secret;

	/**
	 * 设置微信公众号的token.
	 */
	private String token;

	/**
	 * 设置微信公众号的EncodingAESKey.
	 */
	private String aesKey;

	/**
	 * 自定义host配置
	 */
	private WxMpHostConfig host;

	/**
	 * 存储策略
	 */
	private ConfigStorage configStorage = new ConfigStorage();

}
