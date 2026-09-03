package io.github.lijiajia3515.cairo.auth.framework.sdk.sign;

import cn.hutool.crypto.SecureUtil;
import io.github.lijiajia3515.cairo.core.CoreConstants;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * 签名工具
 */
public class SignSdkTools {
	public static SignResp sign() {
		String nonce = CoreConstants.nextIdStr();
		String timestamp = LocalDateTime.now().toEpochSecond(ZoneOffset.ofHours(8)) + "";
		String encodeKey = "cairo:v1:" + SecureUtil.sha256(String.format("%s_%s", timestamp, nonce));
		return SignResp.builder()
			.timestamp(timestamp)
			.nonce(nonce)
			.sign(encodeKey)
			.build();
	}
}
