package io.github.lijiajia3515.cairo.auth.domain.message.app;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 已删除应用消息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeletedAppMessage {

	/**
	 * 删除应用ID
	 */
	private String appId;

	/**
	 * 开发平台用户ID
	 */
	private String eventCairoUserId;

	/**
	 * 事件时间
	 */
	private LocalDateTime eventTime;
}
