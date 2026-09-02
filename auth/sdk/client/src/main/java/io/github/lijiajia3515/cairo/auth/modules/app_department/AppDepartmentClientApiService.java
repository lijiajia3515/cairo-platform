package io.github.lijiajia3515.cairo.auth.modules.app_department;

import io.github.lijiajia3515.cairo.auth.domain.api.client.app_department.GetAppDepartmentArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_department.AppDepartment;
import io.github.lijiajia3515.cairo.core.page.Page;

import java.util.List;


public interface AppDepartmentClientApiService {

	/**
	 * 获取部门列表
	 * 需要权限：app_department:read | app_department:all
	 *
	 * @param args 参数
	 * @return 部门列表
	 */
	List<AppDepartment> getAppDepartmentList(GetAppDepartmentArgs args);

	/**
	 * 获取部门分页列表
	 * 需要权限：app_department:read | app_department:all
	 *
	 * @param args 参数
	 * @return 部门分页列表
	 */
	Page<AppDepartment> getAppDepartmentPageList(GetAppDepartmentArgs args);

}
