package io.github.lijiajia3515.cairo.auth.domain.mongodb;

import io.github.lijiajia3515.cairo.auth.domain.mongodb.field.AbstractAppUserMetadataMongodbField;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.field.AbstractClientMetadataMongodbField;
import io.github.lijiajia3515.cairo.mongodb.data.mapping.model.AbstractMongodbField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
	 * OAuth2 客户端
	 * <p>
	 * 对应 Spring Authorization Server 的 RegisteredClient，持久化至 MongoDB auth_client 集合
	 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientMongodb {

	// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
	// 基础标识
	// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

	/**
	 * MongoDB 文档主键
	 */
	@MongoId
	private ObjectId _id;

	/**
	 * 业务主键
	 */
	@Field(write = Field.Write.ALWAYS)
	private String id;

	// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
	// 应用归属
	// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

	/**
	 * 应用ID
	 * 所属应用的唯一标识
	 */
	@Field(write = Field.Write.ALWAYS)
	private String appId;

	/**
	 * 终端ID
	 * 所属终端 ID，为空时表示客户端不绑定特定终端
	 */
	@Field(write = Field.Write.ALWAYS)
	private String endpointId;

	// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
	// OAuth2 客户端凭证
	// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

	/**
	 * 客户端ID
	 * OAuth2 客户端 ID，用于客户端认证时标识身份，具有唯一索引
	 */
	@Field(write = Field.Write.ALWAYS)
	private String clientId;

	/**
	 * OAuth2 客户端密钥
	 * OAuth2 客户端密钥，用于 client_secret_basic / client_secret_post 等认证方式
	 */
	@Field(write = Field.Write.ALWAYS)
	private String clientSecret;

	/**
	 * 客户端名称
	 * 客户端名称，用于管理后台展示和识别
	 */
	@Field(write = Field.Write.ALWAYS)
	private String clientName;

	// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
	// OAuth2 授权配置
	// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

	/**
	 * 客户端认证方式列表（如 client_secret_basic、client_secret_post、private_key_jwt 等）
	 */
	@Builder.Default
	@Field(write = Field.Write.ALWAYS)
	private List<String> clientAuthenticationMethods = Collections.emptyList();

	/**
	 * 授权类型列表（如 authorization_code、client_credentials、refresh_token 等）
	 */
	@Builder.Default
	@Field(write = Field.Write.ALWAYS)
	private List<String> authorizationGrantTypes = Collections.emptyList();

	/**
	 * 权限范围列表
	 * 权限范围列表，定义客户端可请求的资源访问权限
	 */
	@Builder.Default
	@Field(write = Field.Write.ALWAYS)
	private List<String> scopes = Collections.emptyList();

	/**
	 * OAuth2 授权码回调地址白名单
	 */
	@Builder.Default
	@Field(write = Field.Write.ALWAYS)
	private List<String> redirectUris = Collections.emptyList();

	// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
	// 认证与身份配置
	// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

	/**
	 * 身份类型列表
	 * 身份类型列表，用于区分客户端适用的认证场景（如密码、短信、社交等）
	 */
	@Builder.Default
	@Field(write = Field.Write.ALWAYS)
	private List<String> authenticationTypes = Collections.emptyList();

	/**
	 * 账号第三方社交认证供应商 ID 列表
	 * 账号第三方社交认证供应商 ID 列表，关联 auth_sns_provider 集合中的供应商
	 */
	@Builder.Default
	@Field(write = Field.Write.ALWAYS)
	private List<String> accountSnsProviderIds = Collections.emptyList();

	// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
	// 客户端配置与令牌配置
	// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

	/**
	 * 客户端行为配置（PKCE、授权同意页、JWK 等）
	 */
	@Builder.Default
	@Field(write = Field.Write.ALWAYS)
	private ClientSettings clientSettings = new ClientSettings();

	/**
	 * 各类令牌的格式、有效期及刷新策略配置
	 */
	@Builder.Default
	@Field(write = Field.Write.ALWAYS)
	private TokenSettings tokenSettings = new TokenSettings();

	// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
	// 状态与审计
	// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

	/**
	 * 启用状态
	 * 是否启用，禁用后客户端无法进行认证和授权
	 */
	@Builder.Default
	@Field(write = Field.Write.ALWAYS)
	private Boolean enabled = false;

	/**
	 * 最近一次成功认证时间
	 * 最近一次成功认证时间，为空表示从未使用过
	 */
	@Field(write = Field.Write.ALWAYS)
	private LocalDateTime loginTime;

	/**
	 * 审计元信息（创建人、创建时间、更新人、更新时间）
	 * 元信息，包含创建与更新的用户及时间
	 */
	@Builder.Default
	@Field(write = Field.Write.ALWAYS)
	private AppUserMetadataMongodb metadata = new AppUserMetadataMongodb();

	/**
	 * 客户端行为配置
	 * <p>
	 * 控制 OAuth2 客户端的安全策略与行为参数，对应 Spring Authorization Server 的 ClientSettings
	 */
	@Data
	@Accessors(chain = true)
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class ClientSettings {

		/**
	 * 是否要求 PKCE（Proof Key for Code Exchange）
	 * 是否要求 PKCE（Proof Key for Code Exchange），公开客户端应设为 true
	 */
		@Field(write = Field.Write.ALWAYS)
		private Boolean requireProofKey;

		/**
	 * 是否要求用户授权同意确认
	 * 是否要求用户授权同意确认，设为 true 时授权流程中会展示同意页面
	 */
		@Field(write = Field.Write.ALWAYS)
		private Boolean requireAuthorizationConsent;

		/**
	 * JSON Web Key Set URL
	 * JSON Web Key Set URL，用于 private_key_jwt 认证方式时获取客户端公钥
	 */
		@Field(write = Field.Write.ALWAYS)
		private String jwkSetUrl;

		/**
	 * Token 端点认证签名算法（如 RS256）
	 * Token 端点认证签名算法（如 RS256），用于 private_key_jwt 认证方式
	 */
		@Field(write = Field.Write.ALWAYS)
		private String tokenEndpointAuthenticationSigningAlgorithm;
	}

	/**
	 * 令牌配置
	 * <p>
	 * 按身份维度分组管理各类令牌的格式、有效期及刷新策略。
	 * 默认令牌组用于 client_credentials 场景，其余各组对应不同用户身份类型。
	 */
	@Data
	@Accessors(chain = true)
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class TokenSettings {

		// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
		// ID Token 配置（OpenID Connect）
		// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

		/**
		 * ID Token 签名算法（如 RS256、ES256）
		 */
		@Field(write = Field.Write.ALWAYS)
		private String idTokenSignatureAlgorithm;

		/**
		 * ID Token 格式（如 self-contained JWT）
		 */
		@Field(write = Field.Write.ALWAYS)
		private String idTokenFormat;

		/**
		 * ID Token 有效期
		 */
		@Field(write = Field.Write.ALWAYS)
		private Duration idTokenTimeToLive;

		// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
		// 默认令牌配置（client_credentials 客户端凭证授权场景）
		// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

		/**
		 * Access Token 格式（如 self-contained JWT 或 opaque 不透明令牌）
		 */
		@Field(write = Field.Write.ALWAYS)
		private String accessTokenFormat;

		/**
		 * Access Token 有效期
		 */
		@Field(write = Field.Write.ALWAYS)
		private Duration accessTokenTimeToLive;

		/**
		 * Refresh Token 有效期
		 */
		@Field(write = Field.Write.ALWAYS)
		private Duration refreshTokenTimeToLive;

		/**
		 * 是否允许重用 Refresh Token 刷新 Access Token，
		 * true 表示同一个 Refresh Token 可多次使用，false 表示每次刷新后旧 Token 失效
		 */
		@Field(write = Field.Write.ALWAYS)
		private Boolean reuseRefreshTokens;

		// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
		// Account 令牌配置（系统管理账号维度）
		// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

		/**
		 * Account Access Token 格式
		 */
		@Field(write = Field.Write.ALWAYS)
		private String accountAccessTokenFormat;

		/**
		 * Account Access Token 有效期
		 */
		@Field(write = Field.Write.ALWAYS)
		private Duration accountAccessTokenTimeToLive;

		/**
		 * Account Refresh Token 有效期
		 */
		@Field(write = Field.Write.ALWAYS)
		private Duration accountRefreshTokenTimeToLive;

		/**
		 * 是否允许重用 Account Refresh Token 刷新 Access Token
		 */
		@Field(write = Field.Write.ALWAYS)
		private Boolean reuseAccountRefreshTokens;

		// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
		// App User 令牌配置（应用级用户维度）
		// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

		/**
		 * App User Access Token 格式
		 */
		@Field(write = Field.Write.ALWAYS)
		private String appUserAccessTokenFormat;

		/**
		 * App User Access Token 有效期
		 */
		@Field(write = Field.Write.ALWAYS)
		private Duration appUserAccessTokenTimeToLive;

		/**
		 * App User Refresh Token 有效期
		 */
		@Field(write = Field.Write.ALWAYS)
		private Duration appUserRefreshTokenTimeToLive;

		/**
		 * 是否允许重用 App User Refresh Token 刷新 Access Token
		 */
		@Field(write = Field.Write.ALWAYS)
		private Boolean reuseAppUserRefreshTokens;

		// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
		// Tenant App User 令牌配置（企业应用级用户维度）
		// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

		/**
		 * Tenant App User Access Token 格式
		 */
		@Field(write = Field.Write.ALWAYS)
		private String tenantAppUserAccessTokenFormat;

		/**
		 * Tenant App User Access Token 有效期
		 */
		@Field(write = Field.Write.ALWAYS)
		private Duration tenantAppUserAccessTokenTimeToLive;

		/**
		 * Tenant App User Refresh Token 有效期
		 */
		@Field(write = Field.Write.ALWAYS)
		private Duration tenantAppUserRefreshTokenTimeToLive;

		/**
		 * 是否允许重用 Tenant App User Refresh Token 刷新 Access Token
		 */
		@Field(write = Field.Write.ALWAYS)
		private Boolean reuseTenantAppUserRefreshTokens;
	}

	/**
	 * MongoDB 查询字段常量
	 * <p>
	 * 提供类型安全的字段名引用，避免在 Criteria / Update 中使用硬编码字符串
	 */
	public static final MongodbField FIELD = new MongodbField();

	public static final class MongodbField extends AbstractAppUserMetadataMongodbField {
		private MongodbField() {
		}

		// ── 基础标识 ──────────────────────────────────
		public final String ID = field("id");

		// ── 应用归属 ──────────────────────────────────
		public final String APP_ID = field("appId");
		public final String ENDPOINT_ID = field("endpointId");

		// ── OAuth2 客户端凭证 ─────────────────────────
		public final String CLIENT_ID = field("clientId");
		public final String CLIENT_SECRET = field("clientSecret");
		public final String CLIENT_NAME = field("clientName");

		// ── OAuth2 授权配置 ───────────────────────────
		public final String CLIENT_AUTHENTICATION_METHODS = field("clientAuthenticationMethods");
		public final String AUTHORIZATION_GRANT_TYPES = field("authorizationGrantTypes");
		public final String SCOPES = field("scopes");
		public final String REDIRECT_URIS = field("redirectUris");

		// ── 认证与身份配置 ────────────────────────────
		public final String AUTHENTICATION_TYPES = field("authenticationTypes");
		public final String ACCOUNT_SNS_PROVIDER_IDS = field("accountSnsProviderIds");

		// ── 嵌套配置对象 ──────────────────────────────
		public final ClientSettings CLIENT_SETTINGS = new ClientSettings(this, "clientSettings");
		public final TokenSettings TOKEN_SETTINGS = new TokenSettings(this, "tokenSettings");

		// ── 状态与审计 ────────────────────────────────
		public final String ENABLED = field("enabled");
		public final String LOGIN_TIME = field("loginTime");

		/**
		 * 客户端行为配置字段常量
		 */
		public static final class ClientSettings extends AbstractClientMetadataMongodbField {
			public ClientSettings(AbstractMongodbField parent, String prefix) {
				super(parent, prefix);
			}

			public final String REQUIRE_PROOF_KEY = field("requireProofKey");
			public final String REQUIRE_AUTHORIZATION_CONSENT = field("requireAuthorizationConsent");
			public final String JWK_SET_URL = field("jwkSetUrl");
			public final String TOKEN_ENDPOINT_AUTHENTICATION_SIGNING_ALGORITHM = field("tokenEndpointAuthenticationSigningAlgorithm");
		}

		/**
		 * 令牌配置字段常量，按身份维度分组，与 TokenSettings 数据类的字段一一对应
		 */
		public static final class TokenSettings extends AbstractClientMetadataMongodbField {
			public TokenSettings(AbstractMongodbField parent, String prefix) {
				super(parent, prefix);
			}

			// ── ID Token ──────────────────────────────
			public final String ID_TOKEN_SIGNATURE_ALGORITHM = field("idTokenSignatureAlgorithm");
			public final String ID_TOKEN_FORMAT = field("idTokenFormat");
			public final String ID_TOKEN_TIME_TO_LIVE = field("idTokenTimeToLive");

			// ── 默认令牌（client_credentials） ────────
			public final String ACCESS_TOKEN_FORMAT = field("accessTokenFormat");
			public final String ACCESS_TOKEN_TIME_TO_LIVE = field("accessTokenTimeToLive");
			public final String REFRESH_TOKEN_TIME_TO_LIVE = field("refreshTokenTimeToLive");
			public final String REUSE_REFRESH_TOKENS = field("reuseRefreshTokens");

			// ── Account 令牌 ──────────────────────────
			public final String ACCOUNT_ACCESS_TOKEN_FORMAT = field("accountAccessTokenFormat");
			public final String ACCOUNT_ACCESS_TOKEN_TIME_TO_LIVE = field("accountAccessTokenTimeToLive");
			public final String ACCOUNT_REFRESH_TOKEN_TIME_TO_LIVE = field("accountRefreshTokenTimeToLive");
			public final String REUSE_ACCOUNT_REFRESH_TOKENS = field("reuseAccountRefreshTokens");

			// ── App User 令牌 ────────────────
			public final String APP_USER_ACCESS_TOKEN_FORMAT = field("appUserAccessTokenFormat");
			public final String APP_USER_ACCESS_TOKEN_TIME_TO_LIVE = field("appUserAccessTokenTimeToLive");
			public final String APP_USER_REFRESH_TOKEN_TIME_TO_LIVE = field("appUserRefreshTokenTimeToLive");
			public final String REUSE_APP_USER_REFRESH_TOKENS = field("reuseAppUserRefreshTokens");

			// ── Tenant App User 令牌 ─────────
			public final String TENANT_APP_USER_ACCESS_TOKEN_FORMAT = field("tenantAppUserAccessTokenFormat");
			public final String TENANT_APP_USER_ACCESS_TOKEN_TIME_TO_LIVE = field("tenantAppUserAccessTokenTimeToLive");
			public final String TENANT_APP_USER_REFRESH_TOKEN_TIME_TO_LIVE = field("tenantAppUserRefreshTokenTimeToLive");
			public final String REUSE_TENANT_APP_USER_REFRESH_TOKENS = field("reuseTenantAppUserRefreshTokens");
		}
	}
}
