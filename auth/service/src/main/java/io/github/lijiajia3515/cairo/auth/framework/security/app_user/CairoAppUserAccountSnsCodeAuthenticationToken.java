package io.github.lijiajia3515.cairo.auth.framework.security.app_user;

import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.util.Assert;

/**
 * cairo app user account sns code authentication token
 */
@Getter
public class CairoAppUserAccountSnsCodeAuthenticationToken extends AbstractAuthenticationToken {

	/**
	 * appId
	 */
	private final String appId;

	/**
	 * endpointId
	 */
	private final String endpointId;

	/**
	 * clientId
	 */
	private final String clientId;

	/**
	 * SNS类型
	 */
	private final String snsType;

	/**
	 * 提供方ID
	 */
	private final String snsProviderId;

	/**
	 * 授权码
	 */
	private final String snsCode;

	/**
	 * Creates a token
	 */
	public CairoAppUserAccountSnsCodeAuthenticationToken(String appId, String endpointId, String clientId, String snsType, String snsProviderId, String snsCode) {
		super(null);
		Assert.notNull(appId, "appId not null");
		Assert.notNull(endpointId, "endpointId not null");
		Assert.notNull(clientId, "clientId not null");
		Assert.notNull(snsType, "snsType not null");
		Assert.notNull(snsProviderId, "snsProviderId not null");
		Assert.notNull(snsCode, "snsCode not null");

		this.appId = appId;
		this.endpointId = endpointId;
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
		return String.format("app_user:%s:%s:%s:sns_code:%s_%s_%s", appId, endpointId, clientId, snsType, snsProviderId, snsCode);
	}


}
