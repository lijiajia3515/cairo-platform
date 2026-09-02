package io.github.lijiajia3515.cairo.auth.api.client.app_doc;

import com.baomidou.lock.annotation.Lock4j;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.core.exception.ConflictBusinessException;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.OfficeFileMongodb;
import io.github.lijiajia3515.cairo.auth.domain.dto.weboffice.WebOfficeDocToken;
import io.github.lijiajia3515.cairo.auth.modules.weboffice.v3.WebOfficeAppDocService;
import io.micrometer.tracing.annotation.NewSpan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * [client/api] app doc service
 */
@Slf4j
@Component
public class AppDocClientApiService {

	private final WebOfficeAppDocService webOfficeAppDocService;

	public AppDocClientApiService(WebOfficeAppDocService webOfficeAppDocService) {
		this.webOfficeAppDocService = webOfficeAppDocService;
	}

	@NewSpan
	@BizLog(
		bizId = "app_doc:get_preview_app_doc_token",
		scope = "write",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "userId", value = "#userId"),
			@BizLog.Param(key = "sourceFilePath", value = "#sourceFilePath"),
		}
	)
	public WebOfficeDocToken getPreviewAppDocToken(String appId, String userId, String sourceFilePath) {
		try {
			OfficeFileMongodb fileData = webOfficeAppDocService.getAppDocFile(appId, userId, sourceFilePath);
			return webOfficeAppDocService.generateAppDocToken(fileData.getFileId(), fileData.getType(), false, appId, userId);
		} catch (RuntimeException e) {
			log.info("getPreviewAppDocToken", e);
			throw new ConflictBusinessException(String.format("获取预览应用文档Token失败(%s)", e.getMessage()));
		}
	}

	@NewSpan
	@Lock4j(name = "get_edit_app_doc_token", keys = {"#appId", "#userId", "#sourceFilePath"})
	@BizLog(
		bizId = "app_doc:get_edit_app_doc_token",
		scope = "write",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "userId", value = "#userId"),
			@BizLog.Param(key = "sourceFilePath", value = "#sourceFilePath"),
		}
	)
	public WebOfficeDocToken getEditAppDocToken(String appId, String userId, String sourceFilePath) {
		try {
			OfficeFileMongodb fileData = webOfficeAppDocService.getAppDocFile(appId, userId, sourceFilePath);
			return webOfficeAppDocService.generateAppDocToken(fileData.getFileId(), fileData.getType(), true, appId, userId);
		} catch (RuntimeException e) {
			log.info("getPreviewAppDocToken", e);
			throw new ConflictBusinessException(String.format("获取编辑应用文档Token失败(%s)", e.getMessage()));
		}

	}


}
