package io.github.lijiajia3515.cairo.auth.domain.dto.wxmp.template_msg;

import io.github.lijiajia3515.cairo.auth.domain.dto.app_user.CairoAppUserMetadata;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 微信模板 metadata
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetadataWxmpTemplateMsg {

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
	private List<WxmpTemplateMsgArg> args;

	/**
	 * 公众号管理id
	 */
	private String wxmpProviderId;

	/**
	 * 是否启用
	 */
	private Boolean enabled;

	/**
	 * 跳转链接
	 */
	private String jumpUrl;


	/**
	 * metadata
	 */
	private CairoAppUserMetadata metadata;

}
