package io.github.lijiajia3515.cairo.auth.framework.security.password;

import org.springframework.security.crypto.password.PasswordEncoder;


/**
 * 密码生成器容错实现
 */
public class CairoFallbackPasswordEncoder implements PasswordEncoder {


	private static final String DEFAULT_ID_PREFIX = "{";

	private static final String DEFAULT_ID_SUFFIX = "}";

	/**
	 * id前缀
	 */
	private final String idPrefix = DEFAULT_ID_PREFIX;

	/**
	 * id后缀
	 */
	private final String idSuffix = DEFAULT_ID_SUFFIX;

	public CairoFallbackPasswordEncoder() {

	}

	@Override
	public String encode(CharSequence rawPassword) {
		throw new UnsupportedOperationException("encode is not supported");
	}

	@Override
	public boolean matches(CharSequence rawPassword, String prefixEncodedPassword) {
		String id = extractId(prefixEncodedPassword);
		if (id == null) {
			return false;
		}
		throw new IllegalArgumentException("There is no PasswordEncoder mapped for the id \"" + id + "\"");
	}

	private String extractId(String prefixEncodedPassword) {
		if (prefixEncodedPassword == null) {
			return null;
		}
		int start = prefixEncodedPassword.indexOf(this.idPrefix);
		if (start != 0) {
			return null;
		}
		int end = prefixEncodedPassword.indexOf(this.idSuffix, start);
		if (end < 0) {
			return null;
		}
		return prefixEncodedPassword.substring(start + this.idPrefix.length(), end);
	}
}
