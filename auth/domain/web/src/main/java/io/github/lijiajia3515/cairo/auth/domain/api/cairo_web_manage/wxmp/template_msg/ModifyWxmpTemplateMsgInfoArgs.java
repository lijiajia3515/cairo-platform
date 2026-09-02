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
 * 修改微信模板信息参数
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class ModifyWxmpTemplateMsgInfoArgs implements Serializable {

	/**
	 * 业务ID
	 */
	@NotNull
	private String bizId;

	/**
	 * 公众号管理id
	 */
	private String wxmpProviderId;

	/**
	 * 模板名称
	 */
	private String templateName;

	/**
	 * 模板编码
	 */
	private String templateCode;

	/**
	 * 模板类型
	 */
	private String templateType;

	/**
	 * 模板文本
	 */
	private String templateText;

	/**
	 * 跳转链接
	 */
	private String jumpUrl;

	/**
	 * 参数
	 */
	private List<CreateWxmpTemplateMsgArgs.Arg> args;

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
		 * 参数类型
		 */
		private String argType;

		/**
		 * 参数编码
		 */
		private String argCode;

		/**
		 * 模板参数编码
		 */
		private String templateArgCode;
	}


}
