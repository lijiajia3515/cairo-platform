package io.github.lijiajia3515.cairo.auth.api.client.tenant_app_doc;

import com.baomidou.lock.annotation.Lock4j;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.core.exception.ConflictBusinessException;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.OfficeFileMongodb;
import io.github.lijiajia3515.cairo.auth.domain.dto.weboffice.WebOfficeDocToken;
import io.github.lijiajia3515.cairo.auth.modules.weboffice.v3.WebOfficeTenantAppDocService;
import io.micrometer.tracing.annotation.NewSpan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * [client/api] tenant app doc service
 */
@Slf4j
@Component
public class TenantAppDocClientApiService {

	private final WebOfficeTenantAppDocService webOfficeTenantAppDocService;

	public TenantAppDocClientApiService(WebOfficeTenantAppDocService webOfficeTenantAppDocService) {
		this.webOfficeTenantAppDocService = webOfficeTenantAppDocService;
	}

	@NewSpan
	@BizLog(
		bizId = "tenant_app_doc:get_preview_doc_token",
		scope = "write",
		params = {
			@BizLog.Param(key = "tenantId", value = "#tenantId"),
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "userId", value = "#userId"),
			@BizLog.Param(key = "sourceFilePath", value = "#sourceFilePath"),
		}
	)
	public WebOfficeDocToken getPreviewTenantAppDocToken(String tenantId, String appId, String userId, String sourceFilePath) {
		try {
			OfficeFileMongodb fileData = webOfficeTenantAppDocService.getTenantAppDocFile(tenantId, appId, userId, sourceFilePath);
			return webOfficeTenantAppDocService.generateTenantDocToken(fileData.getFileId(), fileData.getType(), false, tenantId, appId, userId);
		} catch (RuntimeException e) {
			log.info("getPreviewTenantAppDocToken", e);
			throw new ConflictBusinessException(String.format("获取预览企业应用文档Token失败(%s)", e.getMessage()));
		}
	}

	@NewSpan
	@Lock4j(name = "get_edit_doc_token", keys = {"#tenantId","#appId","#userId"})
	@BizLog(
		bizId = "tenant_app_doc:get_edit_doc_token",
		scope = "write",
		params = {
			@BizLog.Param(key = "tenantId", value = "#tenantId"),
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "userId", value = "#userId"),
			@BizLog.Param(key = "sourceFilePath", value = "#sourceFilePath"),
		}
	)
	public WebOfficeDocToken getEditTenantAppDocToken(String tenantId, String appId, String userId, String sourceFilePath) {
		try {
			OfficeFileMongodb fileData = webOfficeTenantAppDocService.getTenantAppDocFile(tenantId, appId, userId, sourceFilePath);
			return webOfficeTenantAppDocService.generateTenantDocToken(fileData.getFileId(), fileData.getType(), true, tenantId, appId, userId);
		} catch (Exception e) {
			log.info("getPreviewTenantAppDocToken", e);
			throw new ConflictBusinessException(String.format("获取编辑企业应用文档Token失败(%s)", e.getMessage()));
		}

	}
}
