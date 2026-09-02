package io.github.lijiajia3515.cairo.auth.domain.mongodb;

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

import java.io.Serializable;


/**
	 * 企业部门模板
	 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class TenantAppDepartmentTemplateMongodb implements Serializable {

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
	 * 父级ID
	 * 父级节点的唯一标识
	 */
	@Field(write = Field.Write.ALWAYS)
	private String parentId;

	/**
	 * 是否根节点
	 * 根节点标识
	 */
	@Field(write = Field.Write.ALWAYS)
	private boolean root;

	/**
	 * 企业部门模板ID
	 */
	@Field(write = Field.Write.ALWAYS)
	private String tenantAppDepartmentTemplateId;

	/**
	 * 企业部门模板名称
	 */
	@Field(write = Field.Write.ALWAYS)
	private String tenantAppDepartmentTemplateName;

	/**
	 * 备注
	 * 备注信息
	 */
	@Field(write = Field.Write.ALWAYS)
	private String remark;

	/**
	 * 左值
	 * 树结构左值
	 */
	@Field(write = Field.Write.ALWAYS)
	private int leftNo;

	/**
	 * 右值
	 * 树结构右值
	 */
	@Field(write = Field.Write.ALWAYS)
	private int rightNo;

	/**
	 * 深度
	 * 树层级深度
	 */
	@Field(write = Field.Write.ALWAYS)
	private int depth;

	/**
	 * 元信息
	 * 元信息，包含创建与更新的用户及时间
	 */
	@Field(write = Field.Write.ALWAYS)
	@Builder.Default
	private AppUserMetadataMongodb metadata = new AppUserMetadataMongodb();

	public static final MongodbField FIELD = new MongodbField();

	public static final class MongodbField extends AbstractAppUserMetadataMongodbField {
		private MongodbField() {
		}
		public final String APP_ID = field("appId");
		public final String PARENT_ID = field("parentId");
		public final String ROOT = field("root");
		public final String TENANT_APP_DEPARTMENT_TEMPLATE_ID = field("tenantAppDepartmentTemplateId");
		public final String TENANT_APP_DEPARTMENT_TEMPLATE_NAME = field("tenantAppDepartmentTemplateName");
		public final String REMARK = field("remark");
		public final String LEFT_NO = field("leftNo");
		public final String RIGHT_NO = field("rightNo");
		public final String DEPTH = field("depth");
	}
}
