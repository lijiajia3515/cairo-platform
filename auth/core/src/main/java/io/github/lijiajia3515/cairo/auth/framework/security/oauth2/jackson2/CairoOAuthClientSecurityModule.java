package io.github.lijiajia3515.cairo.auth.framework.security.oauth2.jackson2;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.security.oauth2.core.OAuth2Error;

public class CairoOAuthClientSecurityModule extends SimpleModule {
	public CairoOAuthClientSecurityModule() {
		super(CairoOAuthClientSecurityModule.class.getName(), new Version(1, 0, 0, null, null, null));
	}

	@Override
	public void setupModule(SetupContext context) {
		context.setMixInAnnotations(OAuth2Error.class, OAuth2ErrorMixin.class);
	}

}
