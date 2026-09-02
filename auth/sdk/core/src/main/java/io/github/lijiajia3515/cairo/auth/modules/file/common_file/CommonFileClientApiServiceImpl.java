package io.github.lijiajia3515.cairo.auth.modules.file.common_file;

import io.github.lijiajia3515.cairo.core.exception.ConflictBusinessException;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import io.github.lijiajia3515.cairo.auth.framework.sdk.CairoOAuthClientSdkService;
import io.github.lijiajia3515.cairo.auth.framework.sdk.sign.SignResp;
import io.github.lijiajia3515.cairo.auth.framework.sdk.sign.SignSdkTools;
import io.github.lijiajia3515.cairo.auth.domain.dto.file.FileStat;
import io.github.lijiajia3515.cairo.auth.domain.api.client.file.common_file.AccessFileArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.file.common_file.CopyFileArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.file.common_file.DeleteFileArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.file.common_file.GetFileStatArgs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
public class CommonFileClientApiServiceImpl implements CommonFileClientApiService {

	private final CommonFileClientApiFeignClient commonFileClientApiFeignClient;
	private final CairoOAuthClientSdkService cairoOAuthClientSdkService;

	public CommonFileClientApiServiceImpl(CommonFileClientApiFeignClient commonFileClientApiFeignClient, CairoOAuthClientSdkService cairoOAuthClientSdkService) {
		this.commonFileClientApiFeignClient = commonFileClientApiFeignClient;
		this.cairoOAuthClientSdkService = cairoOAuthClientSdkService;
	}

	@Override
	public List<String> accessFile(AccessFileArgs args) {
		try {
			ResponseEntity<BusinessResult<List<String>>> businessResultResponseEntity = commonFileClientApiFeignClient.accessFile(cairoOAuthClientSdkService.getHeaderAuthorization(),args);
			return Optional.ofNullable(businessResultResponseEntity.getBody()).map(BusinessResult::getData).orElse(Collections.emptyList());
		} catch (Exception e) {
			log.info("accessFile：", e);
			throw new ConflictBusinessException("访问文件失败");
		}
	}

	@Override
	public List<FileStat> getFileStat(GetFileStatArgs args) {
		try {
			ResponseEntity<BusinessResult<List<FileStat>>> businessResultResponseEntity = commonFileClientApiFeignClient.getFileStat(cairoOAuthClientSdkService.getHeaderAuthorization(),args);
			return Optional.ofNullable(businessResultResponseEntity.getBody()).map(BusinessResult::getData).orElse(Collections.emptyList());
		} catch (Exception e) {
			log.info("getFileStat：", e);
			throw new ConflictBusinessException("获取文件状态失败");
		}
	}

	@Override
	public List<String> uploadFile(String bucket, String path, MultipartFile file) {
		try {
			SignResp sign = SignSdkTools.sign();
			ResponseEntity<BusinessResult<List<String>>> businessResultResponseEntity = commonFileClientApiFeignClient.uploadFile(cairoOAuthClientSdkService.getHeaderAuthorization(),bucket, path, file);
			return Optional.ofNullable(businessResultResponseEntity.getBody()).map(BusinessResult::getData).orElse(Collections.emptyList());
		} catch (Exception e) {
			log.info("uploadFile：", e);
			throw new ConflictBusinessException("上传文件失败");
		}
	}

	@Override
	public String copyFile(CopyFileArgs args) {
		try {
			ResponseEntity<BusinessResult<String>> businessResultResponseEntity = commonFileClientApiFeignClient.copyFile(cairoOAuthClientSdkService.getHeaderAuthorization(),args);
			return Optional.ofNullable(businessResultResponseEntity.getBody()).map(BusinessResult::getData).orElse(null);
		} catch (Exception e) {
			log.info("copyFile：", e);
			throw new ConflictBusinessException("复制文件失败");
		}
	}

	@Override
	public List<FileStat> deleteFile(DeleteFileArgs args) {
		try {
			ResponseEntity<BusinessResult<List<FileStat>>> businessResultResponseEntity = commonFileClientApiFeignClient.deleteFile(cairoOAuthClientSdkService.getHeaderAuthorization(),args);
			return Optional.ofNullable(businessResultResponseEntity.getBody()).map(BusinessResult::getData).orElse(Collections.emptyList());
		} catch (Exception e) {
			log.info("deleteFile：", e);
			throw new ConflictBusinessException("删除文件失败");
		}
	}
}
