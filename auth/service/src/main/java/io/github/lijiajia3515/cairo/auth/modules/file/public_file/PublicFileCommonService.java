package io.github.lijiajia3515.cairo.auth.modules.file.public_file;

import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.core.exception.ConflictBusinessException;
import io.github.lijiajia3515.cairo.auth.framework.minio.MinioProperties;
import io.github.lijiajia3515.cairo.auth.modules.file.AsyncFileService;
import io.github.lijiajia3515.cairo.auth.modules.file.FileBusiness;
import io.github.lijiajia3515.cairo.auth.modules.file.UrlConverter;
import io.github.lijiajia3515.cairo.auth.modules.file.public_file.args.AccessFileArgs;
import io.github.lijiajia3515.cairo.auth.modules.file.public_file.args.DeleteFileArgs;
import io.micrometer.tracing.annotation.NewSpan;
import io.minio.*;
import io.minio.errors.*;
import io.minio.http.Method;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.github.lijiajia3515.cairo.auth.config.MinioConfig.ADMIN;
import static io.github.lijiajia3515.cairo.auth.modules.file.FileConstants.PUBLIC_BUCKET_NAME;
import static io.github.lijiajia3515.cairo.auth.modules.file.FileConverter.urlConverter;
import static io.github.lijiajia3515.cairo.auth.modules.file.FileTools.*;

/**
 * [tenant_app_user/api] file service
 */
@Service
@Slf4j
public class PublicFileCommonService {
	private final MinioClient adminMinioClient;

	private final AsyncFileService asyncFileService;

	private final String endpoint;

	public final String DEFAULT_S3_URL;

	public final String DEFAULT_ACCESS_URL;

	public PublicFileCommonService(MinioClient adminMinioClient, AsyncFileService asyncFileService, MinioProperties properties) {
		MinioProperties.Instance instance = properties.getConfig().get(ADMIN);
		this.endpoint = instance.getEndpoint();
		this.adminMinioClient = adminMinioClient;
		this.asyncFileService = asyncFileService;
		this.DEFAULT_S3_URL = properties.getDefaultS3Url();
		this.DEFAULT_ACCESS_URL = properties.getDefaultAccessUrl();
	}

	@NewSpan
	@BizLog(
		bizId = "public_file:access_file",
		scope = "read",
		params = {
			@BizLog.Param(key = "tenantId", value = "#tenantId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public List<String> accessFile(@Validated AccessFileArgs args) {
		int secondExpiry = (int) Optional.ofNullable(args.getTtl()).orElse(Duration.ofHours(1)).toSeconds();
		return args.getS3Urls().stream()
			.map(s3Url -> {
				try {
					Map<String, String> source = decodeS3Meta(s3Url);
					String bucket = source.get(BUCKET_NAME);

					if (!Objects.equals(PUBLIC_BUCKET_NAME, bucket)) {
						return DEFAULT_ACCESS_URL;
					}

					if (getS3UrlIsPublicUrl(s3Url)) {
						return encodeS3PublicUrl(s3Url, endpoint, args.isEnableVersion());
					} else {
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

					}
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

	/**
	 * 上传文件
	 *
	 * @param path path
	 * @param file file
	 * @return file list
	 */
	@NewSpan
	@BizLog(
		bizId = "public_file:upload_file",
		scope = "write",
		params = {
			@BizLog.Param(key = "path", value = "#path"),
			@BizLog.Param(key = "file", value = "'***'"),
		}
	)
	public List<String> uploadFile(@Valid @NotNull String path, MultipartFile file) {
		try (InputStream in = file.getInputStream()) {
			final PutObjectArgs.Builder argsBuilder = PutObjectArgs.builder();
			argsBuilder.stream(in, -1, 1024 * 1024 * 100);

			argsBuilder
				.bucket(PUBLIC_BUCKET_NAME)
				.object(path)
				.contentType(file.getContentType());
			ObjectWriteResponse objectWriteResponse = this.adminMinioClient.putObject(argsBuilder.build());
			String version = objectWriteResponse.versionId();

			String s3Url = encodeS3Url(PUBLIC_BUCKET_NAME, path, version);

			String publicUrl = encodeS3PublicUrl(s3Url, endpoint,true);

			return List.of(path, s3Url, publicUrl);
		} catch (IOException | ServerException | InsufficientDataException | ErrorResponseException |
				 NoSuchAlgorithmException | InvalidKeyException | InvalidResponseException | XmlParserException |
				 InternalException e) {
			throw new ConflictBusinessException("文件上传失败", e, FileBusiness.UPLOAD_FAILED);
		}
	}

	/**
	 * 文件删除
	 *
	 * @param args 参数
	 */
	@NewSpan
	@BizLog(
		bizId = "public_file:delete_file",
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

		// 校验文件合法
		if (removeObjectList.stream().anyMatch(x -> !x.get(BUCKET_NAME).equals(PUBLIC_BUCKET_NAME) || !x.get(PATH_NAME).startsWith(args.getKeyPrefix()))) {
			throw new ConflictBusinessException("删除失败，含有不符合的文件地址");
		}

		Iterable<Result<DeleteError>> results = adminMinioClient.removeObjects(RemoveObjectsArgs.builder()
			.bucket(PUBLIC_BUCKET_NAME)
			.objects(removeObjectList.stream()
				.map(x -> new DeleteObject(x.get(PATH_NAME), Optional.ofNullable(x.get(VERSION_NAME)).filter(y -> !y.isEmpty()).orElse(null)))
				.collect(Collectors.toList())
			)
			.build());
		asyncFileService.deleteFile(results);
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
			log.error("删除公开存储文件失败", e);
		}
	}
}
