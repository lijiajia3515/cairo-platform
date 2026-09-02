package io.github.lijiajia3515.cairo.auth.modules.file.common_file;

import io.github.lijiajia3515.cairo.core.exception.ErrorBusinessException;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import io.github.lijiajia3515.cairo.auth.domain.dto.file.FileStat;
import io.github.lijiajia3515.cairo.auth.domain.api.client.file.common_file.AccessFileArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.file.common_file.CopyFileArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.file.common_file.DeleteFileArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.file.common_file.GetFileStatArgs;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public class CommonFileClientApiFallbackFeignClient implements CommonFileClientApiFeignClient {
	public static final RuntimeException EX = new ErrorBusinessException("系统服务-通用文件子应用故障");

	@Override
	public ResponseEntity<BusinessResult<List<String>>> accessFile(String authorization,AccessFileArgs args) {
		throw EX;
	}

	@Override
	public ResponseEntity<BusinessResult<List<FileStat>>> getFileStat(String authorization,GetFileStatArgs args) {
		throw EX;
	}

	@Override
	public ResponseEntity<BusinessResult<List<String>>> uploadFile(String authorization,String bucket, String path, MultipartFile file) {
		throw EX;
	}

	@Override
	public ResponseEntity<BusinessResult<String>> copyFile(String authorization,CopyFileArgs args) {
		throw EX;
	}

	@Override
	public ResponseEntity<BusinessResult<List<FileStat>>> deleteFile(String authorization,DeleteFileArgs args) {
		throw EX;
	}
}
