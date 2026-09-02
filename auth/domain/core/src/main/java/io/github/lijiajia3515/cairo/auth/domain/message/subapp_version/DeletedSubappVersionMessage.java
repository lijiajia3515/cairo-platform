package io.github.lijiajia3515.cairo.auth.domain.message.subapp_version;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 已删除的子应用版本消息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeletedSubappVersionMessage {
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
	 * 子应用版本
	 */
	private String subappVersion;

	/**
	 * 开发平台用户ID
	 */
	private String eventCairoUserId;

	/**
	 * 事件时间
	 */
	private LocalDateTime eventTime;
}
