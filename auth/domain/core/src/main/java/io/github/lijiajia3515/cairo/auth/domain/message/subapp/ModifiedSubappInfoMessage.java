package io.github.lijiajia3515.cairo.auth.domain.message.subapp;

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
public class ModifiedSubappInfoMessage {

	/**
	 * ID
	 */
	private String id;

	/**
	 * 应用ID
	 */
	private String appId;

	/**
	 * 应用名称
	 */
	private String appName;

	/**
	 * 终端ID
	 */
	private String endpointId;

	/**
	 * 终端名称
	 */
	private String endpointName;

	/**
	 * 旧端点ID
	 */
	private String oldSubappId;

	/**
	 * 旧端点名称
	 */
	private String oldSubappName;

	/**
	 * 新子应用ID
	 */
	private String newSubappId;

	/**
	 * 新子应用名称
	 */
	private String newSubappName;


	/**
	 * 开发平台用户ID
	 */
	private String eventCairoUserId;

	/**
	 * 事件时间
	 */
	private LocalDateTime eventTime;
}
