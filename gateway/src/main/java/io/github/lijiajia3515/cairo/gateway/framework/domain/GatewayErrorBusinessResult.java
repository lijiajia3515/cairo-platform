package io.github.lijiajia3515.cairo.gateway.framework.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GatewayErrorBusinessResult<T> {
	private String code;

	private String message;

	private T error;

	/**
	 * 链路追踪号（= X-Trace-Id），成功与失败均携带
	 */
	private String requestId;

	/**
	 * 是否可重试（408 / 429 / 5xx 为 true）
	 */
	private Boolean retryable;

	/**
	 * 建议重试等待秒数（限流等场景），未提供时为 null
	 */
	private Long retryAfter;
}
