package io.github.lijiajia3515.cairo.auth.framework.feign.signv1;

import cn.hutool.crypto.SecureUtil;
import io.github.lijiajia3515.cairo.core.CoreConstants;
import feign.RequestInterceptor;
import feign.RequestTemplate;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class CairoFeignSignV1Interceptor implements RequestInterceptor {
	public static final String TIMESTAMP_HEADER_NAME = "Timestamp";
	public static final String NONCE_HEADER_NAME = "Nonce";

	public static final String SIGN_HEADER_NAME = "Sign";

	@Override
	public void apply(RequestTemplate requestTemplate) {
		String nonce = CoreConstants.SNOWFLAKE.nextIdStr();
/*		String raw;
		if (Request.HttpMethod.GET.name().equals(requestTemplate.method())) {
			raw = requestTemplate.url();
		} else {
			raw = new String(requestTemplate.body());
		}
		String contentMd5 = MD5.create().digestHex(raw);*/

		String timestamp = String.valueOf(LocalDateTime.now().toEpochSecond(ZoneOffset.ofHours(8)));
		String encodeKey = "cairo:v1:" + SecureUtil.sha256(String.format("%s_%s", timestamp, nonce));
		requestTemplate.header(TIMESTAMP_HEADER_NAME, timestamp)
			.header(NONCE_HEADER_NAME, nonce)
			.header(SIGN_HEADER_NAME, encodeKey);
	}
}
