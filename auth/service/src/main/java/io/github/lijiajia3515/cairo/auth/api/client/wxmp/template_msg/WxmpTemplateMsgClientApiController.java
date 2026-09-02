 package io.github.lijiajia3515.cairo.auth.api.client.wxmp.template_msg;

 import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthClientPrincipal;
 import io.github.lijiajia3515.cairo.auth.domain.dto.wxmp.template_msg.WxmpTemplateMsg;
 import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurity;
 import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityType;
 import io.github.lijiajia3515.cairo.auth.modules.wxmp.template_msg.GetTemplateMsgArgs;
 import io.github.lijiajia3515.cairo.web.bind.annotation.BusinessResultBody;
 import lombok.RequiredArgsConstructor;
 import lombok.extern.slf4j.Slf4j;
 import org.springframework.security.access.prepost.PreAuthorize;
 import org.springframework.security.core.annotation.AuthenticationPrincipal;
 import org.springframework.validation.annotation.Validated;
 import org.springframework.web.bind.annotation.PostMapping;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RestController;

 /**
  *  [client/api] wxmp_template_msg controller
  *  微信消息模板
  */
 @Slf4j
 @Validated
 @RestController
 @RequestMapping("/client_api/wxmp_template_msg")
 @CairoSecurity(type = CairoSecurityType.CLIENT)
 @BusinessResultBody
 @RequiredArgsConstructor
 public class WxmpTemplateMsgClientApiController {

 	private final WxmpTemplateMsgClientApiService wxmpTemplateMsgClientApiService;


 	/**
 	 * 获取微信模板消息
 	 *
 	 * @param principal 凭证
 	 * @param args      参数
 	 * @return empty
 	 */
	@PostMapping("/get_wxmp_template_msg")
 	@PreAuthorize("hasAnyAuthority('wxmp_template_msg:all', 'wxmp_template_msg:read')")
 	public WxmpTemplateMsg getWxmpTemplateMsg(@AuthenticationPrincipal CairoOAuthClientPrincipal principal, @Validated @RequestBody GetTemplateMsgArgs args) {
		return wxmpTemplateMsgClientApiService.getWxmpTemplateMsg(principal.getAppId(), args.getBizId());
 	}
 }
