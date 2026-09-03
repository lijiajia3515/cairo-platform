package io.github.lijiajia3515.cairo.auth.modules.file.temporary_file;

import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.core.CoreConstants;
import io.github.lijiajia3515.cairo.core.exception.ConflictBusinessException;
import io.github.lijiajia3515.cairo.auth.framework.minio.MinioProperties;
import io.github.lijiajia3515.cairo.auth.modules.file.FileBusiness;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.file.temporary_file.AccessFileArgs;
import io.github.lijiajia3515.cairo.auth.modules.utils.FilesUtil;
import io.micrometer.tracing.annotation.NewSpan;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.ObjectWriteResponse;
import io.minio.PutObjectArgs;
import io.minio.errors.*;
import io.minio.http.Method;
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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static io.github.lijiajia3515.cairo.auth.config.MinioConfig.ADMIN;
import static io.github.lijiajia3515.cairo.auth.modules.file.FileConstants.*;
import static io.github.lijiajia3515.cairo.auth.modules.file.FileTools.*;

/**
 * [common] temporary file service
 */
@Service
@Slf4j
public class TemporaryFileCommonService {
	private final MinioClient adminMinioClient;

	private final String endpoint;

	public final String DEFAULT_S3_URL;

	public final String DEFAULT_ACCESS_URL;

	public TemporaryFileCommonService(MinioClient adminMinioClient,
									  MinioProperties properties) {
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

	/**
	 * 上传文件
	 *
	 * @param appId appId
	 * @param files files
	 * @return file list
	 */
	@NewSpan
	@BizLog(
		bizId = "temporary_file:upload_file",
		scope = "write",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "prefix", value = "#prefix"),
			@BizLog.Param(key = "files", value = "'***'")
		}
	)
	public List<List<String>> uploadFile(@Valid @NotNull String appId, String prefix, List<MultipartFile> files) {
		String pathPrefix = Optional.ofNullable(prefix).orElse("unknown");
		String appPathPrefix = APP_PATH_STRING.replace(VAR_APP_ID_NAME, appId).replace(VAR_KEY_NAME, pathPrefix);
		AtomicInteger i = new AtomicInteger(0);
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
					String s3Path = encodeS3Url(TEMPORARY_BUCKET_NAME, realPath, version);
					final String presignedObjectUrl = this.adminMinioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
						.bucket(TEMPORARY_BUCKET_NAME)
						.object(realPath)
						.versionId(version)
						.expiry(1, TimeUnit.DAYS)
						.method(Method.GET)
						.build());
					return List.of("" + i.incrementAndGet(), s3Path, presignedObjectUrl);
				} catch (IOException | ServerException | InsufficientDataException | ErrorResponseException |
						 NoSuchAlgorithmException | InvalidKeyException | InvalidResponseException |
						 XmlParserException | InternalException e) {
					throw new ConflictBusinessException("文件上传失败", e, FileBusiness.UPLOAD_FAILED);
				}
			})
			.collect(Collectors.toList());
	}
}
