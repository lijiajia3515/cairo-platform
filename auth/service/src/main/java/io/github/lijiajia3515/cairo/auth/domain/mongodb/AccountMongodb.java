package io.github.lijiajia3515.cairo.auth.domain.mongodb;

import io.github.lijiajia3515.cairo.auth.domain.mongodb.field.AbstractAccountMetadataMongodbField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.io.Serializable;
import java.time.LocalDateTime;


/**
	 * 账号
	 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountMongodb implements Serializable {

	/**
	 * 标识
	 */
	@MongoId
	@Field(write = Field.Write.ALWAYS)
	private ObjectId _id;

	/**
	 * 账号ID
	 * 所属账号的唯一标识
	 */
	@Field(write = Field.Write.ALWAYS)
	private String accountId;

	/**
	 * 昵称
	 */
	@Field(write = Field.Write.ALWAYS)
	private String nickname;

	/**
	 * 头像
	 * 头像资源地址
	 */
	@Field(write = Field.Write.ALWAYS)
	private String avatarUrl;

	/**
	 * 用户名
	 * 登录名
	 */
	@Field(write = Field.Write.ALWAYS)
	private String username;

	/**
	 * 邮箱号码
	 * 邮箱地址
	 */
	@Field(write = Field.Write.ALWAYS)
	private String email;

	/**
	 * 手机号
	 * 手机号
	 */
	@Field(write = Field.Write.ALWAYS)
	private String phoneNumber;

	/**
	 * 启用状态
	 * 启用为 true，禁用为 false
	 */
	@Field(write = Field.Write.ALWAYS)
	private boolean enabled;

	/**
	 * 账号锁定
	 */
	@Field(write = Field.Write.ALWAYS)
	private boolean locked;

	/**
	 * 账号锁定时间
	 */
	@Field(write = Field.Write.ALWAYS)
	private LocalDateTime lockedTime;

	/**
	 * 加入时间
	 */
	@Field(write = Field.Write.ALWAYS)
	private LocalDateTime joinTime;

	/**
	 * 最后登录时间
	 */
	@Field(write = Field.Write.ALWAYS)
	private LocalDateTime loginTime;

	/**
	 * 注销状态
	 */
	@Field(write = Field.Write.ALWAYS)
	private String logoffStatus;

	/**
	 * 注销等待时间
	 * 注销等待开始时间
	 */
	@Field(write = Field.Write.ALWAYS)
	private LocalDateTime logoffPendingTime;

	/**
	 * 注销成功时间
	 * 注销完成时间
	 */
	private LocalDateTime logoffSuccessTime;


	/**
	 * 元信息
	 * 元信息，包含创建与更新的用户及时间
	 */
	@Builder.Default
	@Field(write = Field.Write.ALWAYS)
	private AccountMetadataMongodb metadata = new AccountMetadataMongodb();

	public static final MongodbField FIELD = new MongodbField();

	public static class MongodbField extends AbstractAccountMetadataMongodbField {
		private MongodbField() {

		}

		public final String ACCOUNT_ID = field("accountId");

		public final String NICKNAME = field("nickname");

		public final String USERNAME = field("username");
		public final String EMAIL = field("email");

		public final String PHONE_NUMBER = field("phoneNumber");

		public final String AVATAR_URL = field("avatarUrl");


		public final String JOIN_TIME = field("joinTime");

		public final String LOGIN_TIME = field("loginTime");

		public final String ENABLED = field("enabled");
		public final String LOCKED = field("locked");

		public final String LOCKED_TIME = field("lockedTime");

		/**
		 * 注销状态
		 */
		public final String LOGOFF_STATUS = field("logoffStatus");

		/**
		 * 注销时间
		 */
		public final String LOGOFF_PENDING_TIME = field("logoffPendingTime");
		/**
		 * 注销成功时间
		 */
		public final String LOGOFF_SUCCESS_TIME = field("logoffSuccessTime");

	}
}
