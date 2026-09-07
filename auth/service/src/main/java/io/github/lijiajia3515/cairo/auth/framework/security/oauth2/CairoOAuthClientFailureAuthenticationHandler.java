package io.github.lijiajia3515.cairo.auth.framework.security.oauth2;

import io.github.lijiajia3515.cairo.auth.framework.security.web.CairoAuthenticationFailedHandler;
import io.micrometer.tracing.annotation.NewSpan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.web.accept.ContentNegotiationManager;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * 客户端认证失败处理器：输出<b>纯 RFC 6749</b> 错误体（invalid_client 等 + 语义化 HTTP 状态）。
 * /oauth2/token 是协议端点，不做业务信封包装——业务语义由 /open_api 代理层（OAuth2OpenApiController）
 * 凭 {@link CairoOAuthErrorMapper} 转换补齐，双端点契约见 usage.md。
 */
@Slf4j
public class CairoOAuthClientFailureAuthenticationHandler extends CairoAuthenticationFailedHandler implements AuthenticationFailureHandler {

    public CairoOAuthClientFailureAuthenticationHandler(List<HttpMessageConverter<?>> converters) {
        super(converters);
    }

    public CairoOAuthClientFailureAuthenticationHandler(List<HttpMessageConverter<?>> converters, ContentNegotiationManager contentNegotiationManager) {
        super(converters, contentNegotiationManager);
    }

    @Override
    @NewSpan
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        log.info("CairoOAuthClientFailureAuthenticationHandler onAuthenticationFailure", exception);
        OAuth2Error error = ((OAuth2AuthenticationException) exception).getError();
        CairoOAuthErrorMapper.writeRfcError(response, error.getErrorCode(), error.getDescription(), error.getUri());
    }
}
