package io.github.lijiajia3515.cairo.auth.domain.message.app;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 修改应用状态消息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModifiedAppStatusMessage {

	/**
	 * 应用ID
	 */
	private String appId;

	/**
	 * 启用状态
	 */
	private Boolean enabled;


	/**
	 * 开发平台用户ID
	 */
	private String eventCairoUserId;

	/**
	 * 事件事件
	 */
	private LocalDateTime eventTime;
}
