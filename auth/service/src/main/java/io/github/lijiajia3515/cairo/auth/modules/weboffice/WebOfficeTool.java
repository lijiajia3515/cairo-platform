package io.github.lijiajia3515.cairo.auth.modules.weboffice;

import io.github.lijiajia3515.cairo.auth.domain.dto.weboffice.DocMode;
import io.github.lijiajia3515.cairo.auth.modules.file.FileTools;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WebOfficeTool {

	public static final String DOC_BUCKET = "doc";

	public static String tenantAppUserToWebOfficeUserId(String tenantId, String appId, String userId) {
		return String.format("%s__%s__%s__%s", DocMode.TENANT_APP, tenantId, appId, userId);
	}

	public static String appUserToWebOfficeUserId(String appId, String userId) {
		return String.format("%s__%s__%s", DocMode.APP, appId, userId);
	}

	public static CairoTenantAppUser webOfficeUserIdToTenantAppUser(String webOfficeUserId) {
		try {
			boolean valid = webOfficeUserId.startsWith(DocMode.TENANT_APP + "__");
			if (valid) {
				String[] s = webOfficeUserId.split("__");
				if (s.length == 4) {
					return CairoTenantAppUser.builder().tenantId(s[1]).appId(s[2]).userId(s[3]).build();
				}
			}
		} catch (RuntimeException e) {
			log.info("e", e);
		}
		return null;
	}

	public static CairoAppUser webOfficeUserIdToAppUser(String webOfficeUserId) {
		try {
			boolean valid = webOfficeUserId.startsWith(DocMode.APP + "__");
			if (valid) {
				String[] s = webOfficeUserId.split("__");
				if (s.length == 3) {
					return CairoAppUser.builder().appId(s[1]).userId(s[2]).build();
				}
			}
		} catch (RuntimeException e) {
			log.info("e", e);
		}
		return null;
	}

	public static String officeFilePath(String fileId, int fileVersion, String filename) {
		return String.format("%s/%s/%s", fileId, fileVersion, filename);
	}

	public static String officeS3FilePath(String fileId, int fileVersion, String filename) {
		return FileTools.encodeS3Url(DOC_BUCKET, officeFilePath(fileId, fileVersion, filename));
	}
}
