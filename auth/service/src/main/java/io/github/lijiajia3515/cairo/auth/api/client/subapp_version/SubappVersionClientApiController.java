package io.github.lijiajia3515.cairo.auth.api.client.subapp_version;

import io.github.lijiajia3515.cairo.auth.framework.context.CairoContext;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurity;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityType;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthClientPrincipal;
import io.github.lijiajia3515.cairo.auth.domain.dto.subapp_version.MetadataSubappVersion;
import io.github.lijiajia3515.cairo.auth.domain.api.client.subapp_version.GetSubappVersionClientArgs;
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
 * [client/api] subapp_version client controller
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/client_api/subapp_version")
@CairoSecurity(type = CairoSecurityType.CLIENT)
@RequiredArgsConstructor
@BusinessResultBody
public class SubappVersionClientApiController {

	private final SubappVersionClientApiService subappVersionClientApiService;

	/**
	 * 获取子应用版本列表
	 *
	 * @param args 参数
	 * @return app 列表模式
	 */
	@PostMapping("/get_subapp_version_list")
	@PreAuthorize("hasAnyAuthority('subapp_version:all', 'subapp_version:read')")
	@CairoContext
	public List<MetadataSubappVersion> getSubappVersionList(@AuthenticationPrincipal CairoOAuthClientPrincipal principal, @Validated @RequestBody GetSubappVersionClientArgs args) {

		return subappVersionClientApiService.getSubappVersionList(args);
	}


}
