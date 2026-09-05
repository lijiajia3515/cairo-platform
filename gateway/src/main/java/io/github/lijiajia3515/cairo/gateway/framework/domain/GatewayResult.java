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
public class GatewayResult<T> {

	private String code;

	private String message;

	private T data;

	/**
	 * 链路追踪号（= X-Trace-Id）
	 */
	private String requestId;

}
