/*
 * Copyright 2002-2020 the original author or authors.
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

package io.github.lijiajia3515.cairo.auth.framework.security.oauth2.client.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.core.CairoOAuth2Error;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.jackson2.CairoOAuthClientSecurityModule;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.GenericHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.oauth2.core.OAuth2Error;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * A {@link HttpMessageConverter} for an {@link OAuth2Error OAuth 2.0 Error}.
 *
 * @author Joe Grandja
 * @see AbstractHttpMessageConverter
 * @see OAuth2Error
 * @since 5.1
 */
public class CairoOAuth2ErrorHttpMessageConverter extends AbstractHttpMessageConverter<CairoOAuth2Error> {

	private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

	private static final ParameterizedTypeReference<CairoOAuth2Error> RESULT_TYPE = new ParameterizedTypeReference<CairoOAuth2Error>() {
	};

	private final GenericHttpMessageConverter<Object> jsonMessageConverter;

	public CairoOAuth2ErrorHttpMessageConverter() {
		super(DEFAULT_CHARSET, MediaType.APPLICATION_JSON, new MediaType("application", "*+json"));
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.setPropertyNamingStrategy(new PropertyNamingStrategies.UpperCamelCaseStrategy());
		objectMapper.registerModule(new CairoOAuthClientSecurityModule());
		jsonMessageConverter = new MappingJackson2HttpMessageConverter(objectMapper);
	}

	@Override
	protected boolean supports(Class<?> clazz) {
		return RESULT_TYPE.getClass().isAssignableFrom(clazz);
	}

	@Override
	protected CairoOAuth2Error readInternal(Class<? extends CairoOAuth2Error> clazz, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
		try {
			// gh-8157: Parse parameter values as Object in order to handle potential JSON
			// Object and then convert values to String
			CairoOAuth2Error error = (CairoOAuth2Error) jsonMessageConverter.read(RESULT_TYPE.getType(), null, inputMessage);
			return error;
		} catch (Exception ex) {
			throw new HttpMessageNotReadableException(
				"An error occurred reading the OAuth 2.0 Error: " + ex.getMessage(), ex, inputMessage);
		}
	}

	@Override
	protected void writeInternal(CairoOAuth2Error oauth2Error, HttpOutputMessage outputMessage)
		throws HttpMessageNotWritableException {
		try {

			jsonMessageConverter.write(oauth2Error, null, outputMessage);
		} catch (Exception ex) {
			throw new HttpMessageNotWritableException(
				"An error occurred writing the OAuth 2.0 Error: " + ex.getMessage(), ex);
		}
	}

}
