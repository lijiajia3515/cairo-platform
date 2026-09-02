package io.github.lijiajia3515.cairo.auth.modules.biz_log;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 开放级业务记录日志实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OpenBizLog implements Serializable {
	/**
	 * 日志ID
	 */
	private String logId;

	/**
	 * 应用ID
	 */
	private String appId;

	/**
	 * 业务ID
	 */
	private String bizId;

	/**
	 * scope
	 */
	private String scope;

	/**
	 * 参数
	 */
	private String params;

	/**
	 * 是否成功
	 */
	private boolean success;

	/**
	 * 信息
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
	 * 耗时
	 */
	private long mills;

}
