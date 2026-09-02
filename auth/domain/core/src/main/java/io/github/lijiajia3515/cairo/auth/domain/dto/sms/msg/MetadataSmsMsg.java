package io.github.lijiajia3515.cairo.auth.domain.dto.sms.message;

import io.github.lijiajia3515.cairo.auth.domain.dto.app_user.CairoAppUserMetadata;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 短信记录 metadata
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetadataSmsMsg {
	/**
	 * 记录ID
	 */
	private String msgId;

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
	 * 手机号
	 */
	private String phoneNumber;

	/**
	 * 发送类型
	 */
	private String type;

	/**
	 * 短信文本
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
	 * 供应商签名
	 */
	private String providerSign;

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
	 * 失败次数
	 */
	private int failedCount;

	/**
	 * 版本
	 */
	private long version;
	/**
	 * metadata
	 */
	private CairoAppUserMetadata metadata;

}
