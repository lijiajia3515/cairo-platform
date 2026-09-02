package io.github.lijiajia3515.cairo.auth.modules.client;

import io.github.lijiajia3515.cairo.auth.domain.dto.client.BasicClient;
import io.github.lijiajia3515.cairo.auth.domain.dto.client.Client;
import io.github.lijiajia3515.cairo.auth.framework.sdk.CairoOAuthClientSdkService;
import io.github.lijiajia3515.cairo.auth.domain.api.client.client.GetClientArgs;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

@Slf4j
public class ClientClientApiServiceImpl implements ClientClientApiService {

	private final ClientClientApiFeignClient clientClientApiFeignClient;
	private final CairoOAuthClientSdkService cairoOAuthClientSdkService;

	public ClientClientApiServiceImpl(ClientClientApiFeignClient clientClientApiFeignClient, CairoOAuthClientSdkService cairoOAuthClientSdkService) {
		this.clientClientApiFeignClient = clientClientApiFeignClient;
		this.cairoOAuthClientSdkService = cairoOAuthClientSdkService;
	}


	@Override
	public List<BasicClient> getBasicClientList(GetClientArgs args) {
		try {
			ResponseEntity<BusinessResult<List<BasicClient>>> basicClientList = clientClientApiFeignClient.getBasicClientList(cairoOAuthClientSdkService.getHeaderAuthorization(), args);
			return Optional.ofNullable(basicClientList.getBody()).map(BusinessResult::getData).orElse(null);
		} catch (Exception e) {
			log.error("basicClientList error", e);
			throw e;
		}
	}

	@Override
	public List<Client> getClientList(GetClientArgs args) {
		try {
			ResponseEntity<BusinessResult<List<Client>>> clientList = clientClientApiFeignClient.getClientList(cairoOAuthClientSdkService.getHeaderAuthorization(), args);
			return Optional.ofNullable(clientList.getBody()).map(BusinessResult::getData).orElse(null);
		} catch (Exception e) {
			log.error("clientList error", e);
			throw e;
		}
	}
}
