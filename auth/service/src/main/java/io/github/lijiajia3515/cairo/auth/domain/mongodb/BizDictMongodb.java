package io.github.lijiajia3515.cairo.auth.domain.mongodb;

import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantAppUserMetadataMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.field.AbstractTenantAppUserMetadataMongodbField;
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
	 * 业务级字典
	 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class BizDictMongodb {
	/**
	 * id
	 */
	@MongoId
	@Field(write = Field.Write.ALWAYS)
	private ObjectId _id;

	/**
	 * 企业ID
	 * 所属企业的唯一标识
	 */
	@Field(write = Field.Write.ALWAYS)
	private String tenantId;

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
	 * 左值
	 * 树结构左值
	 */
	@Field(write = Field.Write.ALWAYS)
	private Integer leftNo;

	/**
	 * 启用状态
	 * 启用为 true，禁用为 false
	 */
	@Field(write = Field.Write.ALWAYS)
	private Boolean enabled;


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
	 * 还原字典名称
	 */
	@Field(write = Field.Write.ALWAYS)
	private String reductionDictName;

	/**
	 * 还原图标值
	 */
	@Field(write = Field.Write.ALWAYS)
	private String reductionIcon;

	/**
	 * 还原版本
	 */
	@Field(write = Field.Write.ALWAYS)
	private long reductionVersion;

	/**
	 * 同步版本
	 */
	@Field(write = Field.Write.ALWAYS)
	private long syncVersion;

	/**
	 * 是否同步图标
	 */
	@Field(write = Field.Write.ALWAYS)
	private Boolean isSyncIcon;


	/**
	 * 元数据
	 * 元信息，包含创建与更新的用户及时间
	 */
	@Builder.Default
	@Field(write = Field.Write.ALWAYS)
	private TenantAppUserMetadataMongodb metadata = new TenantAppUserMetadataMongodb();

	public static final MongodbField FIELD = new MongodbField();

	public static class MongodbField extends AbstractTenantAppUserMetadataMongodbField {
		public final String TENANT_ID = field("tenantId");
		public final String APP_ID = field("appId");
		public final String DICT_ID = field("dictId");
		public final String DICT_NAME = field("dictName");
		public final String ICON = field("icon");
		public final String LEFT_NO = field("leftNo");
		public final String RIGHT_NO = field("rightNo");
		public final String IS_CREATE_ITEM = field("isCreateItem");
		public final String ENABLED = field("enabled");
		public final String REDUCTION_VERSION = field("reductionVersion");
		public final String REDUCTION_ICON = field("reductionIcon");
		public final String REDUCTION_DICT_NAME = field("reductionDictName");
		public final String SYNC_VERSION = field("syncVersion");
		public final String IS_SYNC_ICON = field("isSyncIcon");
	}
}
