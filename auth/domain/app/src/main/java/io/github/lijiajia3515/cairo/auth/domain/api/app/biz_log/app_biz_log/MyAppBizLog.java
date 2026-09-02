package io.github.lijiajia3515.cairo.auth.domain.api.app.biz_log.app_biz_log;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * 我的应用用户日志参数
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class MyAppBizLog {
	/**
	 * 日志ID
	 */
	private String logId;

	/**
	 * 客户端ID
	 */
	private String clientId;

	/**
	 * 客户端名称
	 */
	private String clientName;

	/**
	 * 会话ID
	 */
	private String tokenId;

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
