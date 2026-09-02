package io.github.lijiajia3515.cairo.auth.modules.area;

import io.github.lijiajia3515.cairo.core.exception.ConflictBusinessException;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import io.github.lijiajia3515.cairo.auth.domain.api.client.area.GetAreaDetailArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.area.GetAreaDetailMapArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.area.GetAreaListArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.area.Area;
import io.github.lijiajia3515.cairo.auth.domain.dto.area.AreaDetail;
import io.github.lijiajia3515.cairo.auth.framework.sdk.CairoOAuthClientSdkService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
public class AreaClientApiServiceImpl implements AreaClientApiService {
	private final AreaClientApiFeignClient areaClientApiFeignClient;
	private final CairoOAuthClientSdkService cairoOAuthClientSdkService;

	public AreaClientApiServiceImpl(AreaClientApiFeignClient areaClientApiFeignClient,
									CairoOAuthClientSdkService cairoOAuthClientSdkService) {
		this.areaClientApiFeignClient = areaClientApiFeignClient;
		this.cairoOAuthClientSdkService = cairoOAuthClientSdkService;
	}

	@Override
	public List<Area> getAreaList(GetAreaListArgs args) {
		try {
			ResponseEntity<BusinessResult<List<Area>>> areaList = areaClientApiFeignClient.getAreaList(cairoOAuthClientSdkService.getHeaderAuthorization(), args);
			return Optional.ofNullable(areaList.getBody()).map(BusinessResult::getData).orElse(Collections.emptyList());
		} catch (Exception e) {
			log.info("getAreaList：", e);
			throw new ConflictBusinessException("查询区域列表失败");
		}
	}

	@Override
	public AreaDetail getAreaDetail(GetAreaDetailArgs args) {
		try {
			ResponseEntity<BusinessResult<AreaDetail>> areaDetail = areaClientApiFeignClient.getAreaDetail(cairoOAuthClientSdkService.getHeaderAuthorization(), args);
			return Optional.ofNullable(areaDetail.getBody()).map(BusinessResult::getData).orElse(AreaDetail.builder().build());
		} catch (Exception e) {
			log.info("getAreaDetail：", e);
			throw new ConflictBusinessException("获取区域详情失败");
		}
	}

	@Override
	public Map<String, AreaDetail> getAreaDetailMap(GetAreaDetailMapArgs args) {
		try {
			ResponseEntity<BusinessResult<Map<String, AreaDetail>>> areaDetailMap = areaClientApiFeignClient.getAreaDetailMap(cairoOAuthClientSdkService.getHeaderAuthorization(), args);
			return Optional.ofNullable(areaDetailMap.getBody()).map(BusinessResult::getData).orElse(Collections.emptyMap());
		} catch (Exception e) {
			log.info("getAreaDetailMap：", e);
			throw new ConflictBusinessException("获取区域详情map失败");
		}
	}

}
