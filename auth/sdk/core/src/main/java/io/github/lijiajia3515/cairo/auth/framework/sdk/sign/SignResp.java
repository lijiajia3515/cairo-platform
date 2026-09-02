package io.github.lijiajia3515.cairo.auth.framework.sdk.sign;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignResp {
	/**
	 * 请求时间戳
	 */
	private String timestamp;
	/**
	 * 随机数值
	 */
	private String nonce;
	/**
	 * 签名
	 */
	private String sign;
}
