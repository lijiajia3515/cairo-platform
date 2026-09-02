package io.github.lijiajia3515.cairo.auth.modules.client;

import io.github.lijiajia3515.cairo.auth.domain.api.client.client.GetClientArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.client.BasicClient;
import io.github.lijiajia3515.cairo.auth.domain.dto.client.Client;
import io.github.lijiajia3515.cairo.core.exception.ErrorBusinessException;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import org.springframework.http.ResponseEntity;

import java.util.List;
/**
 * client-api-app fallback feignclient
 */
public class ClientClientApiFallbackFeignClient implements ClientClientApiFeignClient {
	public static final RuntimeException EX = new ErrorBusinessException("认证服务-服务故障");

	@Override
	public ResponseEntity<BusinessResult<List<BasicClient>>> getBasicClientList(String authorization, GetClientArgs args) {
		throw EX;
	}

	@Override
	public ResponseEntity<BusinessResult<List<Client>>> getClientList(String authorization, GetClientArgs args) {
		throw EX;
	}
}
