package io.github.lijiajia3515.cairo.gateway.controller.error;

import io.github.lijiajia3515.cairo.core.business.Business;
import io.github.lijiajia3515.cairo.core.exception.BusinessException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.annotation.security.PermitAll;

@Controller
@RequestMapping
public class VersionErrorController {
	private static final BusinessException ex = new BusinessException("版本停止支持, 请更新Web/App客户端!", new Business() {
		@Override
		public String code() {
			return "DeprecatedVersion";
		}

		@Override
		public String message() {
			return "版本停止支持, 请更新Web/App客户端!";
		}
	});

	@RequestMapping(value = "/error/version/deprecated", produces = MediaType.TEXT_HTML_VALUE)
	@PermitAll
	public void fallbackHtml() {
		throw ex;
	}

	@RequestMapping("/error/version/deprecated")
	@PermitAll
	@ResponseBody
	public void fallback() {
		throw ex;
	}
}
