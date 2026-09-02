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
public class FeatureErrorController {
	private static final BusinessException ex = new BusinessException("暂未开通, 敬请期待!", new Business() {
		@Override
		public String code() {
			return "FeatureNotOpened";
		}

		@Override
		public String message() {
			return "暂未开通, 敬请期待!";
		}
	});

	@RequestMapping(value = "/error/feature/not_opened", produces = MediaType.TEXT_HTML_VALUE)
	@PermitAll
	public void fallbackHtml() {
		throw ex;
	}

	@RequestMapping("/error/feature/not_opened")
	@PermitAll
	@ResponseBody
	public void fallback() {
		throw ex;
	}
}
