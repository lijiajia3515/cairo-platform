package io.github.lijiajia3515.cairo.core.business;

/**
 * 业务
 */
public interface Business {

	/**
	 * 状态码
	 *
	 * @return 状态码
	 */
	String code();

	/**
	 * 消息
	 *
	 * @return 消息
	 */
	String message();


	default String getCode(){
		return code();
	}

	default String getMessage(){
		return message();
	}
}
