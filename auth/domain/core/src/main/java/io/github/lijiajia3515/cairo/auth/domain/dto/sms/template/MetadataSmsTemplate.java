package io.github.lijiajia3515.cairo.auth.domain.dto.sms.template;

import io.github.lijiajia3515.cairo.auth.domain.dto.app_user.CairoAppUserMetadata;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 短信模板 metadata
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetadataSmsTemplate {

	/**
	 * 应用ID
	 */
	private String appId;

	/**
	 * 应用名称
	 */
	private String appName;

	/**
	 * 应用图标
	 */
	private String appIcon;

	/**
	 * 业务ID
	 */
	private String bizId;

	/**
	 * 模板名称
	 */
	private String templateName;

	/**
	 * 签名
	 */
	private String templateSign;

	/**
	 * 模板编码
	 */
	private String templateCode;

	/**
	 * 模板编码
	 */
	private String templateType;

	/**
	 * 模板文本
	 */
	private String templateText;

	/**
	 * 参数
	 */
	private List<SmsTemplateArg> args;

	/**
	 * 是否启用
	 */
	private Boolean enabled;

	/**
	 * metadata
	 */
	private CairoAppUserMetadata metadata;

}
