package io.github.lijiajia3515.cairo.auth.framework.idempotent;

import java.time.Duration;

public interface IdempotentService {

	/**
	 * 幂等性 校验
	 *
	 * @param token token
	 * @return 是否成功
	 */
	boolean check(String token, Duration timeout);
}
