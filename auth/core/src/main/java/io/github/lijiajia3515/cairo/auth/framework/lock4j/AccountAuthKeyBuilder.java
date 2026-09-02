package io.github.lijiajia3515.cairo.auth.framework.lock4j;

import com.baomidou.lock.LockKeyBuilder;
import io.github.lijiajia3515.cairo.auth.framework.security.CairoSecurityContextHolder;
import org.aopalliance.intercept.MethodInvocation;

public class AccountAuthKeyBuilder implements LockKeyBuilder {
	@Override
	public String buildKey(MethodInvocation invocation, String[] definitionKeys) {
		String accountId = CairoSecurityContextHolder.getAccountId();
		return accountId;
	}
}
