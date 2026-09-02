package io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.authentication.account;

import lombok.Getter;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationGrantAuthenticationToken;

import java.util.Map;
import java.util.Set;

import static io.github.lijiajia3515.cairo.auth.framework.security.oauth2.core.OAuthAccountAuthorizationGrantTypes.ACCOUNT_SNS_CODE;


/**
 * 账号SNS认证 authentication token
 */
@Getter
public class OAuthAccountSnsCodeAuthenticationToken extends OAuth2AuthorizationGrantAuthenticationToken {
	/**
	 * 第三方认证类型
	 */
	private final String snsType;

	/**
	 * 第三方认证提供商ID
	 */
	private final String snsProviderId;

	/**
	 * 第三方认证授权码
	 */
	private final String snsCode;

	private final Set<String> scopes;


	/**
	 * @param snsType              SNS类型
	 * @param snsProviderId        提供者ID
	 * @param snsCode              授权码
	 * @param scopes               范围
	 * @param clientPrincipal      the authenticated client principal
	 * @param additionalParameters the additional parameters
	 */
	protected OAuthAccountSnsCodeAuthenticationToken(String snsType, String snsProviderId, String snsCode, Set<String> scopes, Authentication clientPrincipal, Map<String, Object> additionalParameters) {
		super(ACCOUNT_SNS_CODE, clientPrincipal, additionalParameters);
		this.snsType = snsType;
		this.snsProviderId = snsProviderId;
		this.snsCode = snsCode;
		this.scopes = scopes;
	}

}
