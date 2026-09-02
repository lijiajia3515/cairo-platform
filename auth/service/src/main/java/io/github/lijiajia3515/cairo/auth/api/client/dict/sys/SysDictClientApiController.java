package io.github.lijiajia3515.cairo.auth.api.client.dict.sys;

import io.github.lijiajia3515.cairo.auth.framework.context.CairoContext;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthClientPrincipal;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthTenantAppUserPrincipal;
import io.github.lijiajia3515.cairo.auth.domain.api.client.dict.sys.GetSysDictInfoArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.dict.sys.GetSysDictSubItemArgs;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurity;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityType;
import io.github.lijiajia3515.cairo.auth.domain.dto.dict.sys.PathSysDict;
import io.github.lijiajia3515.cairo.auth.domain.dto.dict.sys.SysDict;
import io.github.lijiajia3515.cairo.auth.domain.dto.dict.sys.SysDictItem;
import io.github.lijiajia3515.cairo.web.bind.annotation.BusinessResultBody;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * client-api dict api
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/client_api/sys_dict")
@CairoSecurity(type = CairoSecurityType.CLIENT)
@BusinessResultBody
@RequiredArgsConstructor
public class SysDictClientApiController {
	private final SysDictClientApiService service;

	/**
	 * 获取系统级字典项map
	 *
	 * @param principal     principal
	 * @param dictIds 字典项ID
	 * @return system dict item map
	 */
	@PostMapping("/get_sys_dict_item_map")
	@PreAuthorize("hasAnyAuthority('sys_dict:all', 'sys_dict:read')")
	public Map<String, ? extends Map<String, SysDictItem>> getSysDictItemMap(@AuthenticationPrincipal CairoOAuthClientPrincipal principal,
																							   @Valid @NotNull @RequestBody Set<String> dictIds) {
		String appId = principal.getAppId();
		if (dictIds.isEmpty()) return Collections.emptyMap();
		return service.getSysDictMap(appId, dictIds);
	}

	/**
	 * 获取系统级字典部分字典项信息
	 *
	 * @param principal       principal
	 * @param dictIdMap args
	 * @return system dict item map
	 */
	@PostMapping("/get_sys_dict_item_id_map")
	@PreAuthorize("hasAnyAuthority('sys_dict:all', 'sys_dict:read')")
	public Map<String, ? extends Map<String, SysDictItem>> getSysDictItemIdMap(@AuthenticationPrincipal CairoOAuthClientPrincipal principal,
																								 @Valid @NotNull @RequestBody Map<String, Set<String>> dictIdMap) {
		String appId = principal.getAppId();
		if (dictIdMap.isEmpty()) return Collections.emptyMap();
		return service.getSysDictItemIdMap(appId, dictIdMap);
	}

	/**
	 * 获取系统级字典详细信息
	 *
	 * @param principal principal
	 * @param args      args
	 * @return system dict detail info
	 */
	@PostMapping("/get_sys_dict_detail_info")
	@PreAuthorize("hasAnyAuthority('sys_dict:all', 'sys_dict:read')")
	public Optional<SysDict> getSysDictDetailInfo(@AuthenticationPrincipal CairoOAuthClientPrincipal principal,
																	@Validated @RequestBody GetSysDictInfoArgs args) {
		String appId = principal.getAppId();
		return Optional.ofNullable(service.getSysDictDetailInfo(appId, args.getDictId(),args.getItemEnabled()));
	}


	/**
	 * 获多级系统级字典项部分信息map
	 *
	 * @param principal principal
	 * @return 字典详情
	 */
	@PostMapping("/get_path_sys_dict_item_id_map")
	@PreAuthorize("hasAnyAuthority('sys_dict:all', 'sys_dict:read')")
	@CairoContext
	public Map<String, Map<String, PathSysDict>> getPathSysDictItemIdMap(@AuthenticationPrincipal CairoOAuthClientPrincipal principal,
																						   @Valid @NotNull @RequestBody Map<String, Set<String>> dictIdMap) {
		String appId = principal.getAppId();
		return service.getPathSysDictItemIdMap(appId, dictIdMap);
	}


	/**
	 * 获取多级系统级字典项map
	 *
	 * @param principal     principal
	 * @param dictIds 字典项ID
	 * @return system dict item map
	 */
	@PostMapping("/get_path_sys_dict_item_map")
	@PreAuthorize("hasAnyAuthority('sys_dict:all', 'sys_dict:read')")
	public Map<String, Map<String, PathSysDict>> getPathSysDictItemMap(@AuthenticationPrincipal CairoOAuthClientPrincipal principal,
																				   @Valid @NotNull @RequestBody Set<String> dictIds) {
		String appId = principal.getAppId();
		if (dictIds.isEmpty()) return Collections.emptyMap();
		return service.getPathSysDictMap(appId, dictIds);
	}

	/**
	 * 获系统级字典信息
	 *
	 * @param principal principal
	 * @param args      args
	 * @return 字典详情
	 */
	@PostMapping("/get_sys_dict_sub_item_list")
	@PreAuthorize("hasAnyAuthority('sys_dict:all', 'sys_dict:read')")
	public List<SysDictItem> getSysDictSubItemList(@AuthenticationPrincipal CairoOAuthClientPrincipal principal,
																	 @Validated @RequestBody GetSysDictSubItemArgs args) {
		return service.getSysDictSubItemList(principal.getAppId(), args);
	}

}
