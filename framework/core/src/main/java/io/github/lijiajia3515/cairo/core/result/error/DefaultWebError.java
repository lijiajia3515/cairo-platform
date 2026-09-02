package io.github.lijiajia3515.cairo.core.result.error;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * web兜底异常
 *
 * @param <T> 二级异常类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DefaultWebError<T> implements Serializable {

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
}
