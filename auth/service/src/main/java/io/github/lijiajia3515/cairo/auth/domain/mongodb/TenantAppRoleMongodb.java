package io.github.lijiajia3515.cairo.auth.domain.mongodb;

import io.github.lijiajia3515.cairo.auth.domain.mongodb.field.AbstractAppUserMetadataMongodbField;
import io.github.lijiajia3515.cairo.core.CoreConstants;
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
	 * 角色
	 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class TenantAppRoleMongodb implements Serializable {

	/**
	 * 标识
	 */
	@MongoId
	@Field(write = Field.Write.ALWAYS)
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
	 * 角色ID
	 * 所属角色的唯一标识
	 */
	@Field(write = Field.Write.ALWAYS)
	private String roleId;

	/**
	 * 名称
	 * 所属角色的名称
	 */
	@Field(write = Field.Write.ALWAYS)
	private String roleName;

	/**
	 * 启用状态
	 * 启用为 true，禁用为 false
	 */
	@Field(write = Field.Write.ALWAYS)
	private Boolean enabled;

	/**
	 * 备注
	 * 备注信息
	 */
	@Field(write = Field.Write.ALWAYS)
	private String remark;


	/**
	 * 排序值
	 * 用于列表展示排序
	 */
	@Builder.Default
	@Field(write = Field.Write.ALWAYS)
	private Long sort = CoreConstants.SNOWFLAKE.nextId();


	/**
	 * 元信息
	 * 元信息，包含创建与更新的用户及时间
	 */
	@Builder.Default
	@Field(write = Field.Write.ALWAYS)
	private TenantAppUserMetadataMongodb metadata = new TenantAppUserMetadataMongodb();

	public static final MongodbField FIELD = new MongodbField();

	public static class MongodbField extends AbstractAppUserMetadataMongodbField {
		private MongodbField() {

		}

		public final String TENANT_ID = field("tenantId");

		public final String APP_ID = field("appId");

		public final String ROLE_ID = field("roleId");
		public final String ROLE_NAME = field("roleName");
		public final String REMARK = field("remark");

		public final String ENABLED = field("enabled");



	}
}
