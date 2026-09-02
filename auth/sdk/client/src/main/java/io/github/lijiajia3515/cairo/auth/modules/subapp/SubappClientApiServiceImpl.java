package io.github.lijiajia3515.cairo.auth.modules.subapp;

import io.github.lijiajia3515.cairo.auth.domain.dto.subapp.Subapp;
import io.github.lijiajia3515.cairo.auth.framework.sdk.CairoOAuthClientSdkService;
import io.github.lijiajia3515.cairo.auth.domain.api.client.subapp.GetSubappClientArgs;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

@Slf4j
public class SubappClientApiServiceImpl implements SubappClientApiService {

	private final SubappClientApiFeignClient subappClientApiFeignClient;
	private final CairoOAuthClientSdkService cairoOAuthClientSdkService;

	public SubappClientApiServiceImpl(SubappClientApiFeignClient subappClientApiFeignClient,
									   CairoOAuthClientSdkService cairoOAuthClientSdkService) {
		this.subappClientApiFeignClient = subappClientApiFeignClient;
		this.cairoOAuthClientSdkService = cairoOAuthClientSdkService;
	}


	@Override
	public List<Subapp> getSubappList(GetSubappClientArgs args) {
		try {
			ResponseEntity<BusinessResult<List<Subapp>>> subappList = subappClientApiFeignClient.getSubappList(cairoOAuthClientSdkService.getHeaderAuthorization(), args);
			return Optional.ofNullable(subappList.getBody()).map(BusinessResult::getData).orElse(null);
		} catch (Exception e) {
			log.error("subapp error", e);
			throw e;
		}
	}
}
