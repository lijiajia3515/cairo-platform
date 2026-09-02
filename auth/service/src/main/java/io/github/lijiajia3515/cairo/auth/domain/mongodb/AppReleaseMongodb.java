package io.github.lijiajia3515.cairo.auth.domain.mongodb;

import io.github.lijiajia3515.cairo.auth.domain.mongodb.AppUserMetadataMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.field.AbstractAppUserMetadataMongodbField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

/**
	 * 应用发行管理
	 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppReleaseMongodb {
	@MongoId
	@Field(write = Field.Write.ALWAYS)
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
	 * 类型
	 * 类型标识
	 */
	@Field(write = Field.Write.ALWAYS)
	private String type;

	/**
	 * app版本
	 * 应用版本号
	 */
	@Field(write = Field.Write.ALWAYS)
	private String appVersion;

	/**
	 * 是否发行版本
	 * 是否发行版本，true-是，否-预览版本
	 */
	@Field(write = Field.Write.ALWAYS)
	private Boolean releaseVersion;

	/**
	 * 是否未最新版本
	 * true 表示最新版本
	 */
	@Field(write = Field.Write.ALWAYS)
	private Boolean latestVersion;

	/**
	 * 标题
	 */
	@Field(write = Field.Write.ALWAYS)
	private String title;


	/**
	 * 备注
	 * 备注信息
	 */
	@Field(write = Field.Write.ALWAYS)
	private String remark;

	/**
	 * 是否强制更新
	 * true 表示强制更新
	 */
	@Field(write = Field.Write.ALWAYS)
	private Boolean force;

	/**
	 * 网页端访问地址
	 * Web 端地址
	 */
	@Field(write = Field.Write.ALWAYS)
	private String webUrl;

	/**
	 * 安卓安装包下载地址
	 * Android 安装包地址
	 */
	@Field(write = Field.Write.ALWAYS)
	private String androidApkUrl;

	/**
	 * ios应用商店跳转地址
	 * iOS App Store 地址
	 */
	@Field(write = Field.Write.ALWAYS)
	private String iosAppStoreUrl;


	/**
	 * metadata
	 * 元信息，包含创建与更新的用户及时间
	 */
	@Builder.Default
	@Field(write = Field.Write.ALWAYS)
	private AppUserMetadataMongodb metadata = new AppUserMetadataMongodb();

	public static final MongodbField FIELD = new MongodbField();

	public static class MongodbField extends AbstractAppUserMetadataMongodbField {
		public final String APP_ID = field("appId");
		public final String ENDPOINT_ID = field("endpointId");
		public final String TYPE = field("type");
		public final String APP_VERSION = field("appVersion");
		public final String RELEASE_VERSION = field("releaseVersion");
		public final String LATEST_VERSION = field("latestVersion");
		public final String TITLE = field("title");
		public final String REMARK = field("remark");
		public final String FORCE = field("force");

		public final String WEB_URL = field("webUrl");
		public final String ANDROID_APK_URL = field("androidApkUrl");
		public final String IOS_APP_STORE_URL = field("iosAppStoreUrl");

	}
}
