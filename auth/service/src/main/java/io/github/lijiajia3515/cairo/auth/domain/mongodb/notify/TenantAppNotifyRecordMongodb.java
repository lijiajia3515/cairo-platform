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

import java.time.LocalDateTime;
import java.util.Map;

/**
	 * 企业应用通知消息记录 mongodb
	 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class TenantAppNotifyRecordMongodb {
	/**
	 * id
	 */
	@MongoId
	@Field(write = Field.Write.ALWAYS)
	private ObjectId _id;

	/**
	 * 消息ID
	 * 消息唯一标识
	 */
	@Field(write = Field.Write.ALWAYS)
	private String msgId;

	/**
	 * 消息时间
	 */
	@Field(write = Field.Write.ALWAYS)
	private LocalDateTime messageTime;

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
	 * 终端ID
	 * 所属终端的唯一标识
	 */
	@Field(write = Field.Write.ALWAYS)
	private String endpointId;

	/**
	 * 用户ID
	 * 所属用户的唯一标识
	 */
	@Field(write = Field.Write.ALWAYS)
	private String userId;

	/**
	 * 设备ID
	 * 设备唯一标识
	 */
	@Field(write = Field.Write.ALWAYS)
	private String deviceId;

	/**
	 * 分类ID
	 * 所属分类的唯一标识
	 */
	@Field(write = Field.Write.ALWAYS)
	private String categoryId;

	/**
	 * 分类名称
	 */
	@Field(write = Field.Write.ALWAYS)
	private String categoryName;

	/**
	 * 分类图标
	 */
	@Field(write = Field.Write.ALWAYS)
	private String categoryIcon;

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
	 * 消息提醒（仅通知栏）
	 * 消息提醒方式
	 */
	@Field(write = Field.Write.ALWAYS)
	private String messageAlert;

	/**
	 * 消息类型（0-提醒消息
	 * 消息类型（0-提醒消息，1-文本消息，2-模板消息）
	 */
	@Field(write = Field.Write.ALWAYS)
	private String messageType;

	/**
	 * 消息内容（对应消息类型=0是生效）
	 * 消息内容
	 */
	@Field(write = Field.Write.ALWAYS)
	private String messageContent;

	/**
	 * 提醒参数值
	 * 提醒参数
	 */
	@Field(write = Field.Write.ALWAYS)
	private Map<String,String> alertArgs;

	/**
	 * 内容参数值
	 * 内容参数
	 */
	@Field(write = Field.Write.ALWAYS)
	private Map<String,String> contentArgs;

	/**
	 * 模板参数参数值
	 * 模板参数
	 */
	@Field(write = Field.Write.ALWAYS)
	private Map<String,String> templateArgs;


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
	 * 扩展参数
	 * 扩展数据
	 */
	@Field(write = Field.Write.ALWAYS)
	private Map<String, String> extras;

	/**
	 * 推送状态
	 */
	@Field(write = Field.Write.ALWAYS)
	private boolean pushStatus;

	/**
	 * 推送时间
	 */
	@Field(write = Field.Write.ALWAYS)
	private boolean pushTime;

	/**
	 * 推送失败原因
	 */
	@Field(write = Field.Write.ALWAYS)
	private String pushFailReason;

	/**
	 * 推送失败次数
	 */
	@Builder.Default
	@Field(write = Field.Write.ALWAYS)
	private long pushFailCount = 0;

	/**
	 * 是否已读
	 */
	@Field(write = Field.Write.ALWAYS)
	private boolean readStatus;

	/**
	 * 已读时间
	 * 阅读时间
	 */
	@Field(write = Field.Write.ALWAYS)
	private LocalDateTime readTime;

	/**
	 * 元数据
	 * 元信息，包含创建与更新的用户及时间
	 */
	@Builder.Default
	@Field(write = Field.Write.ALWAYS)
	private AppUserMetadataMongodb metadata = new AppUserMetadataMongodb();

	public static final MongodbField FIELD = new MongodbField();

	public static class MongodbField extends AbstractAppUserMetadataMongodbField {
		public final String MSG_ID = field("msgId");
		public final String MESSAGE_TIME = field("messageTime");

		public final String TENANT_ID = field("tenantId");
		public final String APP_ID = field("appId");
		public final String USER_ID = field("userId");
		public final String DEVICE_ID = field("deviceId");

		public final String CATEGORY_ID = field("categoryId");
		public final String CATEGORY_NAME = field("categoryName");
		public final String CATEGORY_ICON = field("categoryIcon");

		public final String MESSAGE_CODE = field("messageCode");
		public final String MESSAGE_ICON = field("messageIcon");
		public final String MESSAGE_TITLE = field("messageTitle");
		public final String MESSAGE_ALERT = field("messageAlert");
		public final String MESSAGE_CONTENT = field("messageContent");


		public final String ALERT_ARGS = field("alertArgs");
		public final String CONTENT_ARGS = field("contentArgs");
		public final String TEMPLATE_ARGS = field("templateArgs");

		public final String LINK_TYPE = field("linkType");
		public final String PAGE_URL = field("pageUrl");
		public final String LINK_URL = field("linkUrl");


		public final String EXTRAS = field("extras");

		public final String PUSH_STATUS = field("pushStatus");
		public final String PUSH_TIME = field("pushTime");
		public final String PUSH_FAIL_REASON = field("pushFailReason");
		public final String PUSH_FAIL_COUNT = field("pushFailCount");

		public final String READ_STATUS = field("readTime");
		public final String READ_TIME = field("readTime");
	}

}
