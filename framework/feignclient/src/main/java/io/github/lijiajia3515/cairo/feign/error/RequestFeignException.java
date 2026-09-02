package io.github.lijiajia3515.cairo.feign.error;

import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import feign.Request;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.Collection;
import java.util.Map;

/**
 * 请求异常，错误在自身，比如参数错误，接口地址错误，认证错误，文件过大，地址过长
 */
@Getter
@ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR, reason = "服务异常")
public class RequestFeignException extends RuntimeException {
	private final int status;
	private final Request request;
	private final Map<String, Collection<String>> responseHeaders;
	private final BusinessResult<String> result;

	public RequestFeignException(String message,
								 Throwable cause,
								 int status,
								 Request request,
								 Map<String, Collection<String>> responseHeaders,
								 BusinessResult<String> result) {
		super(message, cause);
		this.status = status;
		this.request = request;
		this.responseHeaders = responseHeaders;
		this.result = result;
	}
}
