package io.github.lijiajia3515.cairo.auth.api.subapp.dict.sys;

import io.github.lijiajia3515.cairo.auth.framework.context.CairoContext;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthSubappUserPrincipal;
import io.github.lijiajia3515.cairo.core.page.Page;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.dict.sys.CopySysDictByDictIdArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.dict.sys.DeleteSysDictArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.dict.sys.DeleteSysDictItemArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.dict.sys.GetSysDictArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.dict.sys.GetSysDictDetailListArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.dict.sys.GetSysDictInfoArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.dict.sys.GetSysDictItemInfoArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.dict.sys.GetSysDictItemPageInfoArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.dict.sys.GetSysDictSubItemArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.dict.sys.ModifyAppUserSysDictArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.dict.sys.ModifyAppUserSysDictIconArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.dict.sys.ModifyAppUserSysDictItemIconArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.dict.sys.ModifyAppUserSysDictItemInfoArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.dict.sys.ModifySysDictItemStatusArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.dict.sys.MoveSysDictItemArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.dict.sys.PutAppUserSysDictItemArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.dict.sys.SyncSysDictArgs;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurity;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityType;
import io.github.lijiajia3515.cairo.auth.domain.dto.dict.sys.MetadataSysDict;
import io.github.lijiajia3515.cairo.auth.domain.dto.dict.sys.MetadataSysDictItem;
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
 * [subapp_user/api] system dict api
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/subapp_user_api/sys_dict")
@CairoSecurity(type = CairoSecurityType.SUBAPP_USER)
@BusinessResultBody
@RequiredArgsConstructor
public class SysDictSubappApiController {
	private final SysDictSubappApiService sysDictSubappApiService;

	/**
	 * 获取系统级字典列表
	 *
	 * @param principal principal
	 * @param args      args
	 * @return 字典列表
	 */
	@PostMapping("/get_sys_dict_list")
	@PreAuthorize("hasAnyAuthority('app_admin', 'sys_dict:all', 'sys_dict:read')")
	public List<MetadataSysDict> getSysDictList(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
																  @Validated @RequestBody GetSysDictArgs args) {
		return sysDictSubappApiService.getSysDictList(principal.getAppId(), args);
	}

	/**
	 * 获系统级字典分页列表
	 *
	 * @param principal principal
	 * @param args      args
	 * @return 字典列表
	 */
	@PostMapping("/get_sys_dict_page_list")
	@PreAuthorize("hasAnyAuthority('app_admin', 'sys_dict:all', 'sys_dict:read')")
	public Page<MetadataSysDict> getSysDictPageList(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
																	  @Validated @RequestBody GetSysDictArgs args) {
		return sysDictSubappApiService.getSysDictPageList(principal.getAppId(), args);
	}

	/**
	 * 获取系统级字典列表
	 *
	 * @param principal principal
	 * @param args      args
	 * @return 字典列表
	 */
	@PostMapping("/get_sys_dict_detail_list")
	@PreAuthorize("hasAnyAuthority('app_admin', 'sys_dict:all', 'sys_dict:read')")
	public List<SysDict> getSysDictDetailList(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
																@Validated @RequestBody GetSysDictDetailListArgs args) {
		return sysDictSubappApiService.getSysDictDetailList(principal.getAppId(), args);
	}

	/**
	 * 获系统级字典信息
	 *
	 * @param principal principal
	 * @param args      args
	 * @return 字典列表
	 */
	@PostMapping("/get_sys_dict_info")
	@PreAuthorize("hasAnyAuthority('app_admin', 'sys_dict:all', 'sys_dict:read')")
	public Optional<SysDict> getSysDictInfo(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
															  @Validated @RequestBody GetSysDictInfoArgs args) {
		return Optional.ofNullable(sysDictSubappApiService.getSysDictInfo(principal.getAppId(), args.getDictId()));
	}

	/**
	 * 获系统级字典详细信息
	 *
	 * @param principal principal
	 * @param args      args
	 * @return 字典详情
	 */
	@PostMapping("/get_sys_dict_detail_info")
	@PreAuthorize("hasAnyAuthority('app_admin', 'sys_dict:all', 'sys_dict:read')")
	public Optional<SysDict> getSysDictDetailInfo(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
																	@Validated @RequestBody GetSysDictInfoArgs args) {
		return Optional.ofNullable(sysDictSubappApiService.getSysDictDetailInfo(principal.getAppId(), args.getDictId(), args.getItemEnabled()));
	}

	/**
	 * 获系统级字典项信息
	 *
	 * @param principal principal
	 * @param args      args
	 * @return 字典详情
	 */
	@PostMapping("/get_sys_dict_item_info")
	@PreAuthorize("hasAnyAuthority('app_admin', 'sys_dict:all', 'sys_dict:read')")
	public List<SysDictItem> getSysDictItemInfo(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
																  @Validated @RequestBody GetSysDictItemInfoArgs args) {
		return sysDictSubappApiService.getSysDictItemInfo(principal.getAppId(), args);
	}


	/**
	 * 获系统级字典信息
	 *
	 * @param principal principal
	 * @param args      args
	 * @return 字典详情
	 */
	@PostMapping("/get_sys_dict_sub_item_list")
	@PreAuthorize("hasAnyAuthority('app_admin', 'sys_dict:all', 'sys_dict:read')")
	public List<SysDictItem> getSysDictSubItemList(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
																	 @Validated @RequestBody GetSysDictSubItemArgs args) {
		return sysDictSubappApiService.getSysDictSubItemList(principal.getAppId(), args);
	}


	/**
	 * 获系统级字典项分页信息
	 *
	 * @param principal principal
	 * @param args      args
	 * @return 字典详情
	 */
	@PostMapping("/get_sys_dict_item_page_list")
	@PreAuthorize("hasAnyAuthority('app_admin', 'sys_dict:all', 'sys_dict:read')")
	@CairoContext
	public Page<MetadataSysDictItem> getSysDictItemPageList(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
																			  @Validated @RequestBody GetSysDictItemPageInfoArgs args) {
		String appId = principal.getAppId();
		return sysDictSubappApiService.getSysDictItemPageList(appId, args);
	}


	/**
	 * 获系统级字典树结构信息
	 *
	 * @param principal principal
	 * @param args      args
	 * @return 字典详情
	 */
	@PostMapping("/get_sys_dict_sub_item_tree_list")
	@PreAuthorize("hasAnyAuthority('app_admin', 'sys_dict:all', 'sys_dict:read')")
	public List<SysDictItem> getSysDictSubItemTreeList(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
																		 @Validated @RequestBody GetSysDictSubItemArgs args) {
		return sysDictSubappApiService.getSysDictSubItemTreeList(principal.getAppId(), args);
	}


	/**
	 * 修改系统级字典 接口
	 *
	 * @param principal principal
	 * @param args      args
	 * @return empty
	 */
	@PostMapping("/modify_sys_dict_info")
	@PreAuthorize("hasAnyAuthority('app_admin', 'sys_dict:all', 'sys_dict:modify_dict_info')")
	@CairoContext
	public Optional<String> modifySysDictInfo(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
													   @Validated @RequestBody ModifyAppUserSysDictArgs args) {
		String appId = principal.getAppId();
		sysDictSubappApiService.modifySysDictInfo(appId, args);
		return Optional.empty();
	}

	/**
	 * 修改系统级字典图标 接口
	 *
	 * @param principal principal
	 * @param args      args
	 * @return empty
	 */
	@PostMapping("/modify_sys_dict_icon")
	@PreAuthorize("hasAnyAuthority('app_admin', 'sys_dict:all', 'sys_dict:modify_dict_icon')")
	@CairoContext
	public Optional<String> modifySysDictIcon(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
													   @Validated @RequestBody ModifyAppUserSysDictIconArgs args) {
		String appId = principal.getAppId();
		sysDictSubappApiService.modifySysDictIcon(appId, args);
		return Optional.empty();
	}


	/**
	 * 添加系统级字典项
	 *
	 * @param principal principal
	 * @param args      args
	 * @return empty
	 */
	@PostMapping("/put_sys_dict_item")
	@PreAuthorize("hasAnyAuthority('app_admin', 'sys_dict:all', 'sys_dict:put_sys_dict_item')")
	@CairoContext
	public Optional<String> putSysDictItem(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
													@Validated @RequestBody PutAppUserSysDictItemArgs args) {
		String appId = principal.getAppId();
		sysDictSubappApiService.putSysDictItem(appId, args);
		return Optional.empty();
	}

	/**
	 * 修改系统级字典项信息
	 *
	 * @param principal principal
	 * @param args      args
	 * @return empty
	 */
	@PostMapping("/modify_sys_dict_item_info")
	@PreAuthorize("hasAnyAuthority('app_admin', 'sys_dict:all', 'sys_dict:modify_sys_dict_item_info')")
	@CairoContext
	public Optional<String> modifySysDictItemInfo(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
														   @Validated @RequestBody ModifyAppUserSysDictItemInfoArgs args) {
		String appId = principal.getAppId();
		sysDictSubappApiService.modifySysDictItemInfo(appId, args);
		return Optional.empty();
	}

	/**
	 * 修改系统级字典项图标
	 *
	 * @param principal principal
	 * @param args      args
	 * @return empty
	 */
	@PostMapping("/modify_sys_dict_item_icon")
	@PreAuthorize("hasAnyAuthority('app_admin', 'sys_dict:all', 'sys_dict:modify_sys_dict_item_icon')")
	@CairoContext
	public Optional<String> modifySysDictItemIcon(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
														   @Validated @RequestBody ModifyAppUserSysDictItemIconArgs args) {
		String appId = principal.getAppId();
		sysDictSubappApiService.modifySysDictItemIcon(appId, args);
		return Optional.empty();
	}

	/**
	 * 修改系统级字典项状态信息
	 *
	 * @param principal principal
	 * @param args      args
	 * @return empty
	 */
	@PostMapping("/modify_sys_dict_item_status")
	@PreAuthorize("hasAnyAuthority('app_admin', 'sys_dict:all', 'sys_dict:modify_sys_dict_item_status')")
	@CairoContext
	public Optional<String> modifySysDictItemStatus(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
															 @Validated @RequestBody ModifySysDictItemStatusArgs args) {
		String appId = principal.getAppId();
		sysDictSubappApiService.modifySysDictItemStatus(appId, args);
		return Optional.empty();
	}

	/**
	 * 移动系统级字典项信息
	 *
	 * @param principal principal
	 * @param args      args
	 * @return empty
	 */
	@PostMapping("/move_sys_dict_item")
	@PreAuthorize("hasAnyAuthority('app_admin', 'sys_dict:all', 'sys_dict:move_sys_dict_item')")
	@CairoContext
	public Optional<String> moveSysDictItemInfo(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
														 @Validated @RequestBody MoveSysDictItemArgs args) {
		String appId = principal.getAppId();
		sysDictSubappApiService.moveSysDictItem(appId, args);
		return Optional.empty();
	}

	/**
	 * 删除系统级字典 接口
	 *
	 * @param args      args
	 * @return empty
	 */
	@PostMapping("/delete_sys_dict")
	@PreAuthorize("hasAnyAuthority('app_admin', 'sys_dict:all', 'sys_dict:delete_sys_dict')")
	@CairoContext
	public Optional<String> deleteSysDict(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,@Validated @RequestBody DeleteSysDictArgs args) {
		String appId = principal.getAppId();
		sysDictSubappApiService.deleteSysDict(appId, args);
		return Optional.empty();
	}

	/**
	 * 删除系统级字典项
	 *
	 * @param principal principal
	 * @param args      args
	 * @return empty
	 */
	@PostMapping("/delete_sys_dict_item")
	@PreAuthorize("hasAnyAuthority('app_admin', 'sys_dict:all', 'sys_dict:delete_sys_dict_item')")
	@CairoContext
	public Optional<String> deleteSysDictItem(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
													   @Validated @RequestBody DeleteSysDictItemArgs args) {
		String appId = principal.getAppId();
		sysDictSubappApiService.deleteSysDictItem(appId, args);
		return Optional.empty();
	}

	/**
	 * 同步系统级字典
	 *
	 * @param principal principal
	 * @return empty
	 */
	@PostMapping("/sync_sys_dict")
	@PreAuthorize("hasAnyAuthority('app_admin', 'sys_dict:all', 'sys_dict:sync_sys_dict')")
	@CairoContext
	public Optional<String> syncSysDict(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
												 @Validated @RequestBody SyncSysDictArgs args) {
		String appId = principal.getAppId();
		sysDictSubappApiService.syncSysDict(appId, args);
		return Optional.empty();
	}


	/**
	 * 根据字典复制系统级字典,字典项
	 *
	 * @return empty
	 */
	@PostMapping("/copy_sys_dict_by_dict_id")
	@PreAuthorize("hasAnyAuthority('app_admin', 'sys_dict:all', 'sys_dict:copy_sys_dict')")
	@CairoContext
	public Optional<String> copySysDictByDictId(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
															   @Validated @RequestBody CopySysDictByDictIdArgs args) {
		String appId = principal.getAppId();
		sysDictSubappApiService.copySysDictByDictId(appId, args);
		return Optional.empty();
	}

}
