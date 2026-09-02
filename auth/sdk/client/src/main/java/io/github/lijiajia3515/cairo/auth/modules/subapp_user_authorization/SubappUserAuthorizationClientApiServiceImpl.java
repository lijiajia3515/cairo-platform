package io.github.lijiajia3515.cairo.auth.modules.subapp_user_authorization;


import io.github.lijiajia3515.cairo.auth.domain.api.client.subapp_user.SubappUserAuthorizationModel;
import io.github.lijiajia3515.cairo.auth.domain.api.client.subapp_user.GetSubappUserAuthorizationArgs;
import io.github.lijiajia3515.cairo.auth.framework.sdk.CairoOAuthClientSdkService;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

@Slf4j
public class SubappUserAuthorizationClientApiServiceImpl implements SubappUserAuthorizationClientApiService {

	private final SubappUserAuthorizationClientApiFeignClient subappUserAuthorizationClientApiFeignClient;
	private final CairoOAuthClientSdkService cairoOAuthClientSdkService;

	public SubappUserAuthorizationClientApiServiceImpl(SubappUserAuthorizationClientApiFeignClient subappUserAuthorizationClientApiFeignClient,
														   CairoOAuthClientSdkService cairoOAuthClientSdkService) {
		this.subappUserAuthorizationClientApiFeignClient = subappUserAuthorizationClientApiFeignClient;
		this.cairoOAuthClientSdkService = cairoOAuthClientSdkService;
	}

	@Override
	public SubappUserAuthorizationModel getSubappUserAuthorization(GetSubappUserAuthorizationArgs args) {
		try {
			ResponseEntity<BusinessResult<SubappUserAuthorizationModel>> authorization = subappUserAuthorizationClientApiFeignClient.getSubappUserAuthorization(cairoOAuthClientSdkService.getHeaderAuthorization(), args);
			return Optional.ofNullable(authorization.getBody()).map(BusinessResult::getData).orElse(null);
		} catch (Exception e) {
			log.error("getSubappUserAuthorization error", e);
			throw e;
		}
	}
}
