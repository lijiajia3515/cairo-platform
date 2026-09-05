package io.github.lijiajia3515.cairo.web.utils;

import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;

/**
 * BusinessResult 信封增强：把链路 requestId 与可重试标记注入到响应信封。
 * <p>
 * 两个调用点（{@code BusinessResultBodyAdvice.beforeBodyWrite} 与
 * {@code AbstractHttpMessageHandler.writeWithMessageConverters}）集中注入，覆盖
 * 全局兜底 / 安全链 / 拦截器的全部错误出口。已显式设置的值不被覆盖。
 */
public final class BusinessResultEnricher {

	private BusinessResultEnricher() {
	}

	/**
	 * 是否可重试的 HTTP 状态：408 请求超时 / 429 限流 / 5xx 服务端错误。
	 *
	 * @param status HTTP 状态码
	 * @return true 表示客户端可退避重试
	 */
	public static boolean isRetryableStatus(int status) {
		return status == HttpStatus.REQUEST_TIMEOUT.value()
			|| status == HttpStatus.TOO_MANY_REQUESTS.value()
			|| status >= HttpStatus.INTERNAL_SERVER_ERROR.value();
	}

	/**
	 * 从请求/响应注入 requestId 与 retryable。
	 * <ul>
	 *   <li>requestId：取 {@code CairoTraceWebFilter} 写入请求属性的链路 traceId（对应 X-Trace-Id），为空或已带值时不覆盖；</li>
	 *   <li>retryable：未显式设置时按 HTTP 状态派生（408/429/5xx 为 true）。</li>
	 * </ul>
	 * retryAfter 不做自动填充（仅限流等场景由业务方显式设置）。
	 *
	 * @param result   目标信封
	 * @param request  当前请求
	 * @param response 当前响应（用于取 HTTP 状态）
	 */
	public static void enrich(BusinessResult<?> result, HttpServletRequest request, HttpServletResponse response) {
		if (result == null) {
			return;
		}
		if (!StringUtils.hasText(result.getRequestId())) {
			Object requestId = request.getAttribute(CairoWebExchangeUtils.REQUEST_ID_ATTRIBUTE);
			if (requestId instanceof String s && StringUtils.hasText(s)) {
				result.setRequestId(s);
			}
		}
		if (result.getRetryable() == null) {
			result.setRetryable(isRetryableStatus(response.getStatus()));
		}
	}
}
