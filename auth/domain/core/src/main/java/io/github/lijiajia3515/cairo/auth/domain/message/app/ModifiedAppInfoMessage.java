package io.github.lijiajia3515.cairo.auth.domain.message.app;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 已修改应用信息消息实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModifiedAppInfoMessage {

	/**
	 * 应用ID
	 */
	private String appId;

	/**
	 * 应用名称
	 */
	private String appName;

	/**
	 * 是否内部应用
	 */
	private Boolean privateApp;

	/**
	 * 图标值
	 */
	private String icon;

	/**
	 * 被移除的管理员ID
	 */
	private List<String> removeAdminAccountIds;

	/**
	 * 新管理员账号ID
	 */
	private List<String> newAdminAccountIds;

	/**
	 * 开发平台用户ID
	 */
	private String eventCairoUserId;

	/**
	 * 事件时间
	 */
	private LocalDateTime eventTime;
}
