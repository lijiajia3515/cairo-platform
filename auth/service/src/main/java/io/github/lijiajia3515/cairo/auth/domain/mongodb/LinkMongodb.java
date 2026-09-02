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
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.time.LocalDateTime;

/**
	 * 短链接 mongodb
	 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class LinkMongodb {

	/**
	 * id
	 */
	@MongoId
	@Field(write = Field.Write.ALWAYS)
	private ObjectId _id;

	/**
	 * 链接ID
	 * 链接唯一标识
	 */
	@Field(write = Field.Write.ALWAYS)
	private String linkId;

	/**
	 * 链接url
	 * 跳转链接地址
	 */
	@Field(write = Field.Write.ALWAYS)
	private String linkUrl;

	/**
	 * 短链url
	 * 短链接地址
	 */
	@Field(write = Field.Write.ALWAYS)
	private String shortUrl;

	/**
	 * 访问次数
	 */
	@Builder.Default
	@Field(write = Field.Write.ALWAYS)
	private int accessCount = 0;

	/**
	 * 最后访问时间
	 */
	@Field(write = Field.Write.ALWAYS)
	private LocalDateTime lastAccessTime;

	/**
	 * 启用状态
	 * 启用为 true，禁用为 false
	 */
	@Field(write = Field.Write.ALWAYS)
	@Builder.Default
	private boolean enabled = true;

	/**
	 * 元数据
	 * 元信息，包含创建与更新的用户及时间
	 */
	@Builder.Default
	@Field(write = Field.Write.ALWAYS)
	private AppUserMetadataMongodb metadata = new AppUserMetadataMongodb();

	public static final MongodbField FIELD = new MongodbField();


	public static class MongodbField extends AbstractAppUserMetadataMongodbField {
		public final String LINK_ID = field("linkId");
		public final String LINK_URL = field("linkUrl");
		public final String SHORT_URL = field("shortUrl");
		public final String ACCESS_COUNT = field("accessCount");
		public final String LAST_ACCESS_TIME = field("lastAccessTime");
		public final String ENABLED = field("enabled");
	}
}
