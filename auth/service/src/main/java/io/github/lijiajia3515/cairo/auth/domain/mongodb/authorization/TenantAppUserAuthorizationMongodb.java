package io.github.lijiajia3515.cairo.auth.domain.mongodb.authorization;

import io.github.lijiajia3515.cairo.auth.domain.mongodb.field.AbstractAccountMetadataMongodbField;
import io.github.lijiajia3515.cairo.mongodb.data.mapping.model.AbstractMongodbField;
import io.github.lijiajia3515.cairo.mongodb.domain.field.AbstractNoneMetadataMongodbField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Set;


/**
	 * 企业终端级用户令牌管理
	 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantAppUserAuthorizationMongodb implements Serializable {

	/**
	 * 数据库标识
	 */
	@MongoId
	private ObjectId id;

	/**
	 * 会话ID
	 * 令牌的唯一标识
	 */
	@Field(write = Field.Write.ALWAYS)
	private String tokenId;

	/**
	 * 企业ID
	 * 所属企业的唯一标识
	 */
	@Field(write = Field.Write.ALWAYS)
	private String tenantId;

	/**
	 * 应用ID
	 * 所属应用的唯一标识
	 */
	@Field(write = Field.Write.ALWAYS)
	private String appId;

	/**
	 * 终端ID
	 * 所属终端的唯一标识
	 */
	@Field(write = Field.Write.ALWAYS)
	private String endpointId;

	/**
	 * 用户ID
	 * 所属用户的唯一标识
	 */
	@Field(write = Field.Write.ALWAYS)
	private String userId;

	/**
	 * 账号名称
	 */
	@Field(write = Field.Write.ALWAYS)
	private String userName;

	/**
	 * 登录方式
	 * 登录类型
	 */
	@Field(write = Field.Write.ALWAYS)
	private String loginType;

	/**
	 * 第三方认证类型
	 * 第三方登录类型
	 */
	@Field(write = Field.Write.ALWAYS)
	private String snsType;


	/**
	 * 客户端ID
	 * 所属客户端的唯一标识
	 */
	@Field(write = Field.Write.ALWAYS)
	private String clientId;
	/**
	 * 客户端标识
	 * 注册客户端ID
	 */
	@Field(write = Field.Write.ALWAYS)
	private String registeredClientId;

	/**
	 * 授权类型
	 */
	@Field(write = Field.Write.ALWAYS)
	private String authorizationGrantType;

	/**
	 * 已授权的范围
	 * 已授权的范围集合
	 */
	@Field(write = Field.Write.ALWAYS)
	private Set<String> authorizedScopes;

	/**
	 * 访问令牌
	 * 访问令牌信息
	 */
	@Field(write = Field.Write.ALWAYS)
	private AccessToken accessToken;

	/**
	 * 刷新令牌
	 * 刷新令牌信息
	 */
	@Field(write = Field.Write.ALWAYS)
	private RefreshToken refreshToken;

	/**
	 * 属性
	 * 扩展属性
	 */
	@Field(write = Field.Write.ALWAYS)
	private String attributes;

	/**
	 * 状态
	 * 状态标识
	 */
	@Field(write = Field.Write.ALWAYS)
	private String status;

	/**
	 * 设备ID
	 * 设备唯一标识
	 */
	@Field(write = Field.Write.ALWAYS)
	private String deviceId;

	/**
	 * 设备注册时间
	 */
	@Field(write = Field.Write.ALWAYS)
	private LocalDateTime deviceTime;

	/**
	 * 客户端IP
	 * 客户端IP地址
	 */
	@Field(write = Field.Write.ALWAYS)
	private String ip;

	/**
	 * 区域
	 * 客户端所属区域
	 */
	@Field(write = Field.Write.ALWAYS)
	private String region;

	/**
	 * 原始agent
	 * 原始 User-Agent
	 */
	@Field(write = Field.Write.ALWAYS)
	private String agent;

	/**
	 * 操作系统
	 * 客户端操作系统
	 */
	@Field(write = Field.Write.ALWAYS)
	private String os;

	/**
	 * 平台
	 * 客户端平台
	 */
	@Field(write = Field.Write.ALWAYS)
	private String platform;

	/**
	 * 引擎
	 * 客户端引擎
	 */
	@Field(write = Field.Write.ALWAYS)
	private String engine;

	/**
	 * 程序名称
	 * 客户端程序名称
	 */
	@Field(write = Field.Write.ALWAYS)
	private String app;

	/**
	 * 是否手机端
	 * true 表示手机端
	 */
	@Field(write = Field.Write.ALWAYS)
	private Boolean mobile;

	/**
	 * 登录时间
	 * 最后登录时间
	 */
	@Field(write = Field.Write.ALWAYS)
	private LocalDateTime loginTime;

	/**
	 * 退出时间
	 */
	@Field(write = Field.Write.ALWAYS)
	private LocalDateTime logoutTime;

	/**
	 * 创建时间
	 * 记录创建时间
	 */
	@Field(write = Field.Write.ALWAYS)
	private LocalDateTime createTime;

	/**
	 * 更新时间
	 * 记录最后更新时间
	 */
	@Field(write = Field.Write.ALWAYS)
	private LocalDateTime updateTime;



	public static MongodbField FIELD = new MongodbField();

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@SuperBuilder(toBuilder = true)
	public static class Token implements Serializable {
		/**
		 * Token真值
		 */
		@Field(write = Field.Write.ALWAYS)
		private String tokenValue;

		/**
		 * 授权时间
		 */
		@Field(write = Field.Write.ALWAYS)
		private Instant issuedAt;

		/**
		 * 过期时间
		 */
		@Field(write = Field.Write.ALWAYS)
		private Instant expiresAt;

		/**
	 * 原信息
	 * 元信息，包含创建与更新的用户及时间
	 */
		@Field(write = Field.Write.ALWAYS)
		private String metadata;
	}

	@Data
	@EqualsAndHashCode(callSuper = true)
	@NoArgsConstructor
	@AllArgsConstructor
	@SuperBuilder(toBuilder = true)
	public static class AccessToken extends TenantAppUserAuthorizationMongodb.Token {
		/**
		 * 类型
		 */
		@Field(write = Field.Write.ALWAYS)
		private String tokenType;

		/**
		 * 范围
		 */
		@Field(write = Field.Write.ALWAYS)
		private Set<String> scopes;

	}


	@Data
	@EqualsAndHashCode(callSuper = true)
	@NoArgsConstructor
	@SuperBuilder(toBuilder = true)
	public static class RefreshToken extends TenantAppUserAuthorizationMongodb.Token {


	}

	public static class MongodbField extends AbstractAccountMetadataMongodbField {
		private MongodbField() {

		}

		public final String ID = field("id");

		public final String TOKEN_ID = field("tokenId");

		public final String TENANT_ID = field("tenantId");
		public final String APP_ID = field("appId");
		public final String ENDPOINT_ID = field("endpointId");

		public final String USER_ID = field("userId");
		public final String USER_NAME = field("username");

		public final String LOGIN_TYPE = field("loginType");

		public final String SNS_TYPE = field("snsType");

		public final String CLIENT_ID = field("clientId");

		public final String REGISTERED_CLIENT_ID = field("registeredClientId");

		public final String AUTHORIZATION_GRANT_TYPE = field("authorizationGrantType");

		public final String AUTHORIZED_SCOPES = field("authorizedScopes");

		public final AccessToken ACCESS_TOKEN = new AccessToken(this, "accessToken");
		public final RefreshToken REFRESH_TOKEN = new RefreshToken(this, "refreshToken");

		public final String ATTRIBUTES = field("attributes");
		public final String STATUS = field("status");

		public final String DEVICE_ID = field("deviceId");
		public final String DEVICE_TIME = field("deviceTime");
		public final String IP = field("ip");
		public final String REGION = field("region");
		public final String AGENT = field("agent");
		public final String OS = field("os");
		public final String PLATFORM = field("platform");
		public final String ENGINE = field("engine");
		public final String APP = field("app");
		public final String MOBILE = field("mobile");

		public final String LOGIN_TIME = field("loginTime");
		public final String LOGOUT_TIME = field("logoutTime");

		public final String CREATE_TIME = field("createTime");
		public final String UPDATE_TIME = field("updateTime");


		public static class AccessToken extends AbstractNoneMetadataMongodbField {
			public AccessToken() {
			}

			public AccessToken(AbstractMongodbField parent, String prefix) {
				super(parent, prefix);
			}

			public final String TOKEN_VALUE = field("tokenValue");
			public final String ISSUED_AT = field("issuedAt");
			public final String EXPIRED_AT = field("expiredAt");
			public final String METADATA = field("metadata");

			public final String TOKEN_TYPE = field("tokenType");

			public final String SCOPES = field("scopes");

		}

		public static class RefreshToken extends AbstractNoneMetadataMongodbField {
			public RefreshToken() {
			}

			public RefreshToken(AbstractMongodbField parent, String prefix) {
				super(parent, prefix);
			}

			public final String TOKEN_VALUE = field("tokenValue");
			public final String ISSUED_AT = field("issuedAt");
			public final String EXPIRED_AT = field("expiredAt");
			public final String METADATA = field("metadata");
			public final String EXPIRES_AT = field("expiresAt");
		}
	}


}
