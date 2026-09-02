package io.github.lijiajia3515.cairo.auth.framework.http;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.useragent.Browser;
import cn.hutool.http.useragent.Engine;
import cn.hutool.http.useragent.OS;
import cn.hutool.http.useragent.Platform;
import cn.hutool.http.useragent.UserAgent;

public class CairoUserAgentUtil {
	/**
	 * 解析User-Agent
	 *
	 * @param userAgentString User-Agent字符串
	 * @return {@link UserAgent}
	 */
	public static UserAgent parse(String userAgentString) {
		if(StrUtil.isBlank(userAgentString)){
			return new UserAgent();
		}
		final UserAgent userAgent = new UserAgent();

		// 浏览器
		final Browser browser = parseBrowser(userAgentString);
		userAgent.setBrowser(browser);
		userAgent.setVersion(browser.getVersion(userAgentString));

		// 浏览器引擎
		final Engine engine = parseEngine(userAgentString);
		userAgent.setEngine(engine);
		userAgent.setEngineVersion(engine.getVersion(userAgentString));

		// 操作系统
		final OS os = parseOS(userAgentString);
		userAgent.setOs(os);
		userAgent.setOsVersion(os.getVersion(userAgentString));

		// 平台
		final Platform platform = parsePlatform(userAgentString);
		userAgent.setPlatform(platform);
		userAgent.setMobile(platform.isMobile() || browser.isMobile());


		return userAgent;
	}

	/**
	 * 解析浏览器类型
	 *
	 * @param userAgentString User-Agent字符串
	 * @return 浏览器类型
	 */
	private static Browser parseBrowser(String userAgentString) {
		for (Browser browser : UserAgentConstants.BROWERS) {
			if (browser.isMatch(userAgentString)) {
				return browser;
			}
		}
		return Browser.Unknown;
	}

	/**
	 * 解析引擎类型
	 *
	 * @param userAgentString User-Agent字符串
	 * @return 引擎类型
	 */
	private static Engine parseEngine(String userAgentString) {
		for (Engine engine : UserAgentConstants.ENGINES) {
			if (engine.isMatch(userAgentString)) {
				return engine;
			}
		}
		return Engine.Unknown;
	}

	/**
	 * 解析系统类型
	 *
	 * @param userAgentString User-Agent字符串
	 * @return 系统类型
	 */
	private static OS parseOS(String userAgentString) {
		for (OS os : OS.oses) {
			if (os.isMatch(userAgentString)) {
				return os;
			}
		}
		return OS.Unknown;
	}

	/**
	 * 解析平台类型
	 *
	 * @param userAgentString User-Agent字符串
	 * @return 平台类型
	 */
	private static Platform parsePlatform(String userAgentString) {
		for (Platform platform : Platform.platforms) {
			if (platform.isMatch(userAgentString)) {
				return platform;
			}
		}
		return Platform.Unknown;
	}
}
