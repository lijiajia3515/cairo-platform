package io.github.lijiajia3515.cairo.auth.api.client.file.tenant_file;

import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthClientPrincipal;
import io.github.lijiajia3515.cairo.auth.domain.api.client.file.tenant_file.AccessFileArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.file.tenant_file.DeleteFileArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.file.tenant_file.GetFileStatArgs;
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
 * [client/api] tenant file controller
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/client_api/tenant_file")
@CairoSecurity(type = CairoSecurityType.CLIENT)
@BusinessResultBody
@RequiredArgsConstructor
public class TenantFileClientApiController {
	private final TenantFileClientApiService tenantfileClientApiService;

	/**
	 * 获取文件
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 签名地址集合
	 */
	@PostMapping("/access_file")
	@PreAuthorize("hasAnyAuthority('tenant_file:all', 'tenant_file:access_file')")
	public List<String> accessFile(@AuthenticationPrincipal CairoOAuthClientPrincipal principal,
								   @Validated @RequestBody AccessFileArgs args) {
		String appId = principal.getAppId();
		return tenantfileClientApiService.accessFile(appId, args);
	}

	/**
	 * 获取文件属性
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 文件状态
	 */
	@PostMapping("/get_file_stat")
	@PreAuthorize("hasAnyAuthority('tenant_file:all', 'tenant_file:get_file_stat')")
	public List<FileStat> getFileStat(@AuthenticationPrincipal CairoOAuthClientPrincipal principal,
									  @Validated @RequestBody GetFileStatArgs args) {
		return tenantfileClientApiService.getFileStat(args);
	}

	/**
	 * 上传文件
	 *
	 * @param principal 凭证
	 * @param tenantId  企业id
	 * @param path      上传路径
	 * @param file      上传文件
	 * @return 签名地址集合
	 */
	@PostMapping("/upload_file")
	@PreAuthorize("hasAnyAuthority('tenant_file:all','tenant_file:upload_file')")
	public List<String> uploadFile(@AuthenticationPrincipal CairoOAuthClientPrincipal principal,
								   @Valid @NotNull @RequestParam(name = "tenant_id") String tenantId,
								   @Valid @NotNull @RequestParam(name = "path") String path,
								   @Valid @NotNull @RequestPart(name = "file") MultipartFile file) {
		return tenantfileClientApiService.uploadFile(tenantId, path, file);
	}

	/**
	 * 删除文件
	 *
	 * @param principal 当前用户
	 * @param args      参数
	 * @return 1
	 */
	@PostMapping("/delete_file")
	@PreAuthorize("hasAnyAuthority('tenant_file:all', 'tenant_file:delete_file')")
	public Optional<String> deleteFile(@AuthenticationPrincipal CairoOAuthClientPrincipal principal,
									   @Validated @RequestBody DeleteFileArgs args) {
		if (args.getS3Urls() == null) {
			args.setS3Urls(Collections.emptyList());
		}
		if (args.getHttpUrls() == null) {
			args.setHttpUrls(Collections.emptyList());
		}
		tenantfileClientApiService.deleteFile(args);
		return Optional.empty();
	}
}
