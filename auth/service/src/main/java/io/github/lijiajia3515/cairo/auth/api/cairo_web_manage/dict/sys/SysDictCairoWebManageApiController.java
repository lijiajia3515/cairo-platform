package io.github.lijiajia3515.cairo.auth.api.cairo_web_manage.dict.sys;

import io.github.lijiajia3515.cairo.auth.framework.context.CairoContext;
import io.github.lijiajia3515.cairo.auth.framework.context.CairoContextConstants;
import io.github.lijiajia3515.cairo.auth.framework.context.CairoContextHolder;
import io.github.lijiajia3515.cairo.core.exception.ParamsErrorBusinessException;
import io.github.lijiajia3515.cairo.core.page.Page;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.dict.sys.CopySysDictByAppIdArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.dict.sys.CopySysDictByDictIdArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.dict.sys.CreateSysDictArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.dict.sys.DeleteSysDictArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.dict.sys.DeleteSysDictItemArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.dict.sys.GetSysDictArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.dict.sys.GetSysDictInfoArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.dict.sys.GetSysDictItemInfoArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.dict.sys.GetSysDictItemPageInfoArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.dict.sys.GetSysDictSubItemArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.dict.sys.ModifySysDictArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.dict.sys.ModifySysDictItemInfoArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.dict.sys.ModifySysDictItemStatusArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.dict.sys.MoveSysDictItemArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.dict.sys.PutSysDictItemArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.dict.sys.SyncSysDictArgs;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurity;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityType;
import io.github.lijiajia3515.cairo.auth.domain.dto.dict.sys.MetadataSysDict;
import io.github.lijiajia3515.cairo.auth.domain.dto.dict.sys.MetadataSysDictItem;
import io.github.lijiajia3515.cairo.web.bind.annotation.BusinessResultBody;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

/**
 * [cairo_web_manage/api] system dict api
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/cairo_web_manage_api/sys_dict")
@CairoSecurity(type = CairoSecurityType.CAIRO_WEB_MANAGE_USER)
@BusinessResultBody
@RequiredArgsConstructor
public class SysDictCairoWebManageApiController {
	private final SysDictCairoWebManageApiService sysDictCairoWebManageApiService;

	/**
	 * 创建系统字典 接口
	 *
	 * @param args      args
	 * @return empty
	 */
	@PostMapping("/create_sys_dict")
	@PreAuthorize("hasAnyAuthority('app_admin', 'sys_dict:all', 'sys_dict:create_sys_dict')")
	@CairoContext
	public Optional<String> createDict(@Validated @RequestBody CreateSysDictArgs args) {
		String appId = CairoContextHolder.getValue(CairoContextConstants.APP_ID).orElseThrow(() -> new ParamsErrorBusinessException("appId不能为空"));
		sysDictCairoWebManageApiService.createSysDict(appId, args);
		return Optional.empty();
	}

	/**
	 * 修改系统级字典 接口
	 *
	 * @param args      args
	 * @return empty
	 */
	@PostMapping("/modify_sys_dict_info")
	@PreAuthorize("hasAnyAuthority('app_admin', 'sys_dict:all', 'sys_dict:modify_dict_info')")
	@CairoContext
	public Optional<String> modifySysDictInfo(@Validated @RequestBody ModifySysDictArgs args) {
		String appId = CairoContextHolder.getValue(CairoContextConstants.APP_ID).orElseThrow(() -> new ParamsErrorBusinessException("appId不能为空"));
		sysDictCairoWebManageApiService.modifySysDictInfo(appId, args);
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
	public Optional<String> deleteSysDict(@Validated @RequestBody DeleteSysDictArgs args) {
		String appId = CairoContextHolder.getValue(CairoContextConstants.APP_ID).orElseThrow(() -> new ParamsErrorBusinessException("appId不能为空"));
		sysDictCairoWebManageApiService.deleteSysDict(appId, args);
		return Optional.empty();
	}

	/**
	 * 获取系统级字典列表
	 *
	 * @param args      args
	 * @return 字典列表
	 */
	@PostMapping("/get_sys_dict_list")
	@PreAuthorize("hasAnyAuthority('app_admin', 'sys_dict:all', 'sys_dict:read')")
	@CairoContext
	public List<MetadataSysDict> getSysDictList(@Validated @RequestBody GetSysDictArgs args) {
		String appId = CairoContextHolder.getValue(CairoContextConstants.APP_ID).orElseThrow(() -> new ParamsErrorBusinessException("appId不能为空"));
		return sysDictCairoWebManageApiService.getSysDictList(appId, args);
	}

	/**
	 * 获系统级字典分页列表
	 *
	 * @param args      args
	 * @return 字典列表
	 */
	@PostMapping("/get_sys_dict_page_list")
	@PreAuthorize("hasAnyAuthority('app_admin', 'sys_dict:all', 'sys_dict:read')")
	@CairoContext
	public Page<MetadataSysDict> getSysDictPageList(@Validated @RequestBody GetSysDictArgs args) {
		String appId = CairoContextHolder.getValue(CairoContextConstants.APP_ID).orElseThrow(() -> new ParamsErrorBusinessException("appId不能为空"));
		return sysDictCairoWebManageApiService.getSysDictPageList(appId, args);
	}

	/**
	 * 获系统级字典信息
	 *
	 * @param args      args
	 * @return 字典详情
	 */
	@PostMapping("/get_sys_dict_info")
	@PreAuthorize("hasAnyAuthority('app_admin', 'sys_dict:all', 'sys_dict:read')")
	@CairoContext
	public Optional<MetadataSysDict> getSysDictInfo(@Validated @RequestBody GetSysDictInfoArgs args) {
		String appId = CairoContextHolder.getValue(CairoContextConstants.APP_ID).orElseThrow(() -> new ParamsErrorBusinessException("appId不能为空"));
		return Optional.ofNullable(sysDictCairoWebManageApiService.getSysDictInfo(appId, args.getDictId()));
	}

	/**
	 * 获系统级字典信息
	 *
	 * @param args      args
	 * @return 字典详情
	 */
	@PostMapping("/get_sys_dict_detail_info")
	@PreAuthorize("hasAnyAuthority('app_admin', 'sys_dict:all', 'sys_dict:read')")
	@CairoContext
	public Optional<MetadataSysDict> getSysDictDetailInfo(@Validated @RequestBody GetSysDictInfoArgs args) {
		String appId = CairoContextHolder.getValue(CairoContextConstants.APP_ID).orElseThrow(() -> new ParamsErrorBusinessException("appId不能为空"));
		return Optional.ofNullable(sysDictCairoWebManageApiService.getSysDictDetailInfo(appId, args.getDictId(),args.getItemEnabled()));
	}

	/**
	 * 添加系统级字典项
	 *
	 * @param args      args
	 * @return empty
	 */
	@PostMapping("/put_sys_dict_item")
	@PreAuthorize("hasAnyAuthority('app_admin', 'sys_dict:all', 'sys_dict:put_sys_dict_item')")
	@CairoContext
	public Optional<String> putSysDictItem(@Validated @RequestBody PutSysDictItemArgs args) {
		String appId = CairoContextHolder.getValue(CairoContextConstants.APP_ID).orElseThrow(() -> new ParamsErrorBusinessException("appId不能为空"));
		sysDictCairoWebManageApiService.putSysDictItem(appId, args);
		return Optional.empty();
	}

	/**
	 * 修改系统级字典项信息
	 *
	 * @param args      args
	 * @return empty
	 */
	@PostMapping("/modify_sys_dict_item_info")
	@PreAuthorize("hasAnyAuthority('app_admin', 'sys_dict:all', 'sys_dict:modify_sys_dict_item_info')")
	@CairoContext
	public Optional<String> modifySysDictItemInfo(@Validated @RequestBody ModifySysDictItemInfoArgs args) {
		String appId = CairoContextHolder.getValue(CairoContextConstants.APP_ID).orElseThrow(() -> new ParamsErrorBusinessException("appId不能为空"));
		sysDictCairoWebManageApiService.modifySysDictItemInfo(appId, args);
		return Optional.empty();
	}

	/**
	 * 修改系统级字典项状态信息
	 *
	 * @param args      args
	 * @return empty
	 */
	@PostMapping("/modify_sys_dict_item_status")
	@PreAuthorize("hasAnyAuthority('app_admin', 'sys_dict:all', 'sys_dict:modify_sys_dict_item_status')")
	@CairoContext
	public Optional<String> modifySysDictItemStatus(@Validated @RequestBody ModifySysDictItemStatusArgs args) {
		String appId = CairoContextHolder.getValue(CairoContextConstants.APP_ID).orElseThrow(() -> new ParamsErrorBusinessException("appId不能为空"));
		sysDictCairoWebManageApiService.modifySysDictItemStatus(appId, args);
		return Optional.empty();
	}

	/**
	 * 移动系统级字典项信息
	 *
	 * @param args      args
	 * @return empty
	 */
	@PostMapping("/move_sys_dict_item")
	@PreAuthorize("hasAnyAuthority('app_admin', 'sys_dict:all', 'sys_dict:move_sys_dict_item')")
	@CairoContext
	public Optional<String> moveSysDictItemInfo(@Validated @RequestBody MoveSysDictItemArgs args) {
		String appId = CairoContextHolder.getValue(CairoContextConstants.APP_ID).orElseThrow(() -> new ParamsErrorBusinessException("appId不能为空"));
		sysDictCairoWebManageApiService.moveSysDictItem(appId, args);
		return Optional.empty();
	}

	/**
	 * 删除系统级字典项
	 *
	 * @param args      args
	 * @return empty
	 */
	@PostMapping("/delete_sys_dict_item")
	@PreAuthorize("hasAnyAuthority('app_admin', 'sys_dict:all', 'sys_dict:delete_sys_dict_item')")
	@CairoContext
	public Optional<String> deleteSysDictItem(@Validated @RequestBody DeleteSysDictItemArgs args) {
		String appId = CairoContextHolder.getValue(CairoContextConstants.APP_ID).orElseThrow(() -> new ParamsErrorBusinessException("appId不能为空"));
		sysDictCairoWebManageApiService.deleteSysDictItem(appId, args);
		return Optional.empty();
	}

	/**
	 * 获系统级字典项信息
	 *
	 * @param args      args
	 * @return 字典详情
	 */
	@PostMapping("/get_sys_dict_item_info")
	@PreAuthorize("hasAnyAuthority('app_admin', 'sys_dict:all', 'sys_dict:read')")
	@CairoContext
	public List<MetadataSysDictItem> getSysDictItemInfo(@Validated @RequestBody GetSysDictItemInfoArgs args) {
		String appId = CairoContextHolder.getValue(CairoContextConstants.APP_ID).orElseThrow(() -> new ParamsErrorBusinessException("appId不能为空"));
		return sysDictCairoWebManageApiService.getSysDictItemInfo(appId, args);
	}


	/**
	 * 获系统级字典项分页信息
	 *
	 * @param args      args
	 * @return 字典详情
	 */
	@PostMapping("/get_sys_dict_item_page_list")
	@PreAuthorize("hasAnyAuthority('app_admin', 'sys_dict:all', 'sys_dict:read')")
	@CairoContext
	public Page<MetadataSysDictItem> getSysDictItemPageList(@Validated @RequestBody GetSysDictItemPageInfoArgs args) {
		String appId = CairoContextHolder.getValue(CairoContextConstants.APP_ID).orElseThrow(() -> new ParamsErrorBusinessException("appId不能为空"));
		return sysDictCairoWebManageApiService.getSysDictItemPageList(appId, args);
	}



	/**
	 * 获系统级字典信息
	 *
	 * @param args      args
	 * @return 字典详情
	 */
	@PostMapping("/get_sys_dict_sub_item_list")
	@PreAuthorize("hasAnyAuthority('app_admin', 'sys_dict:all', 'sys_dict:read')")
	@CairoContext
	public List<MetadataSysDictItem> getSysDictSubItemList(@Validated @RequestBody GetSysDictSubItemArgs args) {
		String appId = CairoContextHolder.getValue(CairoContextConstants.APP_ID).orElseThrow(() -> new ParamsErrorBusinessException("appId不能为空"));
		return sysDictCairoWebManageApiService.getSysDictSubItemList(appId, args);
	}

	/**
	 * 获系统级字典信息
	 *
	 * @param args      args
	 * @return 字典详情
	 */
	@PostMapping("/get_sys_dict_sub_item_tree_list")
	@PreAuthorize("hasAnyAuthority('app_admin', 'sys_dict:all', 'sys_dict:read')")
	@CairoContext
	public List<MetadataSysDictItem> getSysDictSubItemTreeList(@Validated @RequestBody GetSysDictSubItemArgs args) {
		String appId = CairoContextHolder.getValue(CairoContextConstants.APP_ID).orElseThrow(() -> new ParamsErrorBusinessException("appId不能为空"));
		return sysDictCairoWebManageApiService.getSysDictSubItemTreeList(appId, args);
	}

	/**
	 * 同步系统级字典
	 *
	 * @return empty
	 */
	@PostMapping("/sync_sys_dict")
	@PreAuthorize("hasAnyAuthority('app_admin', 'sys_dict:all', 'sys_dict:sync_sys_dict')")
	@CairoContext
	public Optional<String> syncSysDict(@Validated @RequestBody SyncSysDictArgs args) {
		String appId = CairoContextHolder.getValue(CairoContextConstants.APP_ID).orElseThrow(() -> new ParamsErrorBusinessException("appId不能为空"));
		sysDictCairoWebManageApiService.syncSysDict(appId,args);
		return Optional.empty();
	}


	/**
	 * 根据应用复制所有系统级字典,字典项
	 *
	 * @return empty
	 */
	@PostMapping("/copy_sys_dict_by_app_id")
	@PreAuthorize("hasAnyAuthority('app_admin', 'sys_dict:all', 'sys_dict:copy_by_app')")
	@CairoContext
	public Optional<String> copySysDictByAppId(@Validated @RequestBody CopySysDictByAppIdArgs args) {
		String appId = CairoContextHolder.getValue(CairoContextConstants.APP_ID).orElseThrow(() -> new ParamsErrorBusinessException("appId不能为空"));
		sysDictCairoWebManageApiService.copySysDictByAppId(appId,args);
		return Optional.empty();
	}


	/**
	 * 根据字典复制系统级字典,字典项
	 *
	 * @return empty
	 */
	@PostMapping("/copy_sys_dict_by_dict_id")
	@PreAuthorize("hasAnyAuthority('app_admin', 'sys_dict:all', 'sys_dict:copy_by_dict')")
	@CairoContext
	public Optional<String> copySysDictByDictId(@Validated @RequestBody CopySysDictByDictIdArgs args) {
		String appId = CairoContextHolder.getValue(CairoContextConstants.APP_ID).orElseThrow(() -> new ParamsErrorBusinessException("appId不能为空"));
		sysDictCairoWebManageApiService.copySysDictByDictId(appId,args);
		return Optional.empty();
	}

}
