package io.github.lijiajia3515.cairo.auth.domain.mongodb.login_log;

import io.github.lijiajia3515.cairo.auth.domain.mongodb.AppUserMetadataMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.field.AbstractAppUserMetadataMongodbField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.io.Serializable;
import java.time.LocalDateTime;


/**
	 * 用户登录日志
	 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class ClientLoginLogMongodb implements Serializable {

	/**
	 * 标识
	 */
	@MongoId
	@Field(write = Field.Write.ALWAYS)
	private ObjectId _id;

	/**
	 * 日志ID
	 * 日志唯一标识
	 */
	@Field(write = Field.Write.ALWAYS)
	private String logId;

	/**
	 * 登录时间
	 * 最后登录时间
	 */
	@Field(write = Field.Write.ALWAYS)
	private LocalDateTime loginTime;

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
	 * 客户端ID
	 * 所属客户端的唯一标识
	 */
	@Field(write = Field.Write.ALWAYS)
	private String clientId;

	/**
	 * 客户端会话ID
	 * 客户端令牌唯一标识
	 */
	@Field(write = Field.Write.ALWAYS)
	private String clientTokenId;

	/**
	 * 授权方式
	 */
	@Field(write = Field.Write.ALWAYS)
	private String grantType;

	/**
	 * 登录方式
	 */
	@Field(write = Field.Write.ALWAYS)
	private String method;


	/**
	 * 是否成功
	 * 操作成功为 true，失败为 false
	 */
	@Field(write = Field.Write.ALWAYS)
	private Boolean success;

	/**
	 * 错误原因
	 * 失败时的错误信息
	 */
	@Field(write = Field.Write.ALWAYS)
	private String errMsg;

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
	 * 登录agent
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
	 * 元信息
	 * 元信息，包含创建与更新的用户及时间
	 */
	@Field(write = Field.Write.ALWAYS)
	@Builder.Default
	private AppUserMetadataMongodb metadata = new AppUserMetadataMongodb();

	public static final MongodbField FIELD = new MongodbField();

	public static class MongodbField extends AbstractAppUserMetadataMongodbField {
		public final String LOG_ID = field("logId");

		public final String LOGIN_TIME = field("loginTime");

		public final String APP_ID = field("appId");

		public final String ENDPOINT_ID = field("endpointId");

		public final String CLIENT_ID = field("clientId");
		public final String CLIENT_TOKEN_ID = field("clientTokenId");

		public final String GRANT_TYPE = field("grantType");

		public final String METHOD = field("method");

		public final String SUCCESS = field("success");

		public final String ERR_MSG = field("errMsg");



		public final String IP = field("ip");

		public final String OS = field("os");
		public final String PLATFORM = field("platform");
		public final String ENGINE = field("engine");

		public final String APP = field("app");

		public final String MOBILE = field("mobile");

		private MongodbField() {

		}

	}
}
