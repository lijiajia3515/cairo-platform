package io.github.lijiajia3515.cairo.auth.framework.wx.mp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;


/**
 * 微信公众号配置
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CairoWxMpProperties {

	/**
	 * 公众号配置文件配置
	 */
	Map<String, WxMpProperties> configs;


}
