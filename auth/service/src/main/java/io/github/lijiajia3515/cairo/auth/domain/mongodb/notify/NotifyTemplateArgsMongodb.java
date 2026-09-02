package io.github.lijiajia3515.cairo.auth.domain.mongodb.notify;


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
	 * 通知消息模板参数 mongodb
	 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class NotifyTemplateArgsMongodb {
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
	 * 消息编码
	 */
	@Field(write = Field.Write.ALWAYS)
	private String templateId;

	/**
	 * 参数类型（alert=提醒
	 * 参数类型（alert=提醒，content=消息内容，extras-扩展）
	 */
	@Field(write = Field.Write.ALWAYS)
	private String argsType;

	/**
	 * 参数编码
	 */
	@Field(write = Field.Write.ALWAYS)
	private String argsCode;

	/**
	 * 参数名称
	 */
	@Field(write = Field.Write.ALWAYS)
	private String argsName;

	/**
	 * 数据类型
	 */
	@Field(write = Field.Write.ALWAYS)
	private String dataType;

	/**
	 * 默认值
	 */
	@Field(write = Field.Write.ALWAYS)
	private String defaultValue;

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
		public final String TEMPLATE_ID = field("templateId");
		public final String ARGS_TYPE = field("argsType");
		public final String ARGS_CODE = field("argsCode");
		public final String ARGS_NAME = field("argsName");
		public final String DATA_TYPE = field("dataType");
		public final String DEFAULT_VALUE = field("defaultValue");
		public final String SORT = field("sort");
	}

}
