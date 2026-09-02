package io.github.lijiajia3515.cairo.auth.api.tenant_subapp.tenant_app_user_tag;

import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurity;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityType;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthTenantSubappUserPrincipal;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_user_tag.MetadataTenantAppUserTag;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.tenant_app_user_tag.CreateUserTagArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.tenant_app_user_tag.DeleteUserTagArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.tenant_app_user_tag.GetUserTagArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.tenant_app_user_tag.GetUserTagInfoArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.tenant_app_user_tag.ModifyUserTagInfoArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.tenant_app_user_tag.ModifyUserTagStatusArgs;
import io.github.lijiajia3515.cairo.core.page.Page;
import io.github.lijiajia3515.cairo.web.bind.annotation.BusinessResultBody;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

/**
 * [tenant_subapp_user/api] tenant app user tag controller
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/tenant_subapp_user_api/tenant_app_user_tag")
@CairoSecurity(type = CairoSecurityType.TENANT_SUBAPP_USER)
@RequiredArgsConstructor
@BusinessResultBody
public class TenantAppUserTagTenantSubappApiController {

	private final TenantAppUserTagTenantSubappApiService tenantAppUserTagTenantSubappApiService;


	/**
	 * 获取标签列表
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 用户标签列表
	 */
	@PostMapping("/get_tenant_app_user_tag_list")
	@PreAuthorize("hasAnyAuthority('app_admin', 'tenant_app_user_tag:all', 'tenant_app_user_tag:read')")
	public List<MetadataTenantAppUserTag> getTenantAppUserTagList(@AuthenticationPrincipal CairoOAuthTenantSubappUserPrincipal principal,
																  @Valid @Validated @RequestBody(required = false) GetUserTagArgs args) {
		if (args == null) {
			args = new GetUserTagArgs();
		}
		String tenantId = principal.getTenantId();
		String appId = principal.getAppId();

		return tenantAppUserTagTenantSubappApiService.getTenantAppUserTagList(tenantId, appId, args);
	}

	/**
	 * 获取用户标签分页列表
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 用户标签分页列表
	 */
	@PostMapping("/get_tenant_app_user_tag_page_list")
	@PreAuthorize("hasAnyAuthority('app_admin', 'tenant_app_user_tag:all', 'tenant_app_user_tag:read')")
	public Page<MetadataTenantAppUserTag> getTenantAppUserTagPageList(@AuthenticationPrincipal CairoOAuthTenantSubappUserPrincipal principal,
																	  @RequestBody GetUserTagArgs args) {
		if (args == null) {
			args = new GetUserTagArgs();
		}
		String tenantId = principal.getTenantId();
		String appId = principal.getAppId();

		return tenantAppUserTagTenantSubappApiService.getTenantAppUserTagPageList(tenantId, appId, args);
	}

	/**
	 * 用户接口，根据用户标识获取用户信息
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 用户信息
	 */
	@PostMapping("/get_tenant_app_user_tag_info")
	@PreAuthorize("hasAnyAuthority('app_admin', 'tenant_app_user_tag:all', 'tenant_app_user_tag:read')")
	public MetadataTenantAppUserTag getTenantAppUserTagInfo(@AuthenticationPrincipal CairoOAuthTenantSubappUserPrincipal principal, @Validated @RequestBody GetUserTagInfoArgs args) {
		String tenantId = principal.getTenantId();
		String appId = principal.getAppId();

		return tenantAppUserTagTenantSubappApiService.getTenantAppUserTagInfo(tenantId, appId, args.getTagId());
	}

	/**
	 * 用户接口，创建用户标签
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 创建好的用户
	 */
	@PostMapping("/create_tenant_app_user_tag")
	@PreAuthorize("hasAnyAuthority('app_admin', 'tenant_app_user_tag:all', 'tenant_app_user_tag:create_tenant_app_user_tag')")
	public Optional<Object> createTenantAppUserTag(@AuthenticationPrincipal CairoOAuthTenantSubappUserPrincipal principal, @Validated @RequestBody CreateUserTagArgs args) {
		String tenantId = principal.getTenantId();
		String appId = principal.getAppId();

		tenantAppUserTagTenantSubappApiService.createTenantAppUserTag(tenantId, appId, args);
		return Optional.empty();
	}


	/**
	 * 用户接口，修改用户标签信息
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 用户信息
	 */
	@PostMapping("/modify_tenant_app_user_tag_info")
	@PreAuthorize("hasAnyAuthority('app_admin', 'tenant_app_user_tag:all', 'tenant_app_user_tag:modify_tenant_app_user_tag_info')")
	public Optional<String> modifyTenantAppUserInfo(@AuthenticationPrincipal CairoOAuthTenantSubappUserPrincipal principal,
													@Validated @RequestBody ModifyUserTagInfoArgs args) {
		tenantAppUserTagTenantSubappApiService.modifyTenantAppUserTagInfo(principal.getTenantId(), principal.getAppId(), args);
		return Optional.empty();
	}

	/**
	 * 用户接口，修改用户标签状态
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 用户信息
	 */
	@PostMapping("/modify_tenant_app_user_tag_status")
	@PreAuthorize("hasAnyAuthority('app_admin', 'tenant_app_user_tag:all', 'tenant_app_user_tag:modify_tenant_app_user_tag_status')")
	public Optional<String> modifyTenantAppUserTagStatus(@AuthenticationPrincipal CairoOAuthTenantSubappUserPrincipal principal,
														 @Validated @RequestBody ModifyUserTagStatusArgs args) {
		String tenantId = principal.getTenantId();
		String appId = principal.getAppId();
		tenantAppUserTagTenantSubappApiService.modifyTenantAppUserTagStatus(tenantId, appId, args);
		return Optional.empty();
	}

	/**
	 * 删除用户标签
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 空
	 */
	@PostMapping("/delete_tenant_app_user_tag")
	@PreAuthorize("hasAnyAuthority('app_admin', 'tenant_app_user_tag:all', 'tenant_app_user_tag:delete_tenant_app_user_tag')")
	public Optional<String> deleteTenantAppUserTag(@AuthenticationPrincipal CairoOAuthTenantSubappUserPrincipal principal,
												   @Validated @RequestBody DeleteUserTagArgs args) {
		String tenantId = principal.getTenantId();
		String appId = principal.getAppId();

		tenantAppUserTagTenantSubappApiService.deleteTenantAppUserTag(tenantId, appId, args);
		return Optional.empty();
	}

}
