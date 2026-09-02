package io.github.lijiajia3515.cairo.auth.modules.file.common_file;

import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.core.exception.ConflictBusinessException;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.file.common_file.DeleteFileArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.file.common_file.GetFileStatArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.file.FileStat;
import io.github.lijiajia3515.cairo.auth.framework.minio.MinioProperties;
import io.github.lijiajia3515.cairo.auth.modules.file.*;
import io.micrometer.tracing.annotation.NewSpan;
import io.minio.*;
import io.minio.errors.*;
import io.minio.http.Method;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.github.lijiajia3515.cairo.auth.config.MinioConfig.ADMIN;
import static io.github.lijiajia3515.cairo.auth.modules.file.FileConverter.urlConverter;
import static io.github.lijiajia3515.cairo.auth.modules.file.FileTools.*;

/**
 * [common] file service
 */
@Slf4j
@Validated
@Component
public class CommonFileCommonService {

	private final MinioClient adminMinioClient;
	private final String endpoint;

	public final String DEFAULT_S3_URL;
	public final String DEFAULT_ACCESS_URL;

	private final AsyncFileService asyncFileService;


	public CommonFileCommonService(MinioClient adminMinioClient, MinioProperties properties,
								   AsyncFileService asyncFileService) {
		MinioProperties.Instance instance = properties.getConfig().get(ADMIN);
		endpoint = instance.getEndpoint();
		this.adminMinioClient = adminMinioClient;

		this.DEFAULT_S3_URL = properties.getDefaultS3Url();
		this.DEFAULT_ACCESS_URL = properties.getDefaultAccessUrl();
		this.asyncFileService = asyncFileService;
	}

	@SneakyThrows
	@NewSpan
	@BizLog(
		bizId = "common_file:get_access_file_url",
		scope = "read",
		params = {
			@BizLog.Param(key = "s3Url", value = "#s3Url"),
			@BizLog.Param(key = "enableVersion", value = "#enableVersion"),
		}
	)
	public String getAccessFileUrl(String s3Url, boolean enableVersion) {
		if (FileTools.getS3UrlIsPublicUrl(s3Url)) {
			return FileTools.encodeS3PublicUrl(s3Url, endpoint, enableVersion);
		}
		Map<String, String> map = FileTools.decodeS3Meta(s3Url);
		String version = null;
		if (enableVersion && map.containsKey(VERSION_NAME)) {
			version = Optional.ofNullable(map.get(VERSION_NAME)).filter(x -> !x.isEmpty()).orElse(null);
		}

		return adminMinioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
			.bucket(map.get(FileTools.BUCKET_NAME))
			.object(map.get(FileTools.PATH_NAME))
			.versionId(version)
			.method(Method.GET)
			.expiry(2, TimeUnit.HOURS)
			.build());
	}

	/**
	 * 获取文件属性
	 *
	 * @param args 参数值
	 * @return 文件属性
	 */
	@NewSpan
	@BizLog(
		bizId = "common_file:get_file_stat",
		scope = "read",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public List<FileStat> getFileStat(GetFileStatArgs args) {
		final StatObjectArgs.Builder builder = StatObjectArgs.builder();
		return args.getS3Urls().stream().map(x -> {
			FileStat fileStat = null;
			try {
				Map<String, String> sourceMap = decodeS3Meta(x);
				final StatObjectArgs statObjectArgs = builder
					.bucket(sourceMap.get(BUCKET_NAME))
					.object(sourceMap.get(PATH_NAME))
					.versionId(Optional.ofNullable(sourceMap.get(VERSION_NAME)).filter(y -> !y.isEmpty()).orElse(null))
					.build();
				fileStat = FileConverter.convert(adminMinioClient.statObject(statObjectArgs));
			} catch (IllegalArgumentException | ErrorResponseException | InsufficientDataException | InternalException |
					 InvalidKeyException |
					 InvalidResponseException | IOException | NoSuchAlgorithmException | ServerException |
					 XmlParserException e) {
				log.debug("错误", e);

			}
			if (fileStat == null) {
				fileStat = FileStat.builder()
					.exists(false)
					.bucket("default")
					.object(x).lastModified(LocalDateTime.now())
					.size(0L).
					userMetadata(Collections.emptyMap())
					.build();
			}
			fileStat.setS3Url(x);
			return fileStat;
		}).collect(Collectors.toList());
	}

	@NewSpan
	@BizLog(
		bizId = "common_file:get_upload_file_sign_url",
		scope = "write",
		params = {
			@BizLog.Param(key = "bucket", value = "#bucket"),
			@BizLog.Param(key = "paths", value = "#paths"),
		}
	)
	public List<List<String>> getUploadFileSignUrl(String bucket, List<String> paths, Map<String, String> headers) {

		return paths.stream()
			.map(path -> {
				try {
					GetPresignedObjectUrlArgs.Builder builder = GetPresignedObjectUrlArgs.builder()
						.bucket(bucket)
						.object(path)
						.method(Method.PUT)
						.expiry(2, TimeUnit.HOURS);

					if (headers != null && !headers.isEmpty()) {
						builder.extraHeaders(headers);
					}

					String presignedObjectUrl = adminMinioClient.getPresignedObjectUrl(
						builder.build());
					String s3Url = FileTools.encodeS3Url(bucket, path);
					return List.of(path, s3Url, presignedObjectUrl);
				} catch (IllegalArgumentException | ErrorResponseException | InsufficientDataException |
						 InternalException | InvalidKeyException |
						 InvalidResponseException | IOException | NoSuchAlgorithmException | XmlParserException |
						 ServerException e) {
					log.info("获取文件上传url失败", e);
					return List.of(path, DEFAULT_S3_URL, DEFAULT_ACCESS_URL);
				}
			}).collect(Collectors.toList());
	}


	/**
	 * 文件复制
	 *
	 * @param source 源地址
	 * @param target 目标地址
	 */
	@SneakyThrows
	@NewSpan
	@BizLog(
		bizId = "common_file:copy_file",
		scope = "write",
		params = {
			@BizLog.Param(key = "source", value = "#source"),
			@BizLog.Param(key = "target", value = "#target"),
		}
	)
	public String copyFile(String source, String target) {
		Map<String, String> sourceMap = FileTools.decodeS3Meta(source);
		Map<String, String> targetMap = FileTools.decodeS3Meta(target);
		ObjectWriteResponse objectWriteResponse = adminMinioClient.copyObject(CopyObjectArgs.builder()
			.bucket(targetMap.get(FileTools.BUCKET_NAME))
			.object(targetMap.get(FileTools.PATH_NAME))
			.source(CopySource.builder()
				.bucket(sourceMap.get(FileTools.BUCKET_NAME))
				.object(sourceMap.get(FileTools.PATH_NAME))
				.versionId(Optional.ofNullable(sourceMap.get(VERSION_NAME)).filter(x -> !x.isEmpty()).orElse(null))
				.build())
			.build());
		String version = objectWriteResponse.versionId();
		return FileTools.encodeS3Url(targetMap.get(FileTools.BUCKET_NAME), targetMap.get(FileTools.PATH_NAME), version);
	}


	/**
	 * 删除文件
	 *
	 * @param args 参数
	 */
	@NewSpan
	@BizLog(
		bizId = "common_file:delete_file",
		scope = "write",
		params = {
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public void deleteFile(@Validated DeleteFileArgs args) {
		List<Map<String, String>> s3ObjectMapList = args.getS3Urls().stream().map(x -> {
			try {
				return decodeS3Meta(x);
			} catch (Exception e) {
				return null;
			}
		}).collect(Collectors.toList());

		List<Map<String, String>> httpObjectMapList = args.getHttpUrls().stream().map(x -> {
			try {
				return decodeHttpMeta(x);
			} catch (Exception e) {
				return null;
			}
		}).collect(Collectors.toList());

		List<Map<String, String>> removeObjectList = Stream.concat(s3ObjectMapList.stream(), httpObjectMapList.stream()).filter(Objects::nonNull).distinct().collect(Collectors.toList());

		// 非空校验
		if (removeObjectList.isEmpty()) {
			throw new ConflictBusinessException("s3Paths,httpPaths至少一个字段不能为空");
		}

		Map<String, List<Map<String, String>>> removeBucketObjectListMap = removeObjectList.stream().collect(Collectors.groupingBy(x -> x.get(BUCKET_NAME)));

		removeBucketObjectListMap.forEach((key, value) -> {
			Iterable<Result<DeleteError>> results = adminMinioClient.removeObjects(RemoveObjectsArgs.builder()
				.bucket(key)
				.objects(value.stream()
					.map(x -> new DeleteObject(x.get(PATH_NAME), Optional.ofNullable(x.get(VERSION_NAME)).filter(y -> !y.isEmpty()).orElse(null)))
					.collect(Collectors.toList())
				)
				.build());
			asyncFileService.deleteFile(results);
		});
	}

	@Async
	public void deleteFile(String keyPrefix, List<String> urls) {
		try {
			if (!urls.isEmpty()) {
				UrlConverter urlConverter = urlConverter(urls);
				deleteFile(DeleteFileArgs.builder()
					.keyPrefix(keyPrefix)
					.s3Urls(urlConverter.getS3Urls())
					.httpUrls(urlConverter.getHttpUrls())
					.build()
				);
			}
		} catch (Exception e) {
			log.error("删除存储文件失败", e);
		}
	}


}
