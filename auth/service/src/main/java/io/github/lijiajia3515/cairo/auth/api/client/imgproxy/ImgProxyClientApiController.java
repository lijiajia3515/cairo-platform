package io.github.lijiajia3515.cairo.auth.api.client.imgproxy;

import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthClientPrincipal;
import io.github.lijiajia3515.cairo.auth.domain.api.client.imgproxy.GetImgUrlArgs;
import io.github.lijiajia3515.cairo.auth.framework.minio.MinioProperties;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurity;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityType;
import io.github.lijiajia3515.cairo.auth.modules.imgproxy.ImgProxyCommonService;
import io.github.lijiajia3515.cairo.web.bind.annotation.BusinessResultBody;
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

import java.util.List;
import java.util.stream.Collectors;

/**
 * [client/api] imgproxy controller
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/client_api/imgproxy")
@CairoSecurity(type = CairoSecurityType.CLIENT)
@BusinessResultBody
public class ImgProxyClientApiController {
	private final String DEFAULT_ACCESS_URL;
	private final ImgProxyCommonService imgProxyCommonService;

	public ImgProxyClientApiController(ImgProxyCommonService imgProxyCommonService, MinioProperties minioProperties) {
		this.imgProxyCommonService = imgProxyCommonService;
		this.DEFAULT_ACCESS_URL = minioProperties.getDefaultAccessUrl();
	}

	/**
	 * 获取图片代理url
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 图片代理url
	 */
	@PostMapping("/get_imgproxy_url")
	@PreAuthorize("isAuthenticated()")
	public List<String> getProxyUrl(@AuthenticationPrincipal CairoOAuthClientPrincipal principal, @Valid @NotNull @NotEmpty @RequestBody List<GetImgUrlArgs> args) {
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
