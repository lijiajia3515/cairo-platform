package io.github.lijiajia3515.cairo.feign.error;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import feign.FeignException;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;

@Slf4j
public class CairoErrorDecoder implements ErrorDecoder {
	private final ObjectMapper objectMapper;
	private final ErrorDecoder proxyErrorDecoder;

	//               - 408
	//               - 449
	//               - 500
	//               - 502
	//               - 503
	//               - 504
	//               - 509
	//               - 599
	Collection<HttpStatus> ERROR_HTTP_STATUS = List.of(
		HttpStatus.REQUEST_TIMEOUT,
		HttpStatus.TOO_MANY_REQUESTS,
		HttpStatus.INTERNAL_SERVER_ERROR,
		HttpStatus.SERVICE_UNAVAILABLE,
		HttpStatus.BAD_GATEWAY,
		HttpStatus.GATEWAY_TIMEOUT,
		HttpStatus.BANDWIDTH_LIMIT_EXCEEDED
	);

	public CairoErrorDecoder(ObjectMapper objectMapper, ErrorDecoder proxyErrorDecoder) {
		this.objectMapper = objectMapper;
		this.proxyErrorDecoder = proxyErrorDecoder;
	}

	@Override
	public Exception decode(String methodKey, Response response) {
		Exception exception = proxyErrorDecoder.decode(methodKey, response);
		if (exception instanceof FeignException) {
			FeignException feignException = (FeignException) exception;

			int status = feignException.status();
			// 是否调用错误
			boolean isError = ERROR_HTTP_STATUS.stream().anyMatch(x -> x.value() == status);

			return feignException.responseBody().map(x -> {
					try {
						BusinessResult<String> result = objectMapper.readValue(x.array(), new TypeReference<>() {
							@Override
							public Type getType() {
								return super.getType();
							}
						});
						String errorMessage = String.format("HttpStatus: %s, Code: %s, Message: %s, Data: %s", feignException.status(), result.getCode(), result.getMessage(), result.getData());
						if (isError){
							return new ErrorFeignException(errorMessage, feignException,
								feignException.status(),
								feignException.request(),
								feignException.responseHeaders(),
								result);
						} else {
							return new RequestFeignException(errorMessage, feignException,
								feignException.status(),
								feignException.request(),
								feignException.responseHeaders(),
								result);
						}
					} catch (IOException e) {
						log.debug("read result: {}", e.getMessage());
						return exception;
					}
				})
				.orElse(exception);
		}
		return exception;
	}
}
