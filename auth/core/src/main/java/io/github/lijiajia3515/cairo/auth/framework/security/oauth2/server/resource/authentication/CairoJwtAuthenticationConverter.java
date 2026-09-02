package io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.resource.authentication;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * jwt 转 cairo token 处理器
 */
public interface CairoJwtAuthenticationConverter extends Converter<Jwt, AbstractAuthenticationToken> {
}
