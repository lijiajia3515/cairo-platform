package io.github.lijiajia3515.cairo.auth.domain.mongodb;

import io.github.lijiajia3515.cairo.auth.domain.mongodb.field.AbstractAccountMetadataMongodbField;
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
	 * 用户联接
	 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class AccountSnsMongodb implements Serializable {
	/**
	 * 标识
	 */
	@MongoId
	private ObjectId _id;

	/**
	 * 记录ID
	 * 记录唯一标识
	 */
	@Field(write = Field.Write.ALWAYS)
	private String recordId;

	/**
	 * 账号ID
	 * 所属账号的唯一标识
	 */
	@Field(write = Field.Write.ALWAYS)
	private String accountId;

	/**
	 * 第三方账号厂家ID
	 * 第三方登录合作方标识
	 */
	@Field(write = Field.Write.ALWAYS)
	private String snsPartnerId;

	/**
	 * 第三方账号ID（微信
	 * 第三方账号ID（微信：（openId/unionId）支付宝：openId）
	 */
	@Field(write = Field.Write.ALWAYS)
	private String snsPartnerOpenId;

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
	 * 绑定时间
	 */
	@Field(write = Field.Write.ALWAYS)
	private LocalDateTime bindTime;

	/**
	 * 启用状态
	 * 启用为 true，禁用为 false
	 */
	@Field(write = Field.Write.ALWAYS)
	private Boolean enabled;

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

		public final String RECORD_ID = field("recordId");

		public final String ACCOUNT_ID = field("accountId");

		public final String SNS_PARTNER_ID = field("snsPartnerId");

		public final String SNS_PARTNER_OPEN_ID = field("snsPartnerOpenId");

		public final String NICKNAME = field("nickname");

		public final String AVATAR_URL = field("avatarUrl");

		public final String ENABLED = field("enabled");

	}
}
