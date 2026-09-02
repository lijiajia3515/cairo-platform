//package io.github.lijiajia3515.cairo.auth.autoconfigure;
//
//import io.github.lijiajia3515.cairo.auth.framework.feign.signv1.CairoSignV1FeignEncoder;
//import feign.codec.Encoder;
//import feign.form.MultipartFormContentProcessor;
//import feign.form.spring.SpringFormEncoder;
//import org.springframework.beans.factory.ObjectFactory;
//import org.springframework.beans.factory.ObjectProvider;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
//import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties;
//import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
//import org.springframework.cloud.openfeign.support.*;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.core.Ordered;
//import org.springframework.core.annotation.Order;
//
//import static feign.form.ContentType.MULTIPART;
//
//@Configuration(proxyBeanMethods = false)
//public class CairoFeignConfiguration {
//	@Autowired
//	private ObjectFactory<HttpMessageConverters> messageConverters;
//	@Autowired(required = false)
//	private SpringDataWebProperties springDataWebProperties;
//
//	@Autowired(required = false)
//	private FeignEncoderProperties encoderProperties;
//
//	@Bean
//	@ConditionalOnClass(name = "org.springframework.data.domain.Pageable")
//	@ConditionalOnMissingBean
//	@Order(Ordered.HIGHEST_PRECEDENCE)
//	public Encoder feignEncoderPageable(ObjectProvider<AbstractFormWriter> formWriterProvider,
//										ObjectProvider<HttpMessageConverterCustomizer> customizers) {
//		PageableSpringEncoder encoder = new PageableSpringEncoder(
//			springEncoder(formWriterProvider, encoderProperties, customizers));
//
//		if (springDataWebProperties != null) {
//			encoder.setPageParameter(springDataWebProperties.getPageable().getPageParameter());
//			encoder.setSizeParameter(springDataWebProperties.getPageable().getSizeParameter());
//			encoder.setSortParameter(springDataWebProperties.getSort().getSortParameter());
//		}
//		return new CairoSignV1FeignEncoder(encoder);
//	}
//
//	@Bean
//	@ConditionalOnMissingBean
//	@ConditionalOnMissingClass("org.springframework.data.domain.Pageable")
//	@Order(Ordered.HIGHEST_PRECEDENCE - 10)
//	public Encoder cairoSignV1feignEncoder(ObjectProvider<AbstractFormWriter> formWriterProvider,
//										   ObjectProvider<HttpMessageConverterCustomizer> customizers) {
//		return new CairoSignV1FeignEncoder(springEncoder(formWriterProvider, encoderProperties, customizers));
//	}
//
//	private Encoder springEncoder(ObjectProvider<AbstractFormWriter> formWriterProvider,
//								  FeignEncoderProperties encoderProperties, ObjectProvider<HttpMessageConverterCustomizer> customizers) {
//		AbstractFormWriter formWriter = formWriterProvider.getIfAvailable();
//
//		if (formWriter != null) {
//			return new SpringEncoder(new SpringPojoFormEncoder(formWriter), messageConverters, encoderProperties,
//				customizers);
//		} else {
//			return new SpringEncoder(new SpringFormEncoder(), messageConverters, encoderProperties, customizers);
//		}
//	}
//
//	private static class SpringPojoFormEncoder extends SpringFormEncoder {
//
//		SpringPojoFormEncoder(AbstractFormWriter formWriter) {
//			super();
//
//			MultipartFormContentProcessor processor = (MultipartFormContentProcessor) getContentProcessor(MULTIPART);
//			processor.addFirstWriter(formWriter);
//		}
//
//	}
//}
