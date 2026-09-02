package io.github.lijiajia3515.cairo.auth.api.open.link;


import io.github.lijiajia3515.cairo.web.bind.annotation.BusinessResultBody;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

/**
 * [open/api] link controller
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/open_api/link")
@BusinessResultBody
@RequiredArgsConstructor
public class LinkOpenApiController {

	private final LinkOpenApiService linkOpenApiService;

	/**
	 * 访问链接服务
	 * @param linkId 短链ID
	 * @return 重定向链接地址
	 */
	@GetMapping("/access/{linkId}")
	public RedirectView linkOpenApi(@PathVariable("linkId") String linkId) {
		return new RedirectView(linkOpenApiService.getLinkUrl(linkId));
	}
}
