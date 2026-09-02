package io.github.lijiajia3515.cairo.auth.api.cairo_web_manage.subapp;

import io.github.lijiajia3515.cairo.auth.framework.context.CairoContext;
import io.github.lijiajia3515.cairo.auth.framework.context.CairoContextConstants;
import io.github.lijiajia3515.cairo.auth.framework.context.CairoContextHolder;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurity;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityType;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthSubappUserPrincipal;
import io.github.lijiajia3515.cairo.auth.domain.dto.subapp.MetadataSubapp;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.subapp.CreateSubappArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.subapp.DeleteSubappArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.subapp.GetSubappArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.subapp.ModifySubappInfoArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.subapp.ModifySubappStatusArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.subapp.MoveSubappArgs;
import io.github.lijiajia3515.cairo.core.exception.ParamsErrorBusinessException;
import io.github.lijiajia3515.cairo.core.page.Page;
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
 * [cairo-web-manage/api] subapp endpoint controller
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/cairo_web_manage_api/subapp")
@CairoSecurity(type = CairoSecurityType.CAIRO_WEB_MANAGE_USER)
@RequiredArgsConstructor
@BusinessResultBody
public class SubappCairoWebManageApiController {

	private final SubappCairoWebManageApiService subappCairoWebManageApiService;

	/**
	 * 获取子应用列表
	 *
	 * @param args 参数
	 * @return app 列表模式
	 */
	@PostMapping("/get_subapp_list")
	@PreAuthorize("hasAnyAuthority('app_admin', 'subapp:all', 'subapp:read')")
	@CairoContext
	public List<MetadataSubapp> getEndpointList(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal, @Validated @RequestBody GetSubappArgs args) {
		String appId = CairoContextHolder.getValue(CairoContextConstants.APP_ID).orElseThrow(() -> new ParamsErrorBusinessException("appId不能为空"));
		String endpointId = CairoContextHolder.getValue(CairoContextConstants.ENDPOINT_ID).orElse(null);
		return subappCairoWebManageApiService.getSubappList(appId, endpointId, args);
	}

	/**
	 * 获取子应用分页列表
	 *
	 * @param args 参数
	 * @return app 分页模式
	 */
	@PostMapping("/get_subapp_page_list")
	@PreAuthorize("hasAnyAuthority('app_admin', 'subapp:all', 'subapp:read')")
	@CairoContext
	public Page<MetadataSubapp> getSubappPageList(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal, @Validated @RequestBody GetSubappArgs args) {
		String appId = CairoContextHolder.getValue(CairoContextConstants.APP_ID).orElseThrow(() -> new ParamsErrorBusinessException("appId不能为空"));
		String endpointId = CairoContextHolder.getValue(CairoContextConstants.ENDPOINT_ID).orElse(null);
		return subappCairoWebManageApiService.getSubappPageList(appId, endpointId, args);
	}


	/**
	 * 创建子应用
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return app
	 */
	@PostMapping("/create_subapp")
	@PreAuthorize("hasAnyAuthority('app_admin', 'subapp:all', 'subapp:create_subapp')")
	@CairoContext
	public Optional<String> createSubapp(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal, @Validated @RequestBody CreateSubappArgs args) {
		String appId = CairoContextHolder.getValue(CairoContextConstants.APP_ID).orElseThrow(() -> new ParamsErrorBusinessException("appId不能为空"));
		String endpointId = CairoContextHolder.getValue(CairoContextConstants.ENDPOINT_ID).orElseThrow(() -> new ParamsErrorBusinessException("endpointId不能为空"));
		subappCairoWebManageApiService.createSubapp(appId, endpointId, args);
		return Optional.empty();
	}

	/**
	 * 修改 子应用 信息
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return app
	 */
	@PostMapping("/modify_subapp_info")
	@PreAuthorize("hasAnyAuthority('app_admin', 'subapp:all', 'subapp:modify_subapp_info')")
	@CairoContext
	public Optional<String> modifySubappInfo(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal, @Validated @RequestBody ModifySubappInfoArgs args) {
		String appId = CairoContextHolder.getValue(CairoContextConstants.APP_ID).orElseThrow(() -> new ParamsErrorBusinessException("appId不能为空"));
		String endpointId = CairoContextHolder.getValue(CairoContextConstants.ENDPOINT_ID).orElseThrow(() -> new ParamsErrorBusinessException("endpointId不能为空"));
		subappCairoWebManageApiService.modifySubappInfo(appId, endpointId, args);
		return Optional.empty();
	}

	/**
	 * 修改状态
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return app
	 */
	@PostMapping("/modify_subapp_status")
	@PreAuthorize("hasAnyAuthority('app_admin', 'subapp:all', 'subapp:modify_subapp_status')")
	@CairoContext
	public Optional<String> modifySubappStatus(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal, @Validated @RequestBody ModifySubappStatusArgs args) {
		String appId = CairoContextHolder.getValue(CairoContextConstants.APP_ID).orElseThrow(() -> new ParamsErrorBusinessException("appId不能为空"));
		String endpointId = CairoContextHolder.getValue(CairoContextConstants.ENDPOINT_ID).orElseThrow(() -> new ParamsErrorBusinessException("endpointId不能为空"));
		subappCairoWebManageApiService.modifySubappStatus(appId, endpointId, args);
		return Optional.empty();
	}

	/**
	 * 移动 子应用
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return empty
	 */
	@PostMapping("/move_subapp")
	@PreAuthorize("hasAnyAuthority('app_admin', 'subapp:all', 'subapp:move_subapp')")
	@CairoContext
	public Optional<String> moveSubapp(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal, @Validated @RequestBody MoveSubappArgs args) {
		String appId = CairoContextHolder.getValue(CairoContextConstants.APP_ID).orElseThrow(() -> new ParamsErrorBusinessException("appId不能为空"));
		String endpointId = CairoContextHolder.getValue(CairoContextConstants.ENDPOINT_ID).orElseThrow(() -> new ParamsErrorBusinessException("endpointId不能为空"));
		subappCairoWebManageApiService.moveSubapp(appId, endpointId, args);
		return Optional.empty();
	}

	/**
	 * 删除 子应用
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return app
	 */
	@PostMapping("/delete_subapp")
	@PreAuthorize("hasAnyAuthority('app_admin', 'subapp:all', 'subapp:delete_subapp')")
	@CairoContext
	public Optional<String> deleteSubapp(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal, @Validated @RequestBody DeleteSubappArgs args) {
		String appId = CairoContextHolder.getValue(CairoContextConstants.APP_ID).orElseThrow(() -> new ParamsErrorBusinessException("appId不能为空"));
		String endpointId = CairoContextHolder.getValue(CairoContextConstants.ENDPOINT_ID).orElseThrow(() -> new ParamsErrorBusinessException("endpointId不能为空"));
		subappCairoWebManageApiService.deleteSubapp(appId, endpointId, args);
		return Optional.empty();
	}

}
