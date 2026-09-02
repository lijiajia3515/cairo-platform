package io.github.lijiajia3515.cairo.auth.domain.mongodb;

import io.github.lijiajia3515.cairo.auth.domain.mongodb.field.AbstractAppUserMetadataMongodbField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.io.Serializable;


/**
	 * 第三方认证提供方
	 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SnsProviderMongodb implements Serializable {

	/**
	 * 标识
	 */
	@MongoId
	@Field(write = Field.Write.ALWAYS)
	private ObjectId _id;

	/**
	 * 应用ID
	 * 所属应用的唯一标识
	 */
	@Field(write = Field.Write.ALWAYS)
	private String appId;


	/**
	 * 第三方认证提供商ID
	 * 所属第三方登录提供者的唯一标识
	 */
	@Field(write = Field.Write.ALWAYS)
	private String snsProviderId;

	/**
	 * 名称
	 */
	@Field(write = Field.Write.ALWAYS)
	private String snsProviderName;

	/**
	 * 类型
	 */
	@Field(write = Field.Write.ALWAYS)
	private String snsProviderType;

	/**
	 * 厂商
	 */
	@Field(write = Field.Write.ALWAYS)
	private String snsProviderPartner;

	/**
	 * 客户端ID
	 * 所属客户端的唯一标识
	 */
	@Field(write = Field.Write.ALWAYS)
	private String clientId;

	/**
	 * clientSecret
	 */
	@Field(write = Field.Write.ALWAYS)
	private String clientSecret;

	/**
	 * 启用状态
	 * 启用为 true，禁用为 false
	 */
	@Field(write = Field.Write.ALWAYS)
	private Boolean enabled;

	/**
	 * 是否自动注册
	 */
	@Field(write = Field.Write.ALWAYS)
	private Boolean isAutoRegister;


	/**
	 * 元信息
	 * 元信息，包含创建与更新的用户及时间
	 */
	@Builder.Default
	@Field(write = Field.Write.ALWAYS)
	private AppUserMetadataMongodb metadata = new AppUserMetadataMongodb();

	public static final MongodbField FIELD = new MongodbField();

	public static class MongodbField extends AbstractAppUserMetadataMongodbField {
		private MongodbField() {

		}
		public final String ID = field("_id");
		public final String SNS_PROVIDER_ID = field("snsProviderId");
		public final String SNS_PROVIDER_NAME = field("snsProviderName");
		public final String SNS_PROVIDER_TYPE = field("snsProviderType");
		public final String SNS_PROVIDER_PARTNER = field("snsProviderPartner");
		public final String CLIENT_ID =  field("clientId");
		public final String CLIENT_SECRET = field("clientSecret");
		public final String ENABLED = field("enabled");
		public final String IS_AUTO_REGISTER = field("isAutoRegister");
		public final String APP_ID = field("appId");
	}
}
