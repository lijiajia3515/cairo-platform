package io.github.lijiajia3515.cairo.gateway.controller.error;


import io.github.lijiajia3515.cairo.core.business.*;
import io.github.lijiajia3515.cairo.core.exception.BusinessException;
import io.github.lijiajia3515.cairo.gateway.framework.CairoWebExchangeUtils;
import io.github.lijiajia3515.cairo.gateway.framework.domain.GatewayDefaultError;
import io.github.lijiajia3515.cairo.gateway.framework.domain.GatewayErrorBusinessResult;
import jakarta.annotation.security.PermitAll;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;

import java.util.Optional;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.CIRCUITBREAKER_EXECUTION_EXCEPTION_ATTR;

@Slf4j
@RestController
@RequestMapping
public class FallbackController {
	private static final FallbackBusinessException ex = new FallbackBusinessException();

	@ResponseStatus(value = HttpStatus.SERVICE_UNAVAILABLE)
	public static class FallbackBusinessException extends BusinessException {

		public FallbackBusinessException() {
			super(ServiceBusiness.UNAVAILABLE);
		}
	}

	@RequestMapping(value = "/fallback", produces = MediaType.TEXT_HTML_VALUE)
	@PermitAll
	public void fallbackHtml() {
		throw ex;
	}

	@RequestMapping("/fallback")
	@PermitAll
	public ResponseEntity<GatewayErrorBusinessResult<GatewayDefaultError<?>>> fallback(ServerWebExchange exchange) {
		Throwable throwable = exchange.getAttribute(CIRCUITBREAKER_EXECUTION_EXCEPTION_ATTR);
		log.error("error: ", throwable);
		HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
		if (throwable instanceof ResponseStatusException) {
			status = HttpStatus.valueOf(((ResponseStatusException) throwable).getStatusCode().value());
		}
		Business business = ServiceBusiness.ERROR;
		switch (status) {
			case UNAUTHORIZED:
				business = AuthBusiness.INVALID_TOKEN;
				break;
			case FORBIDDEN:
				business = AuthBusiness.DENIED;
				break;
			case BAD_REQUEST:
				business = ParamsBusiness.ERROR;
				break;
			case NOT_FOUND:
			case METHOD_NOT_ALLOWED:
				business = RequestBusiness.NOT_FOUND;
				break;
			case NOT_ACCEPTABLE:
				business = RequestBusiness.NOT_ACCEPTED;
				break;
			case REQUEST_TIMEOUT:
				business = RequestBusiness.TIMEOUT;
				break;
			case CONFLICT:
			case UNPROCESSABLE_ENTITY:
				business = DefaultBusiness.CONFLICT;
				break;

			case REQUEST_ENTITY_TOO_LARGE:
			case PAYLOAD_TOO_LARGE:
				business = RequestBusiness.SIZE_LIMIT_EXCEEDED;
				break;
			case REQUEST_URI_TOO_LONG:
			case URI_TOO_LONG:
				business = RequestBusiness.URI_BAD;
				break;

			case UNSUPPORTED_MEDIA_TYPE:
				business = RequestBusiness.NOT_SUPPORTED;
				break;

			case TOO_MANY_REQUESTS:
				business = RequestBusiness.LIMIT_EXCEEDED;
				break;
			case INTERNAL_SERVER_ERROR:
				business = ServiceBusiness.ERROR;
				break;
			case NOT_IMPLEMENTED:
				business = ServiceBusiness.NOT_IMPLEMENTED;
				break;
			case BAD_GATEWAY:
			case SERVICE_UNAVAILABLE:
				business = ServiceBusiness.UNAVAILABLE;
				break;
			case GATEWAY_TIMEOUT:
				business = ServiceBusiness.TIMEOUT;
				break;
			case HTTP_VERSION_NOT_SUPPORTED:
				business = ServiceBusiness.NOT_SUPPORTED;
				break;
		}
		return ResponseEntity.status(status)
			.body(GatewayErrorBusinessResult.<GatewayDefaultError<?>>builder()
				.code(business.code())
				.message(Optional.ofNullable(throwable).map(Throwable::getMessage).orElse(business.getMessage()))
				.requestId((String) exchange.getAttribute(CairoWebExchangeUtils.REQUEST_ID_ATTRIBUTE))
				.retryable(CairoWebExchangeUtils.isRetryableStatus(status.value()))
				.build()
			);
	}

}
