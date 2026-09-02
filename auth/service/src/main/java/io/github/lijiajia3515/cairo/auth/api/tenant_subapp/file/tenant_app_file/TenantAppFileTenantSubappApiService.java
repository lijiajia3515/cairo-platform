package io.github.lijiajia3515.cairo.auth.api.tenant_subapp.file.tenant_app_file;

import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.core.CoreConstants;
import io.github.lijiajia3515.cairo.core.exception.BusinessException;
import io.github.lijiajia3515.cairo.core.exception.ConflictBusinessException;
import io.github.lijiajia3515.cairo.core.tree.TreeConverter;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.file.tenant_app_file.AccessFileArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.file.tenant_app_file.DeleteFileArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.file.tenant_app_file.GetFileStatArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.file.tenant_app_file.GetFolderArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.file.tenant_app_file.ListFileArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.file.tenant_app_file.MkdirArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.file.tenant_app_file.MoveFileArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.file.tenant_app_file.UploadFileSignArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.file.CairoFileItem;
import io.github.lijiajia3515.cairo.auth.domain.dto.file.FileStat;
import io.github.lijiajia3515.cairo.auth.domain.dto.file.Folder;
import io.github.lijiajia3515.cairo.auth.domain.dto.file.FolderTools;
import io.github.lijiajia3515.cairo.auth.domain.dto.file.UploadSignArgs;
import io.github.lijiajia3515.cairo.auth.framework.minio.MinioProperties;
import io.github.lijiajia3515.cairo.auth.modules.file.AsyncFileService;
import io.github.lijiajia3515.cairo.auth.modules.file.FileBusiness;
import io.github.lijiajia3515.cairo.auth.modules.file.FileConverter;
import io.github.lijiajia3515.cairo.auth.modules.file.FileKey;
import io.github.lijiajia3515.cairo.auth.modules.file.FileTools;
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
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static io.github.lijiajia3515.cairo.auth.config.MinioConfig.ADMIN;
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
 * [tenant_subapp_user/api] tenant app subapp tenant app file file service
 */
@Service
@Slf4j
public class TenantAppFileTenantSubappApiService {
	private final MinioClient adminMinioClient;

	private final AsyncFileService asyncFileService;

	private final String endpoint;

	public final String DEFAULT_S3_URL;

	public final String DEFAULT_ACCESS_URL;

	public TenantAppFileTenantSubappApiService(MinioClient adminMinioClient, AsyncFileService asyncFileService, MinioProperties properties) {
		MinioProperties.Instance instance = properties.getConfig().get(ADMIN);
		this.endpoint = instance.getEndpoint();
		this.adminMinioClient = adminMinioClient;
		this.asyncFileService = asyncFileService;
		this.DEFAULT_S3_URL = properties.getDefaultS3Url();
		this.DEFAULT_ACCESS_URL = properties.getDefaultAccessUrl();
	}

	@NewSpan
	@BizLog(
		bizId = "tenant_app_file:list_file",
		scope = "read",
		params = {
			@BizLog.Param(key = "tenantId", value = "#tenantId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public List<CairoFileItem> listFile(@Valid @NotNull String tenantId, @Valid @NotNull String appId, @Validated ListFileArgs args) {
		String tempPrefix = APP_PATH_STRING.replace(VAR_APP_ID_NAME, appId).replace(VAR_KEY_NAME, Optional.ofNullable(args.getKeyPrefix()).orElse(""));
		if (!tempPrefix.endsWith("/")) {
			tempPrefix += "/";
		}
		String prefix = tempPrefix;
		Iterable<Result<Item>> results = adminMinioClient.listObjects(
			ListObjectsArgs.builder()
				.bucket(tenantId)
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

				Map<String,String> userMetadata;

				if (item.isDir()) {
					StatObjectArgs statObjectArgs = StatObjectArgs.builder()
						.bucket(tenantId)
						.object(item.objectName()+ "/")
						.versionId(null)
						.build();
					StatObjectResponse statObjectResponse = adminMinioClient.statObject(statObjectArgs);
					userMetadata = statObjectResponse.userMetadata();
				} else {
					userMetadata = item.userMetadata();
				}
				cairoFileItems.add(CairoFileItem.builder()
					.name(name)
					.key(item.objectName())
					.s3Url(FileTools.encodeS3Url(tenantId, item.objectName()))
					.size(item.size())
					.lastModifiedDate(item.isDir() ? null : item.lastModified().toLocalDateTime())
					.version(item.isDir() ? null : item.versionId())
					.isLatest(item.isLatest())
					.etag(item.isDir() ? null : item.etag())
					.userMetadata(userMetadata)
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
		bizId = "tenant_app_file:get_folder_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "tenantId", value = "#tenantId"),
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public List<Folder> getFolderList(@Valid @NotNull String tenantId, @Valid @NotNull String appId, @Validated GetFolderArgs args) {
		String tempPrefix = APP_PATH_STRING.replace(VAR_APP_ID_NAME, appId).replace(VAR_KEY_NAME, Optional.ofNullable(args.getKeyPrefix()).orElse(""));
		if (!tempPrefix.endsWith("/")) {
			tempPrefix += "/";
		}
		String prefix = tempPrefix;
		Iterable<Result<Item>> results = adminMinioClient.listObjects(
			ListObjectsArgs.builder()
				.bucket(tenantId)
				.prefix(prefix)
				.recursive(args.isRecursive())
				.build()
		);
		Set<CairoFileItem> cairoFileItems = new HashSet<>();
		results.forEach(itemResult -> {
			try {
				Item item = itemResult.get();
				String name = item.objectName().replace(prefix, "");
				name = name.endsWith("/") ? name.substring(0, name.length() - 1) : name;
				if (!item.objectName().equals(prefix) && item.objectName().endsWith("/")) {
					StatObjectArgs statObjectArgs = StatObjectArgs.builder()
						.bucket(tenantId)
						.object(item.objectName())
						.versionId(item.versionId())
						.build();
					StatObjectResponse statObjectResponse = adminMinioClient.statObject(statObjectArgs);

					cairoFileItems.add(CairoFileItem.builder()
						.name(name)
						.key(item.objectName().substring(0, item.objectName().length() - 1))
						.dir(true)
						.userMetadata(statObjectResponse.userMetadata())
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

		return cairoFileItems.stream().map(x -> {
			return Folder.builder()
				.id(x.getKey())
				.userMetadata(x.getUserMetadata())
				.build();
		}).sorted(Comparator.comparing(Folder::getId)).collect(Collectors.toList());
	}

	@NewSpan
	@BizLog(
		bizId = "tenant_app_file:get_folder_tree_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "tenantId", value = "#tenantId"),
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public List<Folder> getFolderTreeList(@Valid @NotNull String tenantId, @Valid @NotNull String appId, @Validated GetFolderArgs args) {
		String tempPrefix = APP_PATH_STRING.replace(VAR_APP_ID_NAME, appId).replace(VAR_KEY_NAME, Optional.ofNullable(args.getKeyPrefix()).orElse(""));
		if (!tempPrefix.endsWith("/")) {
			tempPrefix += "/";
		}
		String prefix = tempPrefix;
		Iterable<Result<Item>> results = adminMinioClient.listObjects(
			ListObjectsArgs.builder()
				.bucket(tenantId)
				.prefix(prefix)
				.recursive(args.isRecursive())
				.build()
		);


		List<Folder> folders = new ArrayList<>();
		results.forEach(itemResult -> {
			try {
				Item item = itemResult.get();
				if (!item.objectName().equals(prefix) && item.objectName().endsWith("/")) {
					StatObjectArgs statObjectArgs = StatObjectArgs.builder()
						.bucket(tenantId)
						.object(item.objectName())
						.versionId(item.versionId())
						.build();
					StatObjectResponse statObjectResponse = adminMinioClient.statObject(statObjectArgs);


					String name = item.objectName().replace(prefix, "");
					name = name.endsWith("/") ? name.substring(0, name.length() - 1) : name;

					String parentId = "";
					if (name.split("/").length > 1) {
						int parentIdIndex = name.lastIndexOf("/");
						parentId = name.substring(0, parentIdIndex);
					}

					folders.add(Folder.builder()
						.parentId(parentId)
						.id(name)
						.userMetadata(statObjectResponse.userMetadata())
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

		folders.sort(Comparator.comparing(Folder::getId));

		return TreeConverter.build(folders, "", Folder.COMPARATOR);
	}

	@NewSpan
	@BizLog(
		bizId = "tenant_app_file:mkdir",
		scope = "read",
		params = {
			@BizLog.Param(key = "tenantId", value = "#tenantId"),
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public int mkdir(@Valid @NotNull String tenantId, @Valid @NotNull String appId, @Validated MkdirArgs args) {
		AtomicInteger count = new AtomicInteger();
		List<String> dirPaths = new ArrayList<>();
		if (args.getDirPath() != null && !args.getDirPath().isEmpty()) {
			dirPaths.add(APP_PATH_STRING.replace(VAR_APP_ID_NAME, appId).replace(VAR_KEY_NAME, args.getDirPath()));
		}
		if (args.getDirPaths() != null && !args.getDirPaths().isEmpty()) {
			dirPaths.addAll(args.getDirPaths().stream().map(path -> APP_PATH_STRING.replace(VAR_APP_ID_NAME, appId).replace(VAR_KEY_NAME, path)).toList());
		}
		List<String> allDirs = FolderTools.convertTempFolder(dirPaths);
		if (!allDirs.isEmpty()) {
			allDirs.forEach(path -> {
				try {
					String emptyDir = path.endsWith("/") ? path : path + "/";
					try {
						// exists empty folder
						adminMinioClient.statObject(StatObjectArgs.builder()
							.bucket(tenantId)
							.object(emptyDir)
							.build()
						);
					} catch (ErrorResponseException e) {
						// create folder: create empty file
						adminMinioClient.putObject(
							PutObjectArgs.builder()
								.bucket(tenantId)
								.object(emptyDir)
								.stream(new ByteArrayInputStream(new byte[]{}), 0, -1)
								.userMetadata(args.getUserMetadata())
								.build()
						);
						count.incrementAndGet();
					}
				} catch (ErrorResponseException | InsufficientDataException | InternalException | InvalidKeyException |
						 InvalidResponseException | IOException | NoSuchAlgorithmException | ServerException |
						 XmlParserException e) {
					log.info("文件存储服务异常", e);
				}
			});
		}
		return count.get();
	}

	@NewSpan
	@BizLog(
		bizId = "tenant_app_file:access_file",
		scope = "read",
		params = {
			@BizLog.Param(key = "tenantId", value = "#tenantId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public List<String> accessFile(@Valid @NotNull String tenantId, @Valid @NotNull String appId, @Validated AccessFileArgs args) {
		int secondExpiry = (int) Optional.ofNullable(args.getTtl()).orElse(Duration.ofHours(1)).toSeconds();
		return args.getS3Urls().stream()
			.map(s3Url -> {
				try {
					Map<String, String> source = decodeS3Meta(s3Url);
					String bucket = source.get(BUCKET_NAME);
					String path = source.get(PATH_NAME);

					if (!Objects.equals(tenantId, bucket)) {
						return DEFAULT_ACCESS_URL;
					}

					// 非法获取应用文件 返回默认值
					if (appId == null || !path.startsWith(appId + "/")) {
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
		bizId = "tenant_app_file:access_file_url",
		scope = "read",
		params = {
			@BizLog.Param(key = "tenantId", value = "#tenantId"),
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "s3Url", value = "#s3Url"),
			@BizLog.Param(key = "enableVersion", value = "#enableVersion"),
		}
	)
	public String accessFileUrl(@Valid @NotNull String tenantId, @Valid @NotNull String appId, @Valid @NotNull String s3Url, boolean enableVersion) {
		try {
			Map<String, String> map = decodeS3Meta(s3Url);
			String bucket = map.get(BUCKET_NAME);
			String path = map.get(PATH_NAME);
			if (!Objects.equals(tenantId, bucket)) {
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
				.bucket(map.get(BUCKET_NAME))
				.object(map.get(PATH_NAME))
				.versionId(version)
				.method(Method.GET)
				.expiry(2, TimeUnit.HOURS)
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
	}

	@NewSpan
	@BizLog(
		bizId = "tenant_app_file:get_file_stat",
		scope = "read",
		params = {
			@BizLog.Param(key = "tenantId", value = "#tenantId"),
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public List<FileStat> getFileStat(@Valid @NotNull String tenantId, @Valid @NotNull String appId, @Validated GetFileStatArgs args) {
		final StatObjectArgs.Builder builder = StatObjectArgs.builder();

		return args.getS3Urls().stream().map(s3Url -> {
			FileStat fileStat = null;
			try {
				Map<String, String> sourceMap = decodeS3Meta(s3Url);

				String bucket = sourceMap.get(BUCKET_NAME);
				String path = sourceMap.get(PATH_NAME);

				if (Objects.equals(tenantId, bucket) && appId != null && path.startsWith(appId + "/")) {
					final StatObjectArgs statObjectArgs = builder
						.bucket(sourceMap.get(BUCKET_NAME))
						.object(sourceMap.get(PATH_NAME))
						.versionId(Optional.ofNullable(sourceMap.get(VERSION_NAME)).filter(y -> !y.isEmpty()).orElse(null))
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
	 * 上传文件
	 *
	 * @param appId appId
	 * @param path  path
	 * @param file  file
	 * @return file list
	 */
	@NewSpan
	@BizLog(
		bizId = "tenant_app_file:upload_file",
		scope = "write",
		params = {
			@BizLog.Param(key = "tenantId", value = "#tenantId"),
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "path", value = "#path"),
			@BizLog.Param(key = "file", value = "'***'"),
			@BizLog.Param(key = "metadata", value = "#metadata"),
		}
	)
	public List<String> uploadFile(@Valid @NotNull String tenantId, @Valid @NotNull String appId, @Valid @NotNull String path, MultipartFile file, Map<String, String> metadataMap) {
		String realPath = APP_PATH_STRING.replace(VAR_APP_ID_NAME, appId).replace(VAR_KEY_NAME, path);
		try (InputStream in = file.getInputStream()) {
			final PutObjectArgs.Builder argsBuilder = PutObjectArgs.builder();
			argsBuilder.stream(in, -1, 1024 * 1024 * 100);

			argsBuilder
				.bucket(tenantId)
				.object(realPath)
				.contentType(file.getContentType())
				.userMetadata(metadataMap)
			;
			ObjectWriteResponse objectWriteResponse = this.adminMinioClient.putObject(argsBuilder.build());
			String version = objectWriteResponse.versionId();
			final String presignedObjectUrl = this.adminMinioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
				.bucket(tenantId)
				.object(realPath)
				.expiry(1, TimeUnit.DAYS)
				.method(Method.GET)
				.build());
			String dir = realPath.substring(0, realPath.lastIndexOf('/'));
			mkdir(tenantId, appId, MkdirArgs.builder().dirPath(dir).userMetadata(metadataMap).build());

			return List.of(path, encodeS3Url(tenantId, realPath, version), presignedObjectUrl);
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
		bizId = "tenant_app_file:upload_files",
		scope = "write",
		params = {
			@BizLog.Param(key = "tenantId", value = "#tenantId"),
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "prefix", value = "#prefix"),
			@BizLog.Param(key = "files", value = "'***'")
		}
	)
	public List<List<String>> uploadFiles(@Valid @NotNull String tenantId, @Valid @NotNull String appId, String prefix, List<MultipartFile> files, Map<String, String> metadataMap) {
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
						.bucket(tenantId)
						.userMetadata(metadataMap)
					;
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
					String dir = realPath.substring(0, realPath.lastIndexOf('/'));
					mkdir(tenantId, appId, MkdirArgs.builder().dirPath(dir).userMetadata(metadataMap).build());
					return List.of(filename, s3Url, httpUrl);
				} catch (IOException | ServerException | InsufficientDataException | ErrorResponseException |
						 NoSuchAlgorithmException | InvalidKeyException | InvalidResponseException |
						 XmlParserException | InternalException e) {
					throw new ConflictBusinessException("文件上传失败", e, FileBusiness.UPLOAD_FAILED);
				}
			})
			.collect(Collectors.toList());
	}

	/**
	 * 获取上传文件签名
	 *
	 * @param tenantId 企业ID
	 * @param appId    应用ID
	 * @param args     参数
	 * @return 上传文件签名
	 */
	@NewSpan
	@BizLog(
		bizId = "tenant_app_file:get_upload_file_sign",
		scope = "write",
		params = {
			@BizLog.Param(key = "tenantId", value = "#tenantId"),
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public UploadSignArgs getUploadFileSign(@Valid @NotNull String tenantId, @Valid @NotNull String appId, @Validated UploadFileSignArgs args) {
		ZonedDateTime time = ZonedDateTime.now().plus(args.getTtl());
		PostPolicy policy = new PostPolicy(tenantId, time);
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
			.bucket(tenantId)
			.expiresTime(time.toLocalDateTime())
			.keyPrefix(pathPrefix)
			.signPostFormData(formData)
			.build();
	}

	/**
	 * 获取文件签名地址
	 *
	 * @param tenantId 企业ID
	 * @param appId    应用ID
	 * @param paths    上传文件路径
	 * @return 已签名的上传文件地址
	 */
	@NewSpan
	@BizLog(
		bizId = "tenant_app_file:get_upload_file_sign_url",
		scope = "write",
		params = {
			@BizLog.Param(key = "tenantId", value = "#tenantId"),
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "paths", value = "#paths"),
		}
	)
	public List<List<String>> getUploadFileSignUrl(@Valid @NotNull String tenantId, @Valid @NotNull String appId, @Valid @NotNull List<String> paths) {
		return paths.stream()
			.map(path -> {
				try {
					String realPath = APP_PATH_STRING.replace(VAR_APP_ID_NAME, appId).replace(VAR_KEY_NAME, path);
					String httpUrl = adminMinioClient.getPresignedObjectUrl(
						GetPresignedObjectUrlArgs.builder()
							.bucket(tenantId)
							.object(realPath)
							.method(Method.PUT)
							.expiry(2, TimeUnit.HOURS)
							.build());
					String s3Url = encodeS3Url(tenantId, realPath);
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
	 * 文件上传
	 *
	 * @param tenantId tenantId appId
	 * @param appId    appId
	 * @param args     args
	 */
	@NewSpan
	@BizLog(
		bizId = "tenant_app_file:move_file",
		scope = "write",
		params = {
			@BizLog.Param(key = "tenantId", value = "#tenantId"),
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public int moveFile(String tenantId, String appId, MoveFileArgs args) {
		try {
			if (args.isDir()) {
				String sourcePath = APP_PATH_STRING.replace(VAR_APP_ID_NAME, appId).replace(VAR_KEY_NAME, args.getSourcePath());
				if (!sourcePath.endsWith("/")) sourcePath += "/";

				// 复制源文件到目标文件
				List<CairoFileItem> sourceFiles = new ArrayList<>();
				// 1. 读取源所有文件 最新内容
				ListObjectsArgs listArgs = ListObjectsArgs.builder()
					.bucket(tenantId)
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
							.bucket(tenantId)
							.source(CopySource.builder().bucket(tenantId).object(file.getKey()).build())
							.object(tempTargetPath)
							.userMetadata(file.getUserMetadata())
							.build();
						adminMinioClient.copyObject(copyArgs);
						String dir = tempTargetPath.substring(0, tempTargetPath.lastIndexOf("/"));
						mkdir(tenantId, appId, MkdirArgs.builder().dirPath(dir).build());
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
					.bucket(tenantId)
					.prefix(sourcePath)
					.includeVersions(true)
					.recursive(true)
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
					.bucket(tenantId)
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
					.bucket(tenantId)
					.source(CopySource.builder().bucket(tenantId).object(sourcePath).build())
					.object(targetPath)
					.build();
				adminMinioClient.copyObject(copyArgs);

				// 删除文件
				List<FileKey> deleteFileKeys = new ArrayList<>();
				// 1. 读取源文件
				Iterable<Result<Item>> results = adminMinioClient.listObjects(ListObjectsArgs.builder()
					.bucket(tenantId)
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
					.bucket(tenantId)
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
	 * 删除文件
	 *
	 * @param args 参数
	 * @return 1
	 */
	@NewSpan
	@BizLog(
		bizId = "tenant_app_file:delete_file",
		scope = "write",
		params = {
			@BizLog.Param(key = "tenantId", value = "#tenantId"),
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public int deleteFile(@Valid @NotNull String tenantId, @Valid @NotNull String appId, @Validated DeleteFileArgs args) {
		List<FileKey> fileKeys = new ArrayList<>();
		if (args.getFilePaths() != null && !args.getFilePaths().isEmpty()) {
			args.getFilePaths().stream().filter(x -> x != null && !x.isEmpty())
				.map(x -> APP_PATH_STRING.replace(VAR_APP_ID_NAME, appId).replace(VAR_KEY_NAME, x))
				.forEach(fileKey -> {
					Iterable<Result<Item>> results = adminMinioClient.listObjects(ListObjectsArgs.builder()
						.bucket(tenantId)
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
						.bucket(tenantId)
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
					if (!bucketName.equals(tenantId)) {
						throw new RuntimeException(String.format("桶参数错误错误 ok: %s real: %s ", tenantId, bucketName));
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
					if (!bucketName.equals(tenantId)) {
						throw new RuntimeException(String.format("桶参数错误错误 ok: %s real: %s ", tenantId, bucketName));
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
			.bucket(tenantId)
			.objects(fileKeys.stream()
				.map(x -> new DeleteObject(x.getKey(), x.getVersion()))
				.collect(Collectors.toList())
			)
			.build());
		asyncFileService.deleteFile(results);
		return fileKeys.size();
	}
}
