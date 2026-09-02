package io.github.lijiajia3515.cairo.gateway.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import io.github.lijiajia3515.cairo.gateway.framework.webflux.CairoWebfluxResponseContext;
import io.github.lijiajia3515.cairo.gateway.framework.webflux.CairoWebfluxResponseHandler;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.jackson.JsonComponentModule;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.reactive.result.view.ViewResolver;

import java.util.stream.Collectors;

@Configuration(proxyBeanMethods = false)
public class WebfluxConfig {
	@Bean
	public CairoWebfluxResponseContext cairoWebfluxResponseContext(ObjectProvider<ViewResolver> viewResolvers,
                                                                   ServerCodecConfigurer serverCodecConfigurer) {
		return new CairoWebfluxResponseContext(serverCodecConfigurer.getWriters(), viewResolvers.orderedStream().collect(Collectors.toList()));
	}

	@Bean
    CairoWebfluxResponseHandler cairoWebfluxResponseHandler(CairoWebfluxResponseContext context) {
		return new CairoWebfluxResponseHandler(context);
	}


	// @Bean
	// @Order(100)
	public static ObjectMapper springActuatorObjectMapper(ApplicationContext context) {
//		CairoJacksonProperties properties = CairoJacksonProperties.builder()
//			.build();
//		Jackson2ObjectMapperBuilderCustomizer customizer = new StandardJackson2ObjectMapperBuilderCustomizer(context, properties);
//		Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
//		customizer.customize(builder);

		ObjectMapper objectMapper = new ObjectMapper();

		objectMapper.registerModule(new ParameterNamesModule(JsonCreator.Mode.DEFAULT));
		objectMapper.registerModule(new JsonComponentModule());
		objectMapper.registerModule(new Jdk8Module());
		objectMapper.registerModule(new JavaTimeModule());

		return objectMapper;

	}


//	private static final MimeType[] EMPTY_MIME_TYPES = {};



//	@Bean
//	@Order(100)
//	CodecCustomizer defaultJacksonCodecCustomizer(ObjectMapper mapper) {
//		MediaType[] mimeTypes = {
//			new MediaType("application", "json"),
//		};
//		return (configurer) -> {
//			Jackson2JsonEncoder jackson2JsonEncoder = new Jackson2JsonEncoder(mapper, mimeTypes);
//			jackson2JsonEncoder.setStreamingMediaTypes(List.of(mimeTypes));
//			Jackson2JsonDecoder jackson2JsonDecoder = new Jackson2JsonDecoder(mapper, mimeTypes);
//			CodecConfigurer.CustomCodecs customCodecs = configurer.customCodecs();
//			customCodecs.register(jackson2JsonDecoder);
//			customCodecs.register(jackson2JsonEncoder);
//		};
//	}

//	@Bean
//	@Order(200)
//	CodecCustomizer springActuatorJacksonCodecCustomizer(ApplicationContext context) {
//		ObjectMapper springActuatorObjectMapper = springActuatorObjectMapper(context);
//		MediaType[] mimeTypes = {
//			new MediaType("application", "vnd.spring-boot.actuator.v1+json"),
//			new MediaType("application", "vnd.spring-boot.actuator.v2+json"),
//			new MediaType("application", "vnd.spring-boot.actuator.v3+json"),
//			new MediaType("application", "vnd.spring-boot.actuator.v4+json")
//		};
//		return (configurer) -> {
//			Jackson2JsonEncoder jackson2JsonEncoder = new Jackson2JsonEncoder(springActuatorObjectMapper, mimeTypes);
//			jackson2JsonEncoder.setStreamingMediaTypes(List.of(mimeTypes));
//			Jackson2JsonDecoder jackson2JsonDecoder = new Jackson2JsonDecoder(springActuatorObjectMapper, mimeTypes);
//			CodecConfigurer.CustomCodecs customCodecs = configurer.customCodecs();
//			customCodecs.register(jackson2JsonDecoder);
//			customCodecs.register(jackson2JsonEncoder);
//		};
//	}


}
