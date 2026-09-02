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
	 * 子应用
	 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubappMongodb implements Serializable {
	/**
	 * 标识
	 */
	@MongoId
	@Field(write = Field.Write.ALWAYS)
	private ObjectId _id;

	/**
	 * 主键id
	 */
	@Field(write = Field.Write.ALWAYS)
	private String id;

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
	 * 子应用ID，全局唯一
	 */
	@Field(write = Field.Write.ALWAYS)
	private String subappId;

	/**
	 * 终端名称
	 */
	@Field(write = Field.Write.ALWAYS)
	private String subappName;

	/**
	 * 图标
	 */
	@Field(write = Field.Write.ALWAYS)
	private String subappIcon;

	/**
	 * 准入范围
	 * 模块开通策略
	 */
	@Field(write = Field.Write.ALWAYS)
	private String scope;

	/**
	 * 启用状态
	 * 启用为 true，禁用为 false
	 */
	@Field(write = Field.Write.ALWAYS)
	private Boolean enabled;

	/**
	 * 排序值
	 * 用于列表展示排序
	 */
	@Field(write = Field.Write.ALWAYS)
	private int sort;

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

		public final String ID = field("id");
		public final String APP_ID = field("appId");
		public final String ENDPOINT_ID = field("endpointId");
		public final String SUBAPP_ID = field("subappId");
		public final String SUBAPP_NAME = field("subappName");
		public final String SUBAPP_ICON = field("subappIcon");
	
		public final String SCOPE = field("scope");

		public final String SORT = field("sort");

		public final String ENABLED = field("enabled");

	}
}
