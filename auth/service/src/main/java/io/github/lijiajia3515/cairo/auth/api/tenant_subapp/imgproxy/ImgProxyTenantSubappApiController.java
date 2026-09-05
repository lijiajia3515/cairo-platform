package io.github.lijiajia3515.cairo.auth.api.tenant_subapp.imgporxy;

import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthTenantAppUserPrincipal;
import io.github.lijiajia3515.cairo.auth.framework.minio.MinioProperties;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurity;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityType;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.imgproxy.GetImgUrlArgs;
import io.github.lijiajia3515.cairo.auth.modules.imgproxy.ImgProxyCommonService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * [tenant_subapp_user/api]tenant app subapp imgproxy service
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/tenant_subapp_user_api/imgproxy")
@CairoSecurity(type = CairoSecurityType.TENANT_SUBAPP_USER)
public class ImgProxyTenantSubappApiController {
	private final String DEFAULT_ACCESS_URL;
	private final ImgProxyCommonService imgProxyCommonService;

	public ImgProxyTenantSubappApiController(ImgProxyCommonService imgProxyCommonService, MinioProperties minioProperties) {
		this.imgProxyCommonService = imgProxyCommonService;
		DEFAULT_ACCESS_URL = minioProperties.getDefaultAccessUrl();
	}

	@RequestMapping("/access")
	@PreAuthorize("isAuthenticated()")
	public ModelAndView access(@AuthenticationPrincipal CairoOAuthTenantAppUserPrincipal principal, HttpServletRequest request) throws Exception {
		final Map<String, String[]> parameterMap = request.getParameterMap();
		final Map<String, String> params = parameterMap.keySet()
			.stream()
			.filter(x -> x.startsWith("imgproxy_"))
			.collect(Collectors.toMap(x -> x.replaceFirst("imgproxy_", ""), v -> {
				final String[] strings = parameterMap.get(v);
				return strings == null || strings.length == 0 ? "" : strings[0];
			}));
		String sourceUrl = Stream.of(parameterMap.getOrDefault("source_url", new String[]{"default.png"})).findFirst().orElse("default.png");

		return new ModelAndView(new RedirectView(imgProxyCommonService.getProxyUrl(sourceUrl, params)));
	}

	/**
	 * 获取图片代理url
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 图片代理url
	 */
	@PostMapping("/get_proxy_url")
	@PreAuthorize("isAuthenticated()")
	public List<String> getUrl(@AuthenticationPrincipal CairoOAuthTenantAppUserPrincipal principal, @Valid @NotNull @NotEmpty @RequestBody List<GetImgUrlArgs> args) {
		return args.parallelStream()
			.map(x -> {
				try {
					return imgProxyCommonService.getProxyUrl(x.getSourceUrl(), x.getParams());
				} catch (Exception e) {
					return DEFAULT_ACCESS_URL;
				}
			})
			.collect(Collectors.toList());
	}
}
