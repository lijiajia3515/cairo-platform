package io.github.lijiajia3515.cairo.auth.framework.security.oauth2.client.userinfo;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * A {@link HttpMessageConverter} for an {@link OAuth2AccessTokenResponse OAuth 2.0 Access
 * Token Response}.
 *
 * @author Joe Grandja
 * @see AbstractHttpMessageConverter
 * @see OAuth2AccessTokenResponse
 * @since 5.1
 */
public class OAuth2AccessTokenResponseHttpMessageConverter extends AbstractHttpMessageConverter<OAuth2AccessTokenResponse> {

    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    private static final GenericHttpMessageConverter<Object> jsonMessageConverter = new MappingJackson2HttpMessageConverter();
    private final ObjectMapper objectMapper;

    private static final ParameterizedTypeReference<OAuth2AccessTokenResponse> STRING_OBJECT_MAP = new ParameterizedTypeReference<OAuth2AccessTokenResponse>() {
    };

    public OAuth2AccessTokenResponseHttpMessageConverter(ObjectMapper objectMapper) {
        super(DEFAULT_CHARSET, MediaType.APPLICATION_JSON, new MediaType("application", "*+json"));
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean supports(Class<?> clazz) {
        return OAuth2AccessTokenResponse.class.isAssignableFrom(clazz);
    }

    @Override
    protected OAuth2AccessTokenResponse readInternal(Class<? extends OAuth2AccessTokenResponse> clazz, HttpInputMessage inputMessage) throws HttpMessageNotReadableException, IOException {
        return this.objectMapper.readValue(inputMessage.getBody(), OAuth2AccessTokenResponse.class);
    }

    @Override
    protected void writeInternal(OAuth2AccessTokenResponse tokenResponse, HttpOutputMessage outputMessage) throws HttpMessageNotWritableException, IOException {
        jsonMessageConverter.write(tokenResponse, STRING_OBJECT_MAP.getType(), MediaType.APPLICATION_JSON, outputMessage);
    }

}
