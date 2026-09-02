package io.github.lijiajia3515.cairo.auth.modules.sns_provider;

import io.github.lijiajia3515.cairo.auth.domain.dto.sns_provider.SnsProvider;
import io.github.lijiajia3515.cairo.auth.framework.sdk.CairoOAuthClientSdkService;
import io.github.lijiajia3515.cairo.auth.domain.api.client.sns_provider.GetSnsProviderArgs;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

@Slf4j
public class SnsProviderClientApiServiceImpl implements SnsProviderClientApiService {

	private final SnsProviderClientApiFeignClient snsProviderClientApiFeignClient;
	private final CairoOAuthClientSdkService cairoOAuthClientSdkService;

	public SnsProviderClientApiServiceImpl(SnsProviderClientApiFeignClient snsProviderClientApiFeignClient, CairoOAuthClientSdkService cairoOAuthClientSdkService) {
		this.snsProviderClientApiFeignClient = snsProviderClientApiFeignClient;
		this.cairoOAuthClientSdkService = cairoOAuthClientSdkService;
	}

	@Override
	public List<SnsProvider> getSnsProviderList(GetSnsProviderArgs args) {
		try {
			ResponseEntity<BusinessResult<List<SnsProvider>>> snsProviderList = snsProviderClientApiFeignClient.getSnsProviderList(cairoOAuthClientSdkService.getHeaderAuthorization(), args);
			return Optional.of(snsProviderList.getBody()).map(BusinessResult::getData).orElse(null);
		} catch (Exception e) {
			log.error("snsProviderList error", e);
			throw e;
		}
	}
}
