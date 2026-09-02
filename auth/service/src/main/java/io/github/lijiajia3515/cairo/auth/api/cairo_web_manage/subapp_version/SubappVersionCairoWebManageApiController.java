package io.github.lijiajia3515.cairo.auth.api.cairo_web_manage.subapp_version;

import io.github.lijiajia3515.cairo.auth.framework.context.CairoContext;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurity;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityType;
import io.github.lijiajia3515.cairo.auth.domain.dto.subapp_version.MetadataSubappVersion;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.subapp_version.CreateSubappVersionArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.subapp_version.DeleteSubappVersionArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.subapp_version.GetSubappVersionArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.subapp_version.ModifySubappVersionInfoArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.subapp_version.ModifySubappVersionStatusArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.subapp_version.SyncSubappVersionArgs;
import io.github.lijiajia3515.cairo.core.page.Page;
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
 * [cairo-web-manage/api] subapp_version endpoint controller
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/cairo_web_manage_api/subapp_version")
@CairoSecurity(type = CairoSecurityType.CAIRO_WEB_MANAGE_USER)
@RequiredArgsConstructor
@BusinessResultBody
public class SubappVersionCairoWebManageApiController {

	private final SubappVersionCairoWebManageApiService subappVersionCairoWebManageApiService;

	/**
	 * 获取子应用版本列表
	 *
	 * @param args 参数
	 * @return app 列表模式
	 */
	@PostMapping("/get_subapp_version_list")
	@PreAuthorize("hasAnyAuthority('app_admin', 'subapp_version:all', 'subapp_version:read')")
	@CairoContext
	public List<MetadataSubappVersion> getSubappVersionList(@Validated @RequestBody GetSubappVersionArgs args) {
		return subappVersionCairoWebManageApiService.getSubappVersionList(args);
	}

	/**
	 * 获取子应用分页列表
	 *
	 * @param args 参数
	 * @return app 分页模式
	 */
	@PostMapping("/get_subapp_version_page_list")
	@PreAuthorize("hasAnyAuthority('app_admin', 'subapp_version:all', 'subapp_version:read')")
	@CairoContext
	public Page<MetadataSubappVersion> getSubappVersionPageList(@Validated @RequestBody GetSubappVersionArgs args) {

		return subappVersionCairoWebManageApiService.getSubappVersionPageList(args);
	}


	/**
	 * 创建子应用版本
	 *
	 * @param args      参数
	 * @return app
	 */
	@PostMapping("/create_subapp_version")
	@PreAuthorize("hasAnyAuthority('app_admin', 'subapp_version:all', 'subapp_version:create_subapp_version')")
	@CairoContext
	public Optional<String> createSubappVersion(@Validated @RequestBody CreateSubappVersionArgs args) {
		subappVersionCairoWebManageApiService.createSubappVersion(args);
		return Optional.empty();
	}

	/**
	 * 修改 子应用版本信息
	 *
	 * @param args      参数
	 * @return app
	 */
	@PostMapping("/modify_subapp_version_info")
	@PreAuthorize("hasAnyAuthority('app_admin', 'subapp_version:all', 'subapp_version:modify_subapp_version_info')")
	@CairoContext
	public Optional<String> modifySubappVersionInfo(@Validated @RequestBody ModifySubappVersionInfoArgs args) {
		subappVersionCairoWebManageApiService.modifySubappVersionInfo(args);
		return Optional.empty();
	}

	/**
	 * 修改状态
	 *
	 * @param args      参数
	 * @return app
	 */
	@PostMapping("/modify_subapp_version_status")
	@PreAuthorize("hasAnyAuthority('app_admin', 'subapp_version:all', 'subapp_version:modify_subapp_version_status')")
	@CairoContext
	public Optional<String> modifySubappVersionStatus(@Validated @RequestBody ModifySubappVersionStatusArgs args) {
		subappVersionCairoWebManageApiService.modifySubappVersionStatus(args);
		return Optional.empty();
	}


	/**
	 * 删除 子应用版本
	 *
	 * @param args      参数
	 * @return app
	 */
	@PostMapping("/delete_subapp_version")
	@PreAuthorize("hasAnyAuthority('app_admin', 'subapp_version:all', 'subapp_version:delete_subapp_version')")
	@CairoContext
	public Optional<String> deleteSubappVersion( @Validated @RequestBody DeleteSubappVersionArgs args) {
		subappVersionCairoWebManageApiService.deleteSubappVersion(args);
		return Optional.empty();
	}


	/**
	 * 同步 子应用版本
	 *
	 * @param args      参数
	 * @return app
	 */
	@PostMapping("/sync_subapp_version")
	@PreAuthorize("hasAnyAuthority('app_admin', 'subapp_version:all', 'subapp_version:sync_subapp_version')")
	@CairoContext
	public Optional<String> syncSubappVersion( @Validated @RequestBody SyncSubappVersionArgs args) {
		subappVersionCairoWebManageApiService.syncSubappVersion(args);
		return Optional.empty();
	}

}
