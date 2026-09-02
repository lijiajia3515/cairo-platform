package io.github.lijiajia3515.cairo.web;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * web 默认 异常
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DefaultWebError implements Serializable {

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
	 * 参数绑定异常
	 */
	private Object errors;
}
