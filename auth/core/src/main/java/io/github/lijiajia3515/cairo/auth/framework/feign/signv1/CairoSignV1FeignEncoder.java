package io.github.lijiajia3515.cairo.auth.framework.feign.signv1;

import cn.hutool.crypto.SecureUtil;
import io.github.lijiajia3515.cairo.core.CoreConstants;
import feign.RequestTemplate;
import feign.codec.EncodeException;
import feign.codec.Encoder;
import org.springframework.cloud.openfeign.support.PageableSpringEncoder;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.ZoneOffset;


public class CairoSignV1FeignEncoder extends PageableSpringEncoder {

	public static final String TIMESTAMP_HEADER_NAME = "Timestamp";
	public static final String NONCE_HEADER_NAME = "Nonce";

	public static final String SIGN_HEADER_NAME = "Sign";

	public CairoSignV1FeignEncoder(Encoder encoder) {
		super(encoder);
	}

	@Override
	public void encode(Object requestBody, Type bodyType, RequestTemplate request) throws EncodeException {
		if (bodyType == CairoFeignSignV1.class) {
			String nonce = CoreConstants.SNOWFLAKE.nextIdStr();
			String timestamp = LocalDateTime.now().toEpochSecond(ZoneOffset.ofHours(8)) + "";
			String encodeKey = "cairo:v1:" + SecureUtil.sha256(String.format("%s_%s", timestamp, nonce));
			request.header(TIMESTAMP_HEADER_NAME, timestamp)
				.header(NONCE_HEADER_NAME, nonce)
				.header(SIGN_HEADER_NAME, encodeKey);

		} else {
			super.encode(requestBody, bodyType, request);
		}
	}
}
