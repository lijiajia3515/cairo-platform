package io.github.lijiajia3515.cairo.auth.modules.app_role;

import io.github.lijiajia3515.cairo.auth.CairoAuthClientSdkClientFeignClientConfiguration;
import io.github.lijiajia3515.cairo.auth.domain.api.client.app_role.GetAppRoleArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_role.MetadataAppRole;
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
 * 角色子应用-client模式feign客户端
 */
@FeignClient(
	contextId = "appRoleClientFeignClient",
	name = "${cairo.feign.client.cairo-auth-service:cairo-auth-service}",
	path = "/client_api/app_role",
	fallbackFactory = AppRoleClientApiFeignClientFallbackFactory.class,
	configuration = CairoAuthClientSdkClientFeignClientConfiguration.class
)
public interface AppRoleClientApiFeignClient {
	/**
	 * 获取应用角色列表
	 * 需要权限： app_role:read｜app_role:all
	 *
	 * @param args 参数
	 * @return 角色列表
	 */
	@PostMapping("/get_app_role_list")
	ResponseEntity<BusinessResult<List<MetadataAppRole>>> getAppRoleList(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization, @RequestBody GetAppRoleArgs args);

	/**
	 * 获取应用角色分页列表
	 * 需要权限： app_role:read｜app_role:all
	 *
	 * @param args 参数
	 * @return 角色分页
	 */
	@PostMapping("/get_app_role_page_list")
	ResponseEntity<BusinessResult<Page<MetadataAppRole>>> getAppRolePageList(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,@RequestBody GetAppRoleArgs args);

}
