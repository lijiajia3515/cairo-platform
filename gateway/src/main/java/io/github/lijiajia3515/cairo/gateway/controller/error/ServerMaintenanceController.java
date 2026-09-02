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
public class ServerMaintenanceController {
	private static final BusinessException ex = new BusinessException("服务器维护中, 请稍后使用!", new Business() {
		@Override
		public String code() {
			return "ServerMaintenance";
		}

		@Override
		public String message() {
			return "服务器维护中, 请稍后使用!";
		}
	});

	@RequestMapping(value = "/error/server/maintenance", produces = MediaType.TEXT_HTML_VALUE)
	@PermitAll
	public void fallbackHtml() {
		throw ex;
	}

	@RequestMapping("/error/server/maintenance")
	@PermitAll
	@ResponseBody
	public void fallback() {
		throw ex;
	}
}
