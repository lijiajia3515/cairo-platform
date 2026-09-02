package io.github.lijiajia3515.cairo.auth.api.tenant_subapp.dict.sys;

import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthTenantAppUserPrincipal;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthTenantSubappUserPrincipal;
import io.github.lijiajia3515.cairo.core.page.Page;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.dict.sys.GetSysDictArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.dict.sys.GetSysDictDetailListArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.dict.sys.GetSysDictInfoArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.dict.sys.GetSysDictItemInfoArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.dict.sys.GetSysDictSubItemArgs;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurity;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityType;
import io.github.lijiajia3515.cairo.auth.domain.dto.dict.sys.SysDict;
import io.github.lijiajia3515.cairo.auth.domain.dto.dict.sys.SysDictItem;
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
 * [tenant_subapp_user/api]tenant app subapp system dict api
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/tenant_subapp_user_api/sys_dict")
@CairoSecurity(type = CairoSecurityType.TENANT_SUBAPP_USER)
@BusinessResultBody
@RequiredArgsConstructor
public class SysDictTenantSubappApiController {
	private final SysDictTenantSubappApiService sysDictTenantSubappApiService;

	/**
	 * 获取系统级字典列表
	 *
	 * @param principal principal
	 * @param args      args
	 * @return 字典列表
	 */
	@PostMapping("/get_sys_dict_list")
	@PreAuthorize("hasAnyAuthority('app_admin', 'biz_dict:all', 'biz_dict:read')")
	public List<SysDict> getSysDictList(@AuthenticationPrincipal CairoOAuthTenantSubappUserPrincipal principal,
														  @Validated @RequestBody GetSysDictArgs args) {
		return sysDictTenantSubappApiService.getSysDictList(principal.getAppId(), args);
	}

	/**
	 * 获系统级字典分页列表
	 *
	 * @param principal principal
	 * @param args      args
	 * @return 字典列表
	 */
	@PostMapping("/get_sys_dict_page_list")
	@PreAuthorize("hasAnyAuthority('app_admin', 'biz_dict:all', 'biz_dict:read')")
	public Page<SysDict> getSysDictPageList(@AuthenticationPrincipal CairoOAuthTenantSubappUserPrincipal principal,
															  @Validated @RequestBody GetSysDictArgs args) {
		return sysDictTenantSubappApiService.getSysDictPageList(principal.getAppId(), args);
	}

	/**
	 * 获取系统级字典列表
	 *
	 * @param principal principal
	 * @param args      args
	 * @return 字典列表
	 */
	@PostMapping("/get_sys_dict_detail_list")
	@PreAuthorize("hasAnyAuthority('app_admin', 'biz_dict:all', 'biz_dict:read')")
	public List<SysDict> getSysDictDetailList(@AuthenticationPrincipal CairoOAuthTenantSubappUserPrincipal principal,
																@Validated @RequestBody GetSysDictDetailListArgs args) {
		return sysDictTenantSubappApiService.getSysDictDetailList(principal.getAppId(), args);
	}

	/**
	 * 获系统级字典信息
	 *
	 * @param principal principal
	 * @param args      args
	 * @return 字典列表
	 */
	@PostMapping("/get_sys_dict_info")
	@PreAuthorize("hasAnyAuthority('app_admin', 'biz_dict:all', 'biz_dict:read')")
	public Optional<SysDict> getSysDictInfo(@AuthenticationPrincipal CairoOAuthTenantSubappUserPrincipal principal,
															  @Validated @RequestBody GetSysDictInfoArgs args) {
		return Optional.ofNullable(sysDictTenantSubappApiService.getSysDictInfo(principal.getAppId(), args.getDictId()));
	}

	/**
	 * 获系统级字典详细信息
	 *
	 * @param principal principal
	 * @param args      args
	 * @return 字典详情
	 */
	@PostMapping("/get_sys_dict_detail_info")
	@PreAuthorize("hasAnyAuthority('app_admin', 'biz_dict:all', 'biz_dict:read')")
	public Optional<SysDict> getSysDictDetailInfo(@AuthenticationPrincipal CairoOAuthTenantSubappUserPrincipal principal,
																	@Validated @RequestBody GetSysDictInfoArgs args) {
		return Optional.ofNullable(sysDictTenantSubappApiService.getSysDictDetailInfo(principal.getAppId(), args.getDictId()));
	}

	/**
	 * 获系统级字典项信息
	 *
	 * @param principal principal
	 * @param args      args
	 * @return 字典详情
	 */
	@PostMapping("/get_sys_dict_item_info")
	@PreAuthorize("hasAnyAuthority('app_admin', 'biz_dict:all', 'biz_dict:read')")
	public List<SysDictItem> getSysDictItemInfo(@AuthenticationPrincipal CairoOAuthTenantSubappUserPrincipal principal,
																  @Validated @RequestBody GetSysDictItemInfoArgs args) {
		return sysDictTenantSubappApiService.getSysDictItemInfo(principal.getAppId(), args);
	}


	/**
	 * 获系统级字典信息
	 *
	 * @param principal principal
	 * @param args      args
	 * @return 字典详情
	 */
	@PostMapping("/get_sys_dict_sub_item_list")
	@PreAuthorize("hasAnyAuthority('app_admin', 'biz_dict:all', 'biz_dict:read')")
	public List<SysDictItem> getSysDictSubItemList(@AuthenticationPrincipal CairoOAuthTenantAppUserPrincipal principal,
																	 @Validated @RequestBody GetSysDictSubItemArgs args) {
		return sysDictTenantSubappApiService.getSysDictSubItemList(principal.getAppId(), args);
	}

	/**
	 * 获系统级字典信息
	 *
	 * @param principal principal
	 * @param args      args
	 * @return 字典详情
	 */
	@PostMapping("/get_sys_dict_sub_item_tree_list")
	@PreAuthorize("hasAnyAuthority('app_admin', 'biz_dict:all', 'biz_dict:read')")
	public List<SysDictItem> getSysDictSubItemTreeList(@AuthenticationPrincipal CairoOAuthTenantSubappUserPrincipal principal,
																		 @Validated @RequestBody GetSysDictSubItemArgs args) {
		return sysDictTenantSubappApiService.getSysDictSubItemTreeList(principal.getAppId(), args);
	}
}
