package io.github.lijiajia3515.cairo.auth.config.security.providers;

import io.github.lijiajia3515.cairo.auth.framework.security.app_user.CairoAppUserAccountAuthenticationProvider;
import io.github.lijiajia3515.cairo.auth.framework.security.app_user.CairoAppUserAccountSnsCodeAuthenticationProvider;
import io.github.lijiajia3515.cairo.auth.framework.security.app_user.CairoAppUserPasswordAuthenticationProvider;
import io.github.lijiajia3515.cairo.auth.framework.security.app_user.CairoAppUserVerifyCodeAuthenticationProvider;
import io.github.lijiajia3515.cairo.auth.framework.security.app_user.CairoAuthAppUserService;
import io.github.lijiajia3515.cairo.auth.modules.verify_code.VerifyCodeService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration(proxyBeanMethods = false)
public class AppUserProviderSecurityConfig {

	@Bean
	CairoAppUserPasswordAuthenticationProvider cairoAppUserPasswordAuthenticationProvider(CairoAuthAppUserService cairoAuthAppUserService,
																										  PasswordEncoder passwordEncoder) {
		CairoAppUserPasswordAuthenticationProvider provider = new CairoAppUserPasswordAuthenticationProvider();
		provider.setCairoAuthAppUserService(cairoAuthAppUserService);
		provider.setPasswordEncoder(passwordEncoder);
		provider.setHideUserNotFoundExceptions(false);
		return provider;
	}

	@Bean
	CairoAppUserVerifyCodeAuthenticationProvider cairoAppUserVerifyCodeAuthenticationProvider(CairoAuthAppUserService cairoAuthAppUserService,
																											  VerifyCodeService verifyCodeService) {
		CairoAppUserVerifyCodeAuthenticationProvider provider = new CairoAppUserVerifyCodeAuthenticationProvider();
		provider.setCairoAuthAppUserService(cairoAuthAppUserService);
		provider.setVerifyCodeService(verifyCodeService);
		provider.setHideUserNotFoundExceptions(false);
		return provider;
	}

	@Bean
	CairoAppUserAccountAuthenticationProvider cairoAppUserAccountAuthenticationProvider(CairoAuthAppUserService cairoAuthAppUserService) {
		CairoAppUserAccountAuthenticationProvider provider = new CairoAppUserAccountAuthenticationProvider();
		provider.setCairoAuthAppUserService(cairoAuthAppUserService);
		provider.setHideUserNotFoundExceptions(false);
		return provider;
	}

	@Bean
	CairoAppUserAccountSnsCodeAuthenticationProvider cairoAppUserAccountSnsCodeAuthenticationProvider(CairoAuthAppUserService cairoAuthAppUserService) {
		CairoAppUserAccountSnsCodeAuthenticationProvider provider = new CairoAppUserAccountSnsCodeAuthenticationProvider();
		provider.setCairoAuthAppUserService(cairoAuthAppUserService);
		return provider;
	}
}
