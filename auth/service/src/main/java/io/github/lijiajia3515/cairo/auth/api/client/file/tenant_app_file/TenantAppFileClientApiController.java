package io.github.lijiajia3515.cairo.auth.api.client.file.tenant_app_file;

import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthClientPrincipal;
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
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurity;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityType;
import io.github.lijiajia3515.cairo.auth.modules.utils.StringUtils;
import io.github.lijiajia3515.cairo.web.bind.annotation.BusinessResultBody;
import jakarta.validation.Valid;
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

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * [client/api] tenant app file controller
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/client_api/tenant_app_file")
@CairoSecurity(type = CairoSecurityType.CLIENT)
@BusinessResultBody
@RequiredArgsConstructor
public class TenantAppFileClientApiController {
	private final TenantAppFileClientApiService tenantAppFileClientApiService;

	/**
	 * 文件夹
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 签名地址集合
	 */
	@PostMapping("/get_folder_list")
	@PreAuthorize("hasAnyAuthority('tenant_app_file:all', 'tenant_app_file:get_folder')")
	public List<Folder> getFolderList(@AuthenticationPrincipal CairoOAuthClientPrincipal principal,
									  @Validated @RequestBody GetFolderArgs args) {
		String appId = principal.getAppId();
		return tenantAppFileClientApiService.getFolderList(appId, args);
	}

	/**
	 * 文件夹
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 签名地址集合
	 */
	@PostMapping("/get_folder_tree_list")
	@PreAuthorize("hasAnyAuthority('tenant_app_file:all', 'tenant_app_file:get_folder')")
	public List<Folder> getFolderTreeList(@AuthenticationPrincipal CairoOAuthClientPrincipal principal,
										  @Validated @RequestBody GetFolderArgs args) {
		String appId = principal.getAppId();
		return tenantAppFileClientApiService.getFolderTreeList(appId, args);
	}

	/**
	 * 获取访问文件
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 签名地址集合
	 */
	@PostMapping("/list_file")
	@PreAuthorize("hasAnyAuthority('tenant_app_file:all', 'tenant_app_file:list_file')")
	public List<CairoFileItem> listFile(@AuthenticationPrincipal CairoOAuthClientPrincipal principal,
										@Validated @RequestBody ListFileArgs args) {
		String appId = principal.getAppId();
		return tenantAppFileClientApiService.listFile(appId, args);
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
	public int mkdir(@AuthenticationPrincipal CairoOAuthClientPrincipal principal,
					 @Validated @RequestBody MkdirArgs args) {
		return tenantAppFileClientApiService.mkdir(principal.getAppId(), args);
	}

	/**
	 * 获取访问文件
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 签名地址集合
	 */
	@PostMapping("/access_file")
	@PreAuthorize("hasAnyAuthority('tenant_app_file:all', 'tenant_app_file:access_file')")
	public List<String> accessFile(@AuthenticationPrincipal CairoOAuthClientPrincipal principal,
								   @Validated @RequestBody AccessFileArgs args) {
		String appId = principal.getAppId();
		return tenantAppFileClientApiService.accessFile(appId, args);
	}

	/**
	 * 获取企业应用存储文件状态
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 文件状态
	 */
	@PostMapping("/get_file_stat")
	@PreAuthorize("hasAnyAuthority('tenant_app_file:all', 'tenant_app_file:get_file_stat')")
	public List<FileStat> getTenantAppFileStat(@AuthenticationPrincipal CairoOAuthClientPrincipal principal,
											   @Validated @RequestBody GetFileStatArgs args) {
		return tenantAppFileClientApiService.getFileStat(principal.getAppId(), args);
	}

	/**
	 * 上传文件到租户/企业存储
	 *
	 * @param principal 凭证
	 * @param tenantId  租户id
	 * @param path      上传路径
	 * @param file      上传文件
	 * @return 签名地址集合
	 */
	@PostMapping("/upload_file")
	@PreAuthorize("hasAnyAuthority('tenant_app_file:all', 'tenant_app_file:upload_file')")
	public List<String> uploadFile(@AuthenticationPrincipal CairoOAuthClientPrincipal principal,
								   @Valid @NotNull @RequestParam(name = "tenant_id") String tenantId,
								   @Valid @NotNull @RequestParam(name = "path") String path,
								   @Valid @NotNull @RequestPart(name = "file") MultipartFile file,
								   @RequestParam(name = "metadata", required = false) String metadata
	) {
		Map<String, String> metadataMap = StringUtils.str2Map(metadata);
		return tenantAppFileClientApiService.uploadFile(tenantId, principal.getAppId(), path, file, metadataMap);
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
	public UploadSignArgs getUploadFileSign(@AuthenticationPrincipal CairoOAuthClientPrincipal principal,
											@Validated @RequestBody UploadFileSignArgs args) {
		String tenantId = args.getTenantId();
		String appId = principal.getAppId();
		return tenantAppFileClientApiService.getUploadFileSign(tenantId, appId, args);
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
	public List<List<String>> getTenantAppUploadSignUrl(@AuthenticationPrincipal CairoOAuthClientPrincipal principal,
														@Valid @NotNull @RequestParam(name = "tenant_id") String tenantId,
														@RequestBody @Validated List<String> paths) {
		String appId = principal.getAppId();
		return tenantAppFileClientApiService.getUploadFileSignUrl(tenantId, appId, paths);
	}

	/**
	 * 上传文件到租户/企业存储
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 签名地址集合
	 */
	@PostMapping("/move_file")
	@PreAuthorize("hasAnyAuthority('tenant_app_file:all', 'tenant_app_file:move_file')")
	public Integer moveFile(@AuthenticationPrincipal CairoOAuthClientPrincipal principal,
							@Validated @RequestBody MoveFileArgs args) {
		return tenantAppFileClientApiService.moveFile(principal.getAppId(), args);
	}

	/**
	 * 删除企业应用存储文件
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 1
	 */
	@PostMapping("/delete_file")
	@PreAuthorize("hasAnyAuthority('tenant_app_file:all', 'tenant_app_file:delete_file')")
	public Integer deleteFile(@AuthenticationPrincipal CairoOAuthClientPrincipal principal,
							  @Validated @RequestBody DeleteFileArgs args) {
		if (args.getS3Urls() == null) {
			args.setS3Urls(Collections.emptyList());
		}
		if (args.getHttpUrls() == null) {
			args.setHttpUrls(Collections.emptyList());
		}
		return tenantAppFileClientApiService.deleteFile(principal.getAppId(), args);
	}
}
