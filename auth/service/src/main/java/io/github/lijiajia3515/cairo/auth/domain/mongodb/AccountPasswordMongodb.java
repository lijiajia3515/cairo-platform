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
public class AccountPasswordMongodb implements Serializable {

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
	 * 类型
	 * 类型标识
	 */
	@Field(write = Field.Write.ALWAYS)
	private String type;

	/**
	 * 密码
	 */
	@Field(write = Field.Write.ALWAYS)
	private String password;

	/**
	 * 密码错误次数
	 */
	@Field(write = Field.Write.ALWAYS)
	private int passwordFailCount;

	/**
	 * 密码错误时间
	 */
	@Field(write = Field.Write.ALWAYS)
	private LocalDateTime passwordFailTime;


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

		public final String TYPE = field("type");

		public final String PASSWORD = field("password");

		public final String PASSWORD_FAIL_COUNT = field("passwordFailCount");

		public final String PASSWORD_FAIL_TIME = field("passwordFailTime");

	}
}
