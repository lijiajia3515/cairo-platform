package io.github.lijiajia3515.cairo.web.utils;


public class CairoWebExchangeUtils {
	public static final String REQUEST_ID_ATTRIBUTE = qualify("requestId");

	private static String qualify(String attr) {
		return CairoWebExchangeUtils.class.getName() + "." + attr;
	}
}
