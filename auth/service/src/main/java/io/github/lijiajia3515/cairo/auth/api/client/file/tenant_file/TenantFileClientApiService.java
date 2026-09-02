package io.github.lijiajia3515.cairo.auth.api.client.file.tenant_file;

import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.core.exception.ConflictBusinessException;
import io.github.lijiajia3515.cairo.auth.domain.api.client.file.tenant_file.AccessFileArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.file.tenant_file.DeleteFileArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.file.tenant_file.GetFileStatArgs;
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
 * [client/api] file service
 */
@Service
@Slf4j
public class TenantFileClientApiService {
	public final String DEFAULT_S3_URL;

	public final String DEFAULT_ACCESS_URL;

	private final String endpoint;
	private final MinioClient adminMinioClient;
	private final AsyncFileService asyncFileService;


	public TenantFileClientApiService(MinioClient adminMinioClient, MinioProperties properties, AsyncFileService asyncFileService) {
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
		bizId = "tenant_file:access_file",
		scope = "read",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public List<String> accessFile(String appId, AccessFileArgs args) {
		String tenantId = args.getTenantId();
		int secondExpiry = (int) Optional.ofNullable(args.getTtl()).orElse(Duration.ofHours(1)).toSeconds();
		return args.getS3Urls().stream()
			.map(s3Url -> {
				String httpUrl = null;
				try {
					Map<String, String> source = decodeS3Meta(s3Url);
					String bucket = source.get(BUCKET_NAME);
					String path = source.get(PATH_NAME);

					// 访问其他企业存储文件 返回默认值
					if (tenantId != null && List.of(PUBLIC_BUCKET_NAME, TEMPORARY_BUCKET_NAME, APP_BUCKET_NAME, tenantId).contains(bucket)) {
						httpUrl = DEFAULT_ACCESS_URL;
					}

					// 非法获取企业应用文件 返回默认值
					if (appId != null && bucket.equals(tenantId) && !path.startsWith(appId + "/")) {
						httpUrl = DEFAULT_ACCESS_URL;
					}

					if (getS3UrlIsPublicUrl(s3Url)) {
						httpUrl = encodeS3PublicUrl(s3Url, endpoint, args.isEnableVersion());
					} else {
						String version = null;
						if (args.isEnableVersion() && source.containsKey(VERSION_NAME)) {
							version = Optional.ofNullable(source.get(VERSION_NAME)).filter(x -> !x.isEmpty()).orElse(null);
						}
						httpUrl = adminMinioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
							.bucket(source.get(BUCKET_NAME))
							.object(source.get(PATH_NAME))
							.versionId(version)
							.method(Method.GET)
							.expiry(secondExpiry, TimeUnit.SECONDS)
							.build());
					}
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


	@NewSpan
	@BizLog(
		bizId = "tenant_file:get_file_stat",
		scope = "read",
		params = {
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public List<FileStat> getFileStat(@Validated GetFileStatArgs args) {
		String tenantId = args.getTenantId();
		final StatObjectArgs.Builder builder = StatObjectArgs.builder();
		return args.getS3Urls().stream().map(s3Url -> {
			FileStat fileStat = null;
			try {
				Map<String, String> sourceMap = decodeS3Meta(s3Url);
				if (sourceMap.get(BUCKET_NAME).equals(tenantId)) {
					final StatObjectArgs statObjectArgs = builder
						.bucket(tenantId)
						.object(sourceMap.get(PATH_NAME))
						.versionId(args.isEnableVersion() ? Optional.ofNullable(sourceMap.get(VERSION_NAME)).filter(y -> !y.isEmpty()).orElse(null) : null)
						.build();
					fileStat = FileConverter.convert(adminMinioClient.statObject(statObjectArgs));
				}
			} catch (IllegalArgumentException | ErrorResponseException | InsufficientDataException | InternalException |
					 InvalidKeyException |
					 InvalidResponseException | IOException | NoSuchAlgorithmException | ServerException |
					 XmlParserException e) {
				log.debug("错误", e);
			}

			if (fileStat == null) {
				fileStat = FileStat.builder()
					.exists(false)
					.bucket(tenantId)
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
	 * 文件上传
	 *
	 * @param tenantId 企业ID
	 * @param path     文件地址
	 * @param file     文件
	 * @return 文件信息
	 */
	@NewSpan
	@BizLog(
		bizId = "tenant_file:upload_file",
		scope = "write",
		params = {
			@BizLog.Param(key = "bucket", value = "#bucket"),
			@BizLog.Param(key = "path", value = "#path"),
			@BizLog.Param(key = "file", value = "'***'"),
		}
	)
	public List<String> uploadFile(String tenantId, String path, MultipartFile file) {
		try (InputStream in = file.getInputStream()) {
			final PutObjectArgs.Builder argsBuilder = PutObjectArgs.builder();
			argsBuilder.stream(in, -1, 1024 * 1024 * 100);

			argsBuilder
				.bucket(tenantId)
				.object(path)
				.contentType(file.getContentType())
			;
			ObjectWriteResponse objectWriteResponse = this.adminMinioClient.putObject(argsBuilder.build());
			String version = objectWriteResponse.versionId();
			final String presignedObjectUrl = this.adminMinioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
				.bucket(tenantId)
				.object(path)
				.expiry(1, TimeUnit.DAYS)
				.method(Method.GET)
				.build());

			return List.of(path, encodeS3Url(tenantId, path, version), presignedObjectUrl);
		} catch (IOException | ServerException | InsufficientDataException | ErrorResponseException |
				 NoSuchAlgorithmException | InvalidKeyException | InvalidResponseException | XmlParserException |
				 InternalException e) {
			throw new ConflictBusinessException("文件上传失败", e, FileBusiness.UPLOAD_FAILED);
		}

	}

	/**
	 * 企业文件删除
	 *
	 * @param args 参数
	 */
	@NewSpan
	@BizLog(
		bizId = "tenant_file:delete_file",
		scope = "write",
		params = {
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public void deleteFile(@Validated DeleteFileArgs args) {
		String tenantId = args.getTenantId();
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
		if (removeObjectList.stream().anyMatch(x -> !x.get(BUCKET_NAME).equals(tenantId) || !x.get(PATH_NAME).startsWith(args.getKeyPrefix()))) {
			throw new ConflictBusinessException("删除失败，含有不符合的文件地址");
		}

		Iterable<Result<DeleteError>> results = adminMinioClient.removeObjects(RemoveObjectsArgs.builder()
			.bucket(tenantId)
			.objects(removeObjectList.stream()
				.map(x -> new DeleteObject(x.get(PATH_NAME), Optional.ofNullable(x.get(VERSION_NAME)).filter(y -> !y.isEmpty()).orElse(null)))
				.collect(Collectors.toList())
			)
			.build());
		asyncFileService.deleteFile(results);
	}


}
