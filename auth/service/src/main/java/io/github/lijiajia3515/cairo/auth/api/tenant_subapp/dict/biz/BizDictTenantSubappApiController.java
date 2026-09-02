package io.github.lijiajia3515.cairo.auth.api.tenant_subapp.dict.biz;

import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthTenantSubappUserPrincipal;
import io.github.lijiajia3515.cairo.core.page.Page;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.dict.biz.DeleteBizDictItemArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.dict.biz.GetBizDictArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.dict.biz.GetBizDictDetailArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.dict.biz.GetBizDictInfoArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.dict.biz.GetBizDictItemInfoArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.dict.biz.GetBizDictItemPageInfoArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.dict.biz.GetBizDictSubItemArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.dict.biz.ModifyBizDictItemIconArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.dict.biz.ModifyBizDictItemInfoArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.dict.biz.ModifyBizDictItemStatusArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.dict.biz.PutBizDictItemArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.dict.biz.RestoreBizDictArgs;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurity;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityType;
import io.github.lijiajia3515.cairo.auth.domain.dto.dict.biz.BizDict;
import io.github.lijiajia3515.cairo.auth.domain.dto.dict.biz.MetadataBizDict;
import io.github.lijiajia3515.cairo.auth.domain.dto.dict.biz.MetadataBizDictItem;
import io.github.lijiajia3515.cairo.web.bind.annotation.BusinessResultBody;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

/**
 * [tenant_subapp_user/api]tenant app subapp biz dict api
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/tenant_subapp_user_api/biz_dict")
@CairoSecurity(type = CairoSecurityType.TENANT_SUBAPP_USER)
@BusinessResultBody
@RequiredArgsConstructor
public class BizDictTenantSubappApiController {
	private final BizDictTenantSubappApiService bizDictTenantSubappApiService;

	/**
	 * 获取业务级字典列表
	 *
	 * @param principal principal
	 * @param args      args
	 * @return 字典列表
	 */
	@PostMapping("/get_biz_dict_list")
	@PreAuthorize("hasAnyAuthority('app_admin', 'biz_dict:all', 'biz_dict:read')")
	public List<MetadataBizDict> getBizDictList(@AuthenticationPrincipal CairoOAuthTenantSubappUserPrincipal principal,
															@Validated @RequestBody GetBizDictArgs args) {
		String tenantId = principal.getTenantId();
		String appId = principal.getAppId();
		return bizDictTenantSubappApiService.getBizDictList(tenantId, appId, args);
	}

	/**
	 * 获业务级字典分页列表
	 *
	 * @param principal principal
	 * @param args      args
	 * @return 字典列表
	 */
	@PostMapping("/get_biz_dict_page_list")
	@PreAuthorize("hasAnyAuthority('app_admin', 'biz_dict:all', 'biz_dict:read')")
	public Page<MetadataBizDict> getBizDictPageList(@AuthenticationPrincipal CairoOAuthTenantSubappUserPrincipal principal,
																@Validated @RequestBody GetBizDictArgs args) {
		String tenantId = principal.getTenantId();
		String appId = principal.getAppId();
		return bizDictTenantSubappApiService.getBizDictPageList(tenantId, appId, args);
	}

	/**
	 * 获取业务级字典列表
	 *
	 * @param principal principal
	 * @param args      args
	 * @return 字典列表
	 */
	@PostMapping("/get_biz_dict_detail_list")
	@PreAuthorize("hasAnyAuthority('app_admin', 'biz_dict:all', 'biz_dict:read')")
	public List<BizDict> getBizDictList(@AuthenticationPrincipal CairoOAuthTenantSubappUserPrincipal principal,
													@Validated @RequestBody GetBizDictDetailArgs args) {
		String tenantId = principal.getTenantId();
		String appId = principal.getAppId();
		return bizDictTenantSubappApiService.getBizDictDetailList(tenantId, appId, args);
	}

	/**
	 * 获业务级字典信息
	 *
	 * @param principal principal
	 * @param args      args
	 * @return 字典详情
	 */
	@PostMapping("/get_biz_dict_info")
	@PreAuthorize("hasAnyAuthority('app_admin', 'biz_dict:all', 'biz_dict:read')")
	public Optional<MetadataBizDict> getBizDictInfo(@AuthenticationPrincipal CairoOAuthTenantSubappUserPrincipal principal,
																@Validated @RequestBody GetBizDictInfoArgs args) {
		String tenantId = principal.getTenantId();
		String appId = principal.getAppId();
		return Optional.ofNullable(bizDictTenantSubappApiService.getBizDictInfo(tenantId, appId, args.getDictId()));
	}

	/**
	 * 获业务级字典信息
	 *
	 * @param principal principal
	 * @param args      args
	 * @return 字典详情
	 */
	@PostMapping("/get_biz_dict_detail_info")
	@PreAuthorize("hasAnyAuthority('app_admin', 'biz_dict:all', 'biz_dict:read')")
	public Optional<MetadataBizDict> getBizDictDetailInfo(@AuthenticationPrincipal CairoOAuthTenantSubappUserPrincipal principal,
																	  @Validated @RequestBody GetBizDictInfoArgs args) {
		String tenantId = principal.getTenantId();
		String appId = principal.getAppId();
		return Optional.ofNullable(bizDictTenantSubappApiService.getBizDictDetailInfo(tenantId, appId, args.getDictId()));
	}

	/**
	 * 获业务级字典项信息
	 *
	 * @param principal principal
	 * @param args      args
	 * @return 字典详情
	 */
	@PostMapping("/get_biz_dict_item_info")
	@PreAuthorize("hasAnyAuthority('app_admin', 'biz_dict:all', 'biz_dict:read')")
	public List<MetadataBizDictItem> getBizDictItemInfo(@AuthenticationPrincipal CairoOAuthTenantSubappUserPrincipal principal,
																	@Validated @RequestBody GetBizDictItemInfoArgs args) {
		String tenantId = principal.getTenantId();
		String appId = principal.getAppId();
		return bizDictTenantSubappApiService.getBizDictItemInfo(tenantId, appId, args);
	}


	/**
	 * 获业务级字典信息
	 *
	 * @param principal principal
	 * @param args      args
	 * @return 字典详情
	 */
	@PostMapping("/get_biz_dict_sub_item_list")
	@PreAuthorize("hasAnyAuthority('app_admin', 'biz_dict:all', 'biz_dict:read')")
	public List<MetadataBizDictItem> getBizDictSubItemList(@AuthenticationPrincipal CairoOAuthTenantSubappUserPrincipal principal,
																	   @Validated @RequestBody GetBizDictSubItemArgs args) {
		String tenantId = principal.getTenantId();
		String appId = principal.getAppId();
		return bizDictTenantSubappApiService.getBizDictSubItemList(tenantId, appId, args);
	}


	/**
	 * 获业务级字典项分页信息
	 *
	 * @param principal principal
	 * @param args      args
	 * @return 字典详情
	 */
	@PostMapping("/get_biz_dict_item_page_list")
	@PreAuthorize("hasAnyAuthority('app_admin', 'biz_dict:all', 'biz_dict:read')")
	public Page<MetadataBizDictItem> getBizDictItemPageList(@AuthenticationPrincipal CairoOAuthTenantSubappUserPrincipal principal,
																		@Validated @RequestBody GetBizDictItemPageInfoArgs args) {
		String tenantId = principal.getTenantId();
		String appId = principal.getAppId();
		return bizDictTenantSubappApiService.getBizDictItemPageList(tenantId, appId, args);
	}


	/**
	 * 获业务级字典信息
	 *
	 * @param principal principal
	 * @param args      args
	 * @return 字典详情
	 */
	@PostMapping("/get_biz_dict_sub_item_tree_list")
	@PreAuthorize("hasAnyAuthority('app_admin', 'biz_dict:all', 'biz_dict:read')")
	public List<MetadataBizDictItem> getBizDictSubItemTreeList(@AuthenticationPrincipal CairoOAuthTenantSubappUserPrincipal principal,
																		   @Validated @RequestBody GetBizDictSubItemArgs args) {
		String tenantId = principal.getTenantId();
		String appId = principal.getAppId();
		return bizDictTenantSubappApiService.getBizDictSubItemTreeList(tenantId, appId, args);
	}


	/**
	 * 添加业务级字典项
	 *
	 * @param principal principal
	 * @param args      args
	 * @return empty
	 */
	@PostMapping("/put_biz_dict_item")
	@PreAuthorize("hasAnyAuthority('app_admin', 'biz_dict:all', 'biz_dict:put_biz_dict_item')")
	public Optional<String> putBizDictItem(@AuthenticationPrincipal CairoOAuthTenantSubappUserPrincipal principal,
												 @Validated @RequestBody PutBizDictItemArgs args) {
		String tenantId = principal.getTenantId();
		String appId = principal.getAppId();
		bizDictTenantSubappApiService.putBizDictItem(tenantId, appId, args);
		return Optional.empty();
	}

	/**
	 * 修改业务级字典项信息
	 *
	 * @param principal principal
	 * @param args      args
	 * @return empty
	 */
	@PostMapping("/modify_biz_dict_item_info")
	@PreAuthorize("hasAnyAuthority('app_admin', 'biz_dict:all', 'biz_dict:modify_biz_dict_item_info')")
	public Optional<String> modifyBizDictItemInfo(@AuthenticationPrincipal CairoOAuthTenantSubappUserPrincipal principal,
														@Validated @RequestBody ModifyBizDictItemInfoArgs args) {
		String tenantId = principal.getTenantId();
		String appId = principal.getAppId();
		bizDictTenantSubappApiService.modifyBizDictItemInfo(tenantId, appId, args);
		return Optional.empty();
	}

	/**
	 * 修改业务级字典项图标
	 *
	 * @param principal principal
	 * @param args      args
	 * @return empty
	 */
	@PostMapping("/modify_biz_dict_item_icon")
	@PreAuthorize("hasAnyAuthority('app_admin', 'biz_dict:all', 'biz_dict:modify_biz_dict_item_icon')")
	public Optional<String> modifyBizDictItemInfo(@AuthenticationPrincipal CairoOAuthTenantSubappUserPrincipal principal,
														@Validated @RequestBody ModifyBizDictItemIconArgs args) {
		String tenantId = principal.getTenantId();
		String appId = principal.getAppId();
		bizDictTenantSubappApiService.modifyBizDictItemIcon(tenantId, appId, args);
		return Optional.empty();
	}

	/**
	 * 修改业务级字典项状态信息
	 *
	 * @param principal principal
	 * @param args      args
	 * @return empty
	 */
	@PostMapping("/modify_biz_dict_item_status")
	@PreAuthorize("hasAnyAuthority('app_admin', 'biz_dict:all', 'biz_dict:modify_biz_dict_item_status')")
	public Optional<String> modifyBizDictItemStatus(@AuthenticationPrincipal CairoOAuthTenantSubappUserPrincipal principal,
														  @Validated @RequestBody ModifyBizDictItemStatusArgs args) {
		String tenantId = principal.getTenantId();
		String appId = principal.getAppId();
		bizDictTenantSubappApiService.modifyBizDictItemStatus(tenantId, appId, args);
		return Optional.empty();
	}


	/**
	 * 删除业务级字典项
	 *
	 * @param principal principal
	 * @param args      args
	 * @return empty
	 */
	@PostMapping("/delete_biz_dict_item")
	@PreAuthorize("hasAnyAuthority('app_admin', 'biz_dict:all', 'biz_dict:delete_biz_dict_item')")
	public Optional<String> deleteBizDictItem(@AuthenticationPrincipal CairoOAuthTenantSubappUserPrincipal principal,
													@Validated @RequestBody DeleteBizDictItemArgs args) {
		String tenantId = principal.getTenantId();
		String appId = principal.getAppId();
		bizDictTenantSubappApiService.deleteBizDictItem(tenantId, appId, args);
		return Optional.empty();
	}


	/**
	 * 恢复业务级字典
	 *
	 * @param principal principal
	 * @param args      args
	 * @return empty
	 */
	@PostMapping("/restore_biz_dict")
	@PreAuthorize("hasAnyAuthority('app_admin', 'biz_dict:all', 'biz_dict:restore_biz_dict')")
	public Optional<String> restoreBizDict(@AuthenticationPrincipal CairoOAuthTenantSubappUserPrincipal principal,
												 @Validated @RequestBody RestoreBizDictArgs args) {
		String tenantId = principal.getTenantId();
		String appId = principal.getAppId();
		bizDictTenantSubappApiService.restoreBizDict(tenantId, appId, args);
		return Optional.empty();
	}

}
