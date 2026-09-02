package io.github.lijiajia3515.cairo.auth.modules.tenant_app_department;


import io.github.lijiajia3515.cairo.auth.domain.api.client.tenant_app_department.GetDepartmentArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_department.PathTenantAppDepartment;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_department.TenantAppDepartment;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthClientPrincipal;
import io.github.lijiajia3515.cairo.core.page.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface TenantAppDepartmentClientApiService {

	/**
	 * 获取部门列表
	 * 需要权限：department:read | department:all
	 *
	 * @param args 参数
	 * @return 部门列表
	 */
	List<TenantAppDepartment> getDepartmentList(GetDepartmentArgs args);

	/**
	 * 获取部门分页列表
	 * 需要权限：department:read | department:all
	 *
	 * @param args 参数
	 * @return 部门分页列表
	 */
	Page<TenantAppDepartment> getDepartmentPageList(GetDepartmentArgs args);

    List<PathTenantAppDepartment> getPathTenantAppDepartmentList(GetDepartmentArgs args);

	List<TenantAppDepartment> getTenantAppSubDepartmentList(GetDepartmentArgs args);

}
