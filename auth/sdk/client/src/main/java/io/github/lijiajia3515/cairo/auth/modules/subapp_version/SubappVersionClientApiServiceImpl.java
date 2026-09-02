package io.github.lijiajia3515.cairo.auth.modules.subapp_version;

import io.github.lijiajia3515.cairo.auth.domain.dto.subapp_version.SubappVersion;
import io.github.lijiajia3515.cairo.auth.framework.sdk.CairoOAuthClientSdkService;
import io.github.lijiajia3515.cairo.auth.domain.api.client.subapp_version.GetSubappVersionClientArgs;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

@Slf4j
public class SubappVersionClientApiServiceImpl implements SubappVersionClientApiService {

	private final SubappVersionClientApiFeignClient subappVersionClientApiFeignClient;
	private final CairoOAuthClientSdkService cairoOAuthClientSdkService;

	public SubappVersionClientApiServiceImpl(SubappVersionClientApiFeignClient subappVersionClientApiFeignClient,
                                              CairoOAuthClientSdkService cairoOAuthClientSdkService) {
		this.subappVersionClientApiFeignClient = subappVersionClientApiFeignClient;
		this.cairoOAuthClientSdkService = cairoOAuthClientSdkService;
	}


	@Override
	public List<SubappVersion> getSubappVersionList(GetSubappVersionClientArgs args) {
		try {
			ResponseEntity<BusinessResult<List<SubappVersion>>> subappVersionList = subappVersionClientApiFeignClient.getSubappVersionList(cairoOAuthClientSdkService.getHeaderAuthorization(), args);
			return Optional.ofNullable(subappVersionList.getBody()).map(BusinessResult::getData).orElse(null);
		} catch (Exception e) {
			log.error("subappVersion error", e);
			throw e;
		}
	}
}
