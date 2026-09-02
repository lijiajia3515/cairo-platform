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
	 * 第三方账号信息表
	 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class SnsTokenMongodb implements Serializable {
	/**
	 * 标识
	 */
	@MongoId
	private ObjectId _id;

	/**
	 * 记录ID
	 */
	@Field(write = Field.Write.ALWAYS)
	private String token;

	/**
	 * 状态
	 * 状态标识
	 */
	@Field(write = Field.Write.ALWAYS)
	private String status;

	/**
	 * 过期时间
	 */
	@Field(write = Field.Write.ALWAYS)
	private LocalDateTime expiredTime;

	/**
	 * 厂商ID
	 */
	@Field(write = Field.Write.ALWAYS)
	private String partnerId;

	/**
	 * 供应商ID
	 */
	@Field(write = Field.Write.ALWAYS)
	private String providerId;

	/**
	 * 厂商用户ID
	 */
	@Field(write = Field.Write.ALWAYS)
	private String partnerOpenId;

	/**
	 * 供应商用户ID
	 */
	@Field(write = Field.Write.ALWAYS)
	private String providerOpenId;

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
	 * 性别
	 */
	@Field(write = Field.Write.ALWAYS)
	private String sex;

	/**
	 * 创建时间
	 * 记录创建时间
	 */
	@Field(write = Field.Write.ALWAYS)
	private LocalDateTime createTime;

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

		public final String TOKEN = field("token");
		public final String STATUS = field("status");
		public final String EXPIRED_TIME = field("expiredTime");
		public final String PARTNER_ID = field("partnerId");
		public final String PROVIDER_ID = field("providerId");
		public final String PARTNER_OPEN_ID = field("partnerOpenId");
		public final String PROVIDER_OPEN_ID = field("providerOpenId");

		public final String NICKNAME = field("nickname");
		public final String AVATAR_URL = field("avatarUrl");
		public final String ENABLED = field("enabled");
	}
}
