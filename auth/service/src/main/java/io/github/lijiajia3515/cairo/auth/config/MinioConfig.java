package io.github.lijiajia3515.cairo.auth.config;

import io.github.lijiajia3515.cairo.auth.framework.minio.MinioProperties;
import io.minio.MinioClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;

@Configuration(proxyBeanMethods = false)
public class MinioConfig {
	/**
	 * 管理实例
	 */
	public static final String MANAGE = "manage";

	/**
	 * 用户实例
	 */
	public static final String ADMIN = "admin";

	@ConfigurationProperties(prefix = "minio")
	@Bean
	MinioProperties minioProperties() {
		return new MinioProperties();
	}

	@Bean
	public MinioClient manageMinioClient(MinioProperties properties) {
		MinioProperties.Instance instance = properties.getConfig().get(MANAGE);
		Assert.notNull(instance, "minio manage config not null");
		return MinioClient.builder()
			.endpoint(instance.getEndpoint())
			.region(instance.getRegion())
			.credentials(instance.getUsername(), instance.getPassword())
			.build();
	}

	@Bean
	public MinioClient adminMinioClient(MinioProperties properties) {
		MinioProperties.Instance instance = properties.getConfig().get(ADMIN);
		Assert.notNull(instance, "minio admin config not null");
		return MinioClient.builder()
			.endpoint(instance.getEndpoint())
			.region(instance.getRegion())
			.credentials(instance.getUsername(), instance.getPassword())
			.build();
	}
}
