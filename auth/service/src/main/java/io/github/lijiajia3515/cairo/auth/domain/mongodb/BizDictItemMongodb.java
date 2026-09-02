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
	 * 业务级字典项
	 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class BizDictItemMongodb {

	@MongoId
	private ObjectId _id;

	/**
	 * 租户ID
	 * 所属租户的唯一标识
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
	 * 父级字典项ID
	 */
	@Field(write = Field.Write.ALWAYS)
	private String parentItemId;

	/**
	 * 字典项ID
	 */
	@Field(write = Field.Write.ALWAYS)
	private String itemId;

	/**
	 * 字典名称
	 */
	@Field(write = Field.Write.ALWAYS)
	private String itemName;


	/**
	 * 是否允许编辑
	 */
	@Field(write = Field.Write.ALWAYS)
	private Boolean editable;

	/**
	 * 深度
	 * 树层级深度
	 */
	@Field(write = Field.Write.ALWAYS)
	private Integer depth;

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
	 * 备注
	 * 备注信息
	 */
	@Field(write = Field.Write.ALWAYS)
	private String remark;

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
	 * 还原备注
	 */
	@Field(write = Field.Write.ALWAYS)
	private String reductionRemark;

	/**
	 * 还原图标
	 */
	@Field(write = Field.Write.ALWAYS)
	private String reductionIcon;

	/**
	 * 还原版本
	 */
	@Field(write = Field.Write.ALWAYS)
	private long reductionVersion;

	/**
	 * 还原字典名称
	 */
	@Field(write = Field.Write.ALWAYS)
	private String reductionItemName;

	/**
	 * 是否同步字典
	 */
	@Field(write = Field.Write.ALWAYS)
	private Boolean isSync;

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
	 * metadata
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
		public final String PARENT_ITEM_ID = field("parentItemId");
		public final String ITEM_ID = field("itemId");
		public final String ITEM_NAME = field("itemName");
		public final String ICON = field("icon");
		public final String REMARK = field("remark");
		public final String ENABLED = field("enabled");
		public final String EDITABLE = field("editable");
		public final String DEPTH = field("depth");
		public final String LEFT_NO = field("leftNo");
		public final String RIGHT_NO = field("rightNo");
		public final String REDUCTION_REMARK = field("reductionRemark");
		public final String REDUCTION_ICON = field("reductionIcon");
		public final String REDUCTION_VERSION = field("reductionVersion");
		public final String REDUCTION_ITEM_NAME = field("reductionItemName");
		public final String IS_SYNC = field("isSync");
		public final String SYNC_VERSION = field("syncVersion");
		public final String IS_SYNC_ICON = field("isSyncIcon");
	}

}
