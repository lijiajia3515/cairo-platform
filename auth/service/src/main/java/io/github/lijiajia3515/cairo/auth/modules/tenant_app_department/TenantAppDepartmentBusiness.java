package io.github.lijiajia3515.cairo.auth.modules.tenant_app_department;


import io.github.lijiajia3515.cairo.core.business.Business;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
public enum TenantAppDepartmentBusiness implements Business {

	/**
	 * 数据不存在
	 */
	NotExists("Department.NotExists", "数据不存在");

	public final String code;
	public final String message;


	TenantAppDepartmentBusiness(String code, String message) {
		this.code = code;
		this.message = message;
	}
}
