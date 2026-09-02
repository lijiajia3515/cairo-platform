package io.github.lijiajia3515.cairo.auth.domain.mongodb;


import io.github.lijiajia3515.cairo.auth.domain.mongodb.AppUserMetadataMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.field.AbstractAppUserMetadataMongodbField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.time.LocalDateTime;

/**
	 * 短信消息 mongodb
	 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class SmsMsgMongodb {
	/**
	 * id
	 */
	@MongoId
	@Field(write = Field.Write.ALWAYS)
	private ObjectId _id;

	/**
	 * 记录ID
	 * 消息唯一标识
	 */
	@Field(write = Field.Write.ALWAYS)
	private String msgId;

	/**
	 * 时间
	 */
	@Field(write = Field.Write.ALWAYS)
	private LocalDateTime time;

	/**
	 * 应用ID
	 * 所属应用的唯一标识
	 */
	@Field(write = Field.Write.ALWAYS)
	private String appId;

	/**
	 * 业务ID
	 * 业务标识
	 */
	@Field(write = Field.Write.ALWAYS)
	private String bizId;

	/**
	 * 手机号
	 */
	@Field(write = Field.Write.ALWAYS)
	private String phoneNumber;

	/**
	 * 发送类型
	 * 类型标识
	 */
	@Field(write = Field.Write.ALWAYS)
	private String type;

	/**
	 * 短信文本
	 * 文本内容
	 */
	@Field(write = Field.Write.ALWAYS)
	private String text;

	/**
	 * 业务参数
	 */
	@Field(write = Field.Write.ALWAYS)
	private String bizArgs;

	/**
	 * 供应商类型
	 * 服务提供商类型
	 */
	private String providerType;

	/**
	 * 供应商签名
	 */
	private String providerSign;

	/**
	 * 供应商模板类型
	 * 服务提供商模板编码
	 */
	private String providerTemplateCode;

	/**
	 * 供应商参数
	 * 服务提供商参数
	 */
	private String providerArgs;

	/**
	 * 供应商发送回执ID
	 * 服务提供商消息标识
	 */
	private String providerMsgId;

	/**
	 * 是否成功
	 * 操作成功为 true，失败为 false
	 */
	@Field(write = Field.Write.ALWAYS)
	private boolean success;

	/**
	 * 失败原因
	 * 原因说明
	 */
	@Field(write = Field.Write.ALWAYS)
	private String reason;

	/**
	 * 版本
	 */
	@Builder.Default
	@Field(write = Field.Write.ALWAYS)
	private long version = 1;

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
		public final String TIME = field("time");

		public final String APP_ID = field("appId");

		public final String BIZ_ID = field("bizId");

		public final String PHONE_NUMBER = field("phoneNumber");
		public final String TEXT = field("text");
		public final String BIZ_ARGS = field("bizArgs");
		public final String PROVIDER_TYPE = field("providerType");
		public final String PROVIDER_SIGN = field("providerSign");
		public final String PROVIDER_TEMPLATE_CODE = field("providerTemplateCode");
		public final String PROVIDER_ARGS = field("providerArgs");
		public final String PROVIDER_BIZ_ID = field("providerBizId");
		public final String SUCCESS = field("success");
		public final String REASON = field("reason");

		public final String VERSION = field("version");
	}

}
