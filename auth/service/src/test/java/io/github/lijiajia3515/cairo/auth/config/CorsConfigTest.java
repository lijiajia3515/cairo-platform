package io.github.lijiajia3515.cairo.auth.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.filter.CorsFilter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CorsConfigTest {

	private final CorsConfig corsConfig = new CorsConfig();

	private CorsFilter filter() {
		FilterRegistrationBean<CorsFilter> registration = corsConfig.corsFilterRegistration(corsConfig.corsConfigurationSource());
		return registration.getFilter();
	}

	@Test
	void preflightOnOpenApiTokenIsAllowed() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", "/open_api/oauth2/token");
		request.addHeader("Origin", "http://127.0.0.1:5199");
		request.addHeader("Access-Control-Request-Method", "POST");
		request.addHeader("Access-Control-Request-Headers", "content-type");
		MockHttpServletResponse response = new MockHttpServletResponse();

		filter().doFilter(request, response, new MockFilterChain());

		assertEquals(200, response.getStatus());
		assertEquals("http://127.0.0.1:5199", response.getHeader("Access-Control-Allow-Origin"));
		assertEquals("true", response.getHeader("Access-Control-Allow-Credentials"));
	}

	@Test
	void actualRequestOnOpenApiTokenGetsOriginHeader() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/open_api/oauth2/token");
		request.addHeader("Origin", "http://127.0.0.1:5199");
		MockHttpServletResponse response = new MockHttpServletResponse();

		MockFilterChain chain = new MockFilterChain();
		filter().doFilter(request, response, chain);

		assertEquals("http://127.0.0.1:5199", response.getHeader("Access-Control-Allow-Origin"));
		// 实际请求必须继续走后续过滤器链
		assertTrue(chain.getRequest() != null);
	}

	@Test
	void httpsOriginIsAllowed() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", "/open_api/oauth2/token");
		request.addHeader("Origin", "https://console.example.com");
		request.addHeader("Access-Control-Request-Method", "POST");
		MockHttpServletResponse response = new MockHttpServletResponse();

		filter().doFilter(request, response, new MockFilterChain());

		assertEquals(200, response.getStatus());
		assertEquals("https://console.example.com", response.getHeader("Access-Control-Allow-Origin"));
	}
}
