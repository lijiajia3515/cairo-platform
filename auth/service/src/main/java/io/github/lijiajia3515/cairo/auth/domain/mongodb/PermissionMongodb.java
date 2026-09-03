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
import java.util.Set;

/**
	 * 功能权限 mongodb
	 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class PermissionMongodb implements Serializable {
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
	 * 菜单ID
	 * 所属菜单的唯一标识
	 */
	@Field(write = Field.Write.ALWAYS)
	private String menuId;

	/**
	 * 功能权限ID 必填
	 * 功能权限ID 必填，clientId+permission=唯一
	 */
	@Field(write = Field.Write.ALWAYS)
	private String permissionId;


	/**
	 * 功能权限名称 必填
	 */
	@Field(write = Field.Write.ALWAYS)
	private String permissionName;

	/**
	 * 权限值
	 * 权限值，选填
	 */
	@Field(write = Field.Write.ALWAYS)
	private Set<String> authorities;

	/**
	 * 类型
	 * 类型（read=读，write=写，operator=操作）选填
	 */
	@Field(write = Field.Write.ALWAYS)
	private String type;

	/**
	 * 是否默认权限
	 */
	@Field(write = Field.Write.ALWAYS)
	private Boolean defaultPermission;

	/**
	 * 是否隐藏权限
	 */
	@Field(write = Field.Write.ALWAYS)
	private Boolean hiddenPermission;


	/**
	 * 图标
	 * 图标资源地址
	 */
	@Field(write = Field.Write.ALWAYS)
	private String icon;

	/**
	 * 排序值
	 * 用于列表展示排序
	 */
	@Builder.Default
	@Field(write = Field.Write.ALWAYS)
	private Long sort = System.currentTimeMillis();

	/**
	 * 元信息
	 * 元信息，包含创建与更新的用户及时间
	 */
	@Builder.Default
	@Field(write = Field.Write.ALWAYS)
	private AppUserMetadataMongodb metadata = new AppUserMetadataMongodb();

	/**
	 * 字段
	 */
	public static final MongodbField FIELD = new MongodbField();

	public static final class MongodbField extends AbstractAppUserMetadataMongodbField {
		private MongodbField() {

		}

		public final String APP_ID = field("appId");

		public final String ENDPOINT_ID = field("endpointId");

		public final String SUBAPP_ID = field("subappId");

		public final String SUBAPP_VERSION = field("subappVersion");


		public final String MENU_ID = field("menuId");

		public final String PERMISSION_ID = field("permissionId");
		public final String PERMISSION_NAME = field("permissionName");
		public final String AUTHORITIES = field("authorities");

		public final String TYPE = field("type");
		public final String DEFAULT_PERMISSION = field("defaultPermission");
		public final String HIDDEN_PERMISSION = field("hiddenPermission");

		public final String ICON = field("icon");
		public final String SORT = field("sort");

	}
}
