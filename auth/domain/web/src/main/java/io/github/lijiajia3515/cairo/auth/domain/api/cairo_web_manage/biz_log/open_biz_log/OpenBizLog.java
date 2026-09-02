package io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.biz_log.open_biz_log;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * 开放级业务日志
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class OpenBizLog {
	/**
	 * 日志ID
	 */
	private String logId;

	/**
	 * 应用ID
	 */
	private String appId;

	/**
	 * 应用名称
	 */
	private String appName;

	/**
	 * 应用图标
	 */
	private String appIcon;

	/**
	 * 业务ID
	 */
	private String bizId;

	/**
	 * 范围
	 */
	private String scope;

	/**
	 * 参数字符串
	 */
	private String params;

	/**
	 * 是否成功
	 */
	private boolean success;

	/**
	 * 错误信息
	 */
	private String errorMessage;

	/**
	 * ip
	 */
	private String ip;

	/**
	 * 开始时间
	 */
	private LocalDateTime startTime;

	/**
	 * 结束时间
	 */
	private LocalDateTime endTime;

	/**
	 * 毫秒数
	 */
	private Long mills;
}
