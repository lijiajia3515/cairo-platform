package io.github.lijiajia3515.cairo.auth.api.open.sns_provider;

import io.github.lijiajia3515.cairo.auth.framework.context.CairoContext;
import io.github.lijiajia3515.cairo.auth.domain.dto.sns_provider.SnsProvider;
import io.github.lijiajia3515.cairo.auth.domain.api.client.sns_provider.GetSnsProviderArgs;
import io.github.lijiajia3515.cairo.web.bind.annotation.BusinessResultBody;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * [open/api] sns provider controller
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/open_api/sns_provider")
@RequiredArgsConstructor
@BusinessResultBody
public class SnsProviderOpenApiController {

	private final SnsProviderOpenApiService snsProviderOpenApiService;

	/**
	 * 获取第三方认证提供方集合
	 * @param args      参数
	 * @return snsProvider list
	 */
	@PostMapping("/get_sns_provider_list")
	@CairoContext
	public List<SnsProvider> getSnsProviderList(@Validated @RequestBody(required = false) GetSnsProviderArgs args) {
		if (args == null) {
			args = new GetSnsProviderArgs();
		}
		return snsProviderOpenApiService.getSnsProviderList(args);
	}


}
