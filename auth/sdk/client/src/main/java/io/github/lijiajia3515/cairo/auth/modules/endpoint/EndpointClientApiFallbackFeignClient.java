package io.github.lijiajia3515.cairo.auth.modules.endpoint;

import io.github.lijiajia3515.cairo.auth.domain.api.client.endpoint.GetEndpointByAppClientArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.endpoint.GetEndpointClientArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.endpoint.Endpoint;
import io.github.lijiajia3515.cairo.core.exception.ErrorBusinessException;
import io.github.lijiajia3515.cairo.core.page.Page;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import org.springframework.http.ResponseEntity;

import java.util.List;

public class EndpointClientApiFallbackFeignClient implements EndpointClientApiFeignClient {

	public static final RuntimeException EX = new ErrorBusinessException("认证服务-服务故障");

	@Override
	public ResponseEntity<BusinessResult<List<Endpoint>>> getEndpointList(String authorization, GetEndpointClientArgs args) {
		throw EX;
	}

	@Override
	public ResponseEntity<BusinessResult<Page<Endpoint>>> getEndpointPageList(String authorization,GetEndpointClientArgs args) {
		throw EX;
	}

	@Override
	public ResponseEntity<BusinessResult<List<Endpoint>>> getEndpointByAppList(String authorization, GetEndpointByAppClientArgs args) {
		throw EX;
	}
}
