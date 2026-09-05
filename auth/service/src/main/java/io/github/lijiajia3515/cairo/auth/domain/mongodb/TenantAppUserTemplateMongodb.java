package io.github.lijiajia3515.cairo.auth.domain.mongodb;

import io.github.lijiajia3515.cairo.auth.domain.mongodb.field.AbstractAppUserMetadataMongodbField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantAppUserTemplateMongodb implements Serializable {
	/**
	 * 标识
	 */
	@MongoId
	private ObjectId _id;

	/**
	 * 应用ID
	 * 所属应用的唯一标识
	 */
	@Field(write = Field.Write.ALWAYS)
	private String appId;

	/**
	 * 企业应用级用户模板ID
	 */
	@Field(write = Field.Write.ALWAYS)
	private String tenantAppUserTemplateId;


	/**
	 * 昵称
	 */
	@Field(write = Field.Write.ALWAYS)
	private String nickname;

	/**
	 * 手机号
	 * 手机号
	 */
	@Field(write = Field.Write.ALWAYS)
	private String phoneNumber;

	/**
	 * 角色标识
	 */
	@Field(write = Field.Write.ALWAYS)
	private List<String> tenantAppRoleTemplateIds;


	/**
	 * 是否管理员
	 */
	@Field(write = Field.Write.ALWAYS)
	private Boolean admin;

	/**
	 * 职位
	 */
	@Field(write = Field.Write.ALWAYS)
	private String position;

	/**
	 * 主部门id
	 */
	@Field(write = Field.Write.ALWAYS)
	private String tenantMainDepartmentTemplateId;

	/**
	 * 部门标识
	 */
	@Field(write = Field.Write.ALWAYS)
	private List<String> tenantAppDepartmentTemplateIds;

	/**
	 * 启用状态
	 * 启用为 true，禁用为 false
	 */
	@Field(write = Field.Write.ALWAYS)
	private Boolean enabled;


	/**
	 * 账号ID
	 * 所属账号的唯一标识
	 */
	@Field(write = Field.Write.ALWAYS)
	private String accountId;


	/**
	 * 元信息
	 * 元信息，包含创建与更新的用户及时间
	 */
	@Builder.Default
	@Field(write = Field.Write.ALWAYS)
	private AppUserMetadataMongodb metadata = new AppUserMetadataMongodb();

	public static final MongodbField FIELD = new MongodbField();

	public static class MongodbField extends AbstractAppUserMetadataMongodbField {

		public final String APP_ID = field("appId");

		public final String TENANT_APP_USER_TEMPLATE_ID = field("tenantAppUserTemplateId");

		public final String NICKNAME = field("nickname");

		public final String PHONE_NUMBER = field("phoneNumber");

		public final String ADMIN = field("admin");

		public final String TENANT_APP_ROLE_TEMPLATE_IDS = field("tenantAppRoleTemplateIds");
		public final String TENANT_APP_DEPARTMENT_TEMPLATE_IDS = field("tenantAppDepartmentTemplateIds");

		public final String POSITION = field("position");

		public final String TENANT_MAIN_DEPARTMENT_TEMPLATE_ID = field("tenantMainDepartmentTemplateId");

		public final String ENABLED = field("enabled");


		public final String ACCOUNT_ID = field("accountId");


	}
}
