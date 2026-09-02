package io.github.lijiajia3515.cairo.auth.config;

import io.github.lijiajia3515.cairo.auth.framework.auth_code.AuthCodeInterceptor;
import io.github.lijiajia3515.cairo.auth.framework.auth_code.AuthCodeVerifyService;
import io.github.lijiajia3515.cairo.auth.framework.context.CairoContextInterceptor;
import io.github.lijiajia3515.cairo.auth.framework.idempotent.IdempotentService;
import io.github.lijiajia3515.cairo.auth.framework.idempotent.VerifyIdempotentInterceptor;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityInterceptor;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityProperties;
import io.github.lijiajia3515.cairo.auth.framework.sign.v1.SignProperties;
import io.github.lijiajia3515.cairo.auth.framework.sign.v1.SignV1Interceptor;
import io.github.lijiajia3515.cairo.auth.modules.captcha.token.CaptchaTokenInterceptor;
import io.github.lijiajia3515.cairo.auth.modules.captcha.token.CaptchaTokenService;
import io.github.lijiajia3515.cairo.web.filter.CairoTraceWebFilter;
import io.github.lijiajia3515.cairo.web.servlet.method.BusinessResultBodyMethodHandler;
import io.micrometer.tracing.Tracer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;


/**
 * web端配置
 */
@Configuration(proxyBeanMethods = false)

public class WebConfig {

	@Bean
	public CairoTraceWebFilter cairoTraceWebFilter(Tracer tracer) {
		return new CairoTraceWebFilter(tracer);
	}

	@Configuration(proxyBeanMethods = false)
	public static class MvcConfig implements WebMvcConfigurer {
		private final CairoSecurityProperties cairoSecurityProperties;

		private final SignProperties signProperties;
		private final CaptchaTokenService captchaTokenService;
		private final AuthCodeVerifyService authCodeVerifyService;
		private final IdempotentService idempotentService;
		private final List<HttpMessageConverter<?>> messageConverters;
		private final BusinessResultBodyMethodHandler businessResultBodyMethodHandler;

		public MvcConfig(CairoSecurityProperties cairoSecurityProperties, SignProperties signProperties, CaptchaTokenService captchaTokenService, AuthCodeVerifyService authCodeVerifyService, IdempotentService idempotentService, List<HttpMessageConverter<?>> messageConverters, BusinessResultBodyMethodHandler businessResultBodyMethodHandler) {
			this.cairoSecurityProperties = cairoSecurityProperties;
			this.signProperties = signProperties;
			this.captchaTokenService = captchaTokenService;
			this.authCodeVerifyService = authCodeVerifyService;
			this.idempotentService = idempotentService;
			this.messageConverters = messageConverters;
			this.businessResultBodyMethodHandler = businessResultBodyMethodHandler;
		}

		@Override
		public void addInterceptors(InterceptorRegistry registry) {
			registry.addInterceptor(cairoContextInterceptor());
			registry.addInterceptor(cairoSecurityAuthInterceptorInterceptor());
			registry.addInterceptor(captchaTokenInterceptor());
			registry.addInterceptor(authCodeTokenInterceptor());
			registry.addInterceptor(verifyIdempotentInterceptor());
			registry.addInterceptor(cairoSignV1Interceptor());
		}



		@Override
		public void addReturnValueHandlers(List<HandlerMethodReturnValueHandler> handlers) {
			handlers.add(businessResultBodyMethodHandler);
		}

		public CairoContextInterceptor cairoContextInterceptor() {
			return new CairoContextInterceptor();
		}

		CairoSecurityInterceptor cairoSecurityAuthInterceptorInterceptor() {
			return new CairoSecurityInterceptor(cairoSecurityProperties, messageConverters, null);
		}

		CaptchaTokenInterceptor captchaTokenInterceptor() {
			return new CaptchaTokenInterceptor(captchaTokenService, messageConverters, null);
		}

		VerifyIdempotentInterceptor verifyIdempotentInterceptor() {
			return new VerifyIdempotentInterceptor(idempotentService, messageConverters, null);
		}

		AuthCodeInterceptor authCodeTokenInterceptor() {
			return new AuthCodeInterceptor(authCodeVerifyService, messageConverters, null);
		}


		SignV1Interceptor cairoSignV1Interceptor() {
			return new SignV1Interceptor(idempotentService, messageConverters, null, signProperties);
		}


	}

}
