package io.github.lijiajia3515.cairo.auth.modules.app_department;

import io.github.lijiajia3515.cairo.auth.CairoAuthClientSdkClientFeignClientConfiguration;
import io.github.lijiajia3515.cairo.auth.domain.api.client.app_department.GetAppDepartmentArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_department.AppDepartment;
import io.github.lijiajia3515.cairo.core.page.Page;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

/**
 * client-api-app_department feignclient
 */
@FeignClient(
	contextId = "appDepartmentClientApiFeignClient",
	name = "${cairo.feign.client.cairo-auth-service:cairo-auth-service}",
	path = "/client_api/app_department",
	fallbackFactory = AppDepartmentClientApiFeignClientFallbackFactory.class,
	configuration = CairoAuthClientSdkClientFeignClientConfiguration.class
)
public interface AppDepartmentClientApiFeignClient {

	/**
	 * 获取部门列表
	 * 需要权限：app_department:read | app_department:all
	 *
	 * @param args 参数
	 * @return 部门列表
	 */
	@PostMapping("/get_app_department_list")
	ResponseEntity<BusinessResult<List<AppDepartment>>> getAppDepartmentList(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization, @RequestBody GetAppDepartmentArgs args);

	/**
	 * 获取部门分页列表
	 * 需要权限：app_department:read | app_department:all
	 *
	 * @param args 参数
	 * @return 部门分页列表
	 */
	@PostMapping("/get_app_department_page_list")
	ResponseEntity<BusinessResult<Page<AppDepartment>>> getAppDepartmentPageList(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,@RequestBody GetAppDepartmentArgs args);

}
