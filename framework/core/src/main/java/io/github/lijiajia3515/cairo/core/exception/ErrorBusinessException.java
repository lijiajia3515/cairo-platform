package io.github.lijiajia3515.cairo.core.exception;


import io.github.lijiajia3515.cairo.core.business.Business;
import io.github.lijiajia3515.cairo.core.business.ServiceBusiness;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 运行时 未知/无法描述 业务异常
 */
@ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR, reason = "服务异常")
public class ErrorBusinessException extends BusinessException {
	protected static final Business BUSINESS = ServiceBusiness.ERROR;

	public ErrorBusinessException(String message) {
		super(message, BUSINESS);
	}
}
