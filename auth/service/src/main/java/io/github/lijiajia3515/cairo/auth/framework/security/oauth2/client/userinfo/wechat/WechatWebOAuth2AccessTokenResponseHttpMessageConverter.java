/*
package io.github.lijiajia3515.cairo.auth.framework.security.oauth2.client.userinfo.wechat;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponseMapConverter;
import org.springframework.util.Assert;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

public class WechatWebOAuth2AccessTokenResponseHttpMessageConverter
	extends AbstractHttpMessageConverter<OAuth2AccessTokenResponse> {

	private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

	private static final ParameterizedTypeReference<Map<String, Object>> STRING_OBJECT_MAP = new ParameterizedTypeReference<>() {
	};

	private final MappingJackson2HttpMessageConverter jsonMessageConverter = new MappingJackson2HttpMessageConverter();

	protected Converter<Map<String, String>, OAuth2AccessTokenResponse> tokenResponseConverter = new WechatWebMapOAuth2AccessTokenResponseConverter();

	protected Converter<OAuth2AccessTokenResponse, Map<String, String>> tokenResponseParametersConverter = new OAuth2AccessTokenResponseMapConverter();

	public WechatWebOAuth2AccessTokenResponseHttpMessageConverter() {
		super(DEFAULT_CHARSET, MediaType.TEXT_PLAIN);
	}

	@Override
	protected boolean supports(Class<?> clazz) {
		return OAuth2AccessTokenResponse.class.isAssignableFrom(clazz);
	}

	@Override
	@SuppressWarnings("unchecked")
	protected OAuth2AccessTokenResponse readInternal(Class<? extends OAuth2AccessTokenResponse> clazz,
													 HttpInputMessage inputMessage) throws HttpMessageNotReadableException {
		try {
			// gh-6463: Parse parameter values as Object in order to handle potential JSON
			// Object and then convert values to String
			Map<String, Object> tokenResponseParameters = (Map<String, Object>) this.jsonMessageConverter
				.read(STRING_OBJECT_MAP.getType(), null, inputMessage);

			return this.tokenResponseConverter.convert(tokenResponseParameters
				.entrySet()
				.stream()
				.collect(Collectors.toMap(Map.Entry::getKey, (entry) -> String.valueOf(entry.getValue()))));

		} catch (Exception ex) {
			throw new HttpMessageNotReadableException(
				"An error occurred reading the OAuth 2.0 Access Token Response: " + ex.getMessage(), ex,
				inputMessage);
		}
	}

	@Override
	protected void writeInternal(OAuth2AccessTokenResponse tokenResponse, HttpOutputMessage outputMessage)
		throws HttpMessageNotWritableException {
		try {
			Map<String, String> tokenResponseParameters = this.tokenResponseParametersConverter.convert(tokenResponse);
			this.jsonMessageConverter.write(tokenResponseParameters, STRING_OBJECT_MAP.getType(),
				MediaType.APPLICATION_JSON, outputMessage);
		} catch (Exception ex) {
			throw new HttpMessageNotWritableException(
				"An error occurred writing the OAuth 2.0 Access Token Response: " + ex.getMessage(), ex);
		}
	}

	*/
/**
	 * Sets the {@link Converter} used for converting the OAuth 2.0 Access Token Response
	 * parameters to an {@link OAuth2AccessTokenResponse}.
	 *
	 * @param tokenResponseConverter the {@link Converter} used for converting to an
	 *                               {@link OAuth2AccessTokenResponse}
	 *//*

	public final void setTokenResponseConverter(
		Converter<Map<String, String>, OAuth2AccessTokenResponse> tokenResponseConverter) {
		Assert.notNull(tokenResponseConverter, "tokenResponseConverter cannot be null");
		this.tokenResponseConverter = tokenResponseConverter;
	}

	*/
/**
	 * Sets the {@link Converter} used for converting the
	 * {@link OAuth2AccessTokenResponse} to a {@code Map} representation of the OAuth 2.0
	 * Access Token Response parameters.
	 *
	 * @param tokenResponseParametersConverter the {@link Converter} used for converting
	 *                                         to a {@code Map} representation of the Access Token Response parameters
	 *//*

	public final void setTokenResponseParametersConverter(
		Converter<OAuth2AccessTokenResponse, Map<String, String>> tokenResponseParametersConverter) {
		Assert.notNull(tokenResponseParametersConverter, "tokenResponseParametersConverter cannot be null");
		this.tokenResponseParametersConverter = tokenResponseParametersConverter;
	}


}
*/
