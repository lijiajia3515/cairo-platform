package io.github.lijiajia3515.cairo.auth.modules.file.temporary_file;

import io.github.lijiajia3515.cairo.core.exception.ErrorBusinessException;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import io.github.lijiajia3515.cairo.auth.domain.dto.file.FileStat;
import io.github.lijiajia3515.cairo.auth.domain.api.client.file.temporary_file.AccessFileArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.file.temporary_file.DeleteFileArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.file.temporary_file.GetFileStatArgs;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public class TemporaryFileClientApiFallbackFeignClient implements TemporaryFileClientApiFeignClient {
	public static final RuntimeException EX = new ErrorBusinessException("系统服务-临时文件子应用故障");

	@Override
	public ResponseEntity<BusinessResult<List<String>>> accessFile(String authorization,AccessFileArgs args) {
		throw EX;
	}

	@Override
	public ResponseEntity<BusinessResult<List<FileStat>>> getFileStat(String authorization,GetFileStatArgs args) {
		throw EX;
	}

	@Override
	public ResponseEntity<BusinessResult<List<String>>> uploadFile(String authorization,String path, MultipartFile file) {
		throw EX;
	}

	@Override
	public ResponseEntity<BusinessResult<List<List<String>>>> uploadFiles(String authorization,String prefix, List<MultipartFile> file) {
		throw EX;
	}

	@Override
	public ResponseEntity<BusinessResult<List<List<String>>>> getUploadFileSignUrl(String authorization,String prefix, Integer size) {
		throw EX;
	}

	@Override
	public ResponseEntity<BusinessResult<List<FileStat>>> deleteFile(String authorization,DeleteFileArgs args) {
		throw EX;
	}
}
