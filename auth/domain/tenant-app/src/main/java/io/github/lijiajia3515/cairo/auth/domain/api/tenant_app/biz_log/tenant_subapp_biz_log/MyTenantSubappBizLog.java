package io.github.lijiajia3515.cairo.auth.domain.api.tenant_app.biz_log.tenant_subapp_biz_log;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * 我的企业终端业务日志
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class MyTenantSubappBizLog {
	/**
	 * id值
	 */
	private String logId;

	/**
	 * 终端ID
	 */
	private String endpointId;

	/**
	 * 终端名称
	 */
	private String endpointName;

	/**
	 * 终端图标
	 */
	private String endpointIcon;

	/**
	 * 子应用ID
	 */
	private String subappId;

	/**
	 * 子应用名称
	 */
	private String subappName;

	/**
	 * 子应用版本
	 */
	private String subappVersion;

	/**
	 * 子应用图标
	 */
	private String subappIcon;

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
