package io.github.lijiajia3515.cairo.gateway.framework.error;

import io.github.lijiajia3515.cairo.core.business.*;
import io.github.lijiajia3515.cairo.core.exception.BusinessException;
import io.github.lijiajia3515.cairo.gateway.framework.CairoWebExchangeUtils;
import io.github.lijiajia3515.cairo.gateway.framework.domain.GatewayDefaultError;
import io.github.lijiajia3515.cairo.gateway.framework.domain.GatewayErrorBusinessResult;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.cloud.gateway.support.ServiceUnavailableException;
import org.springframework.cloud.gateway.support.TimeoutException;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class CairoErrorAttributes extends DefaultErrorAttributes {
	public static final String ERROR = "error";
	public static final String ERROR_REQUEST_ID = "requestId";

	public static final String ERROR_TIME = "time";
	public static final String ERROR_STATUS = "status";
	public static final String ERROR_ERROR = "error";
	public static final String ERROR_PATH = "path";
	public static final String ERROR_MESSAGE = "message";
	public static final String ERROR_ERRORS = "errors";
	public static final String ERROR_TRACE = "trace";
	public static final String ERROR_EXCEPTION = "exception";

	public static final String BUSINESS = "business";

	@Override
	public Map<String, Object> getErrorAttributes(ServerRequest request, ErrorAttributeOptions options) {
		Map<String, Object> errorAttributes = getErrorAttributes(request, options.isIncluded(ErrorAttributeOptions.Include.STACK_TRACE));

		if (!options.isIncluded(ErrorAttributeOptions.Include.EXCEPTION)) {
			errorAttributes.remove(ERROR_EXCEPTION);
		}
		if (!options.isIncluded(ErrorAttributeOptions.Include.STACK_TRACE)) {
			errorAttributes.remove(ERROR_TRACE);
		}
		if (!options.isIncluded(ErrorAttributeOptions.Include.MESSAGE) && errorAttributes.get(ERROR_MESSAGE) != null) {
			errorAttributes.remove(ERROR_MESSAGE);
		}
		if (!options.isIncluded(ErrorAttributeOptions.Include.BINDING_ERRORS)) {
			errorAttributes.remove(ERROR_ERRORS);
		}

		Business business = getBusiness(request);

		return Collections.singletonMap(BUSINESS, businessResult(business, errorAttributes));
	}

	private Map<String, Object> getErrorAttributes(ServerRequest request, boolean includeStackTrace) {
		Throwable error = getError(request);
		MergedAnnotation<ResponseStatus> responseStatusAnnotation = MergedAnnotations.from(error.getClass(), MergedAnnotations.SearchStrategy.TYPE_HIERARCHY).get(ResponseStatus.class);

		Map<String, Object> errorAttributes = new LinkedHashMap<>();

		errorAttributes.put(ERROR_REQUEST_ID, request.exchange().getAttribute(CairoWebExchangeUtils.REQUEST_ID_ATTRIBUTE));
		errorAttributes.put(ERROR_TIME, LocalDateTime.now());
		errorAttributes.put(ERROR_PATH, request.uri().toString());
		HttpStatus errorStatus = determineHttpStatus(error, responseStatusAnnotation);
		errorAttributes.put(ERROR_STATUS, errorStatus.value());
		errorAttributes.put(ERROR_ERROR, errorStatus.getReasonPhrase());

		errorAttributes.put(ERROR_MESSAGE, determineMessage(error, responseStatusAnnotation));
		handleException(errorAttributes, determineException(error), includeStackTrace);

		return errorAttributes;
	}

	public Business getBusiness(ServerRequest request) {
		Throwable error = getError(request);
		return determineBusiness(error);
	}

	private HttpStatus determineHttpStatus(Throwable error, MergedAnnotation<ResponseStatus> responseStatusAnnotation) {
		if (error instanceof ResponseStatusException) {
			return HttpStatus.valueOf(((ResponseStatusException) error).getStatusCode().value());
		}
		return responseStatusAnnotation.getValue("code", HttpStatus.class).orElse(HttpStatus.INTERNAL_SERVER_ERROR);
	}

	public Business determineBusiness(Throwable throwable) {

		// business
		if (throwable instanceof BusinessException) {
			return ((BusinessException) throwable).getBusiness();
		}

		// reactive
		else if (throwable instanceof WebClientResponseException) {
			// auth
			if (throwable instanceof WebClientResponseException.Unauthorized) {
				return AuthBusiness.INVALID_TOKEN;
			}

			// 40x
			else if (throwable instanceof WebClientResponseException.BadRequest || throwable instanceof WebClientResponseException.UnprocessableEntity) {
				return ParamsBusiness.ERROR;
			} else if (throwable instanceof WebClientResponseException.Forbidden) {
				return RequestBusiness.FORBIDDEN;
			} else if (throwable instanceof WebClientResponseException.NotFound || throwable instanceof WebClientResponseException.MethodNotAllowed || throwable instanceof WebClientResponseException.Gone) {
				return RequestBusiness.NOT_FOUND;
			} else if (throwable instanceof WebClientResponseException.NotAcceptable) {
				return RequestBusiness.NOT_ACCEPTED;
			} else if (throwable instanceof WebClientResponseException.UnsupportedMediaType) {
				return RequestBusiness.NOT_SUPPORTED;
			} else if (throwable instanceof WebClientResponseException.Conflict) {
				return DefaultBusiness.CONFLICT;
			}

			// 50x
			else if (throwable instanceof WebClientResponseException.InternalServerError) {
				return ServiceBusiness.ERROR;
			} else if (throwable instanceof WebClientResponseException.NotImplemented) {
				return ServiceBusiness.NOT_IMPLEMENTED;
			} else if (throwable instanceof WebClientResponseException.BadGateway) {
				return ServiceBusiness.UNAVAILABLE;
			} else if (throwable instanceof WebClientResponseException.ServiceUnavailable) {
				return ServiceBusiness.UNAVAILABLE;
			} else if (throwable instanceof WebClientResponseException.GatewayTimeout) {
				return ServiceBusiness.TIMEOUT;
			}
		}

		// servlet
		if (throwable instanceof ResponseStatusException) {
			if (throwable instanceof MethodNotAllowedException) {
				return RequestBusiness.NOT_FOUND;
			} else if (throwable instanceof ServerWebInputException) {
				return ParamsBusiness.ERROR;
			} else if (throwable instanceof NotAcceptableStatusException) {
				return RequestBusiness.NOT_ACCEPTED;
			} else if (throwable instanceof UnsupportedMediaTypeStatusException) {
				return RequestBusiness.NOT_SUPPORTED;
			} else if (throwable instanceof ServerErrorException) {
				return ServiceBusiness.ERROR;
			}
			HttpStatusCode status = ((ResponseStatusException) throwable).getStatusCode();
			if (status.equals(HttpStatus.NOT_FOUND) || status.equals(HttpStatus.METHOD_NOT_ALLOWED)) {
				return RequestBusiness.NOT_FOUND;
			}
		}

		// gateway
		if (throwable instanceof NotFoundException) {
			return RequestBusiness.NOT_FOUND;
		} else if (throwable instanceof TimeoutException) {
			return ServiceBusiness.TIMEOUT;
		} else if (throwable instanceof ServiceUnavailableException) {
			return ServiceBusiness.UNAVAILABLE;
		}

		// unknown host
		if (throwable instanceof UnknownHostException) {
			return ServiceBusiness.UNAVAILABLE;
		}
		return ServiceBusiness.ERROR;
	}

	private String determineMessage(Throwable error, MergedAnnotation<ResponseStatus> responseStatusAnnotation) {
		if (error instanceof BusinessException) {
			return error.getMessage();
		}
		if (error instanceof BindingResult) {
			return error.getMessage();
		}
		if (error instanceof ResponseStatusException) {
			return ((ResponseStatusException) error).getReason();
		}
		String reason = responseStatusAnnotation.getValue("reason", String.class).orElse("");
		if (StringUtils.hasText(reason)) {
			return reason;
		}
		return (error.getMessage() != null) ? error.getMessage() : "";
	}

	private Throwable determineException(Throwable error) {
		if (error instanceof ResponseStatusException) {
			return (error.getCause() != null) ? error.getCause() : error;
		}
		return error;
	}

	private void addStackTrace(Map<String, Object> errorAttributes, Throwable error) {
		StringWriter stackTrace = new StringWriter();
		error.printStackTrace(new PrintWriter(stackTrace));
		stackTrace.flush();
		errorAttributes.put(ERROR_TRACE, stackTrace.toString());
	}

	private void handleException(Map<String, Object> errorAttributes, Throwable error, boolean includeStackTrace) {
		errorAttributes.put(ERROR_EXCEPTION, error.getClass().getName());
		if (includeStackTrace) {
			addStackTrace(errorAttributes, error);
		}
		if (error instanceof BindingResult) {
			BindingResult result = (BindingResult) error;
			if (result.hasErrors()) {
				errorAttributes.put(ERROR_ERRORS, result.getAllErrors());
			}
		}
	}

	private GatewayErrorBusinessResult<GatewayDefaultError<?>> businessResult(Business business, Map<String, Object> errorAttribute) {
		return GatewayErrorBusinessResult.<GatewayDefaultError<?>>builder()
			.code(business.getCode())
			.message((String) errorAttribute.get(ERROR_MESSAGE))
			.requestId((String) errorAttribute.get(ERROR_REQUEST_ID))
			.retryable(CairoWebExchangeUtils.isRetryableStatus((int) errorAttribute.get(ERROR_STATUS)))
			.error(GatewayDefaultError.builder()
				.requestId((String) errorAttribute.get(ERROR_REQUEST_ID))
				.path((String) errorAttribute.get(ERROR_PATH))
				.status((int) errorAttribute.get(ERROR_STATUS))
				.error((String) errorAttribute.get(ERROR_ERROR))
				.message((String) errorAttribute.get(ERROR_MESSAGE))
				.errors(errorAttribute.get(ERROR_ERRORS))
				.exception((String) errorAttribute.get(ERROR_EXCEPTION))
				.trace((String) errorAttribute.get(ERROR_TRACE))
				.build())
			.build();
	}
}
