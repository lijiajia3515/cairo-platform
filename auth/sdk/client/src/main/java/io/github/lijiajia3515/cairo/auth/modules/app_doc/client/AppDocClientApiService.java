package io.github.lijiajia3515.cairo.auth.modules.app_doc.client;

import io.github.lijiajia3515.cairo.auth.domain.api.client.app_doc.GetPreviewAppDocTokenArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.weboffice.WebOfficeDocToken;

public interface AppDocClientApiService {

	/**
	 * 获取预览文档token
	 * 需要权限: app_doc:preview
	 *
	 * @param args args 参数
	 * @return 在线文档地址
	 */
	WebOfficeDocToken getPreviewAppDocToken(GetPreviewAppDocTokenArgs args);

	/**
	 * 获取在线编辑文档url
	 * 需要权限：app_doc:edit
	 *
	 * @param args args
	 * @return 在线文档地址
	 */
	WebOfficeDocToken getEditAppDocToken(GetPreviewAppDocTokenArgs args);
}
