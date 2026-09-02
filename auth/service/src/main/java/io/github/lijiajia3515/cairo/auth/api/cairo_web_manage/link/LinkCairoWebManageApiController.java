package io.github.lijiajia3515.cairo.auth.api.cairo_web_manage.link;


import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthSubappUserPrincipal;
import io.github.lijiajia3515.cairo.core.page.Page;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.link.CreateLinkArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.link.CreateLinkResponse;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.link.DeleteLinkArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.link.GetLinkPageListArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.link.ModifyLinkStatusArgs;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurity;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityType;
import io.github.lijiajia3515.cairo.auth.domain.dto.link.MetadataLink;
import io.github.lijiajia3515.cairo.web.bind.annotation.BusinessResultBody;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

/**
 * [cairo_web_manage/api] link controller
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/cairo_web_manage_api/link")
@CairoSecurity(type = CairoSecurityType.CAIRO_WEB_MANAGE_USER)
@BusinessResultBody
@RequiredArgsConstructor
public class LinkCairoWebManageApiController {

	private final LinkCairoWebManageApiService linkCairoWebManageApiService;

	/**
	 * 查询短链分页列表
	 *
	 * @param principal principal
	 * @param args      args
	 * @return 字典列表
	 */
	@PostMapping("/get_link_page_list")
	@PreAuthorize("hasAnyAuthority('app_admin', 'link:all', 'link:read')")
	public Page<MetadataLink> getLinkPageList(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
											  @Validated @RequestBody GetLinkPageListArgs args) {
		return linkCairoWebManageApiService.getLinkPageList(args);
	}

	/**
	 * 创建短链
	 *
	 * @param principal principal
	 * @param args      args
	 * @return 短链信息
	 */
	@PostMapping("/create_link")
	@PreAuthorize("hasAnyAuthority('app_admin', 'link:all', 'link:create_link')")
	public CreateLinkResponse createLink(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
                                         @Validated @RequestBody CreateLinkArgs args) {
		return linkCairoWebManageApiService.createLink(args);
	}

	/**
	 * 修改短链状态
	 *
	 * @param principal principal
	 * @param args      args
	 * @return 字典列表
	 */
	@PostMapping("/modify_link_status")
	@PreAuthorize("hasAnyAuthority('app_admin', 'link:all', 'link:modify_link_status')")
	public Optional<String> modifyLinkStatus(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
											 @Validated @RequestBody ModifyLinkStatusArgs args) {
		linkCairoWebManageApiService.modifyLinkStatus(args);
		return Optional.empty();
	}

	/**
	 * 删除短链
	 *
	 * @param principal principal
	 * @param args      args
	 * @return 字典列表
	 */
	@PostMapping("/delete_link")
	@PreAuthorize("hasAnyAuthority('app_admin', 'link:all', 'link:delete_link')")
	public Optional<String> deleteLink(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
									   @Validated @RequestBody DeleteLinkArgs args) {
		linkCairoWebManageApiService.deleteLink(args);
		return Optional.empty();
	}


}
