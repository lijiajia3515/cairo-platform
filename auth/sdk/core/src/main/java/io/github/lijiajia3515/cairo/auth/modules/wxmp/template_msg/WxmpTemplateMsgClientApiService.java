package io.github.lijiajia3515.cairo.auth.modules.wxmp.template_msg;

import io.github.lijiajia3515.cairo.auth.domain.dto.wxmp.template_msg.WxmpTemplateMsg;


public interface WxmpTemplateMsgClientApiService {

	/**
	 * 获取微信模板消息
	 * 需要权限 wxmp_template_msg:read | wxmp_template_msg:all
	 *
	 * @param args 参数
	 * @return empty
	 */
	WxmpTemplateMsg getWxmpTemplateMsg(GetTemplateMsgArgs args);
}
