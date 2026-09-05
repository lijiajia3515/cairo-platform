package io.github.lijiajia3515.cairo.auth.domain.mongodb.biz_log;


import io.github.lijiajia3515.cairo.auth.domain.mongodb.ClientMetadataMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.field.AbstractClientMetadataMongodbField;
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

/**
	 * 客户端业务日志 mongodb
	 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class BizLogClientMongodb {

	/**
	 * id
	 */
	@MongoId
	@Field(write = Field.Write.ALWAYS)
	private ObjectId _id;

	/**
	 * 日志ID
	 * 日志唯一标识
	 */
	@Field(write = Field.Write.ALWAYS)
	private String logId;

	/**
	 * 应用ID
	 * 所属应用的唯一标识
	 */
	@Field(write = Field.Write.ALWAYS)
	private String appId;

	/**
	 * 客户端ID
	 * 所属客户端的唯一标识
	 */
	@Field(write = Field.Write.ALWAYS)
	private String clientId;

	/**
	 * 客户端会话ID
	 * 客户端令牌唯一标识
	 */
	@Field(write = Field.Write.ALWAYS)
	private String clientTokenId;

	/**
	 * 业务ID
	 * 业务标识
	 */
	@Field(write = Field.Write.ALWAYS)
	private String bizId;

	/**
	 * 范围
	 * 业务范围
	 */
	@Field(write = Field.Write.ALWAYS)
	private String scope;

	/**
	 * 参数字符串
	 * 业务参数
	 */
	@Field(write = Field.Write.ALWAYS)
	private String params;

	/**
	 * 是否成功
	 * 操作成功为 true，失败为 false
	 */
	@Field(write = Field.Write.ALWAYS)
	private boolean success;

	/**
	 * 错误信息
	 * 失败时的错误信息
	 */
	@Field(write = Field.Write.ALWAYS)
	private String errorMessage;

	/**
	 * 客户端IP
	 * 客户端IP地址
	 */
	@Field(write = Field.Write.ALWAYS)
	private String ip;

	/**
	 * 开始时间
	 */
	@Field(write = Field.Write.ALWAYS)
	private LocalDateTime startTime;

	/**
	 * 结束时间
	 */
	@Field(write = Field.Write.ALWAYS)
	private LocalDateTime endTime;

	/**
	 * 持续时长（毫秒）
	 * 耗时（毫秒）
	 */
	@Field(write = Field.Write.ALWAYS)
	private Long mills;

	/**
	 * 元数据
	 * 元信息，包含创建与更新的用户及时间
	 */
	@Builder.Default
	@Field(write = Field.Write.ALWAYS)
	private ClientMetadataMongodb metadata = new ClientMetadataMongodb();

	public static final MongodbField FIELD = new MongodbField();


	public static class MongodbField extends AbstractClientMetadataMongodbField {
		public final String LOG_ID = field("logId");
		public final String APP_ID = field("appId");
		public final String CLIENT_ID = field("clientId");
		public final String CLIENT_TOKEN_ID = field("clientTokenId");
		public final String BIZ_ID = field("bizId");
		public final String SCOPE = field("scope");
		public final String PARAMS = field("params");
		public final String SUCCESS = field("success");
		public final String ERROR_MESSAGE = field("errorMessage");
		public final String Ip = field("ip");
		public final String START_TIME = field("startTime");
		public final String END_TIME = field("endTime");
		public final String MILLS = field("mills");
	}
}
