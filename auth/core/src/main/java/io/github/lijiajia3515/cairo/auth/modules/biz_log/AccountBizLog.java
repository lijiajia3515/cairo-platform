package io.github.lijiajia3515.cairo.auth.modules.biz_log;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 账号级别业务日志
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountBizLog implements Serializable {
	/**
	 * 日志ID
	 */
	private String logId;

	/**
	 * 应用ID
	 */
	private String appId;

	/**
	 * 客户端ID
	 */
	private String clientId;

	/**
	 * 账号ID
	 */
	private String accountId;

	/**
	 * 会话ID（账号）
	 */
	private String tokenId;

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
