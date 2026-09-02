package io.github.lijiajia3515.cairo.autoconfigure.web;

import io.github.lijiajia3515.cairo.core.business.AuthBusiness;
import io.github.lijiajia3515.cairo.core.business.ParamsBusiness;
import io.github.lijiajia3515.cairo.core.business.RequestBusiness;
import io.github.lijiajia3515.cairo.core.exception.BusinessException;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 异常兜底
 */
@Slf4j
@RestControllerAdvice
@Configuration(proxyBeanMethods = false)
public class CairoRestControllerAdvice {

	@ExceptionHandler(NoHandlerFoundException.class)
	public ResponseEntity<BusinessResult<?>> noHandlerFoundException(NoHandlerFoundException e, HttpServletRequest request, HttpServletResponse response) {
		BusinessResult<?> body = BusinessResult.builder()
			.code(RequestBusiness.NOT_FOUND.getCode())
			.message(RequestBusiness.NOT_FOUND.getMessage() + String.format("(%s)", e.getMessage()))
			.build();

		return ResponseEntity.status(HttpStatus.NOT_FOUND)
			.body(body);
	}

	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<BusinessResult<?>> businessException(BusinessException e, HttpServletRequest request, HttpServletResponse response) {
		log.debug("[ex] url-> [{}]", request.getRequestURI());
		log.debug("throw ", e);

		HttpStatus httpStatus = getHttpStatusByException(e);

		BusinessResult<?> body = BusinessResult.builder()
			.code(e.getBusiness().getCode())
			.message(e.getMessage())
			.build();

		return ResponseEntity.status(httpStatus)
			.body(body);
	}


	/**
	 * 使用controller级别注解 会出现权限问题
	 *
	 * @param e       异常
	 * @param request 请求
	 * @return 响应结果
	 */
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	@ExceptionHandler(AuthenticationCredentialsNotFoundException.class)
	public BusinessResult<?> authenticationCredentialsNotFoundException(AuthenticationCredentialsNotFoundException e, HttpServletRequest request) {
		log.debug("[ex] url-> [{}]", request.getRequestURI());
		log.debug("[ex]", e);

		return BusinessResult.builder()
			.business(AuthBusiness.INVALID_TOKEN)
			.message(e.getMessage())
			.build();
	}


	/**
	 * 使用controller级别注解 会出现权限问题
	 *
	 * @param e       异常
	 * @param request 请求
	 * @return 响应结果
	 */
	@ResponseStatus(HttpStatus.FORBIDDEN)
	@ExceptionHandler(AccessDeniedException.class)
	public BusinessResult<?> accessDeniedException(AccessDeniedException e, HttpServletRequest request) {
		log.debug("[ex] url-> [{}]", request.getRequestURI());
		log.debug("[ex]", e);

		return BusinessResult.builder()
			.business(AuthBusiness.DENIED)
			.message(e.getMessage())
			.build();
	}

	/**
	 * 参数错误异常
	 *
	 * @param request request
	 * @param e       e
	 * @return return message
	 */
	@ExceptionHandler(value = HttpMessageNotReadableException.class)
	@ResponseStatus(code = HttpStatus.BAD_REQUEST)
	public BusinessResult<?> httpMessageNotReadableExceptionHandler(HttpServletRequest request, HttpMessageNotReadableException e) {
		log.debug("[ex] url-> [{}]", request.getRequestURI());
		log.debug("[ex]", e);

		return BusinessResult.builder()
			.code(ParamsBusiness.ERROR.code)
			.message(ParamsBusiness.ERROR.message + String.format("(%s)", e.getMessage()))
			.build();
	}

	/**
	 * 参数校验异常处理器
	 *
	 * @param request       request
	 * @param e             异常
	 * @param bindingResult br
	 * @return return message
	 * @throws MethodArgumentNotValidException ex
	 */
	@ExceptionHandler(value = MethodArgumentNotValidException.class)
	@ResponseStatus(code = HttpStatus.BAD_REQUEST)
	public BusinessResult<List<Map<String, Object>>> methodArgumentNotValidException(HttpServletRequest request, MethodArgumentNotValidException e, BindingResult bindingResult) throws MethodArgumentNotValidException {
		log.debug("[ex] url-> [{}]", request.getRequestURI());
		log.debug("[ex]", e);

		List<Map<String, Object>> errors = e.getFieldErrors().stream().map(field -> {
			Map<String, Object> map = new HashMap<>();
			map.put("field", field.getField());
			map.put("valid", field.getCode());
			map.put("message", field.getDefaultMessage());
			map.put("rejectValue", field.getRejectedValue());
			return map;
		}).collect(Collectors.toList());

		return BusinessResult.<List<Map<String, Object>>>builder()
			.code(ParamsBusiness.VALIDATION_FAILED.code())
			.message(ParamsBusiness.VALIDATION_FAILED.message())
			.data(errors)
			.build();
	}


	public HttpStatus getHttpStatusByException(Exception throwable) {
		return getHttpStatus(throwable).orElse(HttpStatus.INTERNAL_SERVER_ERROR);
	}

	String getError(Throwable throwable, HttpStatus status) {
		MergedAnnotation<ResponseStatus> responseStatusAnnotation = null;
		if (throwable != null) {
			responseStatusAnnotation = MergedAnnotations.from(throwable.getClass(), MergedAnnotations.SearchStrategy.TYPE_HIERARCHY).get(ResponseStatus.class);
		}
		return Optional.ofNullable(responseStatusAnnotation)
			.flatMap(x -> x.getValue("reason", String.class)
			).orElse(status.getReasonPhrase());
	}

	Optional<HttpStatus> getHttpStatus(Throwable throwable) {
		MergedAnnotation<ResponseStatus> responseStatusAnnotation = null;
		if (throwable != null) {
			responseStatusAnnotation = MergedAnnotations.from(throwable.getClass(), MergedAnnotations.SearchStrategy.TYPE_HIERARCHY).get(ResponseStatus.class);
		}
		return Optional.ofNullable(responseStatusAnnotation)
			.flatMap(x -> x.getValue("code", HttpStatus.class));
	}
}
