package io.github.lijiajia3515.cairo.auth.modules.file;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class FileTools {
	public static final String SCHEMA_NAME = "schema";
	public static final String AUTH_NAME = "auth";
	public static final String HTTP_AUTH_NAME = "httpAuth";
	public static final String ENDPOINT_NAME = "endpoint";
	public static final String BUCKET_NAME = "bucket";
	public static final String PATH_NAME = "path";
	public static final String VERSION_NAME = "version";
	public static final Pattern S3_PATTERN = Pattern.compile("^((?<schema>s3[na]?)://)((?<auth>.*:.*)@)?(?<bucket>[^/\\n]{3,63})/(?<path>[^?:]{1,1024})(\\?version=(?<version>.*)?)?$");
	public static final Pattern HTTP_PATTERN = Pattern.compile("^((?<schema>http[s]?)://)((?<httpAuth>.*:.*)@)?(?<endpoint>[^/\\n]{1,63})/((?<auth>.*:.*)@)?(?<bucket>[^/\\n]{3,63})/(?<path>[^?:]{1,1024})(\\?.*(versionId=(?<version>[^/\\n]{1,63})(&.*)?$))?$");

	public static final String S3_FORMAT = "s3://$bucket/$path";

	public static final String S3_VERSION_FORMAT = "s3://$bucket/$path?version=$version";


	/**
	 * S3URL解码
	 *
	 * @param s3Url s3Url
	 * @return 解码后的存储通和对象
	 */
	public static Map<String, String> decodeS3Meta(String s3Url) {
		Matcher matcher = S3_PATTERN.matcher(s3Url);
		if (matcher.matches()) {
			final Map<String, String> result = new HashMap<>(5);
			result.put(SCHEMA_NAME, matcher.group(SCHEMA_NAME));
			result.put(AUTH_NAME, matcher.group(AUTH_NAME));
			result.put(BUCKET_NAME, matcher.group(BUCKET_NAME));
			result.put(PATH_NAME, matcher.group(PATH_NAME));
			result.put(VERSION_NAME, matcher.group(VERSION_NAME));
			return result;
		}
		throw new IllegalArgumentException("s3地址错误: " + s3Url);
	}

	/**
	 * HttpURL解码
	 *
	 * @param httpUrl httpUrl
	 * @return 解码后的存储通和对象
	 */
	public static Map<String, String> decodeHttpMeta(String httpUrl) {
		Matcher matcher = HTTP_PATTERN.matcher(httpUrl);
		if (matcher.matches()) {
			final Map<String, String> result = new HashMap<>(5);
			result.put(SCHEMA_NAME, matcher.group(SCHEMA_NAME));
			result.put(HTTP_AUTH_NAME, matcher.group(HTTP_AUTH_NAME));
			result.put(ENDPOINT_NAME, matcher.group(ENDPOINT_NAME));
			result.put(AUTH_NAME, matcher.group(AUTH_NAME));
			result.put(BUCKET_NAME, matcher.group(BUCKET_NAME));
			result.put(PATH_NAME, matcher.group(PATH_NAME));
			result.put(VERSION_NAME, matcher.group(VERSION_NAME));
			return result;
		}
		throw new IllegalArgumentException("http地址错误: " + httpUrl);
	}

	/**
	 * 获取s3url是否是公开存储通
	 *
	 * @param s3Url s3Url
	 * @return 是否是公开存储桶
	 */
	public static Boolean getS3UrlIsPublicUrl(String s3Url) {
		return Optional.ofNullable(decodeS3Meta(s3Url).get(BUCKET_NAME))
			.filter(x -> x.equals(FileConstants.PUBLIC_BUCKET_NAME))
			.isPresent();
	}

	/**
	 * 转换s3地址
	 *
	 * @param bucket 桶
	 * @param path   路径
	 * @return s3Url
	 */
	public static String encodeS3Url(String bucket, String path) {
		return S3_FORMAT.replace("$bucket", bucket)
			.replace("$path", path);
	}

	/**
	 * 转换s3地址(带version)
	 *
	 * @param bucket  桶
	 * @param path    路径
	 * @param version 版本
	 * @return s3Url
	 */
	public static String encodeS3Url(String bucket, String path, String version) {
		return S3_VERSION_FORMAT.replace("$bucket", bucket)
			.replace("$path", path)
			.replace("$version", Optional.ofNullable(version).orElse(""));
	}

	public static String encodeS3PublicUrl(String s3Url, String endpoint, boolean enabledVersion) {
		Map<String, String> map = FileTools.decodeS3Meta(s3Url);
		if (enabledVersion) {
			return "$endpoint/$bucket/$path?versionId=$version"
				.replace("$endpoint", endpoint)
				.replace("$bucket", map.get(BUCKET_NAME))
				.replace("$path", map.get(PATH_NAME))
				.replace("$version", Optional.ofNullable(map.get(VERSION_NAME)).orElse(""))
				;
		} else {
			return "$endpoint/$bucket/$path"
				.replace("$endpoint", endpoint)
				.replace("$bucket", map.get(BUCKET_NAME))
				.replace("$path", map.get(PATH_NAME));
		}
	}
}
