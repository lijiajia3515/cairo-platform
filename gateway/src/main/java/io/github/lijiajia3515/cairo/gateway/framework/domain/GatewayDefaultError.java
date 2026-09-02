package io.github.lijiajia3515.cairo.gateway.framework.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
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
	@JsonProperty("RequestId")
	private String requestId;

	/**
	 * 时间
	 */
	@JsonProperty("Time")
	private LocalDateTime time;

	/**
	 * 路径参数
	 */
	@JsonProperty("Path")
	private String path;

	/**
	 * http状态码异常
	 */
	@JsonProperty("Status")
	private int status;

	/**
	 * http状态错误解释
	 */
	@JsonProperty("Error")
	private String error;

	/**
	 * 错误原因
	 */
	@JsonProperty("Message")
	private String message;

	/**
	 * trace信息
	 */
	@JsonProperty("Trace")
	private String trace;

	/**
	 * 异常类型
	 */
	@JsonProperty("Exception")
	private String exception;

	/**
	 * 二级异常类型
	 */
	@JsonProperty("Errors")
	private T errors;
}
