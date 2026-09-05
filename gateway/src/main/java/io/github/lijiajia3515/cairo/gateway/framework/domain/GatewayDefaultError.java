package io.github.lijiajia3515.cairo.gateway.framework.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GatewayDefaultError<T> {

	/**
	 * 请求唯一标识
	 */
	private String requestId;

	/**
	 * 时间
	 */
	private LocalDateTime time;

	/**
	 * 路径参数
	 */
	private String path;

	/**
	 * http状态码异常
	 */
	private int status;

	/**
	 * http状态错误解释
	 */
	private String error;

	/**
	 * 错误原因
	 */
	private String message;

	/**
	 * trace信息
	 */
	private String trace;

	/**
	 * 异常类型
	 */
	private String exception;

	/**
	 * 二级异常类型
	 */
	private T errors;
}
