package io.github.lijiajia3515.cairo.auth.domain.mongodb;


import io.github.lijiajia3515.cairo.auth.domain.mongodb.AccountMetadataMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.field.AbstractAccountMetadataMongodbField;
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
	 * 系统级字典
	 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class SysDictMongodb {
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
	 * 字典ID
	 * 所属数据字典的唯一标识
	 */
	@Field(write = Field.Write.ALWAYS)
	private String dictId;

	/**
	 * 字典类型(SYSTEM,BIZ_TEMPLATE)
	 */
	@Field(write = Field.Write.ALWAYS)
	private String dictType;

	/**
	 * 字典名称
	 */
	@Field(write = Field.Write.ALWAYS)
	private String dictName;

	/**
	 * 图标
	 * 图标资源地址
	 */
	@Field(write = Field.Write.ALWAYS)
	private String icon;

	/**
	 * 启用状态
	 * 启用为 true，禁用为 false
	 */
	@Field(write = Field.Write.ALWAYS)
	private Boolean enabled;

	/**
	 * 左值
	 * 树结构左值
	 */
	@Field(write = Field.Write.ALWAYS)
	private Integer leftNo;

	/**
	 * 右值
	 * 树结构右值
	 */
	@Field(write = Field.Write.ALWAYS)
	private Integer rightNo;

	/**
	 * 是否允许添加子项
	 */
	@Field(write = Field.Write.ALWAYS)
	private Boolean isCreateItem;

	/**
	 * 元信息
	 * 元信息，包含创建与更新的用户及时间
	 */
	@Builder.Default
	@Field(write = Field.Write.ALWAYS)
	private AccountMetadataMongodb metadata = new AccountMetadataMongodb();

	public static final MongodbField FIELD = new MongodbField();

	public static class MongodbField extends AbstractAccountMetadataMongodbField {
		public final String APP_ID = field("appId");
		public final String DICT_ID = field("dictId");
		public final String DICT_TYPE = field("dictType");
		public final String DICT_NAME = field("dictName");
		public final String ICON = field("icon");
		public final String ENABLED = field("enabled");
		public final String LEFT_NO = field("leftNo");
		public final String RIGHT_NO = field("rightNo");
		public final String IS_CREATE_ITEM = field("isCreateItem");
	}

}
