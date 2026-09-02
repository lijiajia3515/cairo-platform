package io.github.lijiajia3515.cairo.auth.api.client.dict.biz;


import io.github.lijiajia3515.cairo.auth.framework.context.CairoContext;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthClientPrincipal;
import io.github.lijiajia3515.cairo.auth.domain.api.client.dict.biz.GetBizDictInfoArgs;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurity;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityType;
import io.github.lijiajia3515.cairo.auth.domain.dto.dict.biz.BizDict;
import io.github.lijiajia3515.cairo.auth.domain.dto.dict.biz.BizDictItem;
import io.github.lijiajia3515.cairo.auth.domain.dto.dict.biz.PathBizDict;
import io.github.lijiajia3515.cairo.web.bind.annotation.BusinessResultBody;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;


@Slf4j
@Validated
@RestController
@RequestMapping("/client_api/biz_dict")
@CairoSecurity(type = CairoSecurityType.CLIENT)
@BusinessResultBody
public class BizDictClientApiController {

	private final BizDictClientApiService bizDictClientApiService;


	public BizDictClientApiController(BizDictClientApiService bizDictClientApiService) {
		this.bizDictClientApiService = bizDictClientApiService;
	}
	/**
	 * 获取业务级字典项map
	 *
	 * @param principal     principal
	 * @param dictIds 字典项ID
	 * @return biz dict item map
	 */
	@PostMapping("/get_biz_dict_item_map")
	@PreAuthorize("hasAnyAuthority('biz_dict:all', 'biz_dict:read')")
	public Map<String, ? extends Map<String, BizDictItem>> getBizDictItemMap(@AuthenticationPrincipal CairoOAuthClientPrincipal principal,
																						 @RequestParam(name = "tenant_id", required = false) String tenantId,
																						 @Valid @NotNull @RequestBody Set<String> dictIds) {
		String appId = principal.getAppId();
		if (dictIds.isEmpty()) return Collections.emptyMap();
		return bizDictClientApiService.getBizDictMap(tenantId,appId, dictIds);
	}

	/**
	 * 获取业务级字典部分字典项信息
	 *
	 * @param principal       principal
	 * @param dictIdMap args
	 * @return biz dict item map
	 */
	@PostMapping("/get_biz_dict_item_id_map")
	@PreAuthorize("hasAnyAuthority('biz_dict:all', 'biz_dict:read')")
	public Map<String, ? extends Map<String, BizDictItem>> getBizDictItemIdMap(@AuthenticationPrincipal CairoOAuthClientPrincipal principal,
																						   @RequestParam(name = "tenant_id", required = false) String tenantId,
																						   @Valid @NotNull @RequestBody Map<String, Set<String>> dictIdMap) {
		String appId = principal.getAppId();
		if (dictIdMap.isEmpty()) return Collections.emptyMap();
		return bizDictClientApiService.getBizDictItemIdMap(tenantId,appId, dictIdMap);
	}


	/**
	 * 获多级业务级字典项部分信息map
	 *
	 * @param principal principal
	 * @return 字典详情
	 */
	@PostMapping("/get_path_biz_dict_item_id_map")
	@PreAuthorize("hasAnyAuthority('biz_dict:all', 'biz_dict:read')")
	@CairoContext
	public Map<String, Map<String, PathBizDict>> getPathBizDictItemIdMap(@AuthenticationPrincipal CairoOAuthClientPrincipal principal,
																					 @RequestParam(name = "tenant_id", required = false) String tenantId,
																					 @Valid @NotNull @RequestBody Map<String, Set<String>> dictIdMap) {
		String appId = principal.getAppId();
		return bizDictClientApiService.getPathBizDictItemIdMap(tenantId, appId, dictIdMap);
	}


	/**
	 * 获取多级业务级字典项map
	 *
	 * @param principal     principal
	 * @param dictIds 字典项ID
	 * @return biz dict item map
	 */
	@PostMapping("/get_path_biz_dict_item_map")
	@PreAuthorize("hasAnyAuthority('biz_dict:all', 'biz_dict:read')")
	public Map<String, Map<String, PathBizDict>> getPathBizDictItemMap(@AuthenticationPrincipal CairoOAuthClientPrincipal principal,
																							@RequestParam(name = "tenant_id", required = false) String tenantId,
																							@Valid @NotNull @RequestBody Set<String> dictIds) {
		String appId = principal.getAppId();
		if (dictIds.isEmpty()) return Collections.emptyMap();
		return bizDictClientApiService.getPathBizDictMap(tenantId,appId, dictIds);
	}


	/**
	 * 获业务级字典信息
	 *
	 * @param principal principal
	 * @param args      args
	 * @return 字典详情
	 */
	@PostMapping("/get_biz_dict_detail_info")
	@PreAuthorize("hasAnyAuthority('biz_dict:all', 'biz_dict:read')")
	public Optional<BizDict> getBizDictDetailInfo(@AuthenticationPrincipal CairoOAuthClientPrincipal principal,
															  @RequestParam(name = "tenant_id", required = false) String tenantId,
															  @Validated @RequestBody GetBizDictInfoArgs args) {
		String appId = principal.getAppId();
		return Optional.ofNullable(bizDictClientApiService.getBizDictDetailInfo(tenantId,appId, args.getDictId()));
	}

}
