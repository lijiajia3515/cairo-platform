package io.github.lijiajia3515.cairo.auth.modules.file;

import io.github.lijiajia3515.cairo.core.exception.ConflictBusinessException;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import io.github.lijiajia3515.cairo.auth.domain.api.client.file.tenant_app_file.AccessFileArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.file.tenant_app_file.DeleteFileArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.file.tenant_app_file.GetFileStatArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.file.tenant_app_file.GetFolderArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.file.tenant_app_file.ListFileArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.file.tenant_app_file.MkdirArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.file.tenant_app_file.MoveFileArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.file.tenant_app_file.UploadFileSignArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.file.CairoFileItem;
import io.github.lijiajia3515.cairo.auth.domain.dto.file.FileStat;
import io.github.lijiajia3515.cairo.auth.domain.dto.file.Folder;
import io.github.lijiajia3515.cairo.auth.domain.dto.file.UploadSignArgs;
import io.github.lijiajia3515.cairo.auth.framework.sdk.CairoOAuthClientSdkService;
import io.github.lijiajia3515.cairo.auth.modules.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
public class TenantAppFileClientApiServiceImpl implements TenantAppFileClientApiService {

	private final TenantAppFileClientApiFeignClient tenantAppFileClientApiFeignClient;
	private final CairoOAuthClientSdkService cairoOAuthClientSdkService;

	public TenantAppFileClientApiServiceImpl(TenantAppFileClientApiFeignClient tenantAppFileClientApiFeignClient, CairoOAuthClientSdkService cairoOAuthClientSdkService) {
		this.tenantAppFileClientApiFeignClient = tenantAppFileClientApiFeignClient;
		this.cairoOAuthClientSdkService = cairoOAuthClientSdkService;
	}

	@Override
	public List<CairoFileItem> listFile(ListFileArgs args) {
		try {
			ResponseEntity<BusinessResult<List<CairoFileItem>>> businessResultResponseEntity = tenantAppFileClientApiFeignClient.listFile(cairoOAuthClientSdkService.getHeaderAuthorization(), args);
			return Optional.ofNullable(businessResultResponseEntity.getBody()).map(BusinessResult::getData).orElse(Collections.emptyList());
		} catch (Exception e) {
			log.info("accessFile：", e);
			throw new ConflictBusinessException("访问文件失败");
		}
	}

	@Override
	public List<Folder> getFolderList(GetFolderArgs args) {
		try {
			ResponseEntity<BusinessResult<List<Folder>>> businessResultResponseEntity = tenantAppFileClientApiFeignClient.getFolderList(cairoOAuthClientSdkService.getHeaderAuthorization(), args);
			return Optional.ofNullable(businessResultResponseEntity.getBody()).map(BusinessResult::getData).orElse(Collections.emptyList());
		} catch (Exception e) {
			log.info("getFolderList：", e);
			throw new ConflictBusinessException("读取文件夹列表失败");
		}
	}

	@Override
	public List<Folder> getFolderTreeList(GetFolderArgs args) {
		try {
			ResponseEntity<BusinessResult<List<Folder>>> businessResultResponseEntity = tenantAppFileClientApiFeignClient.getFolderTreeList(cairoOAuthClientSdkService.getHeaderAuthorization(), args);
			return Optional.ofNullable(businessResultResponseEntity.getBody()).map(BusinessResult::getData).orElse(Collections.emptyList());
		} catch (Exception e) {
			log.info("getFolderTreeList：", e);
			throw new ConflictBusinessException("读取文件夹列表失败");
		}
	}

	@Override
	public Integer mkdir(MkdirArgs args) {
		try {
			ResponseEntity<BusinessResult<Integer>> businessResultResponseEntity = tenantAppFileClientApiFeignClient.mkdir(cairoOAuthClientSdkService.getHeaderAuthorization(), args);
			return Optional.ofNullable(businessResultResponseEntity.getBody()).map(BusinessResult::getData).orElse(0);
		} catch (Exception e) {
			log.info("mkdir：", e);
			throw new ConflictBusinessException("创建文件夹失败");
		}
	}

	@Override
	public List<String> accessFile(AccessFileArgs args) {
		try {
			ResponseEntity<BusinessResult<List<String>>> businessResultResponseEntity = tenantAppFileClientApiFeignClient.accessFile(cairoOAuthClientSdkService.getHeaderAuthorization(), args);
			return Optional.ofNullable(businessResultResponseEntity.getBody()).map(BusinessResult::getData).orElse(Collections.emptyList());
		} catch (Exception e) {
			log.info("accessFile：", e);
			throw new ConflictBusinessException("访问文件失败");
		}
	}

	@Override
	public Map<String, String> getAccessFileMap(String tenantId, List<String> s3Urls) {
		try {
			return Optional.ofNullable(s3Urls)
				.filter(x -> !x.isEmpty())
				.map(os -> {
					ResponseEntity<BusinessResult<List<String>>> businessResultResponseEntity = tenantAppFileClientApiFeignClient.accessFile(cairoOAuthClientSdkService.getHeaderAuthorization(), AccessFileArgs.builder()
						.tenantId(tenantId)
						.s3Urls(s3Urls)
						.build());
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
	public UploadSignArgs getUploadFileSign(UploadFileSignArgs args) {
		try {
			ResponseEntity<BusinessResult<UploadSignArgs>> uploadFileSign = tenantAppFileClientApiFeignClient.getUploadFileSign(cairoOAuthClientSdkService.getHeaderAuthorization(), args);
			return Optional.ofNullable(uploadFileSign.getBody()).map(BusinessResult::getData).orElse(UploadSignArgs.builder().build());
		} catch (Exception e) {
			log.info("getUploadFileSign：", e);
			throw new ConflictBusinessException("上传文件失败");
		}
	}

	@Override
	public Integer moveFile(MoveFileArgs args) {
		try {
			ResponseEntity<BusinessResult<Integer>> businessResultResponseEntity = tenantAppFileClientApiFeignClient.moveFile(cairoOAuthClientSdkService.getHeaderAuthorization(), args);
			return Optional.ofNullable(businessResultResponseEntity.getBody()).map(BusinessResult::getData).orElse(0);
		} catch (Exception e) {
			log.info("getFileStat：", e);
			throw new ConflictBusinessException("文件移动/重命名失败");
		}
	}

	@Override
	public List<FileStat> getFileStat(GetFileStatArgs args) {
		try {
			ResponseEntity<BusinessResult<List<FileStat>>> businessResultResponseEntity = tenantAppFileClientApiFeignClient.getFileStat(cairoOAuthClientSdkService.getHeaderAuthorization(), args);
			return Optional.ofNullable(businessResultResponseEntity.getBody()).map(BusinessResult::getData).orElse(Collections.emptyList());
		} catch (Exception e) {
			log.info("getFileStat：", e);
			throw new ConflictBusinessException("获取文件状态失败");
		}
	}

	@Override
	public List<String> uploadFile(String bucket, String path, MultipartFile file) {
		try {
			ResponseEntity<BusinessResult<List<String>>> businessResultResponseEntity = tenantAppFileClientApiFeignClient.uploadFile(cairoOAuthClientSdkService.getHeaderAuthorization(), bucket, path, file);
			return Optional.ofNullable(businessResultResponseEntity.getBody()).map(BusinessResult::getData).orElse(Collections.emptyList());
		} catch (Exception e) {
			log.info("uploadFile：", e);
			throw new ConflictBusinessException("上传文件失败");
		}
	}

	@Override
	public List<String> uploadFile(String bucket, String path, MultipartFile file, Map<String, String> metadata) {
		try {
			ResponseEntity<BusinessResult<List<String>>> businessResultResponseEntity = tenantAppFileClientApiFeignClient.uploadFile(cairoOAuthClientSdkService.getHeaderAuthorization(), bucket, path, file, StringUtils.map2Str(Optional.ofNullable(metadata).orElse(Collections.emptyMap())));
			return Optional.ofNullable(businessResultResponseEntity.getBody()).map(BusinessResult::getData).orElse(Collections.emptyList());
		} catch (Exception e) {
			log.info("uploadFile：", e);
			throw new ConflictBusinessException("上传文件失败");
		}
	}


	@Override
	public Integer deleteFile(DeleteFileArgs args) {
		try {
			ResponseEntity<BusinessResult<Integer>> businessResultResponseEntity = tenantAppFileClientApiFeignClient.deleteFile(cairoOAuthClientSdkService.getHeaderAuthorization(), args);
			return Optional.ofNullable(businessResultResponseEntity.getBody()).map(BusinessResult::getData).orElse(0);
		} catch (Exception e) {
			log.info("deleteFile：", e);
			throw new ConflictBusinessException("删除文件失败");
		}
	}
}
