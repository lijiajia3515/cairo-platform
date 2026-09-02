package io.github.lijiajia3515.cairo.auth.modules.file.app_file;

import io.github.lijiajia3515.cairo.core.exception.ConflictBusinessException;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import io.github.lijiajia3515.cairo.auth.domain.api.client.file.app_file.AccessFileArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.file.app_file.DeleteFileArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.file.app_file.GetFileStatArgs;
import io.github.lijiajia3515.cairo.auth.framework.sdk.CairoOAuthClientSdkService;
import io.github.lijiajia3515.cairo.auth.domain.dto.file.FileStat;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Slf4j
public class AppFileClientApiServiceImpl implements AppFileClientApiService{

	private final AppFileClientApiFeignClient appFileClientApiFeignClient;
	private final CairoOAuthClientSdkService cairoOAuthClientSdkService;

	public AppFileClientApiServiceImpl(AppFileClientApiFeignClient appFileClientApiFeignClient, CairoOAuthClientSdkService cairoOAuthClientSdkService) {
		this.appFileClientApiFeignClient = appFileClientApiFeignClient;
		this.cairoOAuthClientSdkService = cairoOAuthClientSdkService;
	}

	@Override
	public List<String> accessFile(AccessFileArgs args) {
		try {
			ResponseEntity<BusinessResult<List<String>>> businessResultResponseEntity = appFileClientApiFeignClient.accessFile(cairoOAuthClientSdkService.getHeaderAuthorization(),args);
			return Optional.ofNullable(businessResultResponseEntity.getBody()).map(BusinessResult::getData).orElse(Collections.emptyList());
		} catch (Exception e) {
			log.info("accessFile：", e);
			throw new ConflictBusinessException("访问文件失败");
		}
	}

	@Override
	public List<FileStat> getFileStat(GetFileStatArgs args) {
		try {
			ResponseEntity<BusinessResult<List<FileStat>>> businessResultResponseEntity = appFileClientApiFeignClient.getFileStat(cairoOAuthClientSdkService.getHeaderAuthorization(),args);
			return Optional.ofNullable(businessResultResponseEntity.getBody()).map(BusinessResult::getData).orElse(Collections.emptyList());
		} catch (Exception e) {
			log.info("getFileStat：", e);
			throw new ConflictBusinessException("获取文件状态失败");
		}
	}

	@Override
	public List<String> uploadFile(String path, MultipartFile file) {
		try {
			ResponseEntity<BusinessResult<List<String>>> businessResultResponseEntity = appFileClientApiFeignClient.uploadFile(cairoOAuthClientSdkService.getHeaderAuthorization(),path, file);
			return Optional.ofNullable(businessResultResponseEntity.getBody()).map(BusinessResult::getData).orElse(Collections.emptyList());
		} catch (Exception e) {
			log.info("uploadFile：", e);
			throw new ConflictBusinessException("上传文件失败");
		}
	}

	@Override
	public List<FileStat> deleteFile(DeleteFileArgs args) {
		try {
			ResponseEntity<BusinessResult<List<FileStat>>> businessResultResponseEntity = appFileClientApiFeignClient.deleteFile(cairoOAuthClientSdkService.getHeaderAuthorization(),args);
			return Optional.ofNullable(businessResultResponseEntity.getBody()).map(BusinessResult::getData).orElse(Collections.emptyList());
		} catch (Exception e) {
			log.info("deleteFile：", e);
			throw new ConflictBusinessException("删除文件失败");
		}
	}
}
