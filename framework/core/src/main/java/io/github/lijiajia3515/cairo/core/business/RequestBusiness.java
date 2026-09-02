package io.github.lijiajia3515.cairo.core.business;


import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * request business
 */
@Getter
@Accessors(fluent = true)
public enum RequestBusiness implements Business {

	/**
	 * 404,4o5 统一称为 请求错误
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.5.4">HTTP/1.1: Semantics and Content, section 6.5.4</a>
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.5.5">HTTP/1.1: Semantics and Content, section 6.5.5</a>
	 */
	NOT_FOUND("Request.NotFound", "资源不存在"),

	/**
	 * 类似：413
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.5.11">HTTP/1.1: Semantics and Content, section 6.5.11</a>
	 */
	SIZE_LIMIT_EXCEEDED("Request.SizeLimitExceeded", "请求payload过大"),

	/**
	 * 类似：414
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.5.12">HTTP/1.1: Semantics and Content, section 6.5.12</a>
	 */
	URI_BAD("Request.UriBad", "请求uri错误"),

	/**
	 * 这里和认证无关，只是服务拒绝客户端响应
	 * {@code 403 Forbidden}.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.5.3">HTTP/1.1: Semantics and Content, section 6.5.3</a>
	 */
	FORBIDDEN("Request.Forbidden", "请求被拒绝"),

	/**
	 * 类似: 408
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.5.7">HTTP/1.1: Semantics and Content, section 6.5.7</a>
	 */
	TIMEOUT("Request.Timeout", "请求超时"),

	/**
	 * 类似：406 请求不能被处理
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.5.6">HTTP/1.1: Semantics and Content, section 6.5.6</a>
	 */
	NOT_ACCEPTED("Request.NotAccepted", "无法处理请求"),

	/**
	 * 类似：415 Unsupported Media Type
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.5.13">HTTP/1.1: Semantics and Content, section 6.5.13</a>
	 */
	NOT_SUPPORTED("Request.NotSupported", "请求类型不支持"),

	/**
	 * 类似 429 Too Many Requests
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.5.8">HTTP/1.1: Semantics and Content, section 6.5.8</a>
	 */
	LIMIT_EXCEEDED("Request.LimitExceeded", "请求过于频繁"),
	;

	/**
	 * 业务状态码
	 */
	public final String code;
	/**
	 * 业务状态解释
	 */
	public final String message;


	RequestBusiness(String code, String message) {
		this.code = code;
		this.message = message;
	}
}
