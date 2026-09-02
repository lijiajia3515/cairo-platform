package io.github.lijiajia3515.cairo.web.servlet.method;

import io.github.lijiajia3515.cairo.core.business.DefaultBusiness;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import io.github.lijiajia3515.cairo.web.bind.annotation.BusinessResultBody;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.mvc.method.annotation.JsonViewResponseBodyAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 统一结果返回转换 处理器
 */
@Slf4j
public class BusinessResultBodyMethodHandler implements HandlerMethodReturnValueHandler, BeanPostProcessor {
	private final List<HttpMessageConverter<?>> messageConverters;
	private ContentNegotiationManager contentNegotiationManager;
	private final List<ResponseBodyAdvice<?>> responseBodyAdvices = new ArrayList<>() {{
		add(new JsonViewResponseBodyAdvice());
	}};

	private RequestResponseBodyMethodProcessor proxyHandler = null;

	public BusinessResultBodyMethodHandler(List<HttpMessageConverter<?>> messageConverters) {
		this(messageConverters, null);
	}

	public BusinessResultBodyMethodHandler(List<HttpMessageConverter<?>> messageConverters, List<ResponseBodyAdvice<?>> responseBodyAdvices) {
		this(messageConverters, null, responseBodyAdvices);
	}

	public BusinessResultBodyMethodHandler(List<HttpMessageConverter<?>> messageConverters, ContentNegotiationManager contentNegotiationManager, List<ResponseBodyAdvice<?>> responseBodyAdvices) {
		this.messageConverters = messageConverters;
		this.contentNegotiationManager = contentNegotiationManager;
		if (responseBodyAdvices != null) {
			this.responseBodyAdvices.addAll(responseBodyAdvices);
		}
		initProxyHandler();
	}

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {

		if (bean instanceof ContentNegotiationManager) {
			contentNegotiationManager = ((ContentNegotiationManager) bean);
			initProxyHandler();
		}

		if (bean instanceof RequestMappingHandlerAdapter) {
			List<HandlerMethodReturnValueHandler> handlers = new ArrayList<>(
				Objects.requireNonNull(((RequestMappingHandlerAdapter) bean).getReturnValueHandlers()));
			handlers.stream().filter(x -> x instanceof BusinessResultBodyMethodHandler).findFirst()
				.ifPresent(x -> {
					handlers.remove(x);
					int index = IntStream.range(0, handlers.size())
						.filter(d -> handlers.get(d).getClass().equals(RequestResponseBodyMethodProcessor.class))
						.findFirst().orElse(0);
					handlers.add(index, x);
					((RequestMappingHandlerAdapter) bean).setReturnValueHandlers(handlers);
				});
		}

		return bean;
	}

	public void initProxyHandler() {
		if (messageConverters != null && contentNegotiationManager != null) {
			proxyHandler = new RequestResponseBodyMethodProcessor(messageConverters, contentNegotiationManager, responseBodyAdvices.stream().map(x -> (Object) x).collect(Collectors.toList()));
		}
	}

	@Override
	public boolean supportsReturnType(MethodParameter returnType) {
		return (AnnotatedElementUtils.hasAnnotation(returnType.getContainingClass(), BusinessResultBody.class) ||
			returnType.hasMethodAnnotation(BusinessResultBody.class)) && proxyHandler.supportsReturnType(returnType);
	}

	@Override
	public void handleReturnValue(Object returnValue,
								  MethodParameter returnType,
								  ModelAndViewContainer mavContainer,
								  NativeWebRequest webRequest) throws HttpMediaTypeNotAcceptableException, IOException {
		BusinessResult<Object> businessReturnValue = BusinessResult.builder()
			.business(DefaultBusiness.SUCCESS)
			.data(returnValue)
			.build();
		proxyHandler.handleReturnValue(businessReturnValue, returnType, mavContainer, webRequest);
	}

}
