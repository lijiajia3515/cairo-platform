package io.github.lijiajia3515.cairo.auth.modules.dict.sys;

import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthTenantAppUserPrincipal;
import io.github.lijiajia3515.cairo.auth.domain.api.client.dict.sys.GetSysDictInfoArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.dict.sys.PathSysDict;
import io.github.lijiajia3515.cairo.auth.domain.dto.dict.sys.SysDict;
import io.github.lijiajia3515.cairo.auth.domain.dto.dict.sys.SysDictItem;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface SysDictClientApiService {

	/**
	 * 字典查询
	 *
	 * @param dictIds 字典ids
	 * @return system dict item map
	 */
	Map<String, Map<String, SysDictItem>> getSysDictItemMap(Set<String> dictIds);

	/**
	 * 获取系统级字典部分字典项信息
	 *
	 * @param dictItemIds args
	 * @return system dict item map
	 */
	@PostMapping("/get_sys_dict_item_id_map")
	Map<String, Map<String, SysDictItem>> getSysDictItemIdMap(Map<String, Set<String>> dictItemIds);


	/**
	 * 获取系统级字典详细信息(单个)
	 *
	 * @param args args
	 * @return map 字典
	 */
	SysDict getSysDictDetailInfo(GetSysDictInfoArgs args);


	/**
	 * 获多级系统级字典项部分信息map
	 *
	 * @return 字典详情
	 */
	Map<String, Map<String, PathSysDict>> getPathSysDictItemIdMap(Map<String, Set<String>> dictIdMap);

	/**
	 * 获取多级系统级字典项map
	 *
	 * @param dictIds 字典项ID
	 * @return system dict item map
	 */
	Map<String, Map<String, PathSysDict>> getPathSysDictItemMap(Set<String> dictIds);

	/**
	 * 获系统级字典信息
	 *
	 * @param dictId dictId
	 * @param parentItemId parentItemId
	 * @return 字典详情
	 */
	List<SysDictItem> getSysDictSubItemList(String dictId,String parentItemId);
}
