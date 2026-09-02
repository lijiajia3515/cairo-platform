package io.github.lijiajia3515.cairo.auth.modules.area;

import io.github.lijiajia3515.cairo.core.exception.ErrorBusinessException;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import io.github.lijiajia3515.cairo.auth.domain.api.client.area.GetAreaDetailArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.area.GetAreaDetailMapArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.area.GetAreaListArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.area.Area;
import io.github.lijiajia3515.cairo.auth.domain.dto.area.AreaDetail;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

/**
 * client-api area fallback feignclient
 */
public class AreaClientApiFallbackFeignClient implements AreaClientApiFeignClient {

	public static final RuntimeException EX = new ErrorBusinessException("系统服务-行政区划子应用故障");

	@Override
	public ResponseEntity<BusinessResult<List<Area>>> getAreaList(String authorization, GetAreaListArgs args) {
		throw EX;
	}

	@Override
	public ResponseEntity<BusinessResult<AreaDetail>> getAreaDetail(String authorization, GetAreaDetailArgs args) {
		throw EX;
	}

	@Override
	public ResponseEntity<BusinessResult<Map<String, AreaDetail>>> getAreaDetailMap(String authorization, GetAreaDetailMapArgs args) {
		throw EX;
	}
}
