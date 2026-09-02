package io.github.lijiajia3515.cairo.auth.domain.mongodb;


import io.github.lijiajia3515.cairo.mongodb.domain.NoneMetadataMongodb;
import io.github.lijiajia3515.cairo.mongodb.domain.field.AbstractNoneMetadataMongodbField;
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
	 * Office 文件
	 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class OfficeFileMongodb {

	/**
	 * id
	 */
	@MongoId
	private ObjectId _id;


	/**
	 * id值
	 */
	@Field(write = Field.Write.ALWAYS)
	private String fileId;

	/**
	 * 名称
	 * 文件名称
	 */
	@Field(write = Field.Write.ALWAYS)
	private String fileName;

	/**
	 * WPS文件类型
	 * 类型标识
	 */
	@Field(write = Field.Write.ALWAYS)
	private String type;

	/**
	 * 源地址
	 * 源文件存储路径
	 */
	@Field(write = Field.Write.ALWAYS)
	private String sourceFilePath;

	/**
	 * 文件下载地址
	 * 文件存储路径
	 */
	@Field(write = Field.Write.ALWAYS)
	private String filepath;

	/**
	 * 文件版本号
	 * 文件版本
	 */
	@Field(write = Field.Write.ALWAYS)
	private Integer fileVersion;

	/**
	 * 文件大小
	 * 文件大小（字节）
	 */
	@Field(write = Field.Write.ALWAYS)
	private Integer size;

	/**
	 * 文档校验
	 * 文件摘要
	 */
	@Field(write = Field.Write.ALWAYS)
	private Map<String, String> digest;

	/**
	 * 对接模式
	 * 文件模式
	 */
	@Field(write = Field.Write.ALWAYS)
	private String mode;

	/**
	 * 租户ID
	 * 所属租户的唯一标识
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
	 * 创建用户ID
	 * 创建该记录的用户ID
	 */
	@Field(write = Field.Write.ALWAYS)
	private String createUserId;

	/**
	 * 更新用户ID
	 * 最后更新该记录的用户ID
	 */
	@Field(write = Field.Write.ALWAYS)
	private String updateUserId;

	/**
	 * 文件活跃时间
	 * 生效时间
	 */
	@Field(write = Field.Write.ALWAYS)
	private LocalDateTime activeTime;

	/**
	 * 元数据
	 * 元信息，包含创建与更新的用户及时间
	 */
	@Builder.Default
	@Field(write = Field.Write.ALWAYS)
	private NoneMetadataMongodb metadata = new NoneMetadataMongodb();

	public static final MongodbField FIELD = new MongodbField();

	public static class MongodbField extends AbstractNoneMetadataMongodbField {
		public final String FILE_ID = field("fileId");
		public final String FILE_VERSION = field("fileVersion");
		public final String FILE_NAME = field("fileName");

		public final String SIZE = field("size");
		public final String SOURCE_FILE_PATH = field("sourceFilePath");


		public final String FILEPATH = field("filepath");
		public final String DIGEST = field("digest");


		public final String MODE = field("mode");

		public final String TENANT_ID = field("tenantId");

		public final String APP_ID = field("appId");

		public final String CREATE_USER_ID = field("createUserId");

		public final String UPDATE_USER_ID = field("updateUserId");

		public final String ACTIVE_TIME = field("activeTime");

	}
}
