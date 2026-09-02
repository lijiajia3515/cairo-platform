package io.github.lijiajia3515.cairo.auth.framework.wx.ma;

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
public class CairoWxMaProperties {

	/**
	 * 公众号配置文件配置
	 */
	Map<String, WxMaProperties> configs;


}
