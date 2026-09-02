package io.github.lijiajia3515.cairo.autoconfigure.web;

import io.github.lijiajia3515.cairo.web.error.CairoErrorAttributes;
import io.github.lijiajia3515.cairo.web.error.CairoErrorController;
import io.github.lijiajia3515.cairo.web.error.CairoErrorView;
import io.github.lijiajia3515.cairo.web.servlet.method.BusinessResultBodyMethodHandler;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorViewResolver;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.servlet.View;

import java.util.stream.Collectors;

@Configuration(proxyBeanMethods = false)
@AutoConfigureBefore({ErrorMvcAutoConfiguration.class})
public class CairoWebConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public BusinessResultBodyMethodHandler resultResponseHandler(HttpMessageConverters converters) {
		return new BusinessResultBodyMethodHandler(converters.getConverters());
	}

	@Bean(name = "error")
	@Primary
	public View cairoErrorView() {
		return new CairoErrorView();
	}

	@Bean
	@Primary
	public ErrorAttributes errorAttributes() {
		return new DefaultErrorAttributes();
	}

	@Bean
	public CairoErrorAttributes cairoErrorAttributes() {
		return new CairoErrorAttributes();
	}

	@Bean
	@Primary
	@ConditionalOnBean({CairoErrorAttributes.class, ServerProperties.class})
	public CairoErrorController cairoErrorController(CairoErrorAttributes errorAttributes,
													 ServerProperties serverProperties,
													 ObjectProvider<ErrorViewResolver> errorViewResolvers) {
		return new CairoErrorController(errorAttributes, serverProperties.getError(),
			errorViewResolvers.orderedStream().collect(Collectors.toList()));
	}


}
