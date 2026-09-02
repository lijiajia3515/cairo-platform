package io.github.lijiajia3515.cairo.auth.framework.minio;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.util.Map;

/**
 * minio properties
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class MinioProperties {

	private Map<String, Instance> config;
	/**
	 * 默认错误地址
	 */
	@Builder.Default
	private String defaultS3Url = "s3://public/default.png";
	/**
	 * 默认错误地址
	 */
	@Builder.Default
	private String defaultAccessUrl = "https://minio/public/default.png";

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class Instance {
		/**
		 * endpoint
		 */
		private String endpoint;

		/**
		 * region
		 */
		private String region;

		/**
		 * access key
		 */
		private String username;

		/**
		 * access secret
		 */
		private String password;
	}
}
