package io.github.lijiajia3515.cairo.auth.framework.aliyunsms;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AliyunDysmsProperties {

	/**
	 * endpoint
	 */
	private String endpoint = "dysmsapi.aliyuncs.com";

	/**
	 * region
	 */
	private String region = "cn-hangzhou";

	/**
	 * accessKey
	 */
	private String accessKey;

	/**
	 * accessSecret
	 */
	private String accessSecret;



}
