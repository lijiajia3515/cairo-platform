package io.github.lijiajia3515.cairo.auth.domain.mongodb;


import io.github.lijiajia3515.cairo.auth.domain.mongodb.AppUserMetadataMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.field.AbstractAppUserMetadataMongodbField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

/**
	 * 短信模版 mongodb
	 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class SmsTemplateMongodb {
	/**
	 * id
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
	 * 业务ID
	 * 业务标识
	 */
	@Field(write = Field.Write.ALWAYS)
	private String bizId;

	/**
	 * 签名
	 * 模板签名
	 */
	@Field(write = Field.Write.ALWAYS)
	private String templateSign;

	/**
	 * 模板编号
	 * 模板编码
	 */
	@Field(write = Field.Write.ALWAYS)
	private String templateCode;

	/**
	 * 模板名称
	 */
	@Field(write = Field.Write.ALWAYS)
	private String templateName;

	/**
	 * 模板类型
	 */
	@Field(write = Field.Write.ALWAYS)
	private String templateType;

	/**
	 * 模板内容
	 */
	private String templateText;

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
		public final String APP_ID = field("appId");
		public final String BIZ_ID = field("bizId");

		public final String TEMPLATE_SIGN = field("templateSign");
		public final String TEMPLATE_CODE = field("templateCode");
		public final String TEMPLATE_NAME = field("templateName");
		public final String TEMPLATE_TYPE = field("templateType");

		public final String TEMPLATE_TEXT = field("templateText");

		public final String ENABLED = field("enabled");
	}

}
