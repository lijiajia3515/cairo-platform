package io.github.lijiajia3515.cairo.auth.modules.dict.sys;

import io.github.lijiajia3515.cairo.core.exception.ConflictBusinessException;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import io.github.lijiajia3515.cairo.auth.domain.api.client.dict.sys.GetSysDictSubItemArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.dict.sys.PathSysDict;
import io.github.lijiajia3515.cairo.auth.domain.dto.dict.sys.SysDict;
import io.github.lijiajia3515.cairo.auth.domain.dto.dict.sys.SysDictItem;
import io.github.lijiajia3515.cairo.auth.framework.sdk.CairoOAuthClientSdkService;
import io.github.lijiajia3515.cairo.auth.domain.api.client.dict.sys.GetSysDictInfoArgs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Slf4j
public class SysDictClientApiServiceImpl implements SysDictClientApiService {
	private final SysDictClientApiFeignClient sysDictClientApiFeignClient;
	private final CairoOAuthClientSdkService cairoOAuthClientSdkService;

	public SysDictClientApiServiceImpl(SysDictClientApiFeignClient sysDictClientApiFeignClient, CairoOAuthClientSdkService cairoOAuthClientSdkService) {
		this.sysDictClientApiFeignClient = sysDictClientApiFeignClient;
		this.cairoOAuthClientSdkService = cairoOAuthClientSdkService;
	}

	@Override
	public Map<String, Map<String, SysDictItem>> getSysDictItemMap(Set<String> dictIds) {
		try {
			ResponseEntity<BusinessResult<Map<String, Map<String, SysDictItem>>>> sysDictItemMap = sysDictClientApiFeignClient.getSysDictItemMap(cairoOAuthClientSdkService.getHeaderAuthorization(),dictIds);
			return Optional.ofNullable(sysDictItemMap.getBody()).map(BusinessResult::getData).orElse(Collections.emptyMap());
		} catch (Exception e) {
			log.info("getSysDictItemMap：", e);
			throw new ConflictBusinessException("字典查询失败");
		}
	}

	@Override
	public Map<String, Map<String, SysDictItem>> getSysDictItemIdMap(Map<String, Set<String>> dictItemIds) {
		try {
			ResponseEntity<BusinessResult<Map<String, Map<String, SysDictItem>>>> sysDictItemIdMap = sysDictClientApiFeignClient.getSysDictItemIdMap(cairoOAuthClientSdkService.getHeaderAuthorization(),dictItemIds);
			return Optional.ofNullable(sysDictItemIdMap.getBody()).map(BusinessResult::getData).orElse(Collections.emptyMap());
		} catch (Exception e) {
			log.info("getSysDictItemIdMap：", e);
			throw new ConflictBusinessException("获取系统级字典部分字典项信息失败");
		}
	}

	@Override
	public SysDict getSysDictDetailInfo(GetSysDictInfoArgs args) {
		try {
			ResponseEntity<BusinessResult<SysDict>> sysDictDetailInfo = sysDictClientApiFeignClient.getSysDictDetailInfo(cairoOAuthClientSdkService.getHeaderAuthorization(),args);
			return Optional.ofNullable(sysDictDetailInfo.getBody()).map(BusinessResult::getData).orElse(SysDict.builder().build());
		} catch (Exception e) {
			log.info("getSysDictItemIdMap：", e);
			throw new ConflictBusinessException("获取系统级字典详细信息失败");
		}
	}

	@Override
	public Map<String, Map<String, PathSysDict>> getPathSysDictItemIdMap(Map<String, Set<String>> dictIdMap) {
		try {
			ResponseEntity<BusinessResult<Map<String, Map<String, PathSysDict>>>> pathSysDictItemIdMap = sysDictClientApiFeignClient.getPathSysDictItemIdMap(cairoOAuthClientSdkService.getHeaderAuthorization(),dictIdMap);
			return Optional.ofNullable(pathSysDictItemIdMap.getBody()).map(BusinessResult::getData).orElse(Collections.emptyMap());
		} catch (Exception e) {
			log.info("getPathSysDictItemIdMap：", e);
			throw new ConflictBusinessException("获多级系统级字典项部分信息map失败");
		}
	}

	@Override
	public Map<String, Map<String, PathSysDict>> getPathSysDictItemMap(Set<String> dictIds) {
		try {
			ResponseEntity<BusinessResult<Map<String, Map<String, PathSysDict>>>> pathSysDictItemMap = sysDictClientApiFeignClient.getPathSysDictItemMap(cairoOAuthClientSdkService.getHeaderAuthorization(),dictIds);
			return Optional.ofNullable(pathSysDictItemMap.getBody()).map(BusinessResult::getData).orElse(Collections.emptyMap());
		} catch (Exception e) {
			log.info("getPathSysDictItemMap：", e);
			throw new ConflictBusinessException("获取多级系统级字典项map失败");
		}
	}

	@Override
	public List<SysDictItem> getSysDictSubItemList(String dictId, String parentItemId) {
		try {
			ResponseEntity<BusinessResult<List<SysDictItem>>> sysDictSubItemList = sysDictClientApiFeignClient.getSysDictSubItemList(cairoOAuthClientSdkService.getHeaderAuthorization(), GetSysDictSubItemArgs.builder().dictId(dictId).parentItemId(parentItemId).build());
			return Optional.ofNullable(sysDictSubItemList.getBody()).map(BusinessResult::getData).orElse(Collections.emptyList());
		} catch (Exception e) {
			log.info("getSysDictSubItemList：", e);
			throw new ConflictBusinessException("查询字典项列表失败");
		}
	}
}
