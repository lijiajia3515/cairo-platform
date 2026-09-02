package io.github.lijiajia3515.cairo.auth.domain.mongodb;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.io.Serializable;
import java.time.Instant;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OAuth2AuthorizationMongodb {
	@MongoId
	private String id;

	@Field(write = Field.Write.ALWAYS)
	private String registeredClientId;

	@Field(write = Field.Write.ALWAYS)
	private String principalName;

	@Field(write = Field.Write.ALWAYS)
	private String authorizationGrantType;

	@Field(write = Field.Write.ALWAYS)
	private Set<String> authorizedScopes;

	@Field(write = Field.Write.ALWAYS)
	private String attributes;


	@Field(write = Field.Write.ALWAYS)
	private String state;

	// authorizationCode
	@Field(write = Field.Write.ALWAYS)
	private Token authorizationCode;

	@Field(write = Field.Write.ALWAYS)
	private IdToken idToken;

	// accessToken
	@Field(write = Field.Write.ALWAYS)
	private AccessToken accessToken;

	// refreshToken
	@Field(write = Field.Write.ALWAYS)
	private RefreshToken refreshToken;

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@SuperBuilder(toBuilder = true)
	public static class Token implements Serializable {

		@Field(write = Field.Write.ALWAYS)
		private String tokenValue;

		@Field(write = Field.Write.ALWAYS)
		private Instant issuedAt;

		@Field(write = Field.Write.ALWAYS)
		private Instant expiresAt;

		@Field(write = Field.Write.ALWAYS)
		private String metadata;
	}

	@Data
	@EqualsAndHashCode(callSuper = true)
	@NoArgsConstructor
	// @AllArgsConstructor
	@SuperBuilder(toBuilder = true)
	public static class IdToken extends Token {

	}

	@Data
	@EqualsAndHashCode(callSuper = true)
	@NoArgsConstructor
	@AllArgsConstructor
	@SuperBuilder(toBuilder = true)
	public static class AccessToken extends Token {

		@Field(write = Field.Write.ALWAYS)
		private String type;

		@Field(write = Field.Write.ALWAYS)
		private Set<String> scopes;
	}

	@Data
	@EqualsAndHashCode(callSuper = true)
	@NoArgsConstructor
	// @AllArgsConstructor
	@SuperBuilder(toBuilder = true)
	public static class RefreshToken extends Token {

	}
}
