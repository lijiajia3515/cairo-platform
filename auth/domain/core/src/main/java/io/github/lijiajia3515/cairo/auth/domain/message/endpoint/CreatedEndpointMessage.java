package io.github.lijiajia3515.cairo.auth.domain.message.endpoint;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 创建端点消息实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatedEndpointMessage {
	/**
	 * appId
	 */
	private String appId;

	/**
	 * 端点ID
	 */
	private String endpointId;

	/**
	 * 端点名称
	 */
	private String endpointName;

	/**
	 * 类型
	 */
	private String type;

	/**
	 * 准入范围
	 */
	private String scope;

	/**
	 * 图标值
	 */
	private String icon;

	/**
	 * 开发平台用户ID
	 */
	private String eventCairoUserId;

	/**
	 * 事件时间
	 */
	private LocalDateTime eventTime;
}
