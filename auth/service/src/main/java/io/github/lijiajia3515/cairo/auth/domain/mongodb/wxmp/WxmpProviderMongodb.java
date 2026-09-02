package io.github.lijiajia3515.cairo.auth.domain.mongodb.wxmp;


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


/**
	 * 微信公众号配置 mongodb
	 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class WxmpProviderMongodb {
	/**
	 * id
	 */
	@MongoId
	@Field(write = Field.Write.ALWAYS)
	private ObjectId _id;


	/**
	 * 标识
	 * 所属微信小程序提供者的唯一标识
	 */
	@Field(write = Field.Write.ALWAYS)
	private String wxmpProviderId;

	/**
	 * 名称
	 */
	@Field(write = Field.Write.ALWAYS)
	private String wxmpProviderName;


	/**
	 * 设置微信公众号的appid.
	 */
	@Field(write = Field.Write.ALWAYS)
	private String wxmpAppId;

	/**
	 * 设置微信公众号的app secret.
	 */
	@Field(write = Field.Write.ALWAYS)
	private String wxmpSecret;

	/**
	 * 设置微信公众号的token.
	 */
	@Field(write = Field.Write.ALWAYS)
	private String wxmpToken;

	/**
	 * 设置微信公众号的EncodingAESKey.
	 */
	@Field(write = Field.Write.ALWAYS)
	private String wxmpAesKey;


	/**
	 * 启用状态
	 * 是否启用（启用后，可以发送，未启用不会发送）
	 */
	@Field(write = Field.Write.ALWAYS)
	private boolean enabled;

	/**
	 * 元数据
	 * 元信息，包含创建与更新的用户及时间
	 */
	@Builder.Default
	@Field(write = Field.Write.ALWAYS)
	private AppUserMetadataMongodb metadata = new AppUserMetadataMongodb();

	public static final MongodbField FIELD = new MongodbField();

	public static class MongodbField extends AbstractAppUserMetadataMongodbField {

		public final String _ID = field("_id");

		public final String WX_MP_PROVIDER_ID = field("wxmpProviderId");

		public final String WX_MP_PROVIDER_NAME = field("wxmpProviderName");


		public final String WX_MP_APP_ID = field("wxmpAppId");

		public final String WX_MP_SECRET = field("wxmpSecret");

		public final String WX_MP_TOKEN = field("wxmpToken");

		public final String WX_MP_AES_KEY = field("wxmpAesKey");

		public final String ENABLED = field("enabled");

	}

}
