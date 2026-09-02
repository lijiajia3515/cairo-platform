package io.github.lijiajia3515.cairo.auth.modules.permission;

import io.github.lijiajia3515.cairo.auth.domain.api.client.permission.GetPermissionListArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.permission.Permission;
import io.github.lijiajia3515.cairo.core.exception.ErrorBusinessException;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import org.springframework.http.ResponseEntity;

import java.util.List;

public class PermissionClientApiFallbackFeignClient implements PermissionClientApiFeignClient {
	public static final RuntimeException EX = new ErrorBusinessException("认证服务-服务故障");

	@Override
	public ResponseEntity<BusinessResult<List<Permission>>> getPermissionList(String authorization, GetPermissionListArgs args) {
		throw EX;
	}

	@Override
	public ResponseEntity<BusinessResult<List<Permission>>> getMyPermissionList(String authorization,GetPermissionListArgs args) {
		throw EX;
	}
}
