package io.github.lijiajia3515.cairo.auth.modules.file;

import io.github.lijiajia3515.cairo.core.exception.ConflictBusinessException;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import io.github.lijiajia3515.cairo.auth.domain.api.client.file.tenant_file.AccessFileArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.file.tenant_file.DeleteFileArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.file.tenant_file.GetFileStatArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.file.FileStat;
import io.github.lijiajia3515.cairo.auth.framework.sdk.CairoOAuthClientSdkService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
public class TenantFileClientApiServiceImpl implements TenantFileClientApiService {

	private final TenantFileClientApiFeignClient tenantFileClientApiFeignClient;
	private final CairoOAuthClientSdkService cairoOAuthClientSdkService;

	public TenantFileClientApiServiceImpl(TenantFileClientApiFeignClient tenantFileClientApiFeignClient, CairoOAuthClientSdkService cairoOAuthClientSdkService) {
		this.tenantFileClientApiFeignClient = tenantFileClientApiFeignClient;
		this.cairoOAuthClientSdkService = cairoOAuthClientSdkService;
	}

	@Override
	public List<String> accessFile(AccessFileArgs args) {
		try {
			ResponseEntity<BusinessResult<List<String>>> businessResultResponseEntity = tenantFileClientApiFeignClient.accessFile(cairoOAuthClientSdkService.getHeaderAuthorization(),args);
			return Optional.ofNullable(businessResultResponseEntity.getBody()).map(BusinessResult::getData).orElse(Collections.emptyList());
		} catch (Exception e) {
			log.info("accessFile：", e);
			throw new ConflictBusinessException("访问文件失败");
		}
	}

	@Override
	public Map<String, String> getAccessFileMap(String tenantId,List<String> s3Urls) {
		try {
			return Optional.ofNullable(s3Urls)
				.filter(x -> !x.isEmpty())
				.map(os -> {
					ResponseEntity<BusinessResult<List<String>>> businessResultResponseEntity = tenantFileClientApiFeignClient.accessFile(cairoOAuthClientSdkService.getHeaderAuthorization(),AccessFileArgs
						.builder()
						.tenantId(tenantId)
						.s3Urls(s3Urls)
						.build()
					);
					List<String> httpUrls = Optional.ofNullable(businessResultResponseEntity.getBody()).map(BusinessResult::getData).orElse(Collections.emptyList());
					return IntStream.range(0, os.size())
						.mapToObj(x -> Arrays.asList(s3Urls.get(x), httpUrls.get(x)))
						.collect(Collectors.toMap(x -> x.get(0), x -> x.get(1), (x1, x2) -> x1));
				})
				.orElse(Collections.emptyMap());
		} catch (Exception e) {
			log.info("accessFile：", e);
			throw new ConflictBusinessException("访问文件失败");
		}
	}

	@Override
	public List<FileStat> getFileStat(GetFileStatArgs args) {
		try {
			ResponseEntity<BusinessResult<List<FileStat>>> businessResultResponseEntity = tenantFileClientApiFeignClient.getFileStat(cairoOAuthClientSdkService.getHeaderAuthorization(),args);
			return Optional.ofNullable(businessResultResponseEntity.getBody()).map(BusinessResult::getData).orElse(Collections.emptyList());
		} catch (Exception e) {
			log.info("getFileStat：", e);
			throw new ConflictBusinessException("获取文件状态失败");
		}

	}

	@Override
	public List<String> uploadFile(String bucket, String path, MultipartFile file) {
		try {
			ResponseEntity<BusinessResult<List<String>>> businessResultResponseEntity = tenantFileClientApiFeignClient.uploadFile(cairoOAuthClientSdkService.getHeaderAuthorization(),bucket, path, file);
			return Optional.ofNullable(businessResultResponseEntity.getBody()).map(BusinessResult::getData).orElse(Collections.emptyList());
		} catch (Exception e) {
			log.info("uploadFile：", e);
			throw new ConflictBusinessException("上传文件失败");
		}
	}


	@Override
	public List<FileStat> deleteFile(DeleteFileArgs args) {
		try {
			ResponseEntity<BusinessResult<List<FileStat>>> businessResultResponseEntity = tenantFileClientApiFeignClient.deleteFile(cairoOAuthClientSdkService.getHeaderAuthorization(),args);
			return Optional.ofNullable(businessResultResponseEntity.getBody()).map(BusinessResult::getData).orElse(Collections.emptyList());
		} catch (Exception e) {
			log.info("deleteFile：", e);
			throw new ConflictBusinessException("删除文件失败");
		}
	}
}
