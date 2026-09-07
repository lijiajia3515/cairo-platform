package io.github.lijiajia3515.cairo.auth.framework.security.oauth2;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.lijiajia3515.cairo.auth.framework.security.CairoAuthBusiness;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.core.CairoOAuthBusiness;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import io.github.lijiajia3515.cairo.core.business.Business;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * OAuth2 协议错误码 ↔ 业务码映射。
 * 标准端点 /oauth2/token 输出纯 RFC 6749 响应；/open_api 代理层凭本映射把错误体转换为 BusinessResult 信封。
 * 扩展码：裸 AuthenticationException 以业务码（Auth.* 前缀）作为 RFC 扩展 error 输出（RFC 6749 允许扩展 error 值），
 * open 代理按前缀原样还原业务码，协议客户端按未知 error 码泛化处理，互不干扰。
 */
public final class CairoOAuthErrorMapper {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	private CairoOAuthErrorMapper() {
	}

	/**
	 * 按 RFC 6749 §5.2 写标准错误响应体 {"error","error_description","error_uri"} + 语义化状态
	 * （状态由 {@link #resolveStatus(String)} 决定），不包任何业务信封。
	 * 供标准端点各失败处理器（token / client 认证）共用。
	 */
	public static void writeRfcError(HttpServletResponse response, String errorCode, String description, String errorUri) throws IOException {
		response.setStatus(resolveStatus(errorCode).value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding("UTF-8");
		Map<String, String> body = new LinkedHashMap<>();
		body.put("error", errorCode);
		if (description != null && !description.isBlank()) {
			body.put("error_description", description);
		}
		if (errorUri != null && !errorUri.isBlank()) {
			body.put("error_uri", errorUri);
		}
		OBJECT_MAPPER.writeValue(response.getWriter(), body);
	}

	/**
	 * 协议层失败按 RFC 6749 错误码细分为业务码，调用方只判 code 即可区分失败原因。
	 * 未识别错误码回落 OAUTH_ERROR 兜底。
	 */
	public static Business resolveBusiness(OAuth2Error error) {
		return resolve(error.getErrorCode());
	}

	/**
	 * 错误码字符串（RFC 标准码或 Auth.* 扩展码）→ 业务码。
	 */
	public static Business resolve(String errorCode) {
		if (errorCode != null && errorCode.startsWith("Auth.")) {
			for (CairoAuthBusiness b : CairoAuthBusiness.values()) {
				if (b.code().equals(errorCode)) {
					return b;
				}
			}
			for (CairoOAuthBusiness b : CairoOAuthBusiness.values()) {
				if (b.code().equals(errorCode)) {
					return b;
				}
			}
			return CairoOAuthBusiness.OAUTH_ERROR;
		}
		return switch (errorCode) {
			case OAuth2ErrorCodes.INVALID_CLIENT -> CairoOAuthBusiness.CLIENT_INVALID;
			case OAuth2ErrorCodes.INVALID_REQUEST -> CairoOAuthBusiness.PARAMS_BAD;
			case OAuth2ErrorCodes.UNAUTHORIZED_CLIENT, OAuth2ErrorCodes.UNSUPPORTED_GRANT_TYPE -> CairoOAuthBusiness.GRANT_NOT_SUPPORTED;
			case OAuth2ErrorCodes.INVALID_GRANT -> CairoOAuthBusiness.GRANT_INVALID;
			case OAuth2ErrorCodes.INVALID_SCOPE, OAuth2ErrorCodes.INSUFFICIENT_SCOPE -> CairoOAuthBusiness.SCOPE_INSUFFICIENT;
			default -> CairoOAuthBusiness.OAUTH_ERROR;
		};
	}

	/**
	 * 错误码 → HTTP 状态语义（RFC 6749 5.2/5.3：客户端错误 400，凭证 401，scope 403，服务端 500/503）。
	 */
	public static HttpStatus resolveStatus(String errorCode) {
		if (errorCode != null && errorCode.startsWith("Auth.")) {
			return HttpStatus.BAD_REQUEST;
		}
		if (OAuth2ErrorCodes.INVALID_CLIENT.equals(errorCode)) {
			return HttpStatus.UNAUTHORIZED;
		}
		if (OAuth2ErrorCodes.INSUFFICIENT_SCOPE.equals(errorCode)) {
			return HttpStatus.FORBIDDEN;
		}
		if (OAuth2ErrorCodes.SERVER_ERROR.equals(errorCode)) {
			return HttpStatus.INTERNAL_SERVER_ERROR;
		}
		if (OAuth2ErrorCodes.TEMPORARILY_UNAVAILABLE.equals(errorCode)) {
			return HttpStatus.SERVICE_UNAVAILABLE;
		}
		return HttpStatus.BAD_REQUEST;
	}
}
