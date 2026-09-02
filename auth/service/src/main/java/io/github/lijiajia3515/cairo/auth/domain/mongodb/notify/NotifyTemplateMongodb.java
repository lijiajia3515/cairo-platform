package io.github.lijiajia3515.cairo.auth.domain.mongodb.notify;


import io.github.lijiajia3515.cairo.auth.domain.mongodb.AppUserMetadataMongodb;
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

/**
 * 通知消息模板 mongodb
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class NotifyTemplateMongodb {
	/**
	 * id
	 */
	@MongoId
	@Field(write = Field.Write.ALWAYS)
	private ObjectId _id;

	/**
	 * 模板ID
	 */
	@Field(write = Field.Write.ALWAYS)
	private String templateId;

	/**
	 * 模板名称
	 */
	@Field(write = Field.Write.ALWAYS)
	private String templateName;

	/**
	 * 应用ID
	 * 所属应用的唯一标识
	 */
	@Field(write = Field.Write.ALWAYS)
	private String appId;

	/**
	 * 分类ID
	 * 所属分类的唯一标识
	 */
	@Field(write = Field.Write.ALWAYS)
	private String categoryId;

	/**
	 * 消息编码
	 */
	@Field(write = Field.Write.ALWAYS)
	private String messageCode;

	/**
	 * 消息图标
	 */
	@Field(write = Field.Write.ALWAYS)
	private String messageIcon;

	/**
	 * 消息标题
	 */
	@Field(write = Field.Write.ALWAYS)
	private String messageTitle;

	/**
	 * 消息类型（0-提醒消息
	 * 消息类型（0-提醒消息，1-文本消息，2-模板消息）
	 */
	@Field(write = Field.Write.ALWAYS)
	private String messageType;

	/**
	 * 消息提醒（对消息类型=0/1/2 生效）
	 * 消息提醒方式
	 */
	@Field(write = Field.Write.ALWAYS)
	private String messageAlert;


	/**
	 * 消息内容（对应消息类型=2生效）
	 * 消息内容
	 */
	@Field(write = Field.Write.ALWAYS)
	private String messageContent;

	/**
	 * 跳转类型(0-不跳转
	 * 跳转类型(0-不跳转，1-跳转页面，2-跳转内部链接地址，3-跳转外部链接地址)
	 */
	@Field(write = Field.Write.ALWAYS)
	private String linkType;

	/**
	 * 页面地址（对应消息类型=2生效）
	 * 页面地址
	 */
	@Field(write = Field.Write.ALWAYS)
	private String pageUrl;

	/**
	 * 内部网站地址（对应消息类型=3/4生效）
	 * 跳转链接地址
	 */
	@Field(write = Field.Write.ALWAYS)
	private String linkUrl;

	/**
	 * 启用状态
	 * 是否启用（启用后，可以发送，未启用不会发送）
	 */
	@Field(write = Field.Write.ALWAYS)
	private boolean enabled;

	/**
	 * 元数据
	 * 元信息，包含创建与更新的用户及时间
	 */
	@Builder.Default
	@Field(write = Field.Write.ALWAYS)
	private AppUserMetadataMongodb metadata = new AppUserMetadataMongodb();

	public static final MongodbField FIELD = new MongodbField();

	public static class MongodbField extends AbstractAppUserMetadataMongodbField {
		public final String APP_ID = field("appId");
		public final String ENDPOINT_ID = field("endpointId");
		public final String TEMPLATE_ID = field("templateId");
		public final String TEMPLATE_NAME = field("templateName");
		public final String CATEGORY_ID = field("categoryId");
		public final String MESSAGE_CODE = field("messageCode");
		public final String MESSAGE_ICON = field("messageIcon");
		public final String MESSAGE_TYPE = field("messageType");
		public final String MESSAGE_TITLE = field("messageTitle");
		public final String MESSAGE_ALERT = field("messageAlert");
		public final String MESSAGE_CONTENT = field("messageContent");
		public final String LINK_TYPE = field("linkType");
		public final String PAGE_URL = field("pageUrl");
		public final String LINK_URL = field("linkUrl");
		public final String ENABLED = field("enabled");
	}

}
