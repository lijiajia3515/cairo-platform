package io.github.lijiajia3515.cairo.auth.modules.weboffice.v3.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * 返回值示例
 * {
 *   "code": 0,
 *   "msg": "",
 *   "data": {
 *     "fill_style": "rgba( 192, 192, 192, 0.6 )",
 *     "font": "bold 20px Serif",
 *     "horizontal": 50,
 *     "rotate": -0.7853982,
 *     "type": 1,
 *     "value": "谢绝拷贝\n2022-12-07 13:18:11",
 *     "vertical": 100
 *   }
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebOfficeFileWatermarkResponse {

	/**
	 * 水印类型，0表示无水印，1表示文字水印
	 */
	@JsonProperty("type")
	private Integer type;

	/**
	 * 水印显示的文字内容 (type = 1 时必须)
	 */
	@JsonProperty("value")
	private String value;

	/**
	 * 水印透明度，示例： rgba( 192, 192, 192, 0.6 )
	 */
	@JsonProperty("fill_style")
	private String fillStyle;

	/**
	 * 水印字体设置，示例： bold 20px Serif
	 */
	@JsonProperty("font")
	private String font;

	/**
	 * 水印旋转度，示例： -0.7853982
	 */
	@JsonProperty("rotate")
	private Double rotate;

	/**
	 * 水印水平间距
	 */
	@JsonProperty("horizontal")
	private Integer horizontal;

	/**
	 * 水印垂直间距
	 */
	@JsonProperty("vertical")
	private Integer vertical;
}
