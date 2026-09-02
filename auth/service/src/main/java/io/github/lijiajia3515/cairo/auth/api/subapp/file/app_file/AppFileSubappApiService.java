package io.github.lijiajia3515.cairo.auth.api.subapp.file.app_file;

import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.core.CoreConstants;
import io.github.lijiajia3515.cairo.core.exception.BusinessException;
import io.github.lijiajia3515.cairo.core.exception.ConflictBusinessException;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.file.app_file.AccessFileArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.file.app_file.DeleteFileArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.file.app_file.GetFolderArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.file.app_file.ListFileArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.file.app_file.MoveFileArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.file.app_file.UploadFileSignArgs;
import io.github.lijiajia3515.cairo.auth.framework.minio.MinioProperties;
import io.github.lijiajia3515.cairo.auth.modules.file.AsyncFileService;
import io.github.lijiajia3515.cairo.auth.domain.dto.file.CairoFileItem;
import io.github.lijiajia3515.cairo.auth.modules.file.FileBusiness;
import io.github.lijiajia3515.cairo.auth.modules.file.FileConstants;
import io.github.lijiajia3515.cairo.auth.modules.file.FileKey;
import io.github.lijiajia3515.cairo.auth.modules.file.FileTools;
import io.github.lijiajia3515.cairo.auth.domain.dto.file.Folder;
import io.github.lijiajia3515.cairo.auth.domain.dto.file.FolderTools;
import io.github.lijiajia3515.cairo.auth.domain.dto.file.UploadSignArgs;
import io.github.lijiajia3515.cairo.auth.modules.utils.FilesUtil;
import io.micrometer.tracing.annotation.NewSpan;
import io.minio.CopyObjectArgs;
import io.minio.CopySource;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.ObjectWriteResponse;
import io.minio.PostPolicy;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectsArgs;
import io.minio.Result;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;
import io.minio.http.Method;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
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
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static io.github.lijiajia3515.cairo.auth.config.MinioConfig.ADMIN;
import static io.github.lijiajia3515.cairo.auth.modules.file.FileConstants.APP_BUCKET_NAME;
import static io.github.lijiajia3515.cairo.auth.modules.file.FileConstants.APP_PATH_STRING;
import static io.github.lijiajia3515.cairo.auth.modules.file.FileConstants.VAR_APP_ID_NAME;
import static io.github.lijiajia3515.cairo.auth.modules.file.FileConstants.VAR_KEY_NAME;
import static io.github.lijiajia3515.cairo.auth.modules.file.FileTools.BUCKET_NAME;
import static io.github.lijiajia3515.cairo.auth.modules.file.FileTools.PATH_NAME;
import static io.github.lijiajia3515.cairo.auth.modules.file.FileTools.VERSION_NAME;
import static io.github.lijiajia3515.cairo.auth.modules.file.FileTools.decodeHttpMeta;
import static io.github.lijiajia3515.cairo.auth.modules.file.FileTools.decodeS3Meta;
import static io.github.lijiajia3515.cairo.auth.modules.file.FileTools.encodeS3Url;

/**
 * [subapp_user/api] app file service
 */
@Service
@Slf4j
public class AppFileSubappApiService {
	private final MinioClient adminMinioClient;

	private final AsyncFileService asyncFileService;

	private final String endpoint;

	public final String DEFAULT_S3_URL;

	public final String DEFAULT_ACCESS_URL;

	public AppFileSubappApiService(MinioClient adminMinioClient, AsyncFileService asyncFileService, MinioProperties properties) {
		MinioProperties.Instance instance = properties.getConfig().get(ADMIN);
		this.endpoint = instance.getEndpoint();
		this.adminMinioClient = adminMinioClient;
		this.asyncFileService = asyncFileService;
		this.DEFAULT_S3_URL = properties.getDefaultS3Url();
		this.DEFAULT_ACCESS_URL = properties.getDefaultAccessUrl();
	}

	@NewSpan
	@BizLog(
		bizId = "tenant_endpoint_file:list_file",
		scope = "read",
		params = {
			@BizLog.Param(key = "tenantId", value = "#tenantId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public List<CairoFileItem> listFile(@Valid @NotNull String appId, @Validated ListFileArgs args) {
		String tempPrefix = APP_PATH_STRING.replace(VAR_APP_ID_NAME, appId).replace(VAR_KEY_NAME, Optional.ofNullable(args.getKeyPrefix()).orElse(""));
		if (!tempPrefix.endsWith("/")) {
			tempPrefix += "/";
		}
		String prefix = tempPrefix;
		Iterable<Result<Item>> results = adminMinioClient.listObjects(
			ListObjectsArgs.builder()
				.bucket(APP_BUCKET_NAME)
				.prefix(prefix)
				.recursive(args.isRecursive())
				.includeUserMetadata(true)
				.build()
		);
		List<CairoFileItem> cairoFileItems = new ArrayList<>();
		results.forEach(itemResult -> {
			try {
				Item item = itemResult.get();
				String name = item.objectName().replace(prefix, "");
				name = name.endsWith("/") ? name.substring(0, name.length() - 1) : name;
				if (name.isEmpty()) return;
				cairoFileItems.add(CairoFileItem.builder()
					.name(name)
					.key(item.objectName())
					.s3Url(FileTools.encodeS3Url(APP_BUCKET_NAME, item.objectName()))
					.size(item.size())
					.lastModifiedDate(item.isDir() ? null : item.lastModified().toLocalDateTime())
					.version(item.isDir() ? null : item.versionId())
					.isLatest(item.isLatest())
					.etag(item.isDir() ? null : item.etag())
					.userMetadata(item.userMetadata())
					.dir(item.isDir())
					.build()
				);

			} catch (ErrorResponseException | InsufficientDataException | InternalException |
					 InvalidKeyException |
					 InvalidResponseException | IOException | NoSuchAlgorithmException | XmlParserException |
					 ServerException e) {
				log.info("文件存储服务异常", e);
			}
		});
		return cairoFileItems;
	}

	@NewSpan
	@BizLog(
		bizId = "tenant_endpoint_file:get_folder_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "tenantId", value = "#tenantId"),
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public List<String> getFolderList(@Valid @NotNull String appId, @Validated GetFolderArgs args) {
		String tempPrefix = APP_PATH_STRING.replace(VAR_APP_ID_NAME, appId).replace(VAR_KEY_NAME, Optional.ofNullable(args.getKeyPrefix()).orElse(""));
		if (!tempPrefix.endsWith("/")) {
			tempPrefix += "/";
		}
		String prefix = tempPrefix;
		Iterable<Result<Item>> results = adminMinioClient.listObjects(
			ListObjectsArgs.builder()
				.bucket(APP_BUCKET_NAME)
				.prefix(prefix)
				.recursive(args.isRecursive())
				.build()
		);


		Set<CairoFileItem> cairoFileItems = new HashSet<>();
		if (args.isRecursive()) {
			results.forEach(itemResult -> {
				try {

					Item item = itemResult.get();
					String folderName = item.objectName().replace(prefix, "");
					if (!folderName.contains("/")) return;
					folderName = folderName.substring(0, folderName.lastIndexOf("/"));

					String fullName = item.objectName().endsWith("/") ? item.objectName().substring(0, item.objectName().length() - 1) : item.objectName();
					fullName = fullName.substring(0, fullName.lastIndexOf("/"));

					cairoFileItems.add(CairoFileItem.builder()
						.name(folderName)
						.key(fullName)
						.dir(true)
						.build()
					);
				} catch (ErrorResponseException | InsufficientDataException | InternalException |
						 InvalidKeyException |
						 InvalidResponseException | IOException | NoSuchAlgorithmException | XmlParserException |
						 ServerException e) {
					log.info("文件存储服务异常", e);
				}
			});
		} else {
			results.forEach(itemResult -> {
				try {
					Item item = itemResult.get();
					String name = item.objectName().replace(prefix, "");
					name = name.endsWith("/") ? name.substring(0, name.length() - 1) : name;
					if (item.isDir()) {
						cairoFileItems.add(CairoFileItem.builder()
							.name(name)
							.key(item.objectName().substring(0, item.objectName().length() - 1))
							.dir(true)
							.build()
						);
					}
				} catch (ErrorResponseException | InsufficientDataException | InternalException |
						 InvalidKeyException |
						 InvalidResponseException | IOException | NoSuchAlgorithmException | XmlParserException |
						 ServerException e) {
					log.info("文件存储服务异常", e);
				}
			});
		}
		List<String> folders = cairoFileItems.stream().map(CairoFileItem::getName).collect(Collectors.toList());
		List<String> realFolder = FolderTools.convertTempFolder(folders);
		return realFolder.stream().map(x -> prefix + x).collect(Collectors.toList());
	}

	@NewSpan
	@BizLog(
		bizId = "tenant_endpoint_file:get_folder_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "tenantId", value = "#tenantId"),
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public List<Folder> getFolderTreeList(@Valid @NotNull String appId, @Validated GetFolderArgs args) {
		String tempPrefix = APP_PATH_STRING.replace(VAR_APP_ID_NAME, appId).replace(VAR_KEY_NAME, Optional.ofNullable(args.getKeyPrefix()).orElse(""));
		if (!tempPrefix.endsWith("/")) {
			tempPrefix += "/";
		}
		String prefix = tempPrefix;
		Iterable<Result<Item>> results = adminMinioClient.listObjects(
			ListObjectsArgs.builder()
				.bucket(APP_BUCKET_NAME)
				.prefix(prefix)
				.recursive(args.isRecursive())
				.build()
		);


		Set<CairoFileItem> cairoFileItems = new HashSet<>();
		if (args.isRecursive()) {
			results.forEach(itemResult -> {
				try {
					Item item = itemResult.get();
					String folderName = item.objectName().replace(prefix, "");
					if (!folderName.contains("/")) return;
					folderName = folderName.substring(0, folderName.lastIndexOf("/"));

					String fullName = item.objectName().endsWith("/") ? item.objectName().substring(0, item.objectName().length() - 1) : item.objectName();
					fullName = fullName.substring(0, fullName.lastIndexOf("/"));

					cairoFileItems.add(CairoFileItem.builder()
						.name(folderName)
						.key(fullName)
						.dir(true)
						.build()
					);
				} catch (ErrorResponseException | InsufficientDataException | InternalException |
						 InvalidKeyException |
						 InvalidResponseException | IOException | NoSuchAlgorithmException | XmlParserException |
						 ServerException e) {
					log.info("文件存储服务异常", e);
				}
			});
		} else {
			results.forEach(itemResult -> {
				try {
					Item item = itemResult.get();
					String name = item.objectName().replace(prefix, "");
					name = name.endsWith("/") ? name.substring(0, name.length() - 1) : name;
					if (item.isDir()) {
						cairoFileItems.add(CairoFileItem.builder()
							.name(name)
							.key(item.objectName().substring(0, item.objectName().length() - 1))
							.dir(true)
							.build()
						);
					}
				} catch (ErrorResponseException | InsufficientDataException | InternalException |
						 InvalidKeyException |
						 InvalidResponseException | IOException | NoSuchAlgorithmException | XmlParserException |
						 ServerException e) {
					log.info("文件存储服务异常", e);
				}
			});
		}
		List<String> folders = cairoFileItems.stream().map(CairoFileItem::getName).collect(Collectors.toList());
		return FolderTools.convertTempFolderTree(folders, args.getKeyPrefix());
	}

	@NewSpan
	@BizLog(
		bizId = "app_file:get_access_file",
		scope = "read",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public List<String> accessFile(String appId, @Validated AccessFileArgs args) {
		int secondExpiry = (int) Optional.ofNullable(args.getTtl()).orElse(Duration.ofHours(1)).toSeconds();
		return args.getS3Urls().stream()
			.map(s3Url -> {
				try {
					Map<String, String> source = decodeS3Meta(s3Url);
					String bucket = source.get(BUCKET_NAME);
					String path = source.get(PATH_NAME);

					if (!Objects.equals(APP_BUCKET_NAME, bucket)) {
						return DEFAULT_ACCESS_URL;
					}

					// 非法获取应用文件 返回默认值
					if (appId == null || !path.startsWith(appId + "/")) {
						return DEFAULT_ACCESS_URL;
					}

					String version = null;
					if (args.isEnableVersion() && source.get(VERSION_NAME) != null) {
						version = Optional.ofNullable(source.get(VERSION_NAME)).filter(y -> !y.isEmpty()).orElse(null);
					}
					return adminMinioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
						.bucket(APP_BUCKET_NAME)
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
		bizId = "app_file:access_file_url",
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
			String bucket = map.get(BUCKET_NAME);
			String path = map.get(PATH_NAME);

			if (!Objects.equals(APP_BUCKET_NAME, bucket)) {
				return DEFAULT_ACCESS_URL;
			}

			// 非法获取应用文件 返回默认值
			if (appId == null || !path.startsWith(appId + "/")) {
				return DEFAULT_ACCESS_URL;
			}

			String version = null;
			if (enableVersion && map.get(VERSION_NAME) != null) {
				version = Optional.ofNullable(map.get(VERSION_NAME)).filter(x -> !x.isEmpty()).orElse(null);
			}

			return adminMinioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
				.bucket(APP_BUCKET_NAME)
				.object(map.get(PATH_NAME))
				.versionId(version)
				.method(Method.GET)
				.expiry(2, TimeUnit.HOURS)
				.build());
		} catch (IllegalArgumentException e) {
			log.debug("s3Url is fail : {} error:{}", s3Url, e.getMessage());
			return DEFAULT_ACCESS_URL;
		}
	}


	/**
	 * 文件上传
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
	public List<String> uploadFile(@Valid @NotNull String appId, @Valid @NotNull String path, MultipartFile file) {
		String realPath = APP_PATH_STRING.replace(VAR_APP_ID_NAME, appId).replace(VAR_KEY_NAME, path);
		try (InputStream in = file.getInputStream()) {
			final PutObjectArgs.Builder argsBuilder = PutObjectArgs.builder();
			argsBuilder.stream(in, -1, 1024 * 1024 * 100);

			argsBuilder
				.bucket(APP_BUCKET_NAME)
				.object(realPath)
				.contentType(file.getContentType());
			ObjectWriteResponse objectWriteResponse = this.adminMinioClient.putObject(argsBuilder.build());
			String version = objectWriteResponse.versionId();

			String s3Url = encodeS3Url(APP_BUCKET_NAME, realPath, version);

			final String httpUrl = this.adminMinioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
				.bucket(APP_BUCKET_NAME)
				.object(realPath)
				.versionId(version)
				.expiry(1, TimeUnit.DAYS)
				.method(Method.GET)
				.build());

			return List.of(path, s3Url, httpUrl);
		} catch (IOException | ServerException | InsufficientDataException | ErrorResponseException |
				 NoSuchAlgorithmException | InvalidKeyException | InvalidResponseException | XmlParserException |
				 InternalException e) {
			throw new ConflictBusinessException("文件上传失败", e, FileBusiness.UPLOAD_FAILED);
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
		bizId = "app_file:upload_files",
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
				String filename = Optional.ofNullable(originalFilename).orElse(CoreConstants.SNOWFLAKE.nextIdStr());

				try (InputStream in = file.getInputStream()) {
					String realPath = appPathPrefix.concat("/").concat(filename);
					final PutObjectArgs.Builder argsBuilder = PutObjectArgs.builder();
					argsBuilder.stream(in, -1, 1024 * 1024 * 100);

					argsBuilder.object(realPath)
						.contentType(contentType)
						.bucket(APP_BUCKET_NAME);
					ObjectWriteResponse objectWriteResponse = this.adminMinioClient.putObject(argsBuilder.build());
					String version = objectWriteResponse.versionId();
					String s3Url = encodeS3Url(APP_BUCKET_NAME, realPath, version);
					final String httpUrl = this.adminMinioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
						.bucket(APP_BUCKET_NAME)
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
		bizId = "app_file:get_upload_file_sign",
		scope = "write",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public UploadSignArgs getUploadFileSign(@Valid @NotNull String appId, @Validated UploadFileSignArgs args) {
		ZonedDateTime time = ZonedDateTime.now().plus(args.getTtl());
		PostPolicy policy = new PostPolicy(APP_BUCKET_NAME, time);
		String keyPrefix = Optional.ofNullable(args.getKeyPrefix()).orElse("unknown/");
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
			.bucket(APP_BUCKET_NAME)
			.expiresTime(time.toLocalDateTime())
			.keyPrefix(pathPrefix)
			.signPostFormData(formData)
			.build();
	}


	@NewSpan
	@BizLog(
		bizId = "app_file:get_upload_file_sign_url",
		scope = "read",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "paths", value = "#paths"),
		}
	)
	public List<List<String>> getUploadFileSignUrl(@Valid @NotNull String appId, @Valid @NotNull List<String> paths) {
		return paths.stream()
			.map(appPath -> {
				try {
					String path = APP_PATH_STRING.replace(VAR_APP_ID_NAME, appId).replace(VAR_KEY_NAME, appPath);
					String s3Url = encodeS3Url(FileConstants.APP_BUCKET_NAME, path);
					String httpUrl = adminMinioClient.getPresignedObjectUrl(
						GetPresignedObjectUrlArgs.builder()
							.bucket(FileConstants.APP_BUCKET_NAME)
							.object(path)
							.method(Method.PUT)
							.expiry(2, TimeUnit.HOURS)
							.build());

					return List.of(appPath, s3Url, httpUrl);
				} catch (IllegalArgumentException | ErrorResponseException | InsufficientDataException |
						 InternalException | InvalidKeyException |
						 InvalidResponseException | IOException | NoSuchAlgorithmException | XmlParserException |
						 ServerException e) {
					log.info("获取文件上传url失败", e);
					return List.of(appPath, DEFAULT_S3_URL, DEFAULT_ACCESS_URL);
				}
			}).collect(Collectors.toList());
	}

	/**
	 * 文件上传
	 *
	 * @param appId appId
	 * @param args  args
	 */
	@NewSpan
	@BizLog(
		bizId = "tenant_app_file:move_file",
		scope = "write",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public int moveFile(String appId, MoveFileArgs args) {
		try {
			if (args.isDir()) {
				String sourcePath = APP_PATH_STRING.replace(VAR_APP_ID_NAME, appId).replace(VAR_KEY_NAME, args.getSourcePath());
				if (!sourcePath.endsWith("/")) sourcePath += "/";

				// 复制源文件到目标文件
				List<CairoFileItem> sourceFiles = new ArrayList<>();
				// 1. 读取源所有文件 最新内容
				ListObjectsArgs listArgs = ListObjectsArgs.builder()
					.bucket(APP_BUCKET_NAME)
					.prefix(sourcePath)
					.recursive(true)
					.includeVersions(true)
					.build();
				Iterable<Result<Item>> itemResults = adminMinioClient.listObjects(listArgs);
				// 2. 读取源文件项
				itemResults.forEach(itemResult -> {
					try {
						Item item = itemResult.get();
						if (!item.isDir()) {
							sourceFiles.add(CairoFileItem.builder()
								.key(item.objectName())
								.isLatest(item.isLatest())
								.userMetadata(item.userMetadata())
								.dir(item.isDir())
								.build()
							);
						}
					} catch (ErrorResponseException | InsufficientDataException | InternalException |
							 InvalidKeyException |
							 InvalidResponseException | IOException | NoSuchAlgorithmException | XmlParserException |
							 ServerException e) {
						log.info("文件存储服务异常", e);
					}
				});
				// 3. 复制文件，包含user-metadata信息
				sourceFiles.stream().filter(CairoFileItem::isLatest).forEach(file -> {
					try {
						String tempTargetPath = file.getKey().replace(args.getSourcePath(), args.getTargetPath());
						CopyObjectArgs copyArgs = CopyObjectArgs.builder()
							.bucket(APP_BUCKET_NAME)
							.source(CopySource.builder().bucket(APP_BUCKET_NAME).object(file.getKey()).build())
							.object(tempTargetPath)
							.userMetadata(file.getUserMetadata())
							.build();
						adminMinioClient.copyObject(copyArgs);
					} catch (ErrorResponseException | InsufficientDataException | InternalException |
							 InvalidKeyException |
							 InvalidResponseException | IOException | NoSuchAlgorithmException | XmlParserException |
							 ServerException e) {
						log.info("文件存储服务异常", e);
					}
				});


				// 删除源文件
				List<FileKey> deleteFileKeys = new ArrayList<>();
				// 1. 读取源文件
				Iterable<Result<Item>> results = adminMinioClient.listObjects(ListObjectsArgs.builder()
					.bucket(APP_BUCKET_NAME)
					.prefix(sourcePath)
					.recursive(true)
					.includeVersions(true)
					.build()
				);
				// 2. 读取所有版本
				results.forEach(itemResult -> {
					try {
						Item item = itemResult.get();
						deleteFileKeys.add(new FileKey(item.objectName(), item.versionId()));
					} catch (ErrorResponseException | InsufficientDataException | InternalException |
							 InvalidKeyException | InvalidResponseException | IOException | NoSuchAlgorithmException |
							 ServerException | XmlParserException e) {
						log.info("文件存储服务异常", e);
					}
				});
				// 3. 删除
				RemoveObjectsArgs removeListArgs = RemoveObjectsArgs.builder()
					.bucket(APP_BUCKET_NAME)
					.objects(deleteFileKeys.stream().map(x -> new DeleteObject(x.getKey(), x.getVersion())).collect(Collectors.toList()))
					.build();
				Iterable<Result<DeleteError>> deleteResult = adminMinioClient.removeObjects(removeListArgs);

				// 4. 回执
				asyncFileService.deleteFile(deleteResult);
				return sourceFiles.size();
			} else {
				String sourcePath = APP_PATH_STRING.replace(VAR_APP_ID_NAME, appId).replace(VAR_KEY_NAME, args.getSourcePath());
				String targetPath = APP_PATH_STRING.replace(VAR_APP_ID_NAME, appId).replace(VAR_KEY_NAME, args.getTargetPath());

				// 复制文件 最新版本文件
				CopyObjectArgs copyArgs = CopyObjectArgs.builder()
					.bucket(APP_BUCKET_NAME)
					.source(CopySource.builder().bucket(APP_BUCKET_NAME).object(sourcePath).build())
					.object(targetPath)
					.build();
				adminMinioClient.copyObject(copyArgs);

				// 删除文件
				List<FileKey> deleteFileKeys = new ArrayList<>();
				// 1. 读取源文件
				Iterable<Result<Item>> results = adminMinioClient.listObjects(ListObjectsArgs.builder()
					.bucket(APP_BUCKET_NAME)
					.prefix(sourcePath)
					.includeVersions(true)
					.build()
				);
				// 2. 读取所有版本
				results.forEach(itemResult -> {
					try {
						Item item = itemResult.get();
						deleteFileKeys.add(new FileKey(item.objectName(), item.versionId()));
					} catch (ErrorResponseException | InsufficientDataException | InternalException |
							 InvalidKeyException | InvalidResponseException | IOException | NoSuchAlgorithmException |
							 ServerException | XmlParserException e) {
						log.info("文件存储服务异常", e);
					}
				});
				// 3. 删除
				RemoveObjectsArgs removeListArgs = RemoveObjectsArgs.builder()
					.bucket(APP_BUCKET_NAME)
					.objects(deleteFileKeys.stream().map(x -> new DeleteObject(x.getKey(), x.getVersion())).collect(Collectors.toList()))
					.build();
				Iterable<Result<DeleteError>> deleteResult = adminMinioClient.removeObjects(removeListArgs);

				// 4. 回执
				asyncFileService.deleteFile(deleteResult);
				return 1;
			}
		} catch (IOException | ServerException | InsufficientDataException | ErrorResponseException |
				 NoSuchAlgorithmException | InvalidKeyException | InvalidResponseException | XmlParserException |
				 InternalException e) {
			throw new ConflictBusinessException("移动文件失败", e, FileBusiness.UPLOAD_FAILED);
		}

	}


	/**
	 * app文件删除
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
	public int deleteFile(@Valid @NotNull String appId, @Validated DeleteFileArgs args) {
		List<FileKey> fileKeys = new ArrayList<>();
		if (args.getFilePaths() != null && !args.getFilePaths().isEmpty()) {
			args.getFilePaths().stream().filter(x -> x != null && !x.isEmpty())
				.map(x -> APP_PATH_STRING.replace(VAR_APP_ID_NAME, appId).replace(VAR_KEY_NAME, x))
				.forEach(fileKey -> {
					Iterable<Result<Item>> results = adminMinioClient.listObjects(ListObjectsArgs.builder()
						.bucket(APP_BUCKET_NAME)
						.prefix(fileKey)
						.includeVersions(true)
						.build()
					);
					results.forEach(itemResult -> {
						try {
							Item item = itemResult.get();
							fileKeys.add(new FileKey(item.objectName(), item.versionId()));
						} catch (ErrorResponseException | InsufficientDataException | InternalException |
								 InvalidKeyException | InvalidResponseException | IOException |
								 NoSuchAlgorithmException |
								 ServerException | XmlParserException e) {
							log.info("文件存储服务异常", e);
						}
					});
				});
		}

		if (args.getFolderPaths() != null && !args.getFolderPaths().isEmpty()) {
			args.getFolderPaths().stream().filter(x -> x != null && !x.isEmpty())
				.map(x -> APP_PATH_STRING.replace(VAR_APP_ID_NAME, appId).replace(VAR_KEY_NAME, x))
				.forEach(fileKey -> {
					Iterable<Result<Item>> results = adminMinioClient.listObjects(ListObjectsArgs.builder()
						.bucket(APP_BUCKET_NAME)
						.prefix(fileKey)
						.recursive(true)
						.includeVersions(true)
						.build()
					);
					results.forEach(itemResult -> {
						try {
							Item item = itemResult.get();
							fileKeys.add(new FileKey(item.objectName(), item.versionId()));
						} catch (ErrorResponseException | InsufficientDataException | InternalException |
								 InvalidKeyException | InvalidResponseException | IOException |
								 NoSuchAlgorithmException |
								 ServerException | XmlParserException e) {
							log.info("文件存储服务异常", e);
						}
					});
				});
		}


		if (args.getHttpUrls() != null && !args.getHttpUrls().isEmpty()) {
			args.getHttpUrls().forEach(x -> {
				try {
					Map<String, String> s3Map = decodeHttpMeta(x);
					String bucketName = s3Map.get(BUCKET_NAME);
					if (!bucketName.equals(APP_BUCKET_NAME)) {
						throw new RuntimeException(String.format("桶参数错误错误 ok: %s real: %s ", APP_BUCKET_NAME, bucketName));
					}
					String pathName = s3Map.get(PATH_NAME);
					String versionName = Optional.ofNullable(s3Map.get(VERSION_NAME)).filter(y -> !y.isEmpty()).orElse(null);
					fileKeys.add(new FileKey(pathName, versionName));

				} catch (Exception e) {
					log.debug("文件格式错误 httpUrl: {}, Message: {}", x, e.getMessage());
				}
			});
		}

		if (args.getS3Urls() != null && !args.getS3Urls().isEmpty()) {
			args.getS3Urls().forEach(x -> {
				try {
					Map<String, String> s3Map = decodeS3Meta(x);
					String bucketName = s3Map.get(BUCKET_NAME);
					if (!bucketName.equals(APP_BUCKET_NAME)) {
						throw new RuntimeException(String.format("桶参数错误错误 ok: %s real: %s ", APP_BUCKET_NAME, bucketName));
					}
					String pathName = s3Map.get(PATH_NAME);
					String versionName = Optional.ofNullable(s3Map.get(VERSION_NAME)).filter(y -> !y.isEmpty()).orElse(null);
					fileKeys.add(new FileKey(pathName, versionName));

				} catch (Exception e) {
					log.debug("文件格式错误 s3Url: {}, Message: {}", x, e.getMessage());
				}
			});
		}

		if (fileKeys.isEmpty()) {
			throw new ConflictBusinessException("没有符合的删除文件");
		}

		Iterable<Result<DeleteError>> results = adminMinioClient.removeObjects(RemoveObjectsArgs.builder()
			.bucket(APP_BUCKET_NAME)
			.objects(fileKeys.stream()
				.map(x -> new DeleteObject(x.getKey(), x.getVersion()))
				.collect(Collectors.toList())
			)
			.build());
		asyncFileService.deleteFile(results);
		return fileKeys.size();
	}
}
