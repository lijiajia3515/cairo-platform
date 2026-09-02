package io.github.lijiajia3515.cairo.auth.modules.dict.biz;

import io.github.lijiajia3515.cairo.core.exception.ConflictBusinessException;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import io.github.lijiajia3515.cairo.auth.domain.api.client.dict.biz.GetBizDictInfoArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.dict.biz.BizDict;
import io.github.lijiajia3515.cairo.auth.domain.dto.dict.biz.BizDictItem;
import io.github.lijiajia3515.cairo.auth.domain.dto.dict.biz.PathBizDict;
import io.github.lijiajia3515.cairo.auth.framework.sdk.CairoOAuthClientSdkService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Slf4j
public class BizDictClientApiServiceImpl implements BizDictClientApiService {
	private final BizDictClientApiFeignClient bizDictClientApiFeignClient;
	private final CairoOAuthClientSdkService cairoOAuthClientSdkService;

	public BizDictClientApiServiceImpl(BizDictClientApiFeignClient bizDictClientApiFeignClient, CairoOAuthClientSdkService cairoOAuthClientSdkService) {
		this.bizDictClientApiFeignClient = bizDictClientApiFeignClient;
		this.cairoOAuthClientSdkService = cairoOAuthClientSdkService;
	}

	@Override
	public Map<String, Map<String, BizDictItem>> getBizDictItemMap(String tenantId, Set<String> dictIds) {
		try {
			ResponseEntity<BusinessResult<Map<String, Map<String, BizDictItem>>>> bizDictItemMap = bizDictClientApiFeignClient.getBizDictItemMap(cairoOAuthClientSdkService.getHeaderAuthorization(),tenantId, dictIds);
			return Optional.ofNullable(bizDictItemMap.getBody()).map(BusinessResult::getData).orElse(Collections.emptyMap());
		} catch (Exception e) {
			log.info("getBizDictItemMap：", e);
			throw new ConflictBusinessException("获取业务级字典项map失败");
		}
	}

	@Override
	public Map<String, Map<String, BizDictItem>> getBizDictItemIdMap(String tenantId, Map<String, Set<String>> dictIdMap) {
		try {
			ResponseEntity<BusinessResult<Map<String, Map<String, BizDictItem>>>> bizDictItemIdMap = bizDictClientApiFeignClient.getBizDictItemIdMap(cairoOAuthClientSdkService.getHeaderAuthorization(),tenantId, dictIdMap);
			return Optional.ofNullable(bizDictItemIdMap.getBody()).map(BusinessResult::getData).orElse(Collections.emptyMap());
		} catch (Exception e) {
			log.info("getBizDictItemIdMap：", e);
			throw new ConflictBusinessException("获取业务级字典部分字典项信息失败");
		}
	}

	@Override
	public Map<String, Map<String, PathBizDict>> getPathBizDictItemIdMap(String tenantId, Map<String, Set<String>> dictIdMap) {
		try {
			ResponseEntity<BusinessResult<Map<String, Map<String, PathBizDict>>>> pathBizDictItemIdMap = bizDictClientApiFeignClient.getPathBizDictItemIdMap(cairoOAuthClientSdkService.getHeaderAuthorization(),tenantId, dictIdMap);
			return Optional.ofNullable(pathBizDictItemIdMap.getBody()).map(BusinessResult::getData).orElse(Collections.emptyMap());
		} catch (Exception e) {
			log.info("getPathBizDictItemIdMap：", e);
			throw new ConflictBusinessException("获多级业务级字典项部分信息map失败");
		}
	}

	@Override
	public Map<String, Map<String, PathBizDict>> getPathBizDictItemMap(String tenantId, Set<String> dictIds) {
		try {
			ResponseEntity<BusinessResult<Map<String, Map<String, PathBizDict>>>> pathBizDictItemMap = bizDictClientApiFeignClient.getPathBizDictItemMap(cairoOAuthClientSdkService.getHeaderAuthorization(),tenantId, dictIds);
			return Optional.ofNullable(pathBizDictItemMap.getBody()).map(BusinessResult::getData).orElse(Collections.emptyMap());
		} catch (Exception e) {
			log.info("getPathBizDictItemMap：", e);
			throw new ConflictBusinessException("获取多级业务级字典项map失败");
		}
	}

	@Override
	public BizDict getBizDictDetailInfo(String tenantId, GetBizDictInfoArgs args) {
		try {
			ResponseEntity<BusinessResult<BizDict>> bizDictDetailInfo = bizDictClientApiFeignClient.getBizDictDetailInfo(cairoOAuthClientSdkService.getHeaderAuthorization(), tenantId, args);
			return Optional.ofNullable(bizDictDetailInfo.getBody()).map(BusinessResult::getData).orElse(BizDict.builder().build());
		} catch (Exception e) {
			log.info("getBizDictDetailInfo：", e);
			throw new ConflictBusinessException("获取业务级字典详情失败");
		}
	}
}
