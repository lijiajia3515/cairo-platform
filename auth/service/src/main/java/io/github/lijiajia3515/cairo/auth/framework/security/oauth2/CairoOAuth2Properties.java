package io.github.lijiajia3515.cairo.auth.framework.security.oauth2;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@Builder

@ConfigurationProperties(prefix = "cairo.auth.oauth2")
@Configuration
public class CairoOAuth2Properties {
	private String issuer;
	private List<RsaKeys> rsaKeys;

	@Data

	@Accessors(chain = true)
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class RsaKeys {
		private String id;
		private String privateKey;
		private String publicKey;

	}
}
