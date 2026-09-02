package io.github.lijiajia3515.cairo.core.business;


import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * http 业务
 */
@Getter
@Accessors(fluent = true)
public enum ServiceBusiness implements Business {
	/**
	 * 类似 500
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.6.2">HTTP/1.1: Semantics and Content, section 6.6.2</a>
	 */
	ERROR("Service.Error", "服务端异常,请联系管理员"),
	/**
	 * 类似 501
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.6.2">HTTP/1.1: Semantics and Content, section 6.6.2</a>
	 */
	NOT_IMPLEMENTED("Service.NotImplemented", "服务未实现"),
	/**
	 * 类似502
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.6.3">HTTP/1.1: Semantics and Content, section 6.6.3</a>
	 */
	UNAVAILABLE("Service.Unavailable", "服务暂不可用,请稍后重试"),
	/**
	 * 类似 503
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.6.4">HTTP/1.1: Semantics and Content, section 6.6.4</a>
	 */
	TIMEOUT("Service.Timeout", "服务超时,请稍后重试"),
	/**
	 * 类似 505
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.6.6">HTTP/1.1: Semantics and Content, section 6.6.6</a>
	 */
	NOT_SUPPORTED("Service.NotSupported", "服务不支持");

	private final String code;
	private final String message;


	ServiceBusiness(String code, String message) {
		this.code = code;
		this.message = message;
	}
}
