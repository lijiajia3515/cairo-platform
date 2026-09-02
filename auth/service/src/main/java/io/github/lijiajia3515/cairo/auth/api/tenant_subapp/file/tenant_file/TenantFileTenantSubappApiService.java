package io.github.lijiajia3515.cairo.auth.api.tenant_subapp.file.tenant_file;

import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.core.CoreConstants;
import io.github.lijiajia3515.cairo.core.exception.BusinessException;
import io.github.lijiajia3515.cairo.core.exception.ConflictBusinessException;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.file.tenant_file.AccessFileArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.file.tenant_file.DeleteFileArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.file.tenant_file.GetFileStatArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.file.tenant_file.UploadFileSignArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.file.FileStat;
import io.github.lijiajia3515.cairo.auth.domain.dto.file.UploadSignArgs;
import io.github.lijiajia3515.cairo.auth.framework.minio.MinioProperties;
import io.github.lijiajia3515.cairo.auth.modules.file.*;
import io.github.lijiajia3515.cairo.auth.modules.utils.FilesUtil;
import io.micrometer.tracing.annotation.NewSpan;
import io.minio.*;
import io.minio.errors.*;
import io.minio.http.Method;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.github.lijiajia3515.cairo.auth.config.MinioConfig.ADMIN;
import static io.github.lijiajia3515.cairo.auth.modules.file.FileTools.*;

/**
 * [tenant_subapp_user/api]tenant app subapp tenant file service
 */
@Service
@Slf4j
public class TenantFileTenantSubappApiService {
	private final MinioClient adminMinioClient;

	private final AsyncFileService asyncFileService;

	private final String endpoint;

	public final String DEFAULT_S3_URL;

	public final String DEFAULT_ACCESS_URL;

	public TenantFileTenantSubappApiService(MinioClient adminMinioClient, AsyncFileService asyncFileService, MinioProperties properties) {
		MinioProperties.Instance instance = properties.getConfig().get(ADMIN);
		this.endpoint = instance.getEndpoint();
		this.adminMinioClient = adminMinioClient;
		this.asyncFileService = asyncFileService;
		this.DEFAULT_S3_URL = properties.getDefaultS3Url();
		this.DEFAULT_ACCESS_URL = properties.getDefaultAccessUrl();
	}

	@NewSpan
	@BizLog(
		bizId = "tenant_file:access_file",
		scope = "read",
		params = {
			@BizLog.Param(key = "tenantId", value = "#tenantId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public List<String> accessFile(@Valid @NotNull String tenantId, @Validated AccessFileArgs args) {
		int secondExpiry = (int) Optional.ofNullable(args.getTtl()).orElse(Duration.ofHours(1)).toSeconds();
		return args.getS3Urls().stream()
			.map(s3Url -> {
				try {
					Map<String, String> source = decodeS3Meta(s3Url);
					String bucket = source.get(BUCKET_NAME);

					if (!Objects.equals(tenantId, bucket)) {
						return DEFAULT_ACCESS_URL;
					}

					String version = null;
					if (args.isEnableVersion() && source.containsKey(VERSION_NAME)) {
						version = Optional.ofNullable(source.get(VERSION_NAME)).filter(y -> !y.isEmpty()).orElse(null);
					}
					return adminMinioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
						.bucket(source.get(BUCKET_NAME))
						.object(source.get(PATH_NAME))
						.versionId(version)
						.method(Method.GET)
						.expiry(secondExpiry, TimeUnit.SECONDS)
						.build());

				} catch (IllegalArgumentException e) {
					log.debug("{} is fail url", s3Url);
					return DEFAULT_ACCESS_URL;
				} catch (ErrorResponseException | InsufficientDataException | InternalException |
						 InvalidKeyException |
						 InvalidResponseException | IOException | NoSuchAlgorithmException | XmlParserException |
						 ServerException e) {
					log.info("文件存储服务异常", e);
					// throw new UnknownBusinessException("文件存储服务异常");
					return DEFAULT_S3_URL;
				}
			})
			.collect(Collectors.toList());
	}

	@SneakyThrows
	@NewSpan
	@BizLog(
		bizId = "tenant_file:access_file_url",
		scope = "read",
		params = {
			@BizLog.Param(key = "s3Url", value = "#s3Url"),
			@BizLog.Param(key = "enableVersion", value = "#enableVersion"),
		}
	)
	public String accessFileUrl(@Valid @NotNull String s3Url, boolean enableVersion) {
		if (getS3UrlIsPublicUrl(s3Url)) {
			return encodeS3PublicUrl(s3Url, endpoint, enableVersion);
		}
		Map<String, String> map = decodeS3Meta(s3Url);
		String version = null;
		if (enableVersion && map.containsKey(VERSION_NAME)) {
			version = Optional.ofNullable(map.get(VERSION_NAME)).filter(x -> !x.isEmpty()).orElse(null);
		}

		return adminMinioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
			.bucket(map.get(BUCKET_NAME))
			.object(map.get(PATH_NAME))
			.versionId(version)
			.method(Method.GET)
			.expiry(2, TimeUnit.HOURS)
			.build());
	}

	@NewSpan
	@BizLog(
		bizId = "tenant_file:get_file_stat",
		scope = "read",
		params = {
			@BizLog.Param(key = "tenantId", value = "#tenantId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public List<FileStat> getFileStat(String tenantId, @Validated GetFileStatArgs args) {
		final StatObjectArgs.Builder builder = StatObjectArgs.builder();
		return args.getS3Urls().stream().map(s3Url -> {
				FileStat fileStat = null;
				try {
					Map<String, String> sourceMap = decodeS3Meta(s3Url);
					String bucket = sourceMap.get(BUCKET_NAME);
					String path = sourceMap.get(PATH_NAME);

					if (bucket.equals(tenantId)) {
						StatObjectArgs statObjectArgs = builder
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
			})
			.collect(Collectors.toList());
	}

	/**
	 * 文件上传
	 *
	 * @param tenantId 租户ID
	 * @param path     文件名
	 * @param file     文件
	 * @return file list
	 */
	@NewSpan
	@BizLog(
		bizId = "tenant_file:upload_file",
		scope = "write",
		params = {
			@BizLog.Param(key = "tenantId", value = "#tenantId"),
			@BizLog.Param(key = "path", value = "#path"),
			@BizLog.Param(key = "file", value = "'***'"),
		}
	)
	public List<String> uploadFile(@Valid @NotNull String tenantId, @Valid @NotNull String path, MultipartFile file) {
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
	 * 上传文件
	 *
	 * @param tenantId tenantId
	 * @param files files
	 * @return file list
	 */
	@NewSpan
	@BizLog(
		bizId = "temporary_file:upload_files",
		scope = "write",
		params = {
			@BizLog.Param(key = "tenantId", value = "#tenantId"),
			@BizLog.Param(key = "prefix", value = "#prefix"),
			@BizLog.Param(key = "files", value = "'***'")
		}
	)
	public List<List<String>> uploadFiles(@Valid @NotNull String tenantId, String prefix, List<MultipartFile> files) {
		String pathPrefix = Optional.ofNullable(prefix).orElse("unknown");
		return files.parallelStream()
			.map(file -> {
				String contentType = file.getContentType();
				String originalFilename = Optional.ofNullable(file.getOriginalFilename()).filter(x -> !x.isBlank()).map(x -> FilesUtil.getFilename(x, 50)).orElse(null);
				String filename = Optional.ofNullable(originalFilename).orElse(CoreConstants.SNOWFLAKE.nextIdStr());

				try (InputStream in = file.getInputStream()) {
					String realPath = pathPrefix.concat("/").concat(filename);
					final PutObjectArgs.Builder argsBuilder = PutObjectArgs.builder();
					argsBuilder.stream(in, -1, 1024 * 1024 * 100);

					argsBuilder.object(realPath)
						.contentType(contentType)
						.bucket(tenantId);
					ObjectWriteResponse objectWriteResponse = this.adminMinioClient.putObject(argsBuilder.build());
					String version = objectWriteResponse.versionId();
					String s3Url = encodeS3Url(tenantId, realPath, version);
					final String httpUrl = this.adminMinioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
						.bucket(tenantId)
						.object(realPath)
						.versionId(version)
						.expiry(1, TimeUnit.DAYS)
						.method(Method.GET)
						.build());
					return List.of(filename, s3Url, httpUrl);
				} catch (IOException | ServerException | InsufficientDataException | ErrorResponseException |
						 NoSuchAlgorithmException | InvalidKeyException | InvalidResponseException |
						 XmlParserException | InternalException e) {
					throw new ConflictBusinessException("文件上传失败", e, FileBusiness.UPLOAD_FAILED);
				}
			})
			.collect(Collectors.toList());
	}

	@NewSpan
	@BizLog(
		bizId = "tenant_file:get_upload_file_sign",
		scope = "write",
		params = {
			@BizLog.Param(key = "tenantId", value = "#tenantId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public UploadSignArgs getUploadFileSign(@Valid @NotNull String tenantId, @Validated UploadFileSignArgs args) {
		ZonedDateTime time = ZonedDateTime.now().plus(args.getTtl());
		PostPolicy policy = new PostPolicy(tenantId, time);
		String keyPrefix = Optional.ofNullable(args.getKeyPrefix()).orElse("unknown/");
		policy.addStartsWithCondition("key", keyPrefix);
		Optional.ofNullable(args.getMeta())
			.orElse(Collections.emptyMap())
			.forEach((k, v) -> policy.addStartsWithCondition(String.format("x-amz-meta-%s", k.toLowerCase(Locale.ROOT)), v));
		Map<String, String> formData;
		try {
			formData = adminMinioClient.getPresignedPostFormData(policy);
		} catch (ErrorResponseException | InsufficientDataException | InternalException | InvalidKeyException |
				 InvalidResponseException | IOException | NoSuchAlgorithmException | XmlParserException |
				 ServerException e) {
			log.info("文件存储服务异常", e);
			throw new BusinessException("文件存储服务异常", FileBusiness.SIGN_FAILED);
		}
		return UploadSignArgs.builder()
			.endpoint(endpoint)
			.bucket(tenantId)
			.expiresTime(time.toLocalDateTime())
			.keyPrefix(keyPrefix)
			.signPostFormData(formData)
			.build();
	}

	/**
	 * 获取上传文件签名Url
	 *
	 * @param tenantId 企业ID
	 * @param paths    文件路径
	 * @return 已签名的文件地址
	 */
	@NewSpan
	@BizLog(
		bizId = "tenant_file:get_upload_file_sign_url",
		scope = "read",
		params = {
			@BizLog.Param(key = "tenantId", value = "#tenantId"),
			@BizLog.Param(key = "paths", value = "#paths"),
		}
	)
	public List<List<String>> getUploadFileSignUrl(@Valid @NotNull String tenantId, @Valid @NotNull List<String> paths) {
		return paths.stream()
			.map(path -> {
				try {
					String presignedObjectUrl = adminMinioClient.getPresignedObjectUrl(
						GetPresignedObjectUrlArgs.builder()
							.bucket(tenantId)
							.object(path)
							.method(Method.PUT)
							.expiry(2, TimeUnit.HOURS)
							.build());
					String s3Url = encodeS3Url(tenantId, path);
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
	 * 删除文件
	 *
	 * @param args 参数
	 */
	@NewSpan
	@BizLog(
		bizId = "tenant_file:delete_file",
		scope = "write",
		params = {
			@BizLog.Param(key = "tenantId", value = "#tenantId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public void deleteFile(@Valid @NotNull String tenantId, @Validated DeleteFileArgs args) {
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
