package io.github.lijiajia3515.cairo.auth.api.client.link;


import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthClientPrincipal;
import io.github.lijiajia3515.cairo.auth.domain.api.client.link.CreateBatchLinkArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.link.GetLinkListByLinkIdArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.link.GetLinkListByShortUrlArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.link.LinkInfo;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurity;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityType;
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

import java.util.List;

/**
 * [client/api] link controller
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/client_api/link")
@CairoSecurity(type = CairoSecurityType.CLIENT)
@BusinessResultBody
@RequiredArgsConstructor
public class LinkClientApiController {

	private final LinkClientApiService linkClientApiService;

	/**
	 * 创建短链
	 *
	 * @param principal principal
	 * @param args      args
	 * @return 短链信息
	 */
	@PostMapping("/create_batch_link")
	@PreAuthorize("hasAnyAuthority('link:all', 'link:create_link')")
	public List<LinkInfo> createLink(@AuthenticationPrincipal CairoOAuthClientPrincipal principal,
                                     @Validated @RequestBody CreateBatchLinkArgs args) {
		return linkClientApiService.createBatchLink(args);
	}

	/**
	 * 获取短链集合根据短链数组
	 *
	 * @param principal principal
	 * @param args      args
	 * @return 短链信息数组
	 */
	@PostMapping("/get_link_list_by_short_url")
	@PreAuthorize("hasAnyAuthority('link:all', 'link:read')")
	public List<LinkInfo> getLinkListByShortUrl(@AuthenticationPrincipal CairoOAuthClientPrincipal principal,
												@Validated @RequestBody GetLinkListByShortUrlArgs args) {
		return linkClientApiService.getLinkListByShortUrl(args);
	}


	/**
	 * 获取短链集合根据短链ID数组
	 *
	 * @param principal principal
	 * @param args      args
	 * @return 短链信息数组
	 */
	@PostMapping("/get_link_list_by_link_id")
	@PreAuthorize("hasAnyAuthority('link:all', 'link:read')")
	public List<LinkInfo> getLinkListByLinkId(@AuthenticationPrincipal CairoOAuthClientPrincipal principal,
											  @Validated @RequestBody GetLinkListByLinkIdArgs args) {
		return linkClientApiService.getLinkListByLinkId(args);
	}


}
