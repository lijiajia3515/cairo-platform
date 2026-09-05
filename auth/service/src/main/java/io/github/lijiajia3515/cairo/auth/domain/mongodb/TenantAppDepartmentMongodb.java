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
	 * 部门
	 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class TenantAppDepartmentMongodb implements Serializable {

	/**
	 * 标识
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
	 * 父级ID
	 * 父级节点的唯一标识
	 */
	@Field(write = Field.Write.ALWAYS)
	private String parentId;

	/**
	 * 根节点
	 * 根节点标识
	 */
	private boolean root;

	/**
	 * 部门ID
	 * 所属部门的唯一标识
	 */
	@Field(write = Field.Write.ALWAYS)
	private String departmentId;

	/**
	 * 部门名称
	 */
	@Field(write = Field.Write.ALWAYS)
	private String departmentName;

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
	private TenantAppUserMetadataMongodb metadata = new TenantAppUserMetadataMongodb();

	public static final MongodbField FIELD = new MongodbField();

	public static final class MongodbField extends AbstractAppUserMetadataMongodbField {
		private MongodbField() {
		}

		public final String TENANT_ID = field("tenantId");

		public final String APP_ID = field("appId");

		public final String PARENT_ID = field("parentId");
		public final String ROOT = field("root");

		public final String DEPARTMENT_ID = field("departmentId");

		public final String DEPARTMENT_NAME = field("departmentName");
		public final String REMARK = field("remark");
		public final String LEFT_NO = field("leftNo");
		public final String RIGHT_NO = field("rightNo");
		public final String DEPTH = field("depth");
	}
}
