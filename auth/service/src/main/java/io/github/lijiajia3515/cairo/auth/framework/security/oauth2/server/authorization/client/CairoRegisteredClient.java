package io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.client;

import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.ToString;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.oauth2.server.authorization.util.SpringAuthorizationServerVersion;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

@Setter
@ToString
@EqualsAndHashCode(callSuper = false)
public class CairoRegisteredClient extends RegisteredClient {
	private static final long serialVersionUID = SpringAuthorizationServerVersion.SERIAL_VERSION_UID;

	/**
	 * 唯一标识
	 */
	private String id;
	/**
	 * 应用标识
	 */
	private String appId;
	/**
	 * 端标识
	 */
	private String endpointId;
	/**
	 * access_key
	 */
	private String clientId;

	/**
	 * access 到期时间
	 */
	private Instant clientIdIssuedAt;
	/**
	 * access secret
	 */
	private String clientSecret;
	/**
	 * access secret 到期时间
	 */
	private Instant clientSecretExpiresAt;
	/**
	 * 客户端标识
	 */
	private String clientName;
	/**
	 * 认证方法
	 */
	private Set<ClientAuthenticationMethod> clientAuthenticationMethods = new HashSet<>();
	/**
	 * 授权 类型
	 */
	private Set<AuthorizationGrantType> authorizationGrantTypes = new HashSet<>();
	/**
	 * 回跳地址
	 */
	private Set<String> redirectUris = new HashSet<>();
	/**
	 * 默认 scope 设置
	 */
	private Set<String> scopes = new HashSet<>();
	/**
	 * client 设置
	 */
	private ClientSettings clientSettings;
	/**
	 * token 设置
	 */
	private TokenSettings tokenSettings;

	@Override
	public String getId() {
		return id;
	}

	public String getAppId() {
		return appId;
	}

	public String getEndpointId() {
		return endpointId;
	}

	@Override
	public String getClientId() {
		return this.clientId;
	}

	@Override
	public Instant getClientIdIssuedAt() {
		return this.clientIdIssuedAt;
	}

	@Override
	public String getClientSecret() {
		return this.clientSecret;
	}

	@Override
	public Instant getClientSecretExpiresAt() {
		return this.clientSecretExpiresAt;
	}

	@Override
	public String getClientName() {
		return this.clientName;
	}

	@Override
	public Set<ClientAuthenticationMethod> getClientAuthenticationMethods() {
		return this.clientAuthenticationMethods;
	}

	@Override
	public Set<AuthorizationGrantType> getAuthorizationGrantTypes() {
		return this.authorizationGrantTypes;
	}

	@Override
	public Set<String> getRedirectUris() {
		return this.redirectUris;
	}

	@Override
	public Set<String> getScopes() {
		return this.scopes;
	}

	@Override
	public ClientSettings getClientSettings() {
		return this.clientSettings;
	}

	@Override
	public TokenSettings getTokenSettings() {
		return this.tokenSettings;
	}

	/**
	 * Returns a new {@link org.springframework.security.oauth2.server.authorization.client.RegisteredClient.Builder}, initialized with the provided registration identifier.
	 *
	 * @param id the identifier for the registration
	 * @return the {@link org.springframework.security.oauth2.server.authorization.client.RegisteredClient.Builder}
	 */
	public static CairoRegisteredClient.Builder cairoWithId(String id) {
		Assert.hasText(id, "id cannot be empty");
		return new CairoRegisteredClient.Builder(id);
	}

	/**
	 * Returns a new {@link org.springframework.security.oauth2.server.authorization.client.RegisteredClient.Builder}, initialized with the values from the provided {@link RegisteredClient}.
	 *
	 * @param registeredClient the {@link RegisteredClient} used for initializing the {@link org.springframework.security.oauth2.server.authorization.client.RegisteredClient.Builder}
	 * @return the {@link org.springframework.security.oauth2.server.authorization.client.RegisteredClient.Builder}
	 */
	public static Builder cairoForm(CairoRegisteredClient registeredClient) {
		Assert.notNull(registeredClient, "registeredClient cannot be null");
		return new Builder(registeredClient);
	}

	/**
	 * A builder for {@link RegisteredClient}.
	 */
	public static class Builder implements Serializable {

		private static final long serialVersionUID = SpringAuthorizationServerVersion.SERIAL_VERSION_UID;
		private final Set<ClientAuthenticationMethod> clientAuthenticationMethods = new HashSet<>();
		private final Set<AuthorizationGrantType> authorizationGrantTypes = new HashSet<>();
		private final Set<String> redirectUris = new HashSet<>();
		private final Set<String> scopes = new HashSet<>();
		private String id;
		private String appId;

		private String endpointId;
		private String clientId;
		private Instant clientIdIssuedAt;
		private String clientSecret;
		private Instant clientSecretExpiresAt;
		private String clientName;
		private ClientSettings clientSettings;
		private TokenSettings tokenSettings;

		protected Builder(String id) {
			this.id = id;
		}

		protected Builder(CairoRegisteredClient registeredClient) {
			this.id = registeredClient.getId();
			this.appId = registeredClient.getAppId();
			this.endpointId =  registeredClient.getEndpointId();
			this.clientId = registeredClient.getClientId();
			this.clientIdIssuedAt = registeredClient.getClientIdIssuedAt();
			this.clientSecret = registeredClient.getClientSecret();
			this.clientName = registeredClient.getClientName();
			if (!CollectionUtils.isEmpty(registeredClient.getClientAuthenticationMethods())) {
				this.clientAuthenticationMethods.addAll(registeredClient.getClientAuthenticationMethods());
			}
			if (!CollectionUtils.isEmpty(registeredClient.getAuthorizationGrantTypes())) {
				this.authorizationGrantTypes.addAll(registeredClient.getAuthorizationGrantTypes());
			}
			if (!CollectionUtils.isEmpty(registeredClient.getRedirectUris())) {
				this.redirectUris.addAll(registeredClient.getRedirectUris());
			}
			if (!CollectionUtils.isEmpty(registeredClient.getScopes())) {
				this.scopes.addAll(registeredClient.getScopes());
			}
			this.clientSettings = ClientSettings.withSettings(registeredClient.getClientSettings().getSettings()).build();
			this.tokenSettings = TokenSettings.withSettings(registeredClient.getTokenSettings().getSettings()).build();
		}

		private static boolean validateScope(String scope) {
			return scope == null ||
				scope.chars().allMatch(c -> withinTheRangeOf(c, 0x21, 0x21) ||
					withinTheRangeOf(c, 0x23, 0x5B) ||
					withinTheRangeOf(c, 0x5D, 0x7E));
		}

		private static boolean withinTheRangeOf(int c, int min, int max) {
			return c >= min && c <= max;
		}

		private static boolean validateRedirectUri(String redirectUri) {
			try {
				URI validRedirectUri = new URI(redirectUri);
				return validRedirectUri.getFragment() == null;
			} catch (URISyntaxException ex) {
				return false;
			}
		}

		/**
		 * Sets the identifier for the registration.
		 *
		 * @param id the identifier for the registration
		 * @return the {@link Builder}
		 */
		public Builder id(String id) {
			this.id = id;
			return this;
		}

		/**
		 * sets the appId for the registration.
		 *
		 * @param appId the app for the registration
		 * @return the {@link Builder}
		 */
		public Builder appId(String appId) {
			this.appId = appId;
			return this;
		}

		/**
		 * sets the endpointId for the registration.
		 *
		 * @param endpointId the app for the registration
		 * @return the {@link Builder}
		 */
		public Builder endpointId(String endpointId) {
			this.endpointId = endpointId;
			return this;
		}

		/**
		 * Sets the client identifier.
		 *
		 * @param clientId the client identifier
		 * @return the {@link Builder}
		 */
		public Builder clientId(String clientId) {
			this.clientId = clientId;
			return this;
		}

		/**
		 * Sets the time at which the client identifier was issued.
		 *
		 * @param clientIdIssuedAt the time at which the client identifier was issued
		 * @return the {@link Builder}
		 */
		public Builder clientIdIssuedAt(Instant clientIdIssuedAt) {
			this.clientIdIssuedAt = clientIdIssuedAt;
			return this;
		}

		/**
		 * Sets the client secret.
		 *
		 * @param clientSecret the client secret
		 * @return the {@link Builder}
		 */
		public Builder clientSecret(String clientSecret) {
			this.clientSecret = clientSecret;
			return this;
		}

		/**
		 * Sets the time at which the client secret expires or {@code null} if it does not expire.
		 *
		 * @param clientSecretExpiresAt the time at which the client secret expires or {@code null} if it does not expire
		 * @return the {@link Builder}
		 */
		public Builder clientSecretExpiresAt(Instant clientSecretExpiresAt) {
			this.clientSecretExpiresAt = clientSecretExpiresAt;
			return this;
		}

		/**
		 * Sets the client name.
		 *
		 * @param clientName the client name
		 * @return the {@link Builder}
		 */
		public Builder clientName(String clientName) {
			this.clientName = clientName;
			return this;
		}

		/**
		 * Adds an {@link ClientAuthenticationMethod authentication method}
		 * the client may use when authenticating with the authorization server.
		 *
		 * @param clientAuthenticationMethod the authentication method
		 * @return the {@link Builder}
		 */
		public Builder clientAuthenticationMethod(ClientAuthenticationMethod clientAuthenticationMethod) {
			this.clientAuthenticationMethods.add(clientAuthenticationMethod);
			return this;
		}

		/**
		 * A {@code Consumer} of the {@link ClientAuthenticationMethod authentication method(s)}
		 * allowing the ability to add, replace, or remove.
		 *
		 * @param clientAuthenticationMethodsConsumer a {@code Consumer} of the authentication method(s)
		 * @return the {@link Builder}
		 */
		public Builder clientAuthenticationMethods(
			Consumer<Set<ClientAuthenticationMethod>> clientAuthenticationMethodsConsumer) {
			clientAuthenticationMethodsConsumer.accept(this.clientAuthenticationMethods);
			return this;
		}

		/**
		 * Adds an {@link AuthorizationGrantType authorization grant type} the client may use.
		 *
		 * @param authorizationGrantType the authorization grant type
		 * @return the {@link Builder}
		 */
		public Builder authorizationGrantType(AuthorizationGrantType authorizationGrantType) {
			this.authorizationGrantTypes.add(authorizationGrantType);
			return this;
		}

		/**
		 * A {@code Consumer} of the {@link AuthorizationGrantType authorization grant type(s)}
		 * allowing the ability to add, replace, or remove.
		 *
		 * @param authorizationGrantTypesConsumer a {@code Consumer} of the authorization grant type(s)
		 * @return the {@link Builder}
		 */
		public Builder authorizationGrantTypes(Consumer<Set<AuthorizationGrantType>> authorizationGrantTypesConsumer) {
			authorizationGrantTypesConsumer.accept(this.authorizationGrantTypes);
			return this;
		}

		/**
		 * Adds a redirect URI the client may use in a redirect-based flow.
		 *
		 * @param redirectUri the redirect URI
		 * @return the {@link Builder}
		 */
		public Builder redirectUri(String redirectUri) {
			this.redirectUris.add(redirectUri);
			return this;
		}

		/**
		 * A {@code Consumer} of the redirect URI(s)
		 * allowing the ability to add, replace, or remove.
		 *
		 * @param redirectUrisConsumer a {@link Consumer} of the redirect URI(s)
		 * @return the {@link Builder}
		 */
		public Builder redirectUris(Consumer<Set<String>> redirectUrisConsumer) {
			redirectUrisConsumer.accept(this.redirectUris);
			return this;
		}

		/**
		 * Adds a scope the client may use.
		 *
		 * @param scope the scope
		 * @return the {@link Builder}
		 */
		public Builder scope(String scope) {
			this.scopes.add(scope);
			return this;
		}

		/**
		 * A {@code Consumer} of the scope(s)
		 * allowing the ability to add, replace, or remove.
		 *
		 * @param scopesConsumer a {@link Consumer} of the scope(s)
		 * @return the {@link Builder}
		 */
		public Builder scopes(Consumer<Set<String>> scopesConsumer) {
			scopesConsumer.accept(this.scopes);
			return this;
		}

		/**
		 * Sets the {@link ClientSettings client configuration settings}.
		 *
		 * @param clientSettings the client configuration settings
		 * @return the {@link Builder}
		 */
		public Builder clientSettings(ClientSettings clientSettings) {
			this.clientSettings = clientSettings;
			return this;
		}

		/**
		 * Sets the {@link TokenSettings token configuration settings}.
		 *
		 * @param tokenSettings the token configuration settings
		 * @return the {@link Builder}
		 */
		public Builder tokenSettings(TokenSettings tokenSettings) {
			this.tokenSettings = tokenSettings;
			return this;
		}

		/**
		 * Builds a new {@link CairoRegisteredClient}.
		 *
		 * @return a {@link CairoRegisteredClient}
		 */
		public CairoRegisteredClient build() {
			Assert.hasText(this.clientId, "clientId cannot be empty");
			Assert.notEmpty(this.authorizationGrantTypes, "authorizationGrantTypes cannot be empty");
			if (this.authorizationGrantTypes.contains(AuthorizationGrantType.AUTHORIZATION_CODE)) {
				Assert.notEmpty(this.redirectUris, "redirectUris cannot be empty");
			}
			if (!StringUtils.hasText(this.clientName)) {
				this.clientName = this.id;
			}
			if (CollectionUtils.isEmpty(this.clientAuthenticationMethods)) {
				this.clientAuthenticationMethods.add(ClientAuthenticationMethod.CLIENT_SECRET_BASIC);
			}
			validateScopes();
			validateRedirectUris();
			return create();
		}

		private CairoRegisteredClient create() {
			CairoRegisteredClient client = new CairoRegisteredClient();
			client.id = this.id;
			client.appId = this.appId;
			client.endpointId = this.endpointId;
			client.clientId = this.clientId;
			client.clientIdIssuedAt = this.clientIdIssuedAt;
			client.clientSecret = this.clientSecret;
			client.clientSecretExpiresAt = this.clientSecretExpiresAt;
			client.clientName = this.clientName;
			client.clientAuthenticationMethods = Set.copyOf(this.clientAuthenticationMethods);
			client.authorizationGrantTypes = Set.copyOf(this.authorizationGrantTypes);
			client.redirectUris = Set.copyOf(this.redirectUris);
			client.scopes = Set.copyOf(this.scopes);
			client.clientSettings = this.clientSettings != null ? this.clientSettings : ClientSettings.builder().build();
			client.tokenSettings = this.tokenSettings != null ? this.tokenSettings : TokenSettings.builder().build();

			return client;
		}

		private void validateScopes() {
			if (CollectionUtils.isEmpty(this.scopes)) {
				return;
			}

			for (String scope : this.scopes) {
				Assert.isTrue(validateScope(scope), "scope \"" + scope + "\" contains invalid characters");
			}
		}

		private void validateRedirectUris() {
			if (CollectionUtils.isEmpty(this.redirectUris)) {
				return;
			}

			for (String redirectUri : redirectUris) {
				Assert.isTrue(validateRedirectUri(redirectUri),
					"redirect_uri \"" + redirectUri + "\" is not a valid redirect URI or contains fragment");
			}
		}
	}
}
