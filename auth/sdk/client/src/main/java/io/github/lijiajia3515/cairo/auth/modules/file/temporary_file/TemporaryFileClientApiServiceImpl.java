package io.github.lijiajia3515.cairo.auth.modules.file.temporary_file;

import io.github.lijiajia3515.cairo.core.exception.ConflictBusinessException;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import io.github.lijiajia3515.cairo.auth.framework.sdk.CairoOAuthClientSdkService;
import io.github.lijiajia3515.cairo.auth.domain.dto.file.FileStat;
import io.github.lijiajia3515.cairo.auth.domain.api.client.file.temporary_file.AccessFileArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.file.temporary_file.DeleteFileArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.file.temporary_file.GetFileStatArgs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
public class TemporaryFileClientApiServiceImpl implements TemporaryFileClientApiService {

	private final TemporaryFileClientApiFeignClient temporaryFileClientApiFeignClient;
	private final CairoOAuthClientSdkService cairoOAuthClientSdkService;

	public TemporaryFileClientApiServiceImpl(TemporaryFileClientApiFeignClient temporaryFileClientApiFeignClient, CairoOAuthClientSdkService cairoOAuthClientSdkService) {
		this.temporaryFileClientApiFeignClient = temporaryFileClientApiFeignClient;
		this.cairoOAuthClientSdkService = cairoOAuthClientSdkService;
	}


	@Override
	public List<String> accessFile(AccessFileArgs args) {
		try {
			ResponseEntity<BusinessResult<List<String>>> businessResultResponseEntity = temporaryFileClientApiFeignClient.accessFile(cairoOAuthClientSdkService.getHeaderAuthorization(),args);
			return Optional.ofNullable(businessResultResponseEntity.getBody()).map(BusinessResult::getData).orElse(Collections.emptyList());
		} catch (Exception e) {
			log.info("accessFile：", e);
			throw new ConflictBusinessException("访问文件失败");
		}
	}

	@Override
	public List<FileStat> getFileStat(GetFileStatArgs args) {
		try {
			ResponseEntity<BusinessResult<List<FileStat>>> businessResultResponseEntity = temporaryFileClientApiFeignClient.getFileStat(cairoOAuthClientSdkService.getHeaderAuthorization(),args);
			return Optional.ofNullable(businessResultResponseEntity.getBody()).map(BusinessResult::getData).orElse(Collections.emptyList());
		} catch (Exception e) {
			log.info("getFileStat：", e);
			throw new ConflictBusinessException("获取文件状态失败");
		}
	}

	@Override
	public List<String> uploadFile(String bucket, MultipartFile file) {
		try {
			ResponseEntity<BusinessResult<List<String>>> businessResultResponseEntity = temporaryFileClientApiFeignClient.uploadFile(cairoOAuthClientSdkService.getHeaderAuthorization(),bucket, file);
			return Optional.ofNullable(businessResultResponseEntity.getBody()).map(BusinessResult::getData).orElse(Collections.emptyList());
		} catch (Exception e) {
			log.info("uploadFile：", e);
			throw new ConflictBusinessException("上传文件失败");
		}
	}

	@Override
	public List<List<String>> uploadFiles(String prefix, List<MultipartFile> files) {
		try {
			ResponseEntity<BusinessResult<List<List<String>>>> businessResultResponseEntity = temporaryFileClientApiFeignClient.uploadFiles(cairoOAuthClientSdkService.getHeaderAuthorization(),prefix, files);
			return Optional.ofNullable(businessResultResponseEntity.getBody()).map(BusinessResult::getData).orElse(Collections.emptyList());
		} catch (Exception e) {
			log.info("uploadFiles：", e);
			throw new ConflictBusinessException("上传多个文件失败");
		}
	}

	@Override
	public List<List<String>> getUploadFileSignUrl(String prefix, Integer size) {
		try {
			ResponseEntity<BusinessResult<List<List<String>>>> businessResultResponseEntity = temporaryFileClientApiFeignClient.getUploadFileSignUrl(cairoOAuthClientSdkService.getHeaderAuthorization(),prefix, size);
			return Optional.ofNullable(businessResultResponseEntity.getBody()).map(BusinessResult::getData).orElse(Collections.emptyList());
		} catch (Exception e) {
			log.info("getUploadFileSignUrl：", e);
			throw new ConflictBusinessException("获取上传文件签名url失败");
		}
	}

	@Override
	public List<FileStat> deleteFile(DeleteFileArgs args) {
		try {
			ResponseEntity<BusinessResult<List<FileStat>>> businessResultResponseEntity = temporaryFileClientApiFeignClient.deleteFile(cairoOAuthClientSdkService.getHeaderAuthorization(),args);
			return Optional.ofNullable(businessResultResponseEntity.getBody()).map(BusinessResult::getData).orElse(Collections.emptyList());
		} catch (Exception e) {
			log.info("deleteFile：", e);
			throw new ConflictBusinessException("删除文件失败");
		}
	}
}
