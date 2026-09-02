package io.github.lijiajia3515.cairo.auth.modules.tenant_app_doc;

import io.github.lijiajia3515.cairo.auth.domain.api.client.tenant_app_doc.GetOnlineTenantAppDocArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.weboffice.WebOfficeDocToken;

public interface TenantAppDocClientApiService {
	/**
	 * 获取预览文档token
	 * 需要权限: tenant_app_doc:preview
	 *
	 * @param args args 参数
	 * @return 在线文档地址
	 */
	WebOfficeDocToken getPreviewTenantAppDocToken(GetOnlineTenantAppDocArgs args);

	/**
	 * 获取在线编辑文档url
	 * 需要权限：tenant_app_doc:edit
	 *
	 * @param args args
	 * @return 在线文档地址
	 */
	WebOfficeDocToken getEditTenantAppDocToken(GetOnlineTenantAppDocArgs args);
}
