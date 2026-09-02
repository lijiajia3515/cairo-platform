package io.github.lijiajia3515.cairo.auth.domain.mongodb;

import io.github.lijiajia3515.cairo.auth.domain.mongodb.field.AbstractAppUserMetadataMongodbField;
import lombok.Builder;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.util.List;

/**
	 * 企业角色模板权限 mongodb
	 */
@Data
@Builder
public class TenantAppRoleTemplatePermissionMongodb {
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
	 * 企业角色模板权限ID
	 * 企业角色模板权限id，唯一
	 */
	@Field(write = Field.Write.ALWAYS)
	private String tenantAppRoleTemplatePermissionId;

	/**
	 * 终端ID
	 * 所属终端的唯一标识
	 */
	@Field(write = Field.Write.ALWAYS)
	private String endpointId;

	/**
	 * 子应用ID
	 * 所属子应用的唯一标识
	 */
	@Field(write = Field.Write.ALWAYS)
	private String subappId;

	/**
 * 子应用版本
 * 所属子应用的版本号
 */
	@Field(write = Field.Write.ALWAYS)
	private String subappVersion;

	/**
	 * 角色模板id
	 */
	@Field(write = Field.Write.ALWAYS)
	private String tenantAppRoleTemplateId;

	/**
	 * 功能权限集合
	 * 关联功能权限的唯一标识数组
	 */
	@Field(write = Field.Write.ALWAYS)
	private List<String> permissionIds;

	/**
	 * 元信息
	 * 元信息，包含创建与更新的用户及时间
	 */
	@Builder.Default
	@Field(write = Field.Write.ALWAYS)
	private AppUserMetadataMongodb metadata = new AppUserMetadataMongodb();
	public static final MongodbField FIELD = new MongodbField();

	public static class MongodbField extends AbstractAppUserMetadataMongodbField {
		public final String TENANT_APP_ROLE_TEMPLATE_PERMISSION_ID = field("tenantAppRoleTemplatePermissionId");
		public final String APP_ID = field("appId");
		public final String ENDPOINT_ID = field("endpointId");
		public final String SUBAPP_ID = field("subappId");
		public final String SUBAPP_VERSION = field("subappVersion");
		public final String TENANT_APP_ROLE_TEMPLATE_ID = field("tenantAppRoleTemplateId");
		public final String PERMISSION_IDS = field("permissionIds");

	}
}
