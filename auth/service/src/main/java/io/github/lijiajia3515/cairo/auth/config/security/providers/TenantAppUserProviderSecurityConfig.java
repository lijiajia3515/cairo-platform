package io.github.lijiajia3515.cairo.auth.config.security.providers;

import io.github.lijiajia3515.cairo.auth.framework.security.tenant_app_user.CairoTenantAppUserAccountAuthenticationProvider;
import io.github.lijiajia3515.cairo.auth.framework.security.tenant_app_user.CairoTenantAppUserPasswordAuthenticationProvider;
import io.github.lijiajia3515.cairo.auth.framework.security.tenant_app_user.CairoTenantAppUserVerifyCodeAuthenticationProvider;
import io.github.lijiajia3515.cairo.auth.framework.security.tenant_app_user.CairoAuthTenantAppUserService;
import io.github.lijiajia3515.cairo.auth.modules.verify_code.VerifyCodeService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration(proxyBeanMethods = false)
public class TenantAppUserProviderSecurityConfig {

	@Bean
	CairoTenantAppUserPasswordAuthenticationProvider cairoTenantAppUserPasswordAuthenticationProvider(CairoAuthTenantAppUserService cairoAuthTenantAppUserService,
																												   PasswordEncoder passwordEncoder) {
		CairoTenantAppUserPasswordAuthenticationProvider provider = new CairoTenantAppUserPasswordAuthenticationProvider();
		provider.setCairoAuthTenantAppUserService(cairoAuthTenantAppUserService);
		provider.setPasswordEncoder(passwordEncoder);
		provider.setHideUserNotFoundExceptions(false);
		return provider;
	}

	@Bean
	CairoTenantAppUserVerifyCodeAuthenticationProvider cairoTenantAppUserVerifyCodeAuthenticationProvider(CairoAuthTenantAppUserService cairoAuthTenantAppUserService,
																													   VerifyCodeService verifyCodeService) {
		CairoTenantAppUserVerifyCodeAuthenticationProvider provider = new CairoTenantAppUserVerifyCodeAuthenticationProvider();
		provider.setCairoAuthTenantAppUserService(cairoAuthTenantAppUserService);
		provider.setVerifyCodeService(verifyCodeService);
		provider.setHideUserNotFoundExceptions(false);
		return provider;
	}

	@Bean
    CairoTenantAppUserAccountAuthenticationProvider cairoTenantAppUserAccountAuthenticationProvider(CairoAuthTenantAppUserService cairoAuthTenantAppUserService) {
		CairoTenantAppUserAccountAuthenticationProvider provider = new CairoTenantAppUserAccountAuthenticationProvider();
		provider.setCairoAuthTenantAppUserService(cairoAuthTenantAppUserService);
		provider.setHideUserNotFoundExceptions(false);
		return provider;
	}

}
