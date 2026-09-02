package io.github.lijiajia3515.cairo.core.exception;


import io.github.lijiajia3515.cairo.core.business.Business;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * Cairo 业务异常类
 */
@Getter
@ToString(callSuper = false)
@EqualsAndHashCode(callSuper = false)
public class BusinessException extends RuntimeException {
	@Accessors(chain = true)
	protected Business business;


	/**
	 * 携带固定消息
	 *
	 * @param business 业务
	 */
	public BusinessException(Business business) {
		super(business.message());
		this.business = business;
	}

	/**
	 * 携带自定义异常消息
	 *
	 * @param message  异常消息
	 * @param business 业务
	 */
	public BusinessException(String message, Business business) {
		super(message);
		this.business = business;
	}

	/**
	 * 携带自定义异常消息
	 *
	 * @param message        message
	 * @param superThrowable 父级异常
	 * @param business       业务
	 */
	public BusinessException(String message, Throwable superThrowable, Business business) {
		super(message, superThrowable);
		this.business = business;
	}

	/**
	 * 获取业务
	 *
	 * @return 业务编码
	 */
	public Business getBusiness() {
		return business;
	}

	public Business business() {
		return business;
	}
}
