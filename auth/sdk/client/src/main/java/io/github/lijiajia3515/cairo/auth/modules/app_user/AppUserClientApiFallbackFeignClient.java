package io.github.lijiajia3515.cairo.auth.modules.app_user;

import io.github.lijiajia3515.cairo.auth.domain.api.client.app_user.AppUserAuthModel;
import io.github.lijiajia3515.cairo.auth.domain.api.client.app_user.GetAppUserAuthArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.app_user.GetAppUserClientArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_user.AppUser;
import io.github.lijiajia3515.cairo.core.exception.ErrorBusinessException;
import io.github.lijiajia3515.cairo.core.page.Page;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

/**
 * client-api-user fallback feignclient
 */
public class AppUserClientApiFallbackFeignClient implements AppUserClientApiFeignClient {
	public static final RuntimeException EX = new ErrorBusinessException("认证服务-服务故障");

	@Override
	public ResponseEntity<BusinessResult<List<AppUser>>> getAppUserList(String authorization, GetAppUserClientArgs args) {
		throw EX;
	}

	@Override
	public ResponseEntity<BusinessResult<Page<AppUser>>> getAppUserPageList(String authorization,GetAppUserClientArgs args) {
		throw EX;
	}

	@PostMapping("/get_user_auth")
	@Override
	public ResponseEntity<BusinessResult<AppUserAuthModel>> getAppUserAuth(String authorization, GetAppUserAuthArgs args) {
		throw EX;
	}
}
