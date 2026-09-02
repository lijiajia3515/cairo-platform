package io.github.lijiajia3515.cairo.auth.api.client.app_department;

import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurity;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityType;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthClientPrincipal;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_department.AppDepartment;
import io.github.lijiajia3515.cairo.auth.domain.api.client.app_department.GetAppDepartmentArgs;
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

/**
 * [client/api] department controller
 */

@Slf4j
@Validated
@RestController
@RequestMapping("/client_api/app_department")
@CairoSecurity(type = CairoSecurityType.CLIENT)
@BusinessResultBody
@RequiredArgsConstructor
public class AppDepartmentClientApiController {

	private final AppDepartmentClientApiService appDepartmentClientApiService;

	/**
	 * 获取部门列表
	 *
	 * @param principal principal
	 * @param args      args
	 * @return 部门列表
	 */
	@PostMapping("/get_app_department_list")
	@PreAuthorize("hasAnyAuthority('app_department:all', 'app_department:read')")
	public List<AppDepartment> getAppDepartmentList(@AuthenticationPrincipal CairoOAuthClientPrincipal principal,
													@Validated @RequestBody GetAppDepartmentArgs args) {
		String appId = principal.getAppId();
		return appDepartmentClientApiService.getAppDepartmentList(appId, args);
	}

	/**
	 * 获取部门分页列表
	 *
	 * @param principal principal
	 * @param args      args
	 * @return 部门分页列表
	 */
	@PostMapping("/get_app_department_page_list")
	@PreAuthorize("hasAnyAuthority('app_department:all', 'app_department:read')")
	public Page<AppDepartment> getAppDepartmentPageList(@AuthenticationPrincipal CairoOAuthClientPrincipal principal,
														@Validated @RequestBody GetAppDepartmentArgs args) {
		String appId = principal.getAppId();
		return appDepartmentClientApiService.getAppDepartmentPageList(appId, args);
	}


}
