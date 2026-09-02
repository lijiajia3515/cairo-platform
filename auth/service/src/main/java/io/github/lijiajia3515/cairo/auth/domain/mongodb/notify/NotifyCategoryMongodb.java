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
 * 通知消息分类 mongodb
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class NotifyCategoryMongodb {
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
	 * 分类ID
	 * 所属分类的唯一标识
	 */
	@Field(write = Field.Write.ALWAYS)
	private String categoryId;

	/**
	 * 分类名称
	 */
	@Field(write = Field.Write.ALWAYS)
	private String categoryName;

	/**
	 * 分类图标
	 */
	@Field(write = Field.Write.ALWAYS)
	private String categoryIcon;

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
		public final String CATEGORY_ID = field("categoryId");
		public final String CATEGORY_NAME = field("categoryName");
		public final String CATEGORY_ICON = field("categoryIcon");

		public final String ENABLED = field("enabled");
	}

}
