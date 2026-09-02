package io.github.lijiajia3515.cairo.auth.modules.file;

import io.github.lijiajia3515.cairo.core.exception.ErrorBusinessException;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import io.github.lijiajia3515.cairo.auth.domain.api.client.file.tenant_file.AccessFileArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.file.tenant_file.DeleteFileArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.file.tenant_file.GetFileStatArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.file.FileStat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public class TenantFileClientApiFallbackFeignClient implements TenantFileClientApiFeignClient {
	public static final RuntimeException EX = new ErrorBusinessException("系统服务-企业文件子应用故障");


	@Override
	public ResponseEntity<BusinessResult<List<String>>> accessFile(String authorization, AccessFileArgs args) {
		throw EX;
	}

	@Override
	public ResponseEntity<BusinessResult<List<FileStat>>> getFileStat(String authorization, GetFileStatArgs args) {
		throw EX;
	}

	@Override
	public ResponseEntity<BusinessResult<List<String>>> uploadFile(String authorization, String tenantId, String path, MultipartFile file) {
		throw EX;
	}


	@Override
	public ResponseEntity<BusinessResult<List<FileStat>>> deleteFile(String authorization, DeleteFileArgs args) {
		throw EX;
	}
}
