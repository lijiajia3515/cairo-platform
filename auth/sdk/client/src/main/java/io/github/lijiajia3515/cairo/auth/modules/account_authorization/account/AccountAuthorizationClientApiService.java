package io.github.lijiajia3515.cairo.auth.modules.account_authorization.account;


import io.github.lijiajia3515.cairo.auth.domain.api.client.account_authorization.AccountAuthorizationModel;
import io.github.lijiajia3515.cairo.auth.domain.api.client.account_authorization.GetAccountAuthorizationArgs;

public interface AccountAuthorizationClientApiService {


	/**
	 * 获取账号认证
	 * 需要权限 account:account_auth | account:all
	 *
	 * @param args args
	 * @return auth model
	 */
	AccountAuthorizationModel getAccountAuthorization(GetAccountAuthorizationArgs args);


}
