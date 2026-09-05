package io.github.lijiajia3515.cairo.gateway.framework;

import org.springframework.http.HttpStatus;


public class CairoWebExchangeUtils {
	public static final String REQUEST_ID_ATTRIBUTE = qualify("requestId");

	/**
	 * 是否可重试的 HTTP 状态：408 请求超时 / 429 限流 / 5xx 服务端错误。
	 */
	public static boolean isRetryableStatus(int status) {
		return status == HttpStatus.REQUEST_TIMEOUT.value()
			|| status == HttpStatus.TOO_MANY_REQUESTS.value()
			|| status >= HttpStatus.INTERNAL_SERVER_ERROR.value();
	}

	private static String qualify(String attr) {
		return CairoWebExchangeUtils.class.getName() + "." + attr;
	}
}
