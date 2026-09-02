package io.github.lijiajia3515.cairo.auth.api.subapp.file.app_file;

import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthSubappUserPrincipal;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.file.app_file.AccessFileArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.file.app_file.DeleteFileArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.file.app_file.GetFolderArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.file.app_file.ListFileArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.file.app_file.MoveFileArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.file.app_file.UploadFileSignArgs;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurity;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityType;
import io.github.lijiajia3515.cairo.auth.domain.dto.file.CairoFileItem;
import io.github.lijiajia3515.cairo.auth.domain.dto.file.Folder;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import java.util.List;

/**
 * [subapp_user_api] app file controller
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/subapp_user_api/app_file")
@CairoSecurity(type = CairoSecurityType.SUBAPP_USER)
@BusinessResultBody
@RequiredArgsConstructor
public class AppFileSubappApiController {
	private final AppFileSubappApiService appFileSubappApiService;

	/**
	 * 获取文件
	 *
	 * @param principal 当前用户
	 * @param args      参数
	 * @return 文件列表，按顺序返回
	 */
	@PostMapping("/list_file")
	@PreAuthorize("hasAnyAuthority('app_admin', 'app_file:all', 'app_file:list_file')")
	public List<CairoFileItem> listFile(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
										@Validated @RequestBody ListFileArgs args) {
		String appId = principal.getAppId();
		return appFileSubappApiService.listFile(appId, args);
	}


	/**
	 * 获取文件夹列表
	 *
	 * @param principal 当前用户
	 * @param args      参数
	 * @return 文件列表，按顺序返回
	 */
	@PostMapping("/get_folder_list")
	@PreAuthorize("hasAnyAuthority('app_admin', 'app_file:all', 'app_file:get_folder')")
	public List<String> getFolderList(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
									  @Validated @RequestBody GetFolderArgs args) {
		String appId = principal.getAppId();
		return appFileSubappApiService.getFolderList(appId, args);
	}

	/**
	 * 获取文件夹树
	 *
	 * @param principal 当前用户
	 * @param args      参数
	 * @return 文件夹树
	 */
	@PostMapping("/get_folder_tree_list")
	@PreAuthorize("hasAnyAuthority('app_admin', 'app_file:all', 'app_file:get_folder')")
	public List<Folder> getFolderTreeList(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
										  @Validated @RequestBody GetFolderArgs args) {
		String appId = principal.getAppId();
		return appFileSubappApiService.getFolderTreeList(appId, args);
	}

	/**
	 * 获取访问文件地址
	 *
	 * @param principal 当前用户
	 * @param args      参数
	 * @return 文件列表，按顺序返回
	 */
	@PostMapping("/access_file")
	@PreAuthorize("hasAnyAuthority('app_admin', 'app_file:all', 'app_file:access_file')")
	public List<String> accessFile(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
								   @Validated @RequestBody AccessFileArgs args) {
		String appId = principal.getAppId();
		return appFileSubappApiService.accessFile(appId, args);
	}

	/**
	 * 返回文件
	 *
	 * @param principal     当前用户
	 * @param s3Url         s3文件地址
	 * @param enableVersion 启用版本控制访问
	 */
	@RequestMapping({"/access_file_url"})
	@PreAuthorize("hasAnyAuthority('app_admin', 'app_file:all', 'app_file:access_file')")
	public ModelAndView accessFileUrl(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
									  @RequestParam(name = "s3_url") String s3Url,
									  @RequestParam("enable_version") String enableVersion) {
		String appId = principal.getAppId();
		return new ModelAndView(new RedirectView(appFileSubappApiService.accessFileUrl(appId, s3Url, enableVersion != null)));
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
	@PreAuthorize("hasAnyAuthority('app_admin', 'app_file:all', 'app_file:upload_file')")
	public List<String> uploadFile(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
								   @RequestParam(name = "path") String path,
								   @RequestPart(name = "file") MultipartFile file) {
		String appId = principal.getAppId();
		return appFileSubappApiService.uploadFile(appId, path, file);
	}

	/**
	 * 上传多个文件
	 *
	 * @param files 文件
	 * @return x
	 */
	@PostMapping({"/upload_files"})
	@PreAuthorize("hasAnyAuthority('app_admin', 'app_file:all', 'app_file:upload_file')")
	public List<List<String>> uploadFile(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
										 @RequestParam(required = false) String prefix,
										 @Valid @NotNull @NotEmpty @RequestPart("files") List<MultipartFile> files) {
		String appId = principal.getAppId();
		return appFileSubappApiService.uploadFiles(appId, prefix, files);
	}

	/**
	 * 获取文件上传签名
	 *
	 * @param principal 当前用户
	 * @param args      参数
	 * @return 预上传签名参数值
	 */
	@PostMapping("/get_upload_file_sign")
	@PreAuthorize("hasAnyAuthority('app_admin', 'app_file:all', 'app_file:upload_file')")
	public UploadSignArgs getUploadFileSign(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
											@Validated @RequestBody UploadFileSignArgs args) {
		String appId = principal.getAppId();
		return appFileSubappApiService.getUploadFileSign(appId, args);
	}

	/**
	 * 获取文件上传签名url
	 *
	 * @param principal 当前用户
	 * @param paths     路径
	 * @return 上传urls模式列表
	 */
	@PostMapping("/get_upload_file_sign_url")
	@PreAuthorize("hasAnyAuthority('app_admin', 'app_file:all', 'app_file:upload_file')")
	public List<List<String>> getUploadFileSignUrl(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal, @RequestBody @Validated List<String> paths) {
		String appId = principal.getAppId();
		return appFileSubappApiService.getUploadFileSignUrl(appId, paths);
	}

	/**
	 * 上传文件到租户/企业应用存储
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 签名地址集合
	 */
	@PostMapping("/move_file")
	@PreAuthorize("hasAnyAuthority('app_admin', 'app_file:all', 'app_file:move_file')")
	public int moveFile(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal, @Validated @RequestBody MoveFileArgs args) {
		return appFileSubappApiService.moveFile(principal.getAppId(), args);
	}


	/**
	 * 删除文件
	 *
	 * @param principal 当前用户
	 * @param args      参数
	 * @return 1
	 */
	@PostMapping({"/delete_file"})
	@PreAuthorize("hasAnyAuthority('app_admin', 'app_file:all', 'app_file:delete_file')")
	public int deleteFile(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
						  @Validated @RequestBody DeleteFileArgs args) {
		return appFileSubappApiService.deleteFile(principal.getAppId(), args);
	}
}
