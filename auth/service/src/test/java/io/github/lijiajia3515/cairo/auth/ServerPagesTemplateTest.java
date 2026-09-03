package io.github.lijiajia3515.cairo.auth;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import io.github.lijiajia3515.cairo.auth.framework.security.account.CairoAuthAccount;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.web.IWebExchange;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 服务端渲染页(login/index/logout)契约:模板可渲染、共享壳(fragments.html)被引入、
 * 且 formLogin/登出所需表单与链接不因界面改版而破坏。
 * 用 SpringTemplateEngine + servlet WebContext,与 Boot 生产渲染路径一致(SpEL 方言 + @{...} 链接)
 */
class ServerPagesTemplateTest {

	/**
	 * 品牌主色(auth/web TDesign 默认主题),断言它出现在每页 = 共享壳色板已生效
	 */
	private static final String BRAND_COLOR = "#0052d9";

	private String render(String template, Map<String, Object> variables) {
		ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
		resolver.setPrefix("templates/");
		resolver.setSuffix(".html");
		SpringTemplateEngine engine = new SpringTemplateEngine();
		engine.setTemplateResolver(resolver);

		MockServletContext servletContext = new MockServletContext();
		MockHttpServletRequest request = new MockHttpServletRequest(servletContext, "GET", "/" + template);
		IWebExchange exchange = JakartaServletWebApplication.buildApplication(servletContext)
			.buildExchange(request, new MockHttpServletResponse());
		return engine.process(template, new WebContext(exchange, Locale.CHINA, variables));
	}

	/**
	 * 登录页:表单与三方登录链接契约
	 */
	@Test
	void loginKeepsFormLoginContract() {
		String html = render("login", new HashMap<>());

		assertThat(html).contains("action=\"/login\"");
		assertThat(html).contains("method=\"post\"");
		assertThat(html).contains("name=\"username\"");
		assertThat(html).contains("name=\"password\"");
		assertThat(html).contains("name=\"remember-me\"");

		// 协议门契约:勾选框 + 两协议链接指向服务端静态资源
		assertThat(html).contains("id=\"agree\"");
		assertThat(html).contains("/static/agreement/user.html");
		assertThat(html).contains("/static/agreement/privacy.html");

		assertThat(html).contains("/oauth2/authorization/wechat-web");
		assertThat(html).contains("/oauth2/authorization/wechat-open");
		assertThat(html).contains("/oauth2/authorization/dingding");
		assertThat(html).contains("/oauth2/authorization/alipay");
		assertThat(html).contains("/oauth2/authorization/github");
	}

	/**
	 * 登录页:认证失败提示——有消息时展示,无消息时不渲染提示块
	 * (IndexController 传的是消息字符串——Thymeleaf 3.1 SpEL ACL 禁止访问 Throwable 属性)
	 */
	@Test
	void loginRendersAuthenticationExceptionBlock() {
		Map<String, Object> failed = new HashMap<>();
		failed.put("authenticationException", "用户名或密码错误");
		assertThat(render("login", failed)).contains("用户名或密码错误");

		assertThat(render("login", new HashMap<>())).doesNotContain("class=\"alert\"");
	}

	/**
	 * 首页:已登录展示账号信息与登出入口(用真实 CairoAuthAccount,与生产同走 Bean 属性解析)
	 */
	@Test
	void indexShowsAccountWhenLoggedIn() {
		CairoAuthAccount account = CairoAuthAccount.builder()
			.id("account_6482f1a2e")
			.accountId("2095181599214198784")
			.nickname("管理员")
			.loginname("admin")
			.email("admin@example.com")
			.phoneNumber("13800000000")
			.build();
		Map<String, Object> variables = new HashMap<>();
		variables.put("account", account);

		String html = render("index", variables);

		assertThat(html).contains("账号ID");
		assertThat(html).contains("2095181599214198784");
		assertThat(html).contains("会话ID");
		assertThat(html).contains("account_6482f1a2e");
		assertThat(html).contains("管理员");
		assertThat(html).contains("admin@example.com");
		assertThat(html).contains("退出登录");
		assertThat(html).contains("action=\"/logout\"");
		assertThat(html).contains("method=\"post\"");
	}

	/**
	 * 首页:未登录引导去登录
	 */
	@Test
	void indexGuidesToLoginWhenAnonymous() {
		String html = render("index", new HashMap<>());

		assertThat(html).contains("当前无登录用户");
		assertThat(html).contains("href=\"/login\"");
		assertThat(html).doesNotContain("action=\"/logout\"");
	}

	/**
	 * 登出页:确认文案与登出表单
	 */
	@Test
	void logoutShowsConfirmForm() {
		String html = render("logout", new HashMap<>());

		assertThat(html).contains("退出登录");
		assertThat(html).contains("action=\"/logout\"");
		assertThat(html).contains("method=\"post\"");
	}

	/**
	 * 三页统一:共享壳(fragments.html)注入顶栏品牌与 TDesign 品牌色
	 */
	@Test
	void allPagesShareBrandShell() {
		for (String template : new String[]{"login", "index", "logout"}) {
			String html = render(template, new HashMap<>());
			assertThat(html).as("页面 %s 应注入共享色板", template).contains(BRAND_COLOR);
			assertThat(html).as("页面 %s 应注入顶栏品牌", template).contains("Cairo<small>账号</small>");
		}
	}
}
