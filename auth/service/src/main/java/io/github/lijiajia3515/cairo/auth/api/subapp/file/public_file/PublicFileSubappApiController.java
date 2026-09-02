package io.github.lijiajia3515.cairo.auth.api.subapp.file.public_file;

import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthSubappUserPrincipal;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.file.public_file.AccessFileArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.file.public_file.DeleteFileArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.file.public_file.GetFileStatArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.file.public_file.UploadFileSignArgs;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurity;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityType;
import io.github.lijiajia3515.cairo.auth.domain.dto.file.FileStat;
import io.github.lijiajia3515.cairo.auth.domain.dto.file.UploadSignArgs;
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
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * [subapp_user/api] public file controller
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/subapp_user_api/public_file")
@CairoSecurity(type = CairoSecurityType.SUBAPP_USER)
@BusinessResultBody
@RequiredArgsConstructor
public class PublicFileSubappApiController {
	private final PublicFileSubappApiService publicFileSubappApiService;

	/**
	 * 获取文件访问地址
	 *
	 * @param principal 当前用户
	 * @param args      参数
	 * @return 文件列表，按顺序返回
	 */
	@PostMapping("/access_file")
	@PreAuthorize("hasAnyAuthority('app_admin', 'public_file:all', 'public_file:access_file')")
	public List<String> accessFile(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal, @Validated @RequestBody AccessFileArgs args) {
		return publicFileSubappApiService.accessFile(args);
	}

	/**
	 * 返回文件
	 *
	 * @param principal     当前用户
	 * @param s3Url         s3文件地址
	 * @param enableVersion 启用版本控制访问
	 */
	@RequestMapping({"/access_file_url"})
	@PreAuthorize("hasAnyAuthority('app_admin', 'public_file:all', 'public_file:access_file')")
	public ModelAndView accessS3Url(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal, @RequestParam(name = "s3_url") String s3Url, @RequestParam(value = "enable_version", required = false) String enableVersion) {
		return new ModelAndView(new RedirectView(publicFileSubappApiService.accessFileUrl(s3Url, enableVersion != null)));
	}

	/**
	 * 获取文件状态
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 文件状态
	 */
	@PostMapping("/get_file_stat")
	@PreAuthorize("hasAnyAuthority('app_admin', 'public_file:all', 'public_file:get_file_stat')")
	public List<FileStat> getPublicAppFileStat(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal, @Validated @RequestBody GetFileStatArgs args) {
		return publicFileSubappApiService.getFileStat(args);
	}

	/**
	 * 直接上传文件
	 *
	 * @param principal 当前用户
	 * @param path      文件路径
	 * @param file      file
	 * @return 1
	 */
	@PostMapping({"/upload_file"})
	@PreAuthorize("hasAnyAuthority('app_admin', 'public_file:all', 'public_file:upload_file')")
	public List<String> uploadFile(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
								   @RequestParam(name = "path") String path,
								   @RequestPart(name = "file") MultipartFile file) {
		return publicFileSubappApiService.uploadFile(path, file);
	}

	/**
	 * 上传多个文件
	 *
	 * @param files 文件
	 * @return x
	 */
	@PostMapping({"/upload_files"})
	@PreAuthorize("hasAnyAuthority('app_admin', 'public_file:all', 'public_file:upload_file')")
	public List<List<String>> uploadFile(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
										 @RequestParam(required = false) String prefix,
										 @Valid @NotNull @NotEmpty @RequestPart("files") List<MultipartFile> files) {
		return publicFileSubappApiService.uploadFiles(prefix, files);
	}

	/**
	 * 获取上传文件签名（支持批量文件上传）
	 *
	 * @param principal 当前用户
	 * @param args      参数
	 * @return 预上传签名参数值
	 */
	@PostMapping("/get_upload_file_sign")
	@PreAuthorize("hasAnyAuthority('app_admin', 'public_file:all', 'public_file:upload_file')")
	public UploadSignArgs getUploadFileSign(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal, @Validated @RequestBody UploadFileSignArgs args) {
		return publicFileSubappApiService.getUploadFileSign(args);
	}

	/**
	 * 获取单个文件上传签名url
	 *
	 * @param principal 当前用户
	 * @param paths     路径
	 * @return 上传urls模式列表
	 */
	@PostMapping("/get_upload_file_sign_url")
	@PreAuthorize("hasAnyAuthority('app_admin', 'public_file:all', 'public_file:upload_file')")
	public List<List<String>> getUploadFileSignUrl(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal, @RequestBody @Validated List<String> paths) {
		return publicFileSubappApiService.getUploadFileSignUrl(paths);
	}

	/**
	 * 删除文件
	 *
	 * @param principal 当前用户
	 * @param args      参数
	 * @return 1
	 */
	@PostMapping({"/delete_file"})
	@PreAuthorize("hasAnyAuthority('app_admin', 'public_file:all', 'public_file:delete_file')")
	public Optional<String> deleteFile(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
									   @Validated @RequestBody DeleteFileArgs args) {
		if (args.getS3Urls() == null) {
			args.setS3Urls(Collections.emptyList());
		}
		if (args.getHttpUrls() == null) {
			args.setHttpUrls(Collections.emptyList());
		}
		publicFileSubappApiService.deleteFile(args);
		return Optional.empty();
	}
}
