/*
 * Copyright 2002-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.lijiajia3515.cairo.web.servlet.method;

import io.github.lijiajia3515.cairo.web.bind.annotation.RequestMessageParam;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.StreamUtils;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor;
import org.springframework.web.servlet.mvc.support.DefaultHandlerExceptionResolver;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Resolves method arguments annotated with {@code @RequestBody} and handles return
 * values from methods annotated with {@code @ResponseBody} by reading and writing
 * to the body of the request or response with an {@link HttpMessageConverter}.
 *
 * <p>An {@code @RequestBody} method argument is also validated if it is annotated
 * with {@code @javax.validation.Valid}. In case of validation failure,
 * {@link MethodArgumentNotValidException} is raised and results in an HTTP 400
 * response status code if {@link DefaultHandlerExceptionResolver} is configured.
 *
 * @author Arjen Poutsma
 * @author Rossen Stoyanchev
 * @author Juergen Hoeller
 * @since 3.1
 */
public class RequestMessageParamMethodProcessor extends RequestResponseBodyMethodProcessor {

	/**
	 * Basic constructor with converters only. Suitable for resolving
	 * {@code @RequestBody}. For handling {@code @ResponseBody} consider also
	 * providing a {@code ContentNegotiationManager}.
	 */
	public RequestMessageParamMethodProcessor(List<HttpMessageConverter<?>> converters) {
		super(converters);
	}

	/**
	 * Basic constructor with converters and {@code ContentNegotiationManager}.
	 * Suitable for resolving {@code @RequestBody} and handling
	 * {@code @ResponseBody} without {@code Request~} or
	 * {@code ResponseBodyAdvice}.
	 */
	public RequestMessageParamMethodProcessor(List<HttpMessageConverter<?>> converters,
											  @Nullable ContentNegotiationManager manager) {

		super(converters, manager);
	}

	/**
	 * Complete constructor for resolving {@code @RequestBody} method arguments.
	 * For handling {@code @ResponseBody} consider also providing a
	 * {@code ContentNegotiationManager}.
	 *
	 * @since 4.2
	 */
	public RequestMessageParamMethodProcessor(List<HttpMessageConverter<?>> converters,
											  @Nullable List<Object> requestResponseBodyAdvice) {

		super(converters, null, requestResponseBodyAdvice);
	}

	/**
	 * Complete constructor for resolving {@code @RequestBody} and handling
	 * {@code @ResponseBody}.
	 */
	public RequestMessageParamMethodProcessor(List<HttpMessageConverter<?>> converters,
											  @Nullable ContentNegotiationManager manager, @Nullable List<Object> requestResponseBodyAdvice) {

		super(converters, manager, requestResponseBodyAdvice);
	}


	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return parameter.hasParameterAnnotation(RequestMessageParam.class);
	}

	@Override
	public boolean supportsReturnType(MethodParameter returnType) {
		return false;
	}

	@Override
	@Nullable
	protected Object readWithMessageConverters(NativeWebRequest webRequest, MethodParameter parameter,
											   Type paramType) throws IOException, HttpMediaTypeNotSupportedException, HttpMessageNotReadableException {

		RequestMessageParam messageParam = parameter.getParameterAnnotation(RequestMessageParam.class);
		if (messageParam != null) {
			String parameterName = messageParam.name();
			HttpServletRequest servletRequest = webRequest.getNativeRequest(HttpServletRequest.class);
			Assert.state(servletRequest != null, "No HttpServletRequest");
			String paramValue = servletRequest.getParameter(parameterName);
			EmptyBodyCheckingHttpInputMessage inputMessage = new EmptyBodyCheckingHttpInputMessage(new ServletServerHttpRequest(servletRequest), paramValue);

			Object arg = readWithMessageConverters(inputMessage, parameter, paramType);
			if (arg == null && checkRequired(parameter)) {
				throw new HttpMessageNotReadableException("Required request body is missing: " +
					parameter.getExecutable().toGenericString(), inputMessage);
			}
			return arg;
		}
		return null;
	}

	@Override
	protected boolean checkRequired(MethodParameter parameter) {
		RequestMessageParam messageParam = parameter.getParameterAnnotation(RequestMessageParam.class);
		return (messageParam != null && messageParam.required() && !parameter.isOptional());
	}

	private static class EmptyBodyCheckingHttpInputMessage implements HttpInputMessage {

		private final HttpHeaders headers;

		@Nullable
		private final InputStream body;

		public EmptyBodyCheckingHttpInputMessage(HttpMessage message, String paramValue) throws IOException {
			this.headers = message.getHeaders();
			InputStream inputStream = new ByteArrayInputStream(paramValue.getBytes());
			if (inputStream.markSupported()) {
				inputStream.mark(1);
				this.body = (inputStream.read() != -1 ? inputStream : null);
				inputStream.reset();
			} else {
				PushbackInputStream pushbackInputStream = new PushbackInputStream(inputStream);
				int b = pushbackInputStream.read();
				if (b == -1) {
					this.body = null;
				} else {
					this.body = pushbackInputStream;
					pushbackInputStream.unread(b);
				}
			}
		}

		@Override
		public HttpHeaders getHeaders() {
			return this.headers;
		}

		@Override
		public InputStream getBody() {
			return (this.body != null ? this.body : StreamUtils.emptyInput());
		}

		public boolean hasBody() {
			return (this.body != null);
		}
	}

}
