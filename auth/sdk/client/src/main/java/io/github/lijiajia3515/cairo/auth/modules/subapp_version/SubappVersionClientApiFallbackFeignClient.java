package io.github.lijiajia3515.cairo.auth.modules.subapp_version;


import io.github.lijiajia3515.cairo.auth.domain.api.client.subapp_version.GetSubappVersionClientArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.subapp_version.SubappVersion;
import io.github.lijiajia3515.cairo.core.exception.ErrorBusinessException;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import org.springframework.http.ResponseEntity;

import java.util.List;

public class SubappVersionClientApiFallbackFeignClient implements SubappVersionClientApiFeignClient {

	public static final RuntimeException EX = new ErrorBusinessException("认证服务-服务故障");


	@Override
	public ResponseEntity<BusinessResult<List<SubappVersion>>> getSubappVersionList(String authorization, GetSubappVersionClientArgs args) {
		throw EX;
	}
}
