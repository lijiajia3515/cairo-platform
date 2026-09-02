package io.github.lijiajia3515.cairo.auth.modules.permission;

import io.github.lijiajia3515.cairo.auth.CairoAuthClientSdkCoreFeignClientConfiguration;
import io.github.lijiajia3515.cairo.auth.domain.api.client.permission.GetPermissionListArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.permission.Permission;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

/**
 * 功能权限-client模式feign客户端
 */
@FeignClient(
	contextId = "permissionClientApiFeignClient",
	name = "${cairo.feign.client.cairo-auth-service:cairo-auth-service}",
	path = "/client_api/permission",
	fallbackFactory = PermissionClientApiFeignClientFallbackFactory.class,
	configuration = CairoAuthClientSdkCoreFeignClientConfiguration.class
)
public interface PermissionClientApiFeignClient {

	/**
	 * 获取功能权限list
	 *
	 * @param args      参数
	 * @return 功能权限集合
	 */
	@PostMapping("/get_permission_list")
	ResponseEntity<BusinessResult<List<Permission>>>  getPermissionList(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
																					@RequestBody GetPermissionListArgs args);

	/**
	 * 获取我的功能权限list
	 * @param args      参数
	 * @return 功能权限集合
	 */
	@PostMapping("/get_my_permission_list")
	ResponseEntity<BusinessResult<List<Permission>>> getMyPermissionList(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
																					 @RequestBody GetPermissionListArgs args);
}
