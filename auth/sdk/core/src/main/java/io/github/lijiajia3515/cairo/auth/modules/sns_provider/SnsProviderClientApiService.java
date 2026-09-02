package io.github.lijiajia3515.cairo.auth.modules.sns_provider;


import io.github.lijiajia3515.cairo.auth.domain.api.client.sns_provider.GetSnsProviderArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.sns_provider.SnsProvider;

import java.util.List;


public interface SnsProviderClientApiService {

	/**
	 * 获取第三方认证提供方集合
	 *
	 * @param args 参数
	 * @return snsProvider list
	 */
	List<SnsProvider> getSnsProviderList(GetSnsProviderArgs args);


}
