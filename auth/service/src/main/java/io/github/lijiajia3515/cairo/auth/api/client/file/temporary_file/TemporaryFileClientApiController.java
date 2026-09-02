package io.github.lijiajia3515.cairo.auth.api.client.file.temporary_file;

import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthClientPrincipal;
import io.github.lijiajia3515.cairo.auth.domain.api.client.file.temporary_file.AccessFileArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.file.temporary_file.DeleteFileArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.file.temporary_file.GetFileStatArgs;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurity;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityType;
import io.github.lijiajia3515.cairo.auth.domain.dto.file.FileStat;
import io.github.lijiajia3515.cairo.web.bind.annotation.BusinessResultBody;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
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
 * [client/api] temporary file controller
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/client_api/temporary_file")
@CairoSecurity(type = CairoSecurityType.CLIENT)
@BusinessResultBody
@RequiredArgsConstructor
public class TemporaryFileClientApiController {
	private final TemporaryFileClientApiService temporaryFileClientApiService;

	/**
	 * 获取访问文件签名
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 签名地址集合
	 */
	@PostMapping("/access_file")
	@PreAuthorize("hasAnyAuthority('temporary_file:all', 'temporary_file:access_file')")
	public List<String> accessFile(@AuthenticationPrincipal CairoOAuthClientPrincipal principal,
								   @Validated @RequestBody AccessFileArgs args) {
		String appId = principal.getAppId();
		return temporaryFileClientApiService.accessFile(appId, args);
	}

	/**
	 * 获取临时存储文件状态
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 文件状态
	 */
	@PostMapping("/get_file_stat")
	@PreAuthorize("hasAnyAuthority('temporary_file:all', 'temporary_file:get_file_stat')")
	public List<FileStat> getFileStat(@AuthenticationPrincipal CairoOAuthClientPrincipal principal,
									  @Validated @RequestBody GetFileStatArgs args) {
		return temporaryFileClientApiService.getFileStat(principal.getAppId(), args);
	}



	/**
	 * 上传文件
	 *
	 * @param principal 凭证
	 * @return 签名地址集合
	 */
	@PostMapping("/upload_file")
	@PreAuthorize("hasAnyAuthority('temporary_file:all', 'temporary_file:upload_file')")
	public List<String> uploadFile(@AuthenticationPrincipal CairoOAuthClientPrincipal principal,
										 @RequestParam(value = "path", required = false) String path,
										 @Valid @NotNull @RequestPart(name = "file") MultipartFile file) {
		String appId = principal.getAppId();
		return temporaryFileClientApiService.uploadFile(appId, path, file);
	}

	/**
	 * 上传多个文件
	 *
	 * @param principal 凭证
	 * @param prefix 路径前缀
	 * @param files 文件数组
	 * @return 签名地址集合
	 */
	@PostMapping("/upload_files")
	@PreAuthorize("hasAnyAuthority('temporary_file:all', 'temporary_file:upload_file')")
	public List<List<String>> uploadFile(@AuthenticationPrincipal CairoOAuthClientPrincipal principal,
										 @RequestParam(value = "prefix", required = false) String prefix,
										 @Valid @NotNull @RequestPart(name = "files") @NotEmpty List<MultipartFile> files) {
		String appId = principal.getAppId();
		return temporaryFileClientApiService.uploadFiles(appId, prefix, files);
	}

	/**
	 * 获取临时文件上传签名
	 *
	 * @param principal 凭证
	 * @param size      文件数量
	 * @return 凭证数量
	 */
	@PostMapping("/get_upload_file_sign_url")
	@PreAuthorize("hasAnyAuthority('temporary_file:all', 'temporary_file:upload_file')")
	public List<List<String>> getUploadFileSignUrl(@AuthenticationPrincipal CairoOAuthClientPrincipal principal,
												   @RequestParam(value = "prefix", required = false) String prefix,
												   @RequestParam(name = "size", defaultValue = "1") Integer size) {
		String appId = principal.getAppId();
		return temporaryFileClientApiService.getUploadFileSignUrl(appId, prefix, size);
	}


	/**
	 * 删除文件
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 1
	 */
	@PostMapping({"/delete_file"})
	@PreAuthorize("hasAnyAuthority('temporary_file:all', 'temporary_file:delete_file')")
	public Optional<String> deleteFile(@AuthenticationPrincipal CairoOAuthClientPrincipal principal,
									   @Validated @RequestBody DeleteFileArgs args) {
		if (args.getS3Urls() == null) {
			args.setS3Urls(Collections.emptyList());
		}
		if (args.getHttpUrls() == null) {
			args.setHttpUrls(Collections.emptyList());
		}
		temporaryFileClientApiService.deleteFile(principal.getAppId(), args);
		return Optional.empty();
	}

}
