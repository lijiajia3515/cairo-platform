package io.github.lijiajia3515.cairo.auth.api.tenant_subapp.file.temporary_file;

import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.core.CoreConstants;
import io.github.lijiajia3515.cairo.core.exception.BusinessException;
import io.github.lijiajia3515.cairo.core.exception.ConflictBusinessException;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.file.temporary_file.AccessFileArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.file.temporary_file.DeleteFileArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.file.temporary_file.UploadFileSignArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.file.FileStat;
import io.github.lijiajia3515.cairo.auth.domain.dto.file.UploadSignArgs;
import io.github.lijiajia3515.cairo.auth.framework.minio.MinioProperties;
import io.github.lijiajia3515.cairo.auth.modules.file.*;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.file.temporary_file.GetFileStatArgs;
import io.github.lijiajia3515.cairo.auth.modules.utils.FilesUtil;
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
import static io.github.lijiajia3515.cairo.auth.modules.file.FileConstants.*;
import static io.github.lijiajia3515.cairo.auth.modules.file.FileTools.*;

/**
 * [tenant_subapp_user/api]tenant app subapp temporary file service
 */
@Service
@Slf4j
public class TemporaryFileTenantSubappApiService {
	private final MinioClient adminMinioClient;
	private final AsyncFileService asyncFileService;
	private final String endpoint;

	public final String DEFAULT_S3_URL;

	public final String DEFAULT_ACCESS_URL;

	public TemporaryFileTenantSubappApiService(MinioClient adminMinioClient,
													   AsyncFileService asyncFileService,
													   MinioProperties properties) {
		this.asyncFileService = asyncFileService;
		MinioProperties.Instance instance = properties.getConfig().get(ADMIN);
		this.endpoint = instance.getEndpoint();
		this.adminMinioClient = adminMinioClient;
		this.DEFAULT_S3_URL = properties.getDefaultS3Url();
		this.DEFAULT_ACCESS_URL = properties.getDefaultAccessUrl();
	}

	@NewSpan
	@BizLog(
		bizId = "temporary_file:access_file",
		scope = "read",
		params = {
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public List<String> accessFile(@Valid @NotNull String appId, @Validated AccessFileArgs args) {
		int secondExpiry = (int) Optional.ofNullable(args.getTtl()).orElse(Duration.ofHours(1)).toSeconds();
		return args.getS3Urls().stream()
			.map(s3Url -> {
				try {
					Map<String, String> source = decodeS3Meta(s3Url);
					String bucket = source.get(BUCKET_NAME);

					if (!Objects.equals(TEMPORARY_BUCKET_NAME, bucket)) {
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

	@NewSpan
	@BizLog(
		bizId = "temporary_file:access_file_url",
		scope = "read",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "s3Url", value = "#s3Url"),
			@BizLog.Param(key = "enableVersion", value = "#enableVersion"),
		}
	)
	public String accessFileUrl(String appId, @Valid @NotNull String s3Url, boolean enableVersion) {
		try {
			Map<String, String> map = decodeS3Meta(s3Url);
			Map<String, String> sourceMap = decodeS3Meta(s3Url);
			String bucket = sourceMap.get(BUCKET_NAME);
			String path = sourceMap.get(PATH_NAME);

			if (bucket.equals(TEMPORARY_BUCKET_NAME) && path.startsWith(appId + "/")) {
				String version = null;
				if (enableVersion && map.get(VERSION_NAME) != null) {
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
		} catch (IllegalArgumentException | ErrorResponseException | InsufficientDataException | InternalException |
				 InvalidKeyException |
				 InvalidResponseException | IOException | NoSuchAlgorithmException | ServerException |
				 XmlParserException e) {
			log.debug("错误", e);
		}
		return DEFAULT_ACCESS_URL;
	}

	@NewSpan
	@BizLog(
		bizId = "temporary_file:get_file_stat",
		scope = "read",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public List<FileStat> getFileStat(String appId, @Validated GetFileStatArgs args) {
		final StatObjectArgs.Builder builder = StatObjectArgs.builder();
		return args.getS3Urls().stream().map(s3Url -> {
				FileStat fileStat = null;
				try {
					Map<String, String> sourceMap = decodeS3Meta(s3Url);
					String bucket = sourceMap.get(BUCKET_NAME);
					String path = sourceMap.get(PATH_NAME);

					if (bucket.equals(TEMPORARY_BUCKET_NAME) && path.startsWith(appId + "/")) {
						StatObjectArgs statObjectArgs = builder
							.bucket(TEMPORARY_BUCKET_NAME)
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
						.bucket(TEMPORARY_BUCKET_NAME)
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
	 * 上传文件
	 *
	 * @param appId appId
	 * @param path  path
	 * @param file  file
	 * @return file list
	 */
	@NewSpan
	@BizLog(
		bizId = "temporary_file:upload_file",
		scope = "write",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "path", value = "#path"),
			@BizLog.Param(key = "file", value = "'***'"),
		}
	)
	public List<String> uploadFile(@Valid @NotNull String appId, @Valid @NotNull String path, MultipartFile file) {
		String realPath = APP_PATH_STRING.replace(VAR_APP_ID_NAME, appId).replace(VAR_KEY_NAME, path);
		try (InputStream in = file.getInputStream()) {
			final PutObjectArgs.Builder argsBuilder = PutObjectArgs.builder();
			argsBuilder.stream(in, -1, 1024 * 1024 * 100);

			argsBuilder
				.bucket(TEMPORARY_BUCKET_NAME)
				.object(realPath)
				.contentType(file.getContentType());
			ObjectWriteResponse objectWriteResponse = this.adminMinioClient.putObject(argsBuilder.build());
			String version = objectWriteResponse.versionId();

			String s3Url = encodeS3Url(TEMPORARY_BUCKET_NAME, realPath, version);

			final String httpUrl = this.adminMinioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
				.bucket(TEMPORARY_BUCKET_NAME)
				.object(realPath)
				.versionId(version)
				.expiry(1, TimeUnit.DAYS)
				.method(Method.GET)
				.build());

			return List.of(path, s3Url, httpUrl);
		} catch (IOException | ServerException | InsufficientDataException | ErrorResponseException |
				 NoSuchAlgorithmException | InvalidKeyException | InvalidResponseException | XmlParserException |
				 InternalException e) {
			throw new ConflictBusinessException("上传文件失败", e, FileBusiness.UPLOAD_FAILED);
		}
	}

	/**
	 * 上传文件
	 *
	 * @param appId appId
	 * @param files files
	 * @return file list
	 */
	@NewSpan
	@BizLog(
		bizId = "temporary_file:upload_files",
		scope = "write",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "prefix", value = "#prefix"),
			@BizLog.Param(key = "files", value = "'***'")
		}
	)
	public List<List<String>> uploadFiles(@Valid @NotNull String appId, String prefix, List<MultipartFile> files) {
		String pathPrefix = Optional.ofNullable(prefix).orElse("unknown");
		String appPathPrefix = APP_PATH_STRING.replace(VAR_APP_ID_NAME, appId).replace(VAR_KEY_NAME, pathPrefix);
		return files.parallelStream()
			.map(file -> {
				String contentType = file.getContentType();
				String originalFilename = Optional.ofNullable(file.getOriginalFilename()).filter(x -> !x.isBlank()).map(x -> FilesUtil.getFilename(x, 50)).orElse(null);
				String filename = Optional.ofNullable(originalFilename).orElse(CoreConstants.nextIdStr());

				try (InputStream in = file.getInputStream()) {
					String realPath = appPathPrefix.concat("/").concat(filename);
					final PutObjectArgs.Builder argsBuilder = PutObjectArgs.builder();
					argsBuilder.stream(in, -1, 1024 * 1024 * 100);

					argsBuilder.object(realPath)
						.contentType(contentType)
						.bucket(TEMPORARY_BUCKET_NAME);
					ObjectWriteResponse objectWriteResponse = this.adminMinioClient.putObject(argsBuilder.build());
					String version = objectWriteResponse.versionId();
					String s3Url = encodeS3Url(TEMPORARY_BUCKET_NAME, realPath, version);
					final String httpUrl = this.adminMinioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
						.bucket(TEMPORARY_BUCKET_NAME)
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
		bizId = "temporary_file:get_upload_file_sign",
		scope = "write",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public UploadSignArgs getUploadFileSign(@Valid @NotNull String appId, @Validated UploadFileSignArgs args) {
		ZonedDateTime time = ZonedDateTime.now().plus(args.getTtl());
		PostPolicy policy = new PostPolicy(TEMPORARY_BUCKET_NAME, time);
		String keyPrefix = Optional.ofNullable(args.getKeyPrefix()).orElse("file");
		String pathPrefix = APP_PATH_STRING.replace(VAR_APP_ID_NAME, appId).replace(VAR_KEY_NAME, keyPrefix);
		policy.addStartsWithCondition("key", pathPrefix);
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
			.bucket(TEMPORARY_BUCKET_NAME)
			.expiresTime(time.toLocalDateTime())
			.keyPrefix(pathPrefix)
			.signPostFormData(formData)
			.build();
	}

	@NewSpan
	@BizLog(
		bizId = "temporary_file:get_upload_file_sign_url",
		scope = "read",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "paths", value = "#paths"),
		}
	)
	public List<List<String>> getUploadFileSignUrl(String appId, @Valid @NotNull List<String> paths) {
		return paths.stream()
			.map(path -> {
				try {
					String realPath = APP_PATH_STRING.replace(VAR_APP_ID_NAME, appId).replace(VAR_KEY_NAME, path);
					String s3Url = encodeS3Url(TEMPORARY_BUCKET_NAME, realPath);
					String httpUrl = adminMinioClient.getPresignedObjectUrl(
						GetPresignedObjectUrlArgs.builder()
							.bucket(TEMPORARY_BUCKET_NAME)
							.object(realPath)
							.method(Method.PUT)
							.expiry(2, TimeUnit.HOURS)
							.build());
					return List.of(path, s3Url, httpUrl);
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
	 * 文件删除
	 *
	 * @param args 参数
	 */
	@NewSpan
	@BizLog(
		bizId = "temporary_file:delete_file",
		scope = "write",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public void deleteFile(String appId, @Validated DeleteFileArgs args) {
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
		String keyPrefix = appId + "/" + args.getKeyPrefix();
		if (removeObjectList.stream().anyMatch(x -> !x.get(BUCKET_NAME).equals(TEMPORARY_BUCKET_NAME) || !x.get(PATH_NAME).startsWith(keyPrefix))) {
			throw new ConflictBusinessException("删除失败，含有不符合的文件地址");
		}

		Iterable<Result<DeleteError>> results = adminMinioClient.removeObjects(RemoveObjectsArgs.builder()
			.bucket(TEMPORARY_BUCKET_NAME)
			.objects(removeObjectList.stream()
				.map(x -> new DeleteObject(x.get(PATH_NAME), Optional.ofNullable(x.get(VERSION_NAME)).filter(y -> !y.isEmpty()).orElse(null)))
				.collect(Collectors.toList())
			)
			.build());
		asyncFileService.deleteFile(results);
	}
}
