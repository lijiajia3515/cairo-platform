package io.github.lijiajia3515.cairo.auth.modules.file;

import io.github.lijiajia3515.cairo.core.exception.ErrorBusinessException;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public class TenantAppFileClientApiFallbackFeignClient implements TenantAppFileClientApiFeignClient {
	public static final RuntimeException EX = new ErrorBusinessException("系统服务-企业应用文件子应用故障");


	@Override
	public ResponseEntity<BusinessResult<List<CairoFileItem>>> listFile(String authorization, ListFileArgs args) {
		throw EX;
	}

	@Override
	public ResponseEntity<BusinessResult<List<Folder>>> getFolderList(String authorization, GetFolderArgs args) {
		throw EX;
	}

	@Override
	public ResponseEntity<BusinessResult<List<Folder>>> getFolderTreeList(String authorization, GetFolderArgs args) {
		throw EX;
	}

	@Override
	public ResponseEntity<BusinessResult<Integer>> mkdir(String authorization, MkdirArgs args) {
		throw EX;
	}

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
	public ResponseEntity<BusinessResult<List<String>>> uploadFile(String authorization, String tenantId, String path, MultipartFile file, String metadata) {
		throw EX;
	}

	@Override
	public ResponseEntity<BusinessResult<UploadSignArgs>> getUploadFileSign(String authorization, UploadFileSignArgs args) {
		throw EX;
	}

	@Override
	public ResponseEntity<BusinessResult<Integer>> moveFile(String authorization, MoveFileArgs args) {
		throw EX;
	}

	@Override
	public ResponseEntity<BusinessResult<Integer>> deleteFile(String authorization, DeleteFileArgs args) {
		throw EX;
	}

}
