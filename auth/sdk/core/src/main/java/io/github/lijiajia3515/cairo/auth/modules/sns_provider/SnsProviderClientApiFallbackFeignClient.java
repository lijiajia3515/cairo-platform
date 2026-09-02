package io.github.lijiajia3515.cairo.auth.modules.sns_provider;


import io.github.lijiajia3515.cairo.auth.domain.api.client.sns_provider.GetSnsProviderArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.sns_provider.SnsProvider;
import io.github.lijiajia3515.cairo.core.exception.ErrorBusinessException;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import org.springframework.http.ResponseEntity;

import java.util.List;

/**
 * client-api-sns-provider fallback feignclient
 */
public class SnsProviderClientApiFallbackFeignClient implements SnsProviderClientApiFeignClient {

	public static final RuntimeException EX = new ErrorBusinessException("认证服务-服务故障");

	@Override
	public ResponseEntity<BusinessResult<List<SnsProvider>>> getSnsProviderList(String authorization, GetSnsProviderArgs args) {
		throw EX;
	}
}
