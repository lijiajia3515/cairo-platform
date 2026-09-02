package io.github.lijiajia3515.cairo.auth.modules.file;

public interface FileConstants {
	/**
	 * 公共存储桶
	 */
	String PUBLIC_BUCKET_NAME = "public";

	/**
	 * 应用存储桶
	 */
	String APP_BUCKET_NAME = "app";

	/**
	 * 临时存储通
	 */
	String TEMPORARY_BUCKET_NAME = "temporary";
	/**
	 * 路径前缀
	 */
	String APP_PATH_STRING = "$appId/$key";
	String VAR_APP_ID_NAME = "$appId";
	String VAR_KEY_NAME = "$key";

}
