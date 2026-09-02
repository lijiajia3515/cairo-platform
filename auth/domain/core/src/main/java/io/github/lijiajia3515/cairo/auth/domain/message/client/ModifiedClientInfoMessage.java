package io.github.lijiajia3515.cairo.auth.domain.message.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 已修改client信息消息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModifiedClientInfoMessage {
	/**
	 * id
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
	 * clientId
	 */
	private String clientId;

	/**
     * clientName
	 */
	private String clientName;

	/**
	 * 开发平台用户ID
	 */
	private String eventCairoUserId;

	/**
	 * 事件时间
	 */
	private LocalDateTime eventTime;
}
