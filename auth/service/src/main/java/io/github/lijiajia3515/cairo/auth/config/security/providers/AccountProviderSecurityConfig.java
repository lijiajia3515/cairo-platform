package io.github.lijiajia3515.cairo.auth.config.security.providers;

import io.github.lijiajia3515.cairo.auth.framework.security.account.CairoAccountPasswordAuthenticationProvider;
import io.github.lijiajia3515.cairo.auth.framework.security.account.CairoAccountSnsCodeAuthenticationProvider;
import io.github.lijiajia3515.cairo.auth.framework.security.account.CairoAccountVerifyCodeAuthenticationProvider;
import io.github.lijiajia3515.cairo.auth.framework.security.account.CairoAuthAccountService;
import io.github.lijiajia3515.cairo.auth.modules.verify_code.VerifyCodeService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration(proxyBeanMethods = false)
public class AccountProviderSecurityConfig {
	@Bean
	public CairoAccountPasswordAuthenticationProvider cairoAccountPasswordAuthenticationProvider(CairoAuthAccountService cairoAuthAccountService, PasswordEncoder passwordEncoder) {
		CairoAccountPasswordAuthenticationProvider provider = new CairoAccountPasswordAuthenticationProvider();
		provider.setCairoAuthAccountService(cairoAuthAccountService);
		provider.setPasswordEncoder(passwordEncoder);
		provider.setHideUserNotFoundExceptions(false);
		return provider;
	}

	@Bean
	public CairoAccountVerifyCodeAuthenticationProvider cairoAccountVerifyCodeAuthenticationProvider(CairoAuthAccountService cairoAuthAccountService, VerifyCodeService verifyCodeService) {
		CairoAccountVerifyCodeAuthenticationProvider provider = new CairoAccountVerifyCodeAuthenticationProvider();
		provider.setCairoAuthAccountService(cairoAuthAccountService);
		provider.setVerifyCodeService(verifyCodeService);
		provider.setHideUserNotFoundExceptions(false);
		return provider;
	}

	@Bean
	public CairoAccountSnsCodeAuthenticationProvider cairoAccountSnsCodeAuthenticationProvider(CairoAuthAccountService cairoAuthAccountService) {
		CairoAccountSnsCodeAuthenticationProvider provider = new CairoAccountSnsCodeAuthenticationProvider();
		provider.setCairoAuthAccountService(cairoAuthAccountService);
		return provider;
	}
}
