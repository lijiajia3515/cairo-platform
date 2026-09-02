package io.github.lijiajia3515.cairo.auth.domain.dto.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientSettings {
	private Boolean requireProofKey;
	private Boolean requireAuthorizationConsent;
	private String jwkSetUrl;
	private String tokenEndpointAuthenticationSigningAlgorithm;

}

