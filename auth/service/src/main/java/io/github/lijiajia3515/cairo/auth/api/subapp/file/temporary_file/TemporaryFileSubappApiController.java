package io.github.lijiajia3515.cairo.auth.api.subapp.file.temporary_file;

import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthSubappUserPrincipal;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.file.temporary_file.AccessFileArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.file.temporary_file.DeleteFileArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.file.temporary_file.GetFileStatArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.file.temporary_file.UploadFileSignArgs;
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
 * [subapp_user/api] temporary file controller
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/subapp_user_api/temporary_file")
@CairoSecurity(type = CairoSecurityType.SUBAPP_USER)
@BusinessResultBody
@RequiredArgsConstructor
public class TemporaryFileSubappApiController {
	private final TemporaryFileSubappApiService temporaryFileSubappApiService;

	/**
	 * 获取访问文件地址
	 *
	 * @param principal 当前用户
	 * @param args      参数
	 * @return 文件列表，按顺序返回
	 */
	@PostMapping("/access_file")
	@PreAuthorize("hasAnyAuthority('app_admin', 'temporary_file:all', 'temporary_file:access_file')")
	public List<String> accessFile(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
								   @Validated @RequestBody AccessFileArgs args) {
		String appId = principal.getAppId();
		return temporaryFileSubappApiService.accessFile(appId, args);
	}

	/**
	 * 重定向文件
	 *
	 * @param principal     当前用户
	 * @param s3Url         s3文件地址
	 * @param enableVersion 启用版本控制访问
	 */
	@RequestMapping({"/access_file_url"})
	@PreAuthorize("hasAnyAuthority('app_admin', 'temporary_file:all', 'temporary_file:access_file')")
	public ModelAndView accessS3Url(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal, @RequestParam(name = "s3Url") String s3Url, @RequestParam("enableVersion") String enableVersion) {
		String appId = principal.getAppId();
		return new ModelAndView(new RedirectView(temporaryFileSubappApiService.accessFileUrl(appId, s3Url, enableVersion != null)));
	}

	/**
	 * 获取文件状态
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 文件状态
	 */
	@PostMapping("/get_file_stat")
	@PreAuthorize("hasAnyAuthority('app_admin', 'temporary_file:all', 'temporary_file:get_file_stat')")
	public List<FileStat> getPublicAppFileStat(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal, @Validated @RequestBody GetFileStatArgs args) {
		String appId = principal.getAppId();
		return temporaryFileSubappApiService.getFileStat(appId, args);
	}


	/**
	 * 上传文件
	 *
	 * @param principal 凭证
	 * @return 签名地址集合
	 */
	@PostMapping("/upload_file")
	@PreAuthorize("hasAnyAuthority('app_admin', 'temporary_file:all', 'temporary_file:upload_file')")
	public List<String> uploadFile(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
								   @RequestParam(value = "path", required = false) String path,
								   @Valid @NotNull @RequestPart(name = "file") MultipartFile file) {
		String appId = principal.getAppId();
		return temporaryFileSubappApiService.uploadFile(appId, path, file);
	}

	/**
	 * 上传多个文件
	 *
	 * @param files 文件
	 * @return x
	 */
	@PostMapping({"/upload_files"})
	@PreAuthorize("hasAnyAuthority('app_admin', 'temporary_file:all', 'temporary_file:upload_file')")
	public List<List<String>> uploadFile(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
										 @RequestParam(required = false) String prefix,
										 @Valid @NotNull @NotEmpty @RequestPart("files") List<MultipartFile> files) {
		String appId = principal.getAppId();
		return temporaryFileSubappApiService.uploadFiles(appId, prefix, files);
	}

	/**
	 * 上传文件签名
	 *
	 * @param principal 当前用户
	 * @param args      参数
	 * @return 预上传签名参数值
	 */
	@PostMapping("/get_upload_file_sign")
	@PreAuthorize("hasAnyAuthority('app_admin', 'temporary_file:all', 'temporary_file:upload_file')")
	public UploadSignArgs getTemporaryUploadSign(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
												 @RequestBody @Validated UploadFileSignArgs args) {
		String appId = principal.getAppId();
		return temporaryFileSubappApiService.getUploadFileSign(appId, args);
	}

	/**
	 * 获取单个文件上传签名url
	 *
	 * @param principal 当前用户
	 * @param paths     路径
	 * @return 上传urls模式列表
	 */
	@PostMapping("/get_upload_file_sign_url")
	@PreAuthorize("hasAnyAuthority('app_admin', 'temporary_file:all', 'temporary_file:upload_file')")
	public List<List<String>> getUploadFileSignUrl(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
												   @RequestBody @Validated List<String> paths) {
		String appId = principal.getAppId();
		return temporaryFileSubappApiService.getUploadFileSignUrl(appId, paths);
	}

	/**
	 * 删除文件
	 *
	 * @param principal 当前用户
	 * @param args      参数
	 * @return 1
	 */
	@PostMapping({"/delete_file"})
	@PreAuthorize("hasAnyAuthority('app_admin', 'temporary_file:all', 'temporary_file:delete_file')")
	public Optional<String> deleteFile(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
									   @Validated @RequestBody DeleteFileArgs args) {
		String appId = principal.getAppId();
		if (args.getS3Urls() == null) {
			args.setS3Urls(Collections.emptyList());
		}
		if (args.getHttpUrls() == null) {
			args.setHttpUrls(Collections.emptyList());
		}
		temporaryFileSubappApiService.deleteFile(appId, args);
		return Optional.empty();
	}


}
