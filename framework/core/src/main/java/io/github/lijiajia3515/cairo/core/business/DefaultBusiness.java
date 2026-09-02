package io.github.lijiajia3515.cairo.core.business;


import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * 默认业务结果
 */
@Getter
@Accessors(fluent = true)
public enum DefaultBusiness implements Business {
	/**
	 * 业务默认成功结果
	 */
	SUCCESS("Success", "成功"),

	/**
	 * 类似： 409 请求冲突，422 Unprocessable Entity 语义错误
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.5.8">HTTP/1.1: Semantics and Content, section 6.5.8</a>
	 * @see <a href="https://tools.ietf.org/html/rfc4918#section-11.2">WebDAV</a>
	 */
	CONFLICT("Conflict", "出现错误");

	public final String code;
	public final String message;


	DefaultBusiness(String code, String message) {
		this.code = code;
		this.message = message;
	}
}
