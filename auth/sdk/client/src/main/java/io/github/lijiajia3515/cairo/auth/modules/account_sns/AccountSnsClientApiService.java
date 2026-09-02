package io.github.lijiajia3515.cairo.auth.modules.account_sns;

import io.github.lijiajia3515.cairo.auth.domain.api.client.account_sns.AccountSns;
import io.github.lijiajia3515.cairo.auth.domain.api.client.account_sns.BindAccountSnsArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.account_sns.GetAccountSnsArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.account_sns.GetAccountSnsMapArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.account_sns.UnBindAccountSnsArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.sns.SnsToken;

import java.util.List;
import java.util.Map;
import java.util.Optional;


public interface AccountSnsClientApiService {

	/**
	 * 获取账号的三方用户信息
	 * 需要权限： account_sns:read ｜account_sns:all
	 *
	 * @param args args
	 * @return 三方用户信息
	 */
	Map<String, SnsToken> getAccountSnsMap(GetAccountSnsMapArgs args);

	/**
	 * 查询账号三方绑定列表
	 * 需要权限： account_sns:read ｜account_sns:all
	 *
	 * @return AccountSns list
	 */
	List<AccountSns> getAccountSnsList(GetAccountSnsArgs args);


	/**
	 * 绑定三方账号
	 * 需要权限： account_sns:bind ｜account_sns:all
	 *
	 * @param args args
	 */
	Optional<String> bindAccountSns(BindAccountSnsArgs args);


	/**
	 * 解绑三方账号
	 * 需要权限： account_sns:unbind ｜account_sns:all
	 *
	 * @param args args
	 */
	Optional<String> unbindAccountSns(UnBindAccountSnsArgs args);

}
