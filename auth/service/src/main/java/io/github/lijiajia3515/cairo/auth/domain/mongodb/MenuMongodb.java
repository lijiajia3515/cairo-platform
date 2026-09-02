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
import java.util.List;

/**
	 * 资源
	 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class MenuMongodb implements Serializable {
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
	 * 父级ID
	 * 父级节点的唯一标识
	 */
	@Field(write = Field.Write.ALWAYS)
	private String parentId;

	/**
	 * 菜单名称
	 */
	@Field(write = Field.Write.ALWAYS)
	private String menuName;

	/**
	 * 前端路径/页面地址外部地址
	 * 路由路径
	 */
	@Field(write = Field.Write.ALWAYS)
	private String path;

	/**
	 * 组件名
	 * 前端组件路径
	 */
	@Field(write = Field.Write.ALWAYS)
	private String component;

	/**
	 * 图标
	 * 图标资源地址
	 */
	@Field(write = Field.Write.ALWAYS)
	private String icon;

	/**
	 * 标签, 非必填
	 * 标签列表
	 */
	private List<String> tags;

	/**
	 * 是否隐藏
	 */
	@Field(write = Field.Write.ALWAYS)
	private boolean hiddenMenu;


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
	@Builder.Default
	@Field(write = Field.Write.ALWAYS)
	private AppUserMetadataMongodb metadata = new AppUserMetadataMongodb();


	public static final MongodbField FIELD = new MongodbField();

	public static final class MongodbField extends AbstractAppUserMetadataMongodbField {
		private MongodbField() {

		}

		public final String APP_ID = field("appId");

		public final String ENDPOINT_ID = field("endpointId");

		public final String SUBAPP_ID = field("subappId");

		public final String SUBAPP_VERSION = field("subappVersion");

		public final String MENU_ID = field("menuId");
		public final String PARENT_ID = field("parentId");
		public final String MENU_NAME = field("menuName");
		public final String PATH = field("path");

		public final String COMPONENT = field("component");
		public final String ICON = field("icon");
		public final String HIDDEN_MENU = field("hiddenMenu");
		public final String TAGS = field("tags");

		public final String LEFT_NO = field("leftNo");
		public final String RIGHT_NO = field("rightNo");
		public final String DEPTH = field("depth");



	}
}
