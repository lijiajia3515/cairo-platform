package io.github.lijiajia3515.cairo.auth.domain.message.notify.template;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 删除模板消息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeleteNotifyTemplateMessage {

	/**
	 * 应用ID
	 */
	private String appId;

	/**
	 * 模板ID
	 */
	private String templateId;

	/**
	 * 消息编码
	 */
	private String messageCode;

	/**
	 * 开发平台用户ID
	 */
	private String eventCairoUserId;

	/**
	 * 事件时间
	 */
	private LocalDateTime eventTime;
}
