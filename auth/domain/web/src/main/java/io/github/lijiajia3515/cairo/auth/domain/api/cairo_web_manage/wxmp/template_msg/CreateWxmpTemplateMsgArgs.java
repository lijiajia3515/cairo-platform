package io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.wxmp.template_msg;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * 创建微信模板
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class CreateWxmpTemplateMsgArgs implements Serializable {

	/**
	 * 业务ID
	 */
	@NotNull
	private String bizId;

	/**
	 * 模板名称
	 */
	@NotNull
	private String templateName;


	/**
	 * 模板编码
	 */
	@NotNull
	private String templateCode;

	/**
	 * 模板类型
	 */
	@NotNull
	private String templateType;

	/**
	 * 模板文本
	 */
	@NotNull
	private String templateText;

	/**
	 * 公众号管理id
	 */
	@NotNull
	private String wxmpProviderId;

	/**
	 * 跳转链接
	 */
	@NotNull
	private String jumpUrl;

	/**
	 * 参数
	 */
	private List<Arg> args;

	/**
	 * 模板参数
	 */
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class Arg {
		/**
		 * 参数名称
		 */
		private String argName;

		/**
		 * 参数编码
		 */
		private String argCode;

		/**
		 * 参数类型
		 */
		private String argType;

		/**
		 * 模板参数编码
		 */
		private String templateArgCode;

		/**
		 * 默认颜色
		 */
		private String defaultColor;
	}
}
