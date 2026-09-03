package io.github.lijiajia3515.cairo.auth.framework.security.account;

import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.util.Assert;

/**
 * cairo account sns code authentication token
 */
@Getter
public class CairoAccountSnsCodeAuthenticationToken extends AbstractAuthenticationToken {

	/**
	 * appId
	 */
	private final String appId;

	/**
	 * clientId
	 */
	private final String clientId;

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

	/**
	 * Creates a token
	 */
	public CairoAccountSnsCodeAuthenticationToken(String appId, String clientId, String snsType, String snsProviderId, String snsCode) {
		super(null);
		Assert.notNull(appId, "appId not null");
		Assert.notNull(clientId, "clientId not null");
		Assert.notNull(snsType, "snsType not null");
		Assert.notNull(snsProviderId, "snsProviderId not null");
		Assert.notNull(snsCode, "snsCode not null");

		this.appId = appId;
		this.clientId = clientId;
		this.snsType = snsType;
		this.snsProviderId = snsProviderId;
		this.snsCode = snsCode;
	}

	@Override
	public String getCredentials() {
		return "N/A";
	}

	@Override
	public String getPrincipal() {
		return String.format("account:sns_code:%s_%s_%s", snsType, snsProviderId, snsCode);
	}


}
