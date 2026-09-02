package io.github.lijiajia3515.cairo.auth.modules.app_department;

import io.github.lijiajia3515.cairo.auth.domain.api.client.app_department.GetAppDepartmentArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_department.AppDepartment;
import io.github.lijiajia3515.cairo.core.exception.ErrorBusinessException;
import io.github.lijiajia3515.cairo.core.page.Page;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import org.springframework.http.ResponseEntity;

import java.util.List;

/**
 * client-api-department fallback feignclient
 */
public class AppDepartmentClientApiFallbackFeignClient implements AppDepartmentClientApiFeignClient {
	public static final RuntimeException EX = new ErrorBusinessException("认证服务-服务故障");

	@Override
	public ResponseEntity<BusinessResult<List<AppDepartment>>> getAppDepartmentList(String authorization, GetAppDepartmentArgs args) {
		throw EX;
	}

	@Override
	public ResponseEntity<BusinessResult<Page<AppDepartment>>> getAppDepartmentPageList(String authorization,GetAppDepartmentArgs args) {
		throw EX;
	}
}
