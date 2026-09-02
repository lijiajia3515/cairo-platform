package io.github.lijiajia3515.cairo.auth.domain.message.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 同步client消息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SyncedClientMessage {
	/**
	 * 同步前client
	 */
	private String beforeClientId;

	/**
	 * 同步前应用ID
	 */
	private String beforeAppId;

	/**
	 * 同步前终端ID
	 */
	private String beforeEndpointId;

	/**
	 * 同步后client
	 */
	private String afterClientId;

	/**
	 * 同步后应用ID
	 */
	private String afterAppId;

	/**
	 * 同步后终端ID
	 */
	private String afterEndpointId;


	/**
	 * 开发平台用户ID
	 */
	private String eventCairoUserId;

	/**
	 * 事件时间
	 */
	private LocalDateTime eventTime;
}
