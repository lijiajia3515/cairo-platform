package io.github.lijiajia3515.cairo.auth.framework.lock4j;

import com.baomidou.lock.LockKeyBuilder;
import io.github.lijiajia3515.cairo.auth.framework.security.CairoSecurityContextHolder;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthSubappUserPrincipal;
import io.github.lijiajia3515.cairo.core.CoreConstants;
import org.aopalliance.intercept.MethodInvocation;

import java.util.Optional;

public class SubappUserAuthKeyBuilder implements LockKeyBuilder {
	@Override
	public String buildKey(MethodInvocation invocation, String[] definitionKeys) {
		Optional<CairoOAuthSubappUserPrincipal> appUser = CairoSecurityContextHolder.getSubappUser();
		return appUser.map(x -> x.getAppId() + "_" + x.getUserId()).orElse(CoreConstants.nextIdStr());
	}
}
