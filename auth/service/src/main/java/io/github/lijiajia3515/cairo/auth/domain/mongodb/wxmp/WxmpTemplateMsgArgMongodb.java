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
	 * 微信模板参数 mongodb
	 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class WxmpTemplateMsgArgMongodb {
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
	 * 参数编码
	 */
	@Field(write = Field.Write.ALWAYS)
	private String argCode;

	/**
	 * 模板参数名称
	 */
	@Field(write = Field.Write.ALWAYS)
	private String argName;

	/**
	 * 参数类型
	 */
	@Field(write = Field.Write.ALWAYS)
	private String argType;

	/**
	 * 模板参数编码
	 */
	@Field(write = Field.Write.ALWAYS)
	private String templateArgCode;


	/**
	 * 默认颜色
	 */
	@Field(write = Field.Write.ALWAYS)
	private String defaultColor;

	/**
	 * 排序值
	 * 用于列表展示排序
	 */
	@Field(write = Field.Write.ALWAYS)
	private int sort;


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
		public final String ARG_NAME = field("argName");
		public final String ARG_TYPE = field("argType");
		public final String ARG_CODE = field("argCode");
		public final String TEMPLATE_ARG_CODE = field("templateArgCode");

		public final String SORT = field("sort");

		public final String DEFAULT_COLOR = field("defaultColor");

	}

}
