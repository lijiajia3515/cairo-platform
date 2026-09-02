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
	 * endpoint
	 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EndpointMongodb implements Serializable {
	/**
	 * 标识
	 */
	@MongoId
	@Field(write = Field.Write.ALWAYS)
	private ObjectId _id;

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
	 * 终端名称
	 * 所属终端的名称
	 */
	@Field(write = Field.Write.ALWAYS)
	private String endpointName;

	/**
	 * 终端类型
	 * 类型标识
	 */
	@Field(write = Field.Write.ALWAYS)
	private String type;

	/**
	 * 终端范围
	 * 业务范围
	 */
	@Field(write = Field.Write.ALWAYS)
	private String scope;

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
	 * 图标
	 * 图标资源地址
	 */
	@Field(write = Field.Write.ALWAYS)
	private String icon;


	/**
	 * 网站Url
	 */
	@Field(write = Field.Write.ALWAYS)
	private String websiteUrl;


	/**
	 * 启用状态
	 * 启用为 true，禁用为 false
	 */
	@Field(write = Field.Write.ALWAYS)
	private Boolean enabled;


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

		public final String ENDPOINT_NAME = field("endpointName");

		public final String TYPE = field("type");

		public final String SCOPE = field("scope");

			public final String ICON = field("icon");
		public final String WEBSITE_URL = field("websiteUrl");

		public final String ENABLED = field("enabled");


	}
}
