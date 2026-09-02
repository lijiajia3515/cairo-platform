package io.github.lijiajia3515.cairo.auth.api.client.sns_provider;

import io.github.lijiajia3515.cairo.auth.domain.dto.sns_provider.SnsProvider;
import io.github.lijiajia3515.cairo.auth.framework.context.CairoContext;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurity;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityType;
import io.github.lijiajia3515.cairo.auth.domain.api.client.sns_provider.GetSnsProviderArgs;
import io.github.lijiajia3515.cairo.web.bind.annotation.BusinessResultBody;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * [client/api] snsProvider controller
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/client_api/sns_provider")
@CairoSecurity(type = CairoSecurityType.CLIENT)
@RequiredArgsConstructor
@BusinessResultBody
public class SnsProviderClientApiController {

	private final SnsProviderClientApiService snsProviderClientApiService;

	/**
	 * 获取第三方认证提供方集合
	 * @param args      参数
	 * @return snsProvider list
	 */
	@PostMapping("/get_sns_provider_list")
	@PreAuthorize("hasAnyAuthority('sns_provider:all', 'sns_provider:read')")
	@CairoContext
	public List<SnsProvider> getSnsProviderList(@Validated @RequestBody(required = false) GetSnsProviderArgs args) {
		if (args == null) {
			args = new GetSnsProviderArgs();
		}
		return snsProviderClientApiService.getSnsProviderList(args);
	}


}
