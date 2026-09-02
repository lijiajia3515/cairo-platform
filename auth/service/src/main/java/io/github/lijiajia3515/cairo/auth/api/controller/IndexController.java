package io.github.lijiajia3515.cairo.auth.api.controller;

import io.github.lijiajia3515.cairo.auth.framework.security.account.CairoAuthAccount;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.WebAttributes;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Objects;

/**
 * 服务端渲染入口——返回 templates/ 下的 Thymeleaf 视图名
 * (登录页供 OAuth2 授权码流程 formLogin 跳转使用,不可用 @RestController)
 */
@Slf4j
@Controller
@RequestMapping
public  class IndexController {

	@GetMapping
	public String index(@AuthenticationPrincipal CairoAuthAccount account,
						HttpServletRequest request,
						HttpSession session,
						Model model) {
		model.addAttribute("account", account);
		session.setAttribute("A", "account");
		return "index";
	}

	@GetMapping("/login")
	public String login(Authentication authentication, HttpSession session, Model model) {
		if (authentication != null && !(authentication instanceof AnonymousAuthenticationToken)) return "index";
		// 传消息字符串而非异常对象:Thymeleaf 3.1 SpEL ACL 禁止访问 Throwable 属性,模板取 ${...message} 会 500
		Exception authenticationException = (Exception) session.getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
		if (authenticationException != null) {
			session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
			model.addAttribute("authenticationException",
					Objects.requireNonNullElse(authenticationException.getMessage(), "登录失败"));
		}
		return "login";
	}

	@GetMapping("/logout")
	public String logout() {
		return "logout";
	}

}
