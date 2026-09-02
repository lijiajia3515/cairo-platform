package io.github.lijiajia3515.cairo.auth.modules.biz_log;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 企业子应用业务日志
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantSubappBizLog implements Serializable {
	/**
	 * 日志ID
	 */
	private String logId;
	/**
	 * 企业ID
	 */
	private String tenantId;

	/**
	 * 应用ID
	 */
	private String appId;

	/**
	 * 终端ID
	 */
	private String endpointId;

	/**
	 * 子应用ID
	 */
	private String subappId;

	/**
	 * 子应用版本
	 */
	private String subappVersion;

	/**
	 * 用户ID
	 */
	private String userId;

	/**
	 * 会话ID（企业终端）
	 */
	private String tokenId;

	/**
	 * 应用ID
	 */
	private String bizId;

	/**
	 * 范围
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
