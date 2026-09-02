package io.github.lijiajia3515.cairo.auth.api.tenant_subapp.file.public_file;

import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthTenantSubappUserPrincipal;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurity;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityType;
import io.github.lijiajia3515.cairo.auth.domain.dto.file.FileStat;
import io.github.lijiajia3515.cairo.auth.domain.dto.file.UploadSignArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.file.common_file.AccessFileArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.file.common_file.DeleteFileArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.file.common_file.UploadFileSignArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.file.public_file.GetFileStatArgs;
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
 * [tenant_subapp_user/api]tenant app subapp public file controller
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/tenant_subapp_user_api/public_file")
@CairoSecurity(type = CairoSecurityType.TENANT_SUBAPP_USER)
@BusinessResultBody
@RequiredArgsConstructor
public class PublicFileTenantSubappApiController {
	private final PublicFileTenantSubappApiService publicFileTenantSubappApiService;


	/**
	 * 获取访问文件地址
	 *
	 * @param principal 当前用户
	 * @param args      参数
	 * @return 文件列表，按顺序返回
	 */
	@PostMapping("/access_file")
	@PreAuthorize("hasAnyAuthority('app_admin', 'public_file:all', 'public_file:access_file')")
	public List<String> accessFile(@AuthenticationPrincipal CairoOAuthTenantSubappUserPrincipal principal, @Validated @RequestBody AccessFileArgs args) {
		return publicFileTenantSubappApiService.accessFile(args);
	}


	/**
	 * 返回文件
	 *
	 * @param principal     当前用户
	 * @param s3Url         s3文件地址
	 * @param enableVersion 启用版本控制访问
	 */
	@RequestMapping({"/access_file_url"})
	@PreAuthorize("hasAnyAuthority('app_admin', 'public_file:all', 'public_file:access')")
	public ModelAndView accessS3Url(@AuthenticationPrincipal CairoOAuthTenantSubappUserPrincipal principal,
									@RequestParam(name = "s3_url") String s3Url,
									@RequestParam(value = "enabled_version",required = false) String enableVersion) {
		return new ModelAndView(new RedirectView(publicFileTenantSubappApiService.accessFileUrl(s3Url, enableVersion != null)));
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
	public List<FileStat> getPublicAppFileStat(@AuthenticationPrincipal CairoOAuthTenantSubappUserPrincipal principal, @Validated @RequestBody GetFileStatArgs args) {
		return publicFileTenantSubappApiService.getFileStat(args);
	}

	/**
	 * 上传文件
	 *
	 * @param principal 当前用户
	 * @param path      文件路径
	 * @param file      file
	 * @return 1
	 */
	@PostMapping({"/upload_file"})
	@PreAuthorize("hasAnyAuthority('app_admin', 'public_file:all', 'public_file:upload_file')")
	public List<String> uploadFile(@AuthenticationPrincipal CairoOAuthTenantSubappUserPrincipal principal,
								   @RequestParam(name = "path") String path,
								   @RequestPart(name = "file") MultipartFile file) {
		return publicFileTenantSubappApiService.uploadFile(path, file);
	}

	/**
	 * 上传多个文件
	 *
	 * @param files 文件
	 * @return x
	 */
	@PostMapping({"/upload_files"})
	@PreAuthorize("hasAnyAuthority('app_admin', 'public_file:all', 'public_file:upload_file')")
	public List<List<String>> uploadFile(@AuthenticationPrincipal CairoOAuthTenantSubappUserPrincipal principal,
										 @RequestParam(required = false) String prefix,
										 @Valid @NotNull @NotEmpty @RequestPart("files") List<MultipartFile> files) {
		return publicFileTenantSubappApiService.uploadFiles(prefix, files);
	}

	/**
	 * 获取上传文件签名
	 *
	 * @param principal 当前用户
	 * @param args      参数
	 * @return 预上传签名参数值
	 */
	@PostMapping("/get_upload_file_sign")
	@PreAuthorize("hasAnyAuthority('app_admin', 'public_file:all', 'public_file:upload_file')")
	public UploadSignArgs getUploadFileSign(@AuthenticationPrincipal CairoOAuthTenantSubappUserPrincipal principal,
											@Validated @RequestBody UploadFileSignArgs args) {
		return publicFileTenantSubappApiService.getUploadFileSign(args);
	}

	/**
	 * 获取上传文件签名Url
	 *
	 * @param principal 当前用户
	 * @param paths     路径
	 * @return 上传urls模式列表
	 */
	@PostMapping("/get_upload_file_sign_url")
	@PreAuthorize("hasAnyAuthority('app_admin', 'public_file:all', 'public_file:upload_file')")
	public List<List<String>> getUploadFileSignUrl(@AuthenticationPrincipal CairoOAuthTenantSubappUserPrincipal principal,
												   @RequestBody @Validated List<String> paths) {
		return publicFileTenantSubappApiService.getUploadFileSignUrl(paths);
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
	public Optional<String> deleteFile(@AuthenticationPrincipal CairoOAuthTenantSubappUserPrincipal principal,
									   @Validated @RequestBody DeleteFileArgs args) {
		if (args.getS3Urls() == null) {
			args.setS3Urls(Collections.emptyList());
		}
		if (args.getHttpUrls() == null) {
			args.setHttpUrls(Collections.emptyList());
		}
		publicFileTenantSubappApiService.deleteFile(args);
		return Optional.empty();
	}
}
