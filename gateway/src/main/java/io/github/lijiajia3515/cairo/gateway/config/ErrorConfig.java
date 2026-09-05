package io.github.lijiajia3515.cairo.gateway.config;

import io.github.lijiajia3515.cairo.core.business.ParamsBusiness;
import io.github.lijiajia3515.cairo.core.exception.BusinessException;
import io.github.lijiajia3515.cairo.gateway.framework.CairoWebExchangeUtils;
import io.github.lijiajia3515.cairo.gateway.framework.domain.GatewayErrorBusinessResult;
import io.github.lijiajia3515.cairo.gateway.framework.error.CairoErrorAttributes;
import io.github.lijiajia3515.cairo.gateway.framework.error.CairoErrorWebExceptionHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.result.view.ViewResolver;
import org.springframework.web.server.ServerWebExchange;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Configuration(proxyBeanMethods = false)
public class ErrorConfig {
	@Bean
	CairoErrorAttributes cairoErrorAttributes() {
		return new CairoErrorAttributes();
	}

	@Bean
	@Order(-1)
	CairoErrorWebExceptionHandler cairoErrorWebExceptionHandler(CairoErrorAttributes errorAttributes,
																WebProperties webProperties, ObjectProvider<ViewResolver> viewResolvers,
																ServerProperties serverProperties,
																ServerCodecConfigurer serverCodecConfigurer, ApplicationContext applicationContext) {
		CairoErrorWebExceptionHandler exceptionHandler = new CairoErrorWebExceptionHandler(errorAttributes, webProperties.getResources(), serverProperties.getError(), applicationContext);
		exceptionHandler.setViewResolvers(viewResolvers.orderedStream().collect(Collectors.toList()));
		exceptionHandler.setMessageWriters(serverCodecConfigurer.getWriters());
		exceptionHandler.setMessageReaders(serverCodecConfigurer.getReaders());
		return exceptionHandler;
	}

	@Slf4j(topic = "[ExceptionHandler]")
	@RestControllerAdvice
	@Configuration(proxyBeanMethods = false)
	public static class CairoControllerExceptionHandler {
		/**
		 * 业务异常
		 *
		 * @param e        e
		 * @param exchange exchange
		 * @return gateway result
		 */
		@ExceptionHandler(BusinessException.class)
		public ResponseEntity<GatewayErrorBusinessResult<?>> statusException(BusinessException e, ServerWebExchange exchange) {
			log.info("[ex] url-> [{}]", exchange.getRequest().getURI());
			log.info("throw ", e);

			HttpStatus httpStatus = getHttpStatusByException(e);

			GatewayErrorBusinessResult<?> body = GatewayErrorBusinessResult.builder()
				.code(e.getBusiness().code())
				.message(e.getMessage())
				.requestId((String) exchange.getAttribute(CairoWebExchangeUtils.REQUEST_ID_ATTRIBUTE))
				.retryable(CairoWebExchangeUtils.isRetryableStatus(httpStatus.value()))
				.build();

			return ResponseEntity.status(httpStatus)
				.body(body);
		}

		/**
		 * 参数校验异常
		 *
		 * @param e        e
		 * @param exchange exchange
		 * @return gateway result
		 */
		@ExceptionHandler(value = MethodArgumentNotValidException.class)
		public ResponseEntity<GatewayErrorBusinessResult<Object>> methodArgumentNotValidException(MethodArgumentNotValidException e, BindingResult bindingResult, ServerWebExchange exchange) throws MethodArgumentNotValidException {
			log.debug("[ex] url-> [{}]", exchange.getRequest().getURI());
			log.debug("[ex]", e);


			List<Map<String, Object>> errors = e.getFieldErrors().stream().map(field -> {
				Map<String, Object> map = new HashMap<>();
				map.put("field", field.getField());
				map.put("valid", field.getCode());
				map.put("message", field.getDefaultMessage());
				map.put("rejectValue", field.getRejectedValue());
				return map;
			}).collect(Collectors.toList());


			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(GatewayErrorBusinessResult.builder()
					.code(ParamsBusiness.ERROR.getCode())
					.message(e.getMessage())
					.error(errors)
					.requestId((String) exchange.getAttribute(CairoWebExchangeUtils.REQUEST_ID_ATTRIBUTE))
					.retryable(CairoWebExchangeUtils.isRetryableStatus(HttpStatus.BAD_REQUEST.value()))
					.build()
				);
		}

//		/**
//		 * 网关路由不存在异常
//		 *
//		 * @param e        e
//		 * @param exchange exchange
//		 * @return gateway result
//		 */
//		@ExceptionHandler(value = ResponseStatusException.class)
//		public ResponseEntity<GatewayErrorBusinessResult<Object>> statusException(ResponseStatusException e, ServerWebExchange exchange) {
//			log.info("[ex] url-> [{}]", exchange.getRequest().getURI());
//			log.info("[ex]", e);
//
//			HttpStatus httpStatus = getHttpStatus(e).orElse(HttpStatus.INTERNAL_SERVER_ERROR);
//
//
//			return ResponseEntity.status(httpStatus)
//				.body(GatewayErrorBusinessResult.builder()
//					.code(ClientBusiness.NOT_FOUND.getCode())
//					.message(e.getMessage())
//					.build());
//		}
//
//
//		/**
//		 * 网关路由不存在异常
//		 *
//		 * @param e        e
//		 * @param exchange exchange
//		 * @return gateway result
//		 */
//		@ExceptionHandler(value = NotFoundException.class)
//		public ResponseEntity<GatewayErrorBusinessResult<Object>> gatewayNotFoundException(NotFoundException e, ServerWebExchange exchange) {
//			log.info("[ex] url-> [{}]", exchange.getRequest().getURI());
//			log.info("[ex]", e);
//
//
//			return ResponseEntity.status(HttpStatus.NOT_FOUND)
//				.body(GatewayErrorBusinessResult.builder()
//					.code(ClientBusiness.NOT_FOUND.getCode())
//					.message(e.getMessage())
//					.build());
//		}
//
//		/**
//		 * 网关服务超时异常
//		 *
//		 * @param e        e
//		 * @param exchange exchange
//		 * @return gateway result
//		 */
//		@ExceptionHandler(value = TimeoutException.class)
//		public ResponseEntity<GatewayErrorBusinessResult<Object>> gatewayTimeoutException(TimeoutException e, ServerWebExchange exchange) {
//			log.info("[ex] url-> [{}]", exchange.getRequest().getURI());
//			log.info("[ex]", e);
//
//
//			return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT)
//				.body(GatewayErrorBusinessResult.builder()
//					.code(ServiceBusiness.TIMEOUT.getCode())
//					.message(e.getMessage())
//					.build());
//		}
//
//		/**
//		 * 网关服务不可用异常
//		 *
//		 * @param e        e
//		 * @param exchange exchange
//		 * @return gateway result
//		 */
//		@ExceptionHandler(value = ServiceUnavailableException.class)
//		public ResponseEntity<GatewayErrorBusinessResult<Object>> gatewayServiceUnavailableException(ServiceUnavailableException e, ServerWebExchange exchange) {
//			log.info("[ex] url-> [{}]", exchange.getRequest().getURI());
//			log.info("[ex]", e);
//
//
//			return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
//				.header(HttpHeaders.RETRY_AFTER, String.valueOf(30))
//				.body(GatewayErrorBusinessResult.builder()
//					.code(ServiceBusiness.UNAVAILABLE.getCode())
//					.message(e.getMessage())
//					.build());
//		}

		public HttpStatus getHttpStatusByException(Exception throwable) {
			return getHttpStatus(throwable).orElse(HttpStatus.INTERNAL_SERVER_ERROR);
		}

		public String getError(Throwable throwable, HttpStatus status) {
			MergedAnnotation<ResponseStatus> responseStatusAnnotation = null;
			if (throwable != null) {
				responseStatusAnnotation = MergedAnnotations.from(throwable.getClass(), MergedAnnotations.SearchStrategy.TYPE_HIERARCHY).get(ResponseStatus.class);
			}
			return Optional.ofNullable(responseStatusAnnotation)
				.flatMap(x -> x.getValue("reason", String.class)
				).orElse(status.getReasonPhrase());
		}

		public Optional<HttpStatus> getHttpStatus(Throwable throwable) {
			MergedAnnotation<ResponseStatus> responseStatusAnnotation = null;
			if (throwable != null) {
				responseStatusAnnotation = MergedAnnotations.from(throwable.getClass(), MergedAnnotations.SearchStrategy.TYPE_HIERARCHY).get(ResponseStatus.class);
			}
			return Optional.ofNullable(responseStatusAnnotation)
				.flatMap(x -> x.getValue("code", HttpStatus.class));
		}
	}
}
