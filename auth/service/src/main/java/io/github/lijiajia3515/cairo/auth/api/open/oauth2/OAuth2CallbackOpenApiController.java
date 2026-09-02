package io.github.lijiajia3515.cairo.auth.api.open.oauth2;

import cn.hutool.core.net.url.UrlBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

import jakarta.servlet.http.HttpServletRequest;
import org.hibernate.validator.constraints.URL;
import jakarta.validation.Valid;
import java.util.Map;

/**
 * [open_api/oauth2] oauth2 controller
 */
@Slf4j
@Validated
@Controller
@RequestMapping("/open_api/oauth2")
public class OAuth2CallbackOpenApiController {
	@GetMapping("/callback")
	public RedirectView redirectView(@Valid @URL @RequestParam("redirect_uri") String redirectUri, HttpServletRequest request) {
		UrlBuilder urlBuilder = UrlBuilder.of(redirectUri);
		Map<String, String[]> parameterMap = request.getParameterMap();

		parameterMap.forEach((key, values) -> {
			if (key.equals("redirect_uri")) return;
			if (values != null) {
				for (String value : values) {
					urlBuilder.addQuery(key, value);
				}
			} else {
				urlBuilder.addQuery(key, "");
			}
		});
		String url = urlBuilder.build();
		log.info("redirectUrl: {}", url);
		return new RedirectView(url);
	}
}
