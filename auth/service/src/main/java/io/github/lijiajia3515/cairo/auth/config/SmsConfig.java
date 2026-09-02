package io.github.lijiajia3515.cairo.auth.config;

import com.aliyun.auth.credentials.Credential;
import com.aliyun.auth.credentials.provider.ICredentialProvider;
import com.aliyun.auth.credentials.provider.StaticCredentialProvider;
import com.aliyun.sdk.service.dysmsapi20170525.AsyncClient;
import io.github.lijiajia3515.cairo.auth.framework.aliyunsms.AliyunDysmsDestroy;
import io.github.lijiajia3515.cairo.auth.framework.aliyunsms.AliyunDysmsProperties;
import darabonba.core.client.ClientOverrideConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class SmsConfig {

	@Bean
	@ConfigurationProperties("aliyun.dysms")
	AliyunDysmsProperties aliyunDysmsProperties() {
		return new AliyunDysmsProperties();
	}

	@Bean
	ICredentialProvider aliyunDysmsCredentialProvider(AliyunDysmsProperties aliyunDySmsProperties) {
		StaticCredentialProvider provider = StaticCredentialProvider.create(Credential.builder()
			.accessKeyId(aliyunDySmsProperties.getAccessKey())
			.accessKeySecret(aliyunDySmsProperties.getAccessSecret())
			.build());
		return provider;
	}

	@Bean
	AsyncClient aliyunDysmsAsyncClient(ICredentialProvider provider, AliyunDysmsProperties aliyunDySmsProperties) {
		return AsyncClient.builder()
			.region(aliyunDySmsProperties.getRegion())
			.credentialsProvider(provider)
			.overrideConfiguration(
				ClientOverrideConfiguration.create()
					.setEndpointOverride(aliyunDySmsProperties.getEndpoint())
			)
			.build();
	}

	@Bean
	AliyunDysmsDestroy aliyunDysmsDestroy(AsyncClient client) {
		return new AliyunDysmsDestroy(client);
	}
}
