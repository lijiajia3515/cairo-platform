package io.github.lijiajia3515.cairo.auth.domain.mongodb;

import io.github.lijiajia3515.cairo.auth.domain.mongodb.field.AbstractAppUserMetadataMongodbField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.io.Serializable;
import java.util.List;

/**
	 * app
	 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppMongodb implements Serializable {
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
	 * 名称
	 */
	@Field(write = Field.Write.ALWAYS)
	private String appName;

	/**
	 * 应用范围
	 */
	@Field(write = Field.Write.ALWAYS)
	private List<String> scopes;

	/**
	 * 是否私有应用
	 */
	@Field(write = Field.Write.ALWAYS)
	private Boolean privateApp;

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
	 * 绑定管理员账号ID
	 * 管理员账号的唯一标识数组
	 */
	@Field(write = Field.Write.ALWAYS)
	private List<String> adminAccountIds;

	/**
	 * 开通自动注册
	 * true 表示自动注册
	 */
	@Field(write = Field.Write.ALWAYS)
	private Boolean autoRegister;

	/**
	 * 企业部门模板状态
	 */
	@Field(write = Field.Write.ALWAYS)
	private Boolean tenantAppDepartmentTemplateStatus;


	/**
	 * 元信息
	 * 元信息，包含创建与更新的用户及时间
	 */
	@Builder.Default
	@Field(write = Field.Write.ALWAYS)
	private AppUserMetadataMongodb metadata = new AppUserMetadataMongodb();

	/**
	 * 字段常量
	 */
	public static final MongodbField FIELD = new MongodbField();

	public static final class MongodbField extends AbstractAppUserMetadataMongodbField {

		public final String APP_ID = field("appId");

		public final String APP_NAME = field("appName");


		public final String ICON = field("icon");

		public final String SCOPES = field("scopes");

		public final String PRIVATE_APP = field("privateApp");


		public final String ENABLED = field("enabled");

		public final String ADMIN_ACCOUNT_IDS = field("adminAccountIds");

		public final String AUTO_REGISTER = field("autoRegister");

		public final String TENANT_APP_DEPARTMENT_TEMPLATE_STATUS = field("tenantAppDepartmentTemplateStatus");


	}
}
