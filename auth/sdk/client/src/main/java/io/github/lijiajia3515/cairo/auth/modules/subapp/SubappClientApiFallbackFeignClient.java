package io.github.lijiajia3515.cairo.auth.modules.subapp;

import io.github.lijiajia3515.cairo.auth.domain.api.client.subapp.GetSubappClientArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.subapp.Subapp;
import io.github.lijiajia3515.cairo.core.exception.ErrorBusinessException;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import org.springframework.http.ResponseEntity;

import java.util.List;

public class SubappClientApiFallbackFeignClient implements SubappClientApiFeignClient {

	public static final RuntimeException EX = new ErrorBusinessException("认证服务-服务故障");


	@Override
	public ResponseEntity<BusinessResult<List<Subapp>>> getSubappList(String authorization, GetSubappClientArgs args) {
		throw EX;
	}
}
