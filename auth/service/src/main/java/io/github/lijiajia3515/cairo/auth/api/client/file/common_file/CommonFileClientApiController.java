package io.github.lijiajia3515.cairo.auth.api.client.file.common_file;

import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthClientPrincipal;
import io.github.lijiajia3515.cairo.auth.domain.api.client.file.common_file.AccessFileArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.file.common_file.CopyFileArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.file.common_file.DeleteFileArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.file.common_file.GetFileStatArgs;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurity;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityType;
import io.github.lijiajia3515.cairo.auth.domain.dto.file.FileStat;
import io.github.lijiajia3515.cairo.web.bind.annotation.BusinessResultBody;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * [client/api] common file controller
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/client_api/common_file")
@CairoSecurity(type = CairoSecurityType.CLIENT)
@BusinessResultBody
@RequiredArgsConstructor
public class CommonFileClientApiController {
	private final CommonFileClientApiService commonFileClientApiService;

	/**
	 * 访问文件
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 签名地址集合
	 */
	@PostMapping("/access_file")
	@PreAuthorize("hasAnyAuthority('common_file:all', 'common_file:access_file')")
	public List<String> getAccessSignUrl(@AuthenticationPrincipal CairoOAuthClientPrincipal principal,
										 @Validated @RequestBody AccessFileArgs args) {
		return commonFileClientApiService.accessFile(args);
	}

	/**
	 * 获取应用存储文件状态
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 文件状态
	 */
	@PostMapping("/get_file_stat")
	@PreAuthorize("hasAnyAuthority('common_file:all', 'common_file:get_file_stat')")
	public List<FileStat> getAppFileStat(@AuthenticationPrincipal CairoOAuthClientPrincipal principal,
										 @Validated @RequestBody GetFileStatArgs args) {
		return commonFileClientApiService.getFileStat(args);
	}

	/**
	 * 上传文件
	 *
	 * @param principal 凭证
	 * @param bucket    桶
	 * @param path      上传路径
	 * @param file      上传文件
	 * @return 签名地址集合
	 */
	@PostMapping("/upload_file")
	@PreAuthorize("hasAnyAuthority('common_file:all', 'common_file:upload_file')")
	public List<String> uploadFile(@AuthenticationPrincipal CairoOAuthClientPrincipal principal,
								   @Valid @NotNull @RequestParam("bucket") String bucket,
								   @Valid @NotNull @RequestParam(name = "path") String path,
								   @Valid @NotNull @RequestPart(name = "file") MultipartFile file) {
		return commonFileClientApiService.uploadFile(bucket, path, file);
	}


	/**
	 * 复制文件
	 *
	 * @param principal 凭证
	 * @param args      参数
	 */
	@PostMapping("/copy_file")
	@PreAuthorize("hasAnyAuthority('common_file:all', 'common_file:copy_file')")
	public Optional<String> copyFile(@AuthenticationPrincipal CairoOAuthClientPrincipal principal,
									 @Validated @RequestBody CopyFileArgs args) {
		return Optional.of(commonFileClientApiService.copyFile(args.getSource(), args.getTarget()));
	}

	/**
	 * 删除文件
	 *
	 * @param principal 当前用户
	 * @param args      参数
	 * @return 1
	 */
	@PostMapping("/delete_file")
	@PreAuthorize("hasAnyAuthority('app_file:all', 'common_file:delete_file')")
	public Optional<String> deleteFile(@AuthenticationPrincipal CairoOAuthClientPrincipal principal,
									   @Validated @RequestBody DeleteFileArgs args) {
		if (args.getS3Urls() == null) {
			args.setS3Urls(Collections.emptyList());
		}
		if (args.getHttpUrls() == null) {
			args.setHttpUrls(Collections.emptyList());
		}
		commonFileClientApiService.deleteFile(args);
		return Optional.empty();
	}
}
