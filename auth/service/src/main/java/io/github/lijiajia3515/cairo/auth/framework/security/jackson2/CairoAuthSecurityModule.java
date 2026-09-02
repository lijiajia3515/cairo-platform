package io.github.lijiajia3515.cairo.auth.framework.security.jackson2;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.github.lijiajia3515.cairo.auth.framework.security.account.CairoAuthAccount;
import io.github.lijiajia3515.cairo.auth.framework.security.app_user.CairoAuthAppUser;
import io.github.lijiajia3515.cairo.auth.framework.security.tenant_app_user.CairoAuthTenantAppUser;
import io.github.lijiajia3515.cairo.auth.framework.security.core.LoginType;
import org.springframework.security.jackson2.SecurityJackson2Modules;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;

public class CairoAuthSecurityModule extends SimpleModule {
	public CairoAuthSecurityModule() {
		super(CairoAuthSecurityModule.class.getName(), new Version(1, 0, 0, null, null, null));
	}

	@Override
	public void setupModule(SetupContext context) {
		SecurityJackson2Modules.enableDefaultTyping(context.getOwner());
		// context.setMixInAnnotations(Collections.emptySet().getClass(), EmptySetMixin.class);
		context.setMixInAnnotations(LoginType.class, LoginTypeMixin.class);

		context.setMixInAnnotations(CairoAuthAccount.class, CairoAuthAccountMixin.class);
		context.setMixInAnnotations(CairoAuthAppUser.class, CairoAuthAppUserMixin.class);
		context.setMixInAnnotations(CairoAuthTenantAppUser.class, CairoAuthTenantAppUserMixin.class);

		context.setMixInAnnotations(ClientAuthenticationMethod.class, ClientAuthenticationMethodsMixin.class);
		context.setMixInAnnotations(AuthorizationGrantType.class, AuthorizationGrantTypeMixin.class);
		context.setMixInAnnotations(ClientSettings.class, ClientSettingsMixin.class);
		context.setMixInAnnotations(TokenSettings.class, TokenSettingsMixin.class);
	}

}
