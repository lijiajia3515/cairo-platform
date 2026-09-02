package io.github.lijiajia3515.cairo.auth.api.tenant_subapp.file.tenant_app_file;

import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthTenantSubappUserPrincipal;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.file.tenant_app_file.AccessFileArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.file.tenant_app_file.DeleteFileArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.file.tenant_app_file.GetFileStatArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.file.tenant_app_file.GetFolderArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.file.tenant_app_file.ListFileArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.file.tenant_app_file.MkdirArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.file.tenant_app_file.MoveFileArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.file.tenant_app_file.UploadFileSignArgs;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurity;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityType;
import io.github.lijiajia3515.cairo.auth.domain.dto.file.CairoFileItem;
import io.github.lijiajia3515.cairo.auth.domain.dto.file.FileStat;
import io.github.lijiajia3515.cairo.auth.domain.dto.file.Folder;
import io.github.lijiajia3515.cairo.auth.domain.dto.file.UploadSignArgs;
import io.github.lijiajia3515.cairo.auth.modules.utils.StringUtils;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * [tenant_subapp_user/api] tenant app subapp tenant app file controller
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/tenant_subapp_user_api/tenant_app_file")
@CairoSecurity(type = CairoSecurityType.TENANT_SUBAPP_USER)
@BusinessResultBody
@RequiredArgsConstructor
public class TenantAppFileTenantSubappApiController {
	private final TenantAppFileTenantSubappApiService tenantAppFileTenantSubappApiService;

	/**
	 * 获取文件
	 *
	 * @param principal 当前用户
	 * @param args      参数
	 * @return 文件列表，按顺序返回
	 */
	@PostMapping("/list_file")
	@PreAuthorize("hasAnyAuthority('app_admin', 'tenant_app_file:all', 'tenant_app_file:list_file')")
	public List<CairoFileItem> listFile(@AuthenticationPrincipal CairoOAuthTenantSubappUserPrincipal principal,
										@Validated @RequestBody ListFileArgs args) {
		String tenantId = principal.getTenantId();
		String appId = principal.getAppId();
		return tenantAppFileTenantSubappApiService.listFile(tenantId, appId, args);
	}

	/**
	 * 获取文件夹列表
	 *
	 * @param principal 当前用户
	 * @param args      参数
	 * @return 文件列表，按顺序返回
	 */
	@PostMapping("/get_folder_list")
	@PreAuthorize("hasAnyAuthority('app_admin', 'tenant_app_file:all', 'tenant_app_file:get_folder')")
	public List<Folder> getFolderList(@AuthenticationPrincipal CairoOAuthTenantSubappUserPrincipal principal,
									  @Validated @RequestBody GetFolderArgs args) {
		String tenantId = principal.getTenantId();
		String appId = principal.getAppId();
		return tenantAppFileTenantSubappApiService.getFolderList(tenantId, appId, args);
	}

	/**
	 * 获取文件夹树
	 *
	 * @param principal 当前用户
	 * @param args      参数
	 * @return 文件夹树
	 */
	@PostMapping({"/get_folder_tree_list","/get_dir_tree_list"})
	@PreAuthorize("hasAnyAuthority('app_admin', 'tenant_app_file:all', 'tenant_app_file:get_folder')")
	public List<Folder> getFolderTreeList(@AuthenticationPrincipal CairoOAuthTenantSubappUserPrincipal principal,
										  @Validated @RequestBody GetFolderArgs args) {
		String tenantId = principal.getTenantId();
		String appId = principal.getAppId();
		return tenantAppFileTenantSubappApiService.getFolderTreeList(tenantId, appId, args);
	}

	/**
	 * 创建文件夹
	 *
	 * @param principal 当前用户
	 * @param args      参数
	 * @return 1
	 */
	@PostMapping("/mkdir")
	@PreAuthorize("hasAnyAuthority('app_admin', 'tenant_app_file:all', 'tenant_app_file:mkdir')")
	public int mkdir(@AuthenticationPrincipal CairoOAuthTenantSubappUserPrincipal principal,
					 @Validated @RequestBody MkdirArgs args) {
		Map<String, String> metadataMap =  args.getUserMetadata();
		if (metadataMap == null || metadataMap.isEmpty()) {
			metadataMap = new HashMap<>();
		}
		metadataMap.put("User-Id", principal.getUserId());
		return tenantAppFileTenantSubappApiService.mkdir(principal.getTenantId(), principal.getAppId(), args);
	}

	/**
	 * 获取访问文件地址
	 *
	 * @param principal 当前用户
	 * @param args      参数
	 * @return 文件列表，按顺序返回
	 */
	@PostMapping("/access_file")
	@PreAuthorize("hasAnyAuthority('app_admin', 'tenant_app_file:all', 'tenant_app_file:access_file')")
	public List<String> accessFile(@AuthenticationPrincipal CairoOAuthTenantSubappUserPrincipal principal,
								   @Validated @RequestBody AccessFileArgs args) {
		String tenantId = principal.getTenantId();
		String appId = principal.getAppId();
		return tenantAppFileTenantSubappApiService.accessFile(tenantId, appId, args);
	}

	/**
	 * 重定向文件
	 *
	 * @param principal     当前用户
	 * @param s3Url         s3文件地址
	 * @param enableVersion 启用版本控制访问
	 */
	@RequestMapping("/access_file_url")
	@PreAuthorize("hasAnyAuthority('app_admin', 'tenant_app_file:all', 'tenant_app_file:access_file')")
	public ModelAndView accessFileUrl(@AuthenticationPrincipal CairoOAuthTenantSubappUserPrincipal principal,
									  @RequestParam(name = "s3_url") String s3Url,
									  @RequestParam(value = "enable_version", required = false) String enableVersion) {
		String tenantId = principal.getTenantId();
		String appId = principal.getAppId();
		return new ModelAndView(new RedirectView(
			tenantAppFileTenantSubappApiService.accessFileUrl(tenantId, appId, s3Url, enableVersion != null)
		));
	}

	/**
	 * 获取文件状态
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 文件状态
	 */
	@PostMapping("/get_file_stat")
	@PreAuthorize("hasAnyAuthority('app_admin', 'tenant_app_file:all', 'tenant_app_file:get_file_stat')")
	public List<FileStat> getTenantAppFileStat(@AuthenticationPrincipal CairoOAuthTenantSubappUserPrincipal principal,
											   @Validated @RequestBody GetFileStatArgs args) {
		String tenantId = principal.getTenantId();
		String appId = principal.getAppId();
		return tenantAppFileTenantSubappApiService.getFileStat(tenantId, appId, args);
	}

	/**
	 * 上传文件
	 *
	 * @param principal 当前用户
	 * @param path      文件路径
	 * @param file      file
	 * @return 1
	 */
	@PostMapping("/upload_file")
	@PreAuthorize("hasAnyAuthority('app_admin', 'tenant_app_file:all', 'tenant_app_file:upload_file')")
	public List<String> uploadTenantAppFile(@AuthenticationPrincipal CairoOAuthTenantSubappUserPrincipal principal,
											@RequestParam(name = "path") String path,
											@RequestPart(name = "file") MultipartFile file,
											@RequestParam(name = "metadata", required = false) String metadata
	) {
		String tenantId = principal.getTenantId();
		String appId = principal.getAppId();
		Map<String, String> metadataMap = StringUtils.str2Map(metadata);
		metadataMap.put("User-Id", principal.getUserId());
		return tenantAppFileTenantSubappApiService.uploadFile(tenantId, appId, path, file, metadataMap);
	}

	/**
	 * 上传多个文件
	 *
	 * @param files 文件
	 * @return x
	 */
	@PostMapping("/upload_files")
	@PreAuthorize("hasAnyAuthority('app_admin', 'tenant_app_file:all', 'tenant_app_file:upload_file')")
	public List<List<String>> uploadFile(@AuthenticationPrincipal CairoOAuthTenantSubappUserPrincipal principal,
										 @RequestParam(required = false) String prefix,
										 @Valid @NotNull @NotEmpty @RequestPart("files") List<MultipartFile> files,
										 @RequestParam(name = "metadata", required = false) String metadata) {
		String tenantId = principal.getTenantId();
		String appId = principal.getAppId();
		Map<String, String> metadataMap = StringUtils.str2Map(metadata);
		metadataMap.put("User-Id", principal.getUserId());
		return tenantAppFileTenantSubappApiService.uploadFiles(tenantId, appId, prefix, files, metadataMap);
	}

	/**
	 * 获取上传文件签名参数
	 *
	 * @param principal 当前用户
	 * @param args      参数
	 * @return 预上传签名参数值
	 */
	@PostMapping("/get_upload_file_sign")
	@PreAuthorize("hasAnyAuthority('app_admin', 'tenant_app_file:all', 'tenant_app_file:upload_file')")
	public UploadSignArgs getUploadFileSign(@AuthenticationPrincipal CairoOAuthTenantSubappUserPrincipal principal,
											@Validated @RequestBody UploadFileSignArgs args) {
		String tenantId = principal.getTenantId();
		String appId = principal.getAppId();
		return tenantAppFileTenantSubappApiService.getUploadFileSign(tenantId, appId, args);
	}

	/**
	 * 获取上传文件签名Url
	 *
	 * @param principal 当前用户
	 * @param paths     路径
	 * @return 上传urls模式列表
	 */
	@PostMapping("/get_upload_file_sign_url")
	@PreAuthorize("hasAnyAuthority('app_admin', 'tenant_app_file:all', 'tenant_app_file:upload_file')")
	public List<List<String>> getTenantAppUploadSignUrl(@AuthenticationPrincipal CairoOAuthTenantSubappUserPrincipal principal, @RequestBody @Validated List<String> paths) {
		String appId = principal.getAppId();
		String tenantId = principal.getTenantId();
		return tenantAppFileTenantSubappApiService.getUploadFileSignUrl(tenantId, appId, paths);
	}

	/**
	 * 上传文件到租户/企业应用存储
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 签名地址集合
	 */
	@PostMapping("/move_file")
	@PreAuthorize("hasAnyAuthority('app_admin', 'tenant_app_file:all', 'tenant_app_file:move_file')")
	public int moveFile(@AuthenticationPrincipal CairoOAuthTenantSubappUserPrincipal principal, @Validated @RequestBody MoveFileArgs args) {
		return tenantAppFileTenantSubappApiService.moveFile(principal.getTenantId(), principal.getAppId(), args);
	}


	/**
	 * 删除文件
	 *
	 * @param principal 当前用户
	 * @param args      参数
	 * @return 1
	 */
	@PostMapping("/delete_file")
	@PreAuthorize("hasAnyAuthority('app_admin', 'tenant_app_file:all', 'tenant_app_file:delete_file')")
	public int deleteTenantApp(@AuthenticationPrincipal CairoOAuthTenantSubappUserPrincipal principal,
							   @Validated @RequestBody DeleteFileArgs args) {
		return tenantAppFileTenantSubappApiService.deleteFile(principal.getTenantId(), principal.getAppId(), args);
	}


}
