package io.github.lijiajia3515.cairo.auth.domain.message.subapp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 已创建 子应用 消息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatedSubappMessage {
	/**
	 * 应用ID
	 */
	private String id;

	/**
	 * 应用ID
	 */
	private String appId;

	/**
	 * 终端ID
	 */
	private String endpointId;

	/**
	 * 子应用ID
	 */
	private String subappId;

	/**
	 * 子应用名称
	 */
	private String subappName;

	/**
	 * 子应用图标
	 */
	private String subappIcon;

	/**
	 * 准入范围
	 */
	private String scope;

	/**
	 * 是否开启
	 */
	private Boolean enabled;

	/**
	 * 开发平台用户ID
	 */
	private String eventCairoUserId;

	/**
	 * 事件时间
	 */
	private LocalDateTime eventTime;
}
