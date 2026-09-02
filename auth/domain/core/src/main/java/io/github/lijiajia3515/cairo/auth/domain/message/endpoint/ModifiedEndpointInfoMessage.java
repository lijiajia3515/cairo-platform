package io.github.lijiajia3515.cairo.auth.domain.message.endpoint;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 已修改终端消息实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModifiedEndpointInfoMessage {

	/**
	 * id
	 */
	private String id;

	/**
	 * appId
	 */
	private String appId;

	/**
	 * 名称
	 */
	private String appName;

	/**
	 * 旧端点ID
	 */
	private String oldEndpointId;

	/**
	 * 旧端点名称
	 */
	private String oldEndpointName;

	/**
	 * 新端点ID
	 */
	private String newEndpointId;

	/**
	 * 新端点名称
	 */
	private String newEndpointName;

	/**
	 * 老类型
	 */
	private String oldType;

	/**
	 * 新类型
	 */
	private String newType;

	/**
	 * 老范围
	 */
	private String oldScope;

	/**
	 * 新范围
	 */
	private String newScope;

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
