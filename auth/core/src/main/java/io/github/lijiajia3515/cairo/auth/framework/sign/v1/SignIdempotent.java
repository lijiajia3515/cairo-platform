package io.github.lijiajia3515.cairo.auth.framework.sign.v1;

public enum SignIdempotent {
	/**
	 * 必须幂等
	 */
	REQUIRED,
	/**
	 * 重复跳过执行，直接返回
	 */
	SKIP,

	/**
	 * 继续执行
	 */
	CONTINUE,

}
