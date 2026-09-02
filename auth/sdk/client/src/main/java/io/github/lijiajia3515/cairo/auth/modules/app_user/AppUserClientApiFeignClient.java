package io.github.lijiajia3515.cairo.auth.modules.app_user;

import io.github.lijiajia3515.cairo.auth.CairoAuthClientSdkClientFeignClientConfiguration;
import io.github.lijiajia3515.cairo.auth.domain.api.client.app_user.AppUserAuthModel;
import io.github.lijiajia3515.cairo.auth.domain.api.client.app_user.GetAppUserAuthArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.app_user.GetAppUserClientArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_user.AppUser;
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
 * client-api-user feignclient
 */
@FeignClient(
        contextId = "appUserClientApiFeignClient",
        name = "${cairo.feign.client.cairo-auth-service:cairo-auth-service}",
        path = "/client_api/app_user",
        fallbackFactory = AppUserClientApiFeignClientFallbackFactory.class,
        configuration = CairoAuthClientSdkClientFeignClientConfiguration.class
)
public interface AppUserClientApiFeignClient {

    /**
     * 查询用户列表
     * 需要权限： app_user:read｜app_user:all
     * @param args args
     * @return 用户集合
     */
    @PostMapping("/get_app_user_list")
    ResponseEntity<BusinessResult<List<AppUser>>> getAppUserList(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization, @RequestBody GetAppUserClientArgs args);

    /**
     * 查询用户分页
     * 需要权限： app_user:read｜app_user:all
     * @param args args
     * @return 分页对象
     */
    @PostMapping("/get_app_user_page_list")
    ResponseEntity<BusinessResult<Page<AppUser>>> getAppUserPageList(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,@RequestBody GetAppUserClientArgs args);

    /**
     * 获取用户认证信息
     * 需要权限： app_user:app_user_auth｜app_user:all
     * @param args args
     * @return 用户信息
     */
    @PostMapping("/get_app_user_auth")
    ResponseEntity<BusinessResult<AppUserAuthModel>> getAppUserAuth(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization, @RequestBody GetAppUserAuthArgs args);

}
