package io.github.lijiajia3515.cairo.auth.domain.message.subapp_version;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 同步子应用版本消息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SyncedSubappVersionMessage {

	/**
	 * 数据来源子应用ID
	 */
	private String sourceSubappId;

	/**
	 * 数据来源子应用版本
	 */
	private String sourceSubappVersion;

	/**
	 * 数据变动应用ID
	 */
	private String changeAppId;

	/**
	 * 数据变动终端ID
	 */
	private String changeEndpointId;

	/**
	 * 数据变动子应用ID
	 */
	private String changeSubappId;

	/**
	 * 数据变动子应用版本号
	 */
	private String changeSubappVersion;


	/**
	 * 开发平台用户ID
	 */
	private String eventCairoUserId;

	/**
	 * 事件时间
	 */
	private LocalDateTime eventTime;
}
