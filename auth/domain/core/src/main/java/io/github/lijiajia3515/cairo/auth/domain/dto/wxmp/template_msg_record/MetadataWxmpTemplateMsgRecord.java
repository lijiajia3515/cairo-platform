package io.github.lijiajia3515.cairo.auth.domain.dto.wxmp.template_msg_record;

import io.github.lijiajia3515.cairo.auth.domain.dto.app_user.CairoAppUserMetadata;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 微信公众号消息记录 metadata
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetadataWxmpTemplateMsgRecord {
	/**
	 * 记录ID
	 */
	private String msgId;

	/**
	 * 公众号管理id
	 */
	private String wxmpProviderId;

	/**
	 * 时间
	 */
	private LocalDateTime time;

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
	 * 连接id
	 */
	private String openId;

	/**
	 * 发送类型
	 */
	private String type;

	/**
	 * 微信消息文本
	 */
	private String text;

	/**
	 * 业务参数
	 */
	private String bizArgs;

	/**
	 * 供应商类型
	 */
	private String providerType;


	/**
	 * 供应商模板类型
	 */
	private String providerTemplateCode;

	/**
	 * 供应商参数
	 */
	private String providerArgs;

	/**
	 * 供应商发送回执ID
	 */
	private String providerMsgId;

	/**
	 * 是否发送成功
	 */
	private boolean success;

	/**
	 * 失败原因
	 */
	private String reason;

	/**
	 * 跳转链接
	 */
	private String jumpUrl;


	/**
	 * 来源
	 */
	private String source;

	/**
	 * 版本
	 */
	private long version;

	/**
	 * metadata
	 */
	private CairoAppUserMetadata metadata;

}
