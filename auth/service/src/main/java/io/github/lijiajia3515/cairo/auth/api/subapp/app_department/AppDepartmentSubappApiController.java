package io.github.lijiajia3515.cairo.auth.api.subapp.app_department;

import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurity;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityType;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthSubappUserPrincipal;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_department.MetadataAppDepartment;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_department.PathAppDepartment;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_department.TreeNodeAppDepartment;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.app_department.CreateAppDepartmentArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.app_department.DeleteAppDepartmentArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.app_department.GetAppDepartmentArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.app_department.GetAppDepartmentByDepartmentIdArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.app_department.GetAppDepartmentTreeArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.app_department.ModifyAppDepartmentArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.app_department.MoveAppDepartmentArgs;
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
 * [subapp_user/api] app department controller
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/subapp_user_api/app_department")
@CairoSecurity(type = CairoSecurityType.SUBAPP_USER)
@RequiredArgsConstructor
@BusinessResultBody
public class AppDepartmentSubappApiController {

	private final AppDepartmentSubappApiService appDepartmentSubappApiService;

	/**
	 * 获取应用部门列表
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 应用部门集合
	 */
	@PostMapping("/get_app_department_list")
	@PreAuthorize("hasAnyAuthority('app_admin', 'app_department:all', 'app_department:read')")
	public List<MetadataAppDepartment> getAppDepartmentList(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
															@Validated @RequestBody GetAppDepartmentArgs args) {
		String appId = principal.getAppId();
		return appDepartmentSubappApiService.getAppDepartmentList(appId, args);
	}

	/**
	 * 获取应用部门分页列表
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 应用部门分页列表
	 */
	@PostMapping("/get_app_department_page_list")
	@PreAuthorize("hasAnyAuthority('app_admin', 'app_department:all', 'app_department:read')")
	public Page<MetadataAppDepartment> getAppDepartmentPageList(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
																@Validated @RequestBody GetAppDepartmentArgs args) {
		String appId = principal.getAppId();
		return appDepartmentSubappApiService.getAppDepartmentPageList(appId, args);
	}

	@PostMapping("/get_path_app_department_list")
	@PreAuthorize("hasAnyAuthority('app_admin', 'app_department:all', 'app_department:read')")
	public List<PathAppDepartment> getDepartmentAncestor(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
														 @Validated @RequestBody GetAppDepartmentArgs args) {
		String appId = principal.getAppId();
		return appDepartmentSubappApiService.getPathAppDepartmentList(appId, args);
	}

	@PostMapping("/get_path_app_department_page_list")
	@PreAuthorize("hasAnyAuthority('app_admin', 'app_department:all', 'app_department:read')")
	public Page<PathAppDepartment> getPathAppDepartmentPageList(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
																@Validated @RequestBody GetAppDepartmentArgs args) {
		String appId = principal.getAppId();
		return appDepartmentSubappApiService.getPathAppDepartmentPageList(appId, args);
	}

	/**
	 * 获取应用部门树形列表
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 应用部门分页列表
	 */
	@PostMapping("/get_app_department_tree")
	@PreAuthorize("hasAnyAuthority('app_admin', 'app_department:all', 'app_department:read')")
	public TreeNodeAppDepartment getDepartmentTreeList(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
													   @Validated @RequestBody GetAppDepartmentTreeArgs args) {
		String appId = principal.getAppId();
		return appDepartmentSubappApiService.getAppDepartmentTree(appId, args);
	}

	/**
	 * 获取应用部门根据应用部门ID
	 *
	 * @param principal principal
	 * @param args      参数
	 * @return PathDepartment
	 */
	@PostMapping("/get_app_department_by_department_id")
	@PreAuthorize("hasAnyAuthority('app_admin', 'app_department:all', 'app_department:read')")
	public Optional<PathAppDepartment> getAppDepartmentById(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
															@Validated @RequestBody GetAppDepartmentByDepartmentIdArgs args) {
		String appId = principal.getAppId();
		return appDepartmentSubappApiService.getAppDepartmentByAppDepartmentId(appId, args.getDepartmentId());
	}

	/**
	 * 创建应用部门
	 *
	 * @param principal principal
	 * @param args      args
	 * @return 应用部门
	 */

	@PostMapping("/create_app_department")
	@PreAuthorize("hasAnyAuthority('app_admin', 'app_department:all', 'app_department:create_app_department')")
	public Optional<String> createAppDepartment(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
												@Validated @RequestBody CreateAppDepartmentArgs args) {
		String appId = principal.getAppId();
		appDepartmentSubappApiService.createAppDepartment(appId, args);
		return Optional.empty();
	}

	/**
	 * 修改应用部门
	 *
	 * @param principal principal
	 * @param args      args
	 * @return 应用部门
	 */
	@PostMapping("/modify_app_department_info")
	@PreAuthorize("hasAnyAuthority('app_admin', 'app_department:all', 'app_department:modify_app_department_info')")
	public Optional<String> modifyAppDepartmentInfo(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
													@Validated @RequestBody ModifyAppDepartmentArgs args) {
		String appId = principal.getAppId();
		appDepartmentSubappApiService.modifyAppDepartmentInfo(appId, args);
		return Optional.empty();
	}

	/**
	 * 应用部门移动
	 *
	 * @param principal principal
	 * @param args      args
	 * @return 应用部门
	 */
	@PostMapping("/move_app_department")
	@PreAuthorize("hasAnyAuthority('app_admin', 'app_department:all', 'department:move_app_department')")
	public Optional<String> moveAppDepartment(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
											  @Validated @RequestBody MoveAppDepartmentArgs args) {
		String appId = principal.getAppId();
		appDepartmentSubappApiService.moveAppDepartment(appId, args);
		return Optional.empty();
	}

	/**
	 * 删除应用部门,包含删除子级应用部门
	 *
	 * @param principal principal
	 * @param args      args
	 * @return 删除的应用部门列表
	 */
	@PostMapping("/delete_app_department")
	@PreAuthorize("hasAnyAuthority('app_admin', 'app_department:all', 'app_department:delete_app_department')")
	public Optional<String> deleteAppDepartment(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
												@Validated @RequestBody DeleteAppDepartmentArgs args) {
		String appId = principal.getAppId();
		appDepartmentSubappApiService.deleteAppDepartment(appId, args);
		return Optional.empty();
	}

}
