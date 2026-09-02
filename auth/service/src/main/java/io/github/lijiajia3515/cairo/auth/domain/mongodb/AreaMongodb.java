package io.github.lijiajia3515.cairo.auth.domain.mongodb;


import io.github.lijiajia3515.cairo.auth.domain.mongodb.AppUserMetadataMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.field.AbstractAppUserMetadataMongodbField;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.field.AbstractClientMetadataMongodbField;
import io.github.lijiajia3515.cairo.mongodb.data.mapping.model.AbstractMongodbField;
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
	 * 区域 mongodb
	 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class AreaMongodb implements Serializable {
	/**
	 * 数据库ID
	 */
	@MongoId
	@Field(write = Field.Write.ALWAYS)
	private ObjectId _id;

	/**
	 * 区域ID（唯一
	 * 区域ID（唯一，主键ID）
	 */
	@Field(write = Field.Write.ALWAYS)
	private String areaId;

	/**
	 * 区域名称
	 */
	@Field(write = Field.Write.ALWAYS)
	private String areaName;

	/**
	 * 区域名称简称
	 */
	@Field(write = Field.Write.ALWAYS)
	private String shortAreaName;

	/**
	 * 拼音前缀
	 */
	@Field(write = Field.Write.ALWAYS)
	private String pinYinPrefix;

	/**
	 * 拼音
	 */
	@Field(write = Field.Write.ALWAYS)
	private String pinYin;

	/**
	 * 深度
	 * 层级（1-省，2-市，3-区，4-街道）
	 */
	@Field(write = Field.Write.ALWAYS)
	private int depth;

	/**
	 * 热门
	 */
	@Field(write = Field.Write.ALWAYS)
	private boolean hot;

	/**
	 * 启用状态
	 * 启用为 true，禁用为 false
	 */
	@Field(write = Field.Write.ALWAYS)
	private boolean enabled;

	/**
	 * 排序值
	 * 用于列表展示排序
	 */
	@Field(write = Field.Write.ALWAYS)
	private int sort;

	/**
	 * 父级区域ID
	 */
	@Field(write = Field.Write.ALWAYS)
	private String parentAreaId;

	/**
	 * 区域ID
	 */
	@Field(write = Field.Write.ALWAYS)
	private List<String> areaIds;

	/**
	 * 区域名称集合
	 */
	@Field(write = Field.Write.ALWAYS)
	private List<String> areaNames;

	/**
	 * 区域名称简称集合
	 */
	@Field(write = Field.Write.ALWAYS)
	private List<String> shortAreaNames;

	/**
	 * 元信息
	 * 元信息，包含创建与更新的用户及时间
	 */
	@Builder.Default
	@Field(write = Field.Write.ALWAYS)
	private AppUserMetadataMongodb metadata = new AppUserMetadataMongodb();

	public static final MongodbField FIELD = new MongodbField();

	public static class MongodbField extends AbstractAppUserMetadataMongodbField {
		public final String AREA_ID = field("areaId");
		public final String AREA_NAME = field("areaName");
		public final String SHORT_AREA_NAME = field("shortAreaName");
		public final String PIN_YIN_PREFIX = field("pinYinPrefix");
		public final String PIN_YIN = field("pinYin");
		public final String PARENT_AREA_ID = field("parentAreaId");
		public final Array AREA_IDS = new Array(this,"AreaIds");
		public final Array AREA_NAMES =  new Array(this,"AreaNames");
		public final Array SHORT_AREA_NAMES = new Array(this,"ShortAreaNames");
		public final String DEPTH = field("depth");
		public final String SORT = field("sort");
		public final String HOT = field("hot");
		public final String ENABLED = field("enabled");

		public static class Array extends AbstractClientMetadataMongodbField {
			public Array() {
			}

			public Array(AbstractMongodbField parent, String prefix) {
				super(parent, prefix);
			}

			public final String index(Integer i) {
				return field("" + i);
			}
		}
	}
}
