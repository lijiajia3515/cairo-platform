package io.github.lijiajia3515.cairo.auth.domain.message.app;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 已创建应用消息实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatedAppMessage {
	/**
	 * appId
	 */
	private String appId;

	/**
	 * 名称
	 */
	private String appName;

	/**
	 * 图标值
	 */
	private String icon;

	/**
	 * 范围
	 */
	private List<String> scopes;

	/**
	 * 绑定管理员账号id
	 */
	private List<String> adminAccountIds;


	/**
	 * 开发平台用户ID
	 */
	private String eventCairoUserId;

	/**
	 * 事件时间
	 */
	private LocalDateTime eventTime;
}
