package io.github.lijiajia3515.cairo.auth.modules.app_role;

import io.github.lijiajia3515.cairo.auth.domain.api.client.app_role.GetAppRoleArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_role.MetadataAppRole;
import io.github.lijiajia3515.cairo.core.exception.ErrorBusinessException;
import io.github.lijiajia3515.cairo.core.page.Page;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import org.springframework.http.ResponseEntity;

import java.util.List;

/**
 * 容错实现 的 role client
 */
public class AppRoleClientApiFallbackFeignClient implements AppRoleClientApiFeignClient {
	public static final RuntimeException EX = new ErrorBusinessException("认证服务-服务故障");

	@Override
	public ResponseEntity<BusinessResult<List<MetadataAppRole>>> getAppRoleList(String authorization, GetAppRoleArgs args) {
		throw EX;
	}

	@Override
	public ResponseEntity<BusinessResult<Page<MetadataAppRole>>> getAppRolePageList(String authorization, GetAppRoleArgs args) {
		throw EX;
	}
}
