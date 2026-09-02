package io.github.lijiajia3515.cairo.auth.modules.weboffice.v3;

import io.github.lijiajia3515.cairo.auth.framework.weboffice.VerifyWebOfficeSign;
import io.github.lijiajia3515.cairo.auth.domain.dto.weboffice.DocMode;
import io.github.lijiajia3515.cairo.auth.modules.weboffice.WebOfficeError;
import io.github.lijiajia3515.cairo.auth.modules.weboffice.WebOfficeRuntimeException;
import io.github.lijiajia3515.cairo.auth.modules.weboffice.WebOfficeTicketToken;
import io.github.lijiajia3515.cairo.auth.modules.weboffice.v3.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * [weboffice/v3] weboffice controller
 */
@Slf4j
@Validated
@VerifyWebOfficeSign
@RestController
@RequestMapping("/weboffice/v3/3rd")
public class WebOfficeApiController {
	private final WebOfficeAppDocService webOfficeAppDocService;
	private final WebOfficeTenantAppDocService webOfficeTenantAppDocService;

	public WebOfficeApiController(WebOfficeAppDocService webOfficeAppDocService, WebOfficeTenantAppDocService webOfficeTenantAppDocService) {
		this.webOfficeAppDocService = webOfficeAppDocService;
		this.webOfficeTenantAppDocService = webOfficeTenantAppDocService;
	}

	@GetMapping("/files/{fileId}")
	public WebofficeResult<WebOfficeFileResponse> fileInfo(@RequestHeader("x-app-id") String appid,
														   @RequestHeader("x-weboffice-token") String webofficeToken,
														   @RequestHeader("x-request-id") String webofficeRequestId,
														   @PathVariable("fileId") String fileId) {
		log.info("file info: appId: {} token: {} requestId: {} fileId: {}", appid, webofficeToken, webofficeRequestId, fileId);
		WebOfficeTicketToken token = WebOfficeTicketToken.valueOf(webofficeToken);
		WebOfficeFileResponse data = null;
		if (token.getMode().equals(DocMode.APP)) {
			data = webOfficeAppDocService.appDocFileInfo(fileId, token.getAppId(), token.getUserId());
		} else if (token.getMode().equals(DocMode.TENANT_APP)) {
			data = webOfficeTenantAppDocService.tenantAppDocFileInfo(fileId, token.getTenantId(), token.getAppId(), token.getUserId());
		} else {
			throw new WebOfficeRuntimeException(WebOfficeError.E40005);
		}
		return WebofficeResult.<WebOfficeFileResponse>builder()
			.code(WebOfficeError.SUCCESS.getCode())
			.message(WebOfficeError.SUCCESS.getMessage())
			.data(data)
			.build();
	}

	@GetMapping("/files/{fileId}/download")
	public WebofficeResult<WebOfficeFileDownloadResponse> downloadFile(@RequestHeader("x-app-id") String appid,
																	   @RequestHeader("x-weboffice-token") String webofficeToken,
																	   @RequestHeader("x-request-id") String webofficeRequestId,
																	   @PathVariable("fileId") String fileId) {
		log.info("file download: appId: {} token: {} requestId: {} fileId: {}", appid, webofficeToken, webofficeRequestId, fileId);
		WebOfficeTicketToken token = WebOfficeTicketToken.valueOf(webofficeToken);

		WebOfficeFileDownloadResponse data = null;
		if (token.getMode().equals(DocMode.APP)) {
			data = webOfficeAppDocService.appDocFileDownloadFile(fileId, token.getAppId(), token.getUserId());
		} else if (token.getMode().equals(DocMode.TENANT_APP)) {
			data = webOfficeTenantAppDocService.tenantAppDocFileDownloadFile(fileId, token.getAppId(), token.getTenantId(), token.getUserId());
		} else {
			throw new WebOfficeRuntimeException(WebOfficeError.E40005);
		}

		return WebofficeResult.<WebOfficeFileDownloadResponse>builder()
			.code(WebOfficeError.SUCCESS.getCode())
			.message(WebOfficeError.SUCCESS.getMessage())
			.data(data)
			.build();
	}

	@GetMapping("/files/{fileId}/permission")
	public WebofficeResult<WebOfficeFilePermissionResponse> permissionFile(@RequestHeader("x-app-id") String appid,
																		   @RequestHeader("x-weboffice-token") String webofficeToken,
																		   @RequestHeader("x-request-id") String webofficeRequestId,
																		   @PathVariable("fileId") String fileId) {
		log.info("file permission: appId: {} token: {} requestId: {} fileId: {}", appid, webofficeToken, webofficeRequestId, fileId);
		WebOfficeTicketToken token = WebOfficeTicketToken.valueOf(webofficeToken);

		WebOfficeFilePermissionResponse data = null;
		if (token.getMode().equals(DocMode.APP)) {
			data = webOfficeAppDocService.appDocPermissionFile(fileId, token.getWrite(), token.getAppId(), token.getUserId());
		} else if (token.getMode().equals(DocMode.TENANT_APP)) {
			data = webOfficeTenantAppDocService.tenantAppDocPermissionFile(fileId, token.getWrite(), token.getAppId(), token.getTenantId(), token.getUserId());
		} else {
			throw new WebOfficeRuntimeException(WebOfficeError.E40005);
		}

		return WebofficeResult.<WebOfficeFilePermissionResponse>builder()
			.code(WebOfficeError.SUCCESS.getCode())
			.message(WebOfficeError.SUCCESS.getMessage())
			.data(data)
			.build();
	}

	@GetMapping("/files/{fileId}/versions")
	public WebofficeResult<List<WebOfficeFileVersionResponse>> fileVersions(@RequestHeader("x-app-id") String appid,
																			@RequestHeader("x-weboffice-token") String webofficeToken,
																			@RequestHeader("x-request-id") String webofficeRequestId,
																			@PathVariable("fileId") String fileId,
																			@RequestParam int offset,
																			@RequestParam int limit) {
		log.info("file versions: appId: {} token: {} requestId: {} fileId: {} offset: {} limit: {}", appid, webofficeToken, webofficeRequestId, fileId, offset, limit);
		WebOfficeTicketToken token = WebOfficeTicketToken.valueOf(webofficeToken);

		List<WebOfficeFileVersionResponse> data = null;
		if (token.getMode().equals(DocMode.APP)) {
			data = webOfficeAppDocService.appDocFileVersions(fileId, offset, limit, token.getAppId(), token.getUserId());
		} else if (token.getMode().equals(DocMode.TENANT_APP)) {
			data = webOfficeTenantAppDocService.tenantAppDocFileVersions(fileId, offset, limit, token.getTenantId(), token.getAppId(), token.getUserId());
		} else {
			throw new WebOfficeRuntimeException(WebOfficeError.E40005);
		}

		return WebofficeResult.<List<WebOfficeFileVersionResponse>>builder()
			.code(WebOfficeError.SUCCESS.getCode())
			.message(WebOfficeError.SUCCESS.getMessage())
			.data(data)
			.build();
	}

	@GetMapping("/files/{fileId}/versions/{fileVersion}")
	public WebofficeResult<WebOfficeFileVersionResponse> fileVersion(@RequestHeader("x-app-id") String appid,
																	 @RequestHeader("x-weboffice-token") String webofficeToken,
																	 @RequestHeader("x-request-id") String webofficeRequestId,
																	 @PathVariable("fileId") String fileId,
																	 @PathVariable("fileVersion") int fileVersion) {
		log.info("file version: appId: {} token: {} requestId: {} fileId: {} version: {}", appid, webofficeToken, webofficeRequestId, fileId, fileVersion);
		WebOfficeTicketToken token = WebOfficeTicketToken.valueOf(webofficeToken);

		WebOfficeFileVersionResponse data = null;
		if (token.getMode().equals(DocMode.APP)) {
			data = webOfficeAppDocService.appDocFileVersion(fileId, fileVersion, token.getAppId(), token.getUserId());
		} else if (token.getMode().equals(DocMode.TENANT_APP)) {
			data = webOfficeTenantAppDocService.tenantDocFileVersion(fileId, fileVersion, token.getTenantId(), token.getAppId(), token.getUserId());
		} else {
			throw new WebOfficeRuntimeException(WebOfficeError.E40005);
		}

		return WebofficeResult.<WebOfficeFileVersionResponse>builder()
			.code(WebOfficeError.SUCCESS.getCode())
			.message(WebOfficeError.SUCCESS.getMessage())
			.data(data)
			.build();
	}

	@GetMapping("/files/{fileId}/versions/{fileVersion}/download")
	public WebofficeResult<WebOfficeFileDownloadResponse> downloadFileVersion(@RequestHeader("x-app-id") String appid,
																			  @RequestHeader("x-weboffice-token") String webofficeToken,
																			  @RequestHeader("x-request-id") String webofficeRequestId,
																			  @PathVariable("fileId") String fileId,
																			  @PathVariable("fileVersion") int fileVersion) {
		log.info("file version download: appId: {} token: {} requestId: {} fileId: {} version: {}", appid, webofficeToken, webofficeRequestId, fileId, fileVersion);
		WebOfficeTicketToken token = WebOfficeTicketToken.valueOf(webofficeToken);

		WebOfficeFileDownloadResponse data = null;
		if (token.getMode().equals(DocMode.APP)) {
			data = webOfficeAppDocService.appDocDownloadFileVersion(fileId, fileVersion, token.getAppId(), token.getUserId());
		} else if (token.getMode().equals(DocMode.TENANT_APP)) {
			data = webOfficeTenantAppDocService.tenantAppDocDownloadFileVersion(fileId, fileVersion, token.getTenantId(), token.getAppId(), token.getUserId());
		} else {
			throw new WebOfficeRuntimeException(WebOfficeError.E40005);
		}

		return WebofficeResult.<WebOfficeFileDownloadResponse>builder()
			.code(WebOfficeError.SUCCESS.getCode())
			.message(WebOfficeError.SUCCESS.getMessage())
			.data(data)
			.build();
	}

	@GetMapping("/files/{fileId}/upload/prepare")
	public WebofficeResult<WebOfficeFileUploadPrepareResponse> downloadFileVersion(@RequestHeader("x-app-id") String appid,
																				   @RequestHeader("x-weboffice-token") String webofficeToken,
																				   @RequestHeader("x-request-id") String webofficeRequestId,
																				   @PathVariable("fileId") String fileId) {
		log.info("file upload prepare: appId: {} token: {} requestId: {} fileId: {}", appid, webofficeToken, webofficeRequestId, fileId);
		WebOfficeTicketToken token = WebOfficeTicketToken.valueOf(webofficeToken);

		return WebofficeResult.<WebOfficeFileUploadPrepareResponse>builder()
			.code(WebOfficeError.SUCCESS.getCode())
			.message(WebOfficeError.SUCCESS.getMessage())
			.data(WebOfficeFileUploadPrepareResponse.builder().digestTypes(Set.of("sha256")).build())
			.build();
	}

	@PostMapping("/files/{fileId}/upload/address")
	public WebofficeResult<WebOfficeFileUploadAddressResponse> prepareUploadFile(@RequestHeader("x-app-id") String appid,
																				 @RequestHeader("x-weboffice-token") String webofficeToken,
																				 @RequestHeader("x-request-id") String webofficeRequestId,
																				 @PathVariable("fileId") String fileId,
																				 @RequestBody WebOfficeFileUploadAddressRequest body) {
		log.info("file upload address: appId: {} token: {} requestId: {} fileId: {} body: {}", appid, webofficeToken, webofficeRequestId, fileId, body);
		WebOfficeTicketToken token = WebOfficeTicketToken.valueOf(webofficeToken);

		WebOfficeFileUploadAddressResponse data = null;
		if (token.getMode().equals(DocMode.APP)) {
			data = webOfficeAppDocService.appDocUploadAddressFile(fileId, body, token.getAppId(), token.getUserId());
		} else if (token.getMode().equals(DocMode.TENANT_APP)) {
			data = webOfficeTenantAppDocService.tenantAppUploadAddressFile(fileId, body, token.getTenantId(), token.getAppId(), token.getUserId());
		} else {
			throw new WebOfficeRuntimeException(WebOfficeError.E40005);
		}

		log.info("response data: {}", data);

		return WebofficeResult.<WebOfficeFileUploadAddressResponse>builder()
			.code(WebOfficeError.SUCCESS.getCode())
			.message(WebOfficeError.SUCCESS.getMessage())
			.data(data)
			.build();
	}

	@PostMapping("/files/{fileId}/upload/complete")
	public WebofficeResult<WebOfficeFileResponse> completeUploadFile(@RequestHeader("x-app-id") String appid,
																	 @RequestHeader("x-weboffice-token") String webofficeToken,
																	 @RequestHeader("x-request-id") String webofficeRequestId,
																	 @PathVariable("fileId") String fileId,
																	 @RequestBody WebOfficeFileUploadCompleteRequest body) {
		log.info("file upload complete: appId: {} token: {} requestId: {} fileId: {} body: {}", appid, webofficeToken, webofficeRequestId, fileId, body);
		WebOfficeTicketToken token = WebOfficeTicketToken.valueOf(webofficeToken);

		WebOfficeFileResponse data = null;
		if (token.getMode().equals(DocMode.APP)) {
			data = webOfficeAppDocService.appDocCompleteUploadFile(fileId, body, token.getAppId(), token.getUserId());
		} else if (token.getMode().equals(DocMode.TENANT_APP)) {
			data = webOfficeTenantAppDocService.tenantAppDocCompleteUploadFile(fileId, body, token.getTenantId(), token.getAppId(), token.getUserId());
		} else {
			throw new WebOfficeRuntimeException(WebOfficeError.E40005);
		}

		return WebofficeResult.<WebOfficeFileResponse>builder()
			.code(WebOfficeError.SUCCESS.getCode())
			.message(WebOfficeError.SUCCESS.getMessage())
			.data(data)
			.build();
	}

	@GetMapping("/users")
	public WebofficeResult<List<WebOfficeUser>> userList(@RequestHeader("x-app-id") String appid,
														 @RequestHeader("x-weboffice-token") String webofficeToken,
														 @RequestHeader("x-request-id") String webofficeRequestId,
														 @RequestParam("user_ids") List<String> userIds) {
		log.info("user list: appId: {} token: {} requestId: {} userIds: {}", appid, webofficeToken, webofficeRequestId, userIds);
		WebOfficeTicketToken token = WebOfficeTicketToken.valueOf(webofficeToken);

		List<WebOfficeUser> data = null;
		if (token.getMode().equals(DocMode.APP)) {
			data = webOfficeAppDocService.appDocUserList(userIds, token.getAppId());
		} else if (token.getMode().equals(DocMode.TENANT_APP)) {
			data = webOfficeTenantAppDocService.tenantAppDocUserList(userIds, token.getTenantId(), token.getAppId());
		} else {
			throw new WebOfficeRuntimeException(WebOfficeError.E40005);
		}

		return WebofficeResult.<List<WebOfficeUser>>builder()
			.code(WebOfficeError.SUCCESS.getCode())
			.message(WebOfficeError.SUCCESS.getMessage())
			.data(data)
			.build();
	}

	@GetMapping("/files/{fileId}/watermark")
	public WebofficeResult<WebOfficeFileWatermarkResponse> watermarkFile(@RequestHeader("x-app-id") String appid,
																		 @RequestHeader("x-weboffice-token") String webofficeToken,
																		 @RequestHeader("x-request-id") String webofficeRequestId,
																		 @PathVariable("fileId") String fileId) {
		log.info("watermark: appId: {} token: {} requestId: {} fileId: {}", appid, webofficeToken, webofficeRequestId, fileId);
		WebOfficeTicketToken token = WebOfficeTicketToken.valueOf(webofficeToken);

		WebOfficeFileWatermarkResponse data = null;
		if (token.getMode().equals(DocMode.APP)) {
			data = webOfficeAppDocService.appDocWatermarkFile(fileId, token.getAppId(), token.getUserId());
		} else if (token.getMode().equals(DocMode.TENANT_APP)) {
			data = webOfficeTenantAppDocService.tenantAppDocWatermarkFile(fileId, token.getTenantId(), token.getAppId(), token.getUserId());
		} else {
			throw new WebOfficeRuntimeException(WebOfficeError.E40005);
		}

		return WebofficeResult.<WebOfficeFileWatermarkResponse>builder()
			.code(WebOfficeError.SUCCESS.getCode())
			.message(WebOfficeError.SUCCESS.getMessage())
			.data(data)
			.build();
	}

	@ExceptionHandler(WebOfficeRuntimeException.class)
	public WebofficeResult<Object> errorCode(WebOfficeRuntimeException e) {
		return WebofficeResult.builder()
			.code(e.getError().getCode())
			.message(e.getError().getMessage())
			.build();
	}
}
