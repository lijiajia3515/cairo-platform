package io.github.lijiajia3515.cairo.feign.error;

import feign.FeignException;
import feign.Response;
import feign.RetryableException;
import feign.codec.ErrorDecoder;

import java.util.Collection;
import java.util.Date;
import java.util.Map;

import static feign.FeignException.errorStatus;
import static org.springframework.http.HttpHeaders.RETRY_AFTER;

public class DefaultErrorDecoder implements ErrorDecoder {

	private final io.github.lijiajia3515.cairo.feign.error.RetryAfterDecoder retryAfterDecoder = new io.github.lijiajia3515.cairo.feign.error.RetryAfterDecoder();

	@Override
	public Exception decode(String methodKey, Response response) {
		FeignException exception = errorStatus(methodKey, response);
		Date retryAfter = retryAfterDecoder.apply(firstOrNull(response.headers(), RETRY_AFTER));
		if (retryAfter != null) {
			return new RetryableException(
				response.status(),
				exception.getMessage(),
				response.request().httpMethod(),
				exception,
				retryAfter,
				response.request());
		}
		return exception;
	}

	private <T> T firstOrNull(Map<String, Collection<T>> map, String key) {
		if (map.containsKey(key) && !map.get(key).isEmpty()) {
			return map.get(key).iterator().next();
		}
		return null;
	}
}
