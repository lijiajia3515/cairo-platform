package io.github.lijiajia3515.cairo.auth.api.client.file.app_file;

import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.core.exception.ConflictBusinessException;
import io.github.lijiajia3515.cairo.auth.domain.api.client.file.app_file.AccessFileArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.file.app_file.DeleteFileArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.file.app_file.GetFileStatArgs;
import io.github.lijiajia3515.cairo.auth.framework.minio.MinioProperties;
import io.github.lijiajia3515.cairo.auth.modules.file.AsyncFileService;
import io.github.lijiajia3515.cairo.auth.modules.file.FileBusiness;
import io.github.lijiajia3515.cairo.auth.modules.file.FileConverter;
import io.github.lijiajia3515.cairo.auth.domain.dto.file.FileStat;
import io.micrometer.tracing.annotation.NewSpan;
import io.minio.*;
import io.minio.errors.*;
import io.minio.http.Method;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.github.lijiajia3515.cairo.auth.config.MinioConfig.ADMIN;
import static io.github.lijiajia3515.cairo.auth.modules.file.FileConstants.*;
import static io.github.lijiajia3515.cairo.auth.modules.file.FileTools.*;

/**
 * [client/api] app file service
 */
@Service
@Slf4j
public class AppFileClientApiService {
	public final String DEFAULT_S3_URL;

	public final String DEFAULT_ACCESS_URL;

	private final String endpoint;
	private final MinioClient adminMinioClient;
	private final AsyncFileService asyncFileService;


	public AppFileClientApiService(MinioClient adminMinioClient, MinioProperties properties, AsyncFileService asyncFileService) {
		this.adminMinioClient = adminMinioClient;
		MinioProperties.Instance instance = properties.getConfig().get(ADMIN);
		Assert.notNull(instance, "minio admin config not null");
		this.endpoint = instance.getEndpoint();
		this.DEFAULT_S3_URL = properties.getDefaultS3Url();
		this.DEFAULT_ACCESS_URL = properties.getDefaultAccessUrl();
		this.asyncFileService = asyncFileService;
	}

	/**
	 * 获取存储访问地址
	 *
	 * @param appId 应用ID
	 * @param args  参数
	 * @return 数组列表
	 */
	@NewSpan
	@BizLog(
		bizId = "app_file:access_file",
		scope = "read",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public List<String> accessFile(String appId, AccessFileArgs args) {
		int secondExpiry = (int) Optional.ofNullable(args.getTtl()).orElse(Duration.ofHours(1)).toSeconds();
		return args.getS3Urls().stream()
			.map(s3Url -> {
				String httpUrl = null;
				try {
					Map<String, String> source = decodeS3Meta(s3Url);
					String bucket = source.get(BUCKET_NAME);
					String path = source.get(PATH_NAME);

					// 访问其他存储文件 返回默认值
					if (!Objects.equals(APP_BUCKET_NAME, bucket)) {
						httpUrl = DEFAULT_ACCESS_URL;
						return httpUrl;
					}

					// 非法获取企业应用文件 返回默认值
					if (appId == null || !path.startsWith(appId + "/")) {
						httpUrl = DEFAULT_ACCESS_URL;
						return httpUrl;
					}

					String version = null;
					if (args.isEnableVersion() && source.containsKey(VERSION_NAME)) {
						version = Optional.ofNullable(source.get(VERSION_NAME)).filter(x -> !x.isEmpty()).orElse(null);
					}
					httpUrl = adminMinioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
						.bucket(bucket)
						.object(path)
						.versionId(version)
						.method(Method.GET)
						.expiry(secondExpiry, TimeUnit.SECONDS)
						.build());

				} catch (IllegalArgumentException e) {
					log.debug("{} is fail url", s3Url);
				} catch (ErrorResponseException | InsufficientDataException | InternalException |
						 InvalidKeyException |
						 InvalidResponseException | IOException | NoSuchAlgorithmException | XmlParserException |
						 ServerException e) {
					log.info("minio-执行异常", e);
				}
				return httpUrl == null ? DEFAULT_ACCESS_URL : httpUrl;
			})
			.collect(Collectors.toList());
	}

	/**
	 * 获取文件属性
	 *
	 * @param appId 应用ID
	 * @param args  参数
	 * @return 文件属性
	 */
	@NewSpan
	@BizLog(
		bizId = "app_file:get_file_stat",
		scope = "read",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public List<FileStat> getFileStat(@Valid @NotNull String appId, @Validated GetFileStatArgs args) {
		final StatObjectArgs.Builder builder = StatObjectArgs.builder();
		return args.getS3Urls().stream().map(s3Url -> {
			FileStat fileStat = null;
			try {
				Map<String, String> sourceMap = decodeS3Meta(s3Url);
				if (sourceMap.get(BUCKET_NAME).equals(APP_BUCKET_NAME) && sourceMap.get(PATH_NAME).startsWith(appId + "/")) {
					final StatObjectArgs statObjectArgs = builder
						.bucket(APP_BUCKET_NAME)
						.object(sourceMap.get(PATH_NAME))
						.versionId(args.isEnableVersion() ? Optional.ofNullable(sourceMap.get(VERSION_NAME)).filter(y -> !y.isEmpty()).orElse(null) : null)
						.build();
					fileStat = FileConverter.convert(adminMinioClient.statObject(statObjectArgs));
				}
			} catch (IllegalArgumentException | ErrorResponseException | InsufficientDataException |
					 InternalException |
					 InvalidKeyException |
					 InvalidResponseException | IOException | NoSuchAlgorithmException | ServerException |
					 XmlParserException e) {
				log.debug("minio-执行异常", e);
			}
			if (fileStat == null) {
				fileStat= FileStat.builder()
					.exists(false)
					.bucket(APP_BUCKET_NAME)
					.object(null)
					.lastModified(LocalDateTime.now())
					.size(0L)
					.userMetadata(Collections.emptyMap())
					.build();
			}
			fileStat.setS3Url(s3Url);
			return fileStat;
		}).collect(Collectors.toList());
	}

	/**
	 * 上传文件
	 *
	 * @param appId appId
	 * @param path  path
	 * @param file  file
	 * @return file list
	 */
	@NewSpan
	@BizLog(
		bizId = "app_file:upload_file",
		scope = "write",
		params = {
			@BizLog.Param(key = "appId", value = "#appid"),
			@BizLog.Param(key = "path", value = "#path"),
			@BizLog.Param(key = "file", value = "'***'"),
		}
	)
	public List<String> uploadFile(String appId, String path, MultipartFile file) {
		String bucket = APP_BUCKET_NAME;
		String realPath = APP_PATH_STRING.replace(VAR_APP_ID_NAME, appId).replace(VAR_KEY_NAME, path);
		try (InputStream in = file.getInputStream()) {
			final PutObjectArgs.Builder argsBuilder = PutObjectArgs.builder();
			argsBuilder.stream(in, -1, 1024 * 1024 * 100);

			argsBuilder
				.bucket(bucket)
				.object(realPath)
				.contentType(file.getContentType());
			ObjectWriteResponse objectWriteResponse = this.adminMinioClient.putObject(argsBuilder.build());
			String version = objectWriteResponse.versionId();
			String s3Url = encodeS3Url(bucket, realPath, version);
			final String presignedObjectUrl = this.adminMinioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
				.bucket(APP_BUCKET_NAME)
				.object(realPath)
				.versionId(version)
				.expiry(1, TimeUnit.DAYS)
				.method(Method.GET)
				.build());
			return List.of(path, s3Url, presignedObjectUrl);
		} catch (IOException | ServerException | InsufficientDataException | ErrorResponseException |
				 NoSuchAlgorithmException | InvalidKeyException | InvalidResponseException | XmlParserException |
				 InternalException e) {
			throw new ConflictBusinessException("文件上传失败", e, FileBusiness.UPLOAD_FAILED);
		}

	}

	/**
	 * 删除文件
	 *
	 * @param args 参数
	 */
	@NewSpan
	@BizLog(
		bizId = "app_file:delete_file",
		scope = "write",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public void deleteFile(@Valid @NotNull String appId, @Validated DeleteFileArgs args) {
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

		// 校验文件合法
		String realKeyPrefix = APP_PATH_STRING.replace(VAR_APP_ID_NAME, appId).replace(VAR_KEY_NAME, args.getKeyPrefix());
		if (removeObjectList.stream().anyMatch(x -> !x.get(BUCKET_NAME).equals(APP_BUCKET_NAME) || !x.get(PATH_NAME).startsWith(realKeyPrefix))) {
			throw new ConflictBusinessException("删除失败，含有不符合的文件地址");
		}

		Iterable<Result<DeleteError>> results = adminMinioClient.removeObjects(RemoveObjectsArgs.builder()
			.bucket(APP_BUCKET_NAME)
			.objects(removeObjectList.stream()
				.map(x -> new DeleteObject(x.get(PATH_NAME), Optional.ofNullable(x.get(VERSION_NAME)).filter(y -> !y.isEmpty()).orElse(null)))
				.collect(Collectors.toList())
			)
			.build());
		asyncFileService.deleteFile(results);
	}
}
