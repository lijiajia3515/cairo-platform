 package io.github.lijiajia3515.cairo.auth.api.client.wxmp.send_msg;

 import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthClientPrincipal;
 import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurity;
 import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityType;
 import io.github.lijiajia3515.cairo.auth.domain.dto.wxmp.template_msg.SendWxmpMsgByArgs;
 import io.github.lijiajia3515.cairo.auth.modules.wxmp.send_msg.SendWxmpMsgArgs;
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

 import java.util.Optional;

 /**
  *  [client/api] wxms controller
  *  发送微信消息
  */
 @Slf4j
 @Validated
 @RestController
 @RequestMapping("/client_api/wxmp_send_msg")
 @CairoSecurity(type = CairoSecurityType.CLIENT)
 @BusinessResultBody
 @RequiredArgsConstructor
 public class WxmpSendMsgClientApiController {

 	private final WxmpSendMsgClientApiService wxmpSendMsgClientApiService;


 	/**
 	 * 应用用户发送微信消息
 	 *
 	 * @param principal 凭证
 	 * @param args      参数
 	 * @return empty
 	 */
	@PostMapping("/send_msg_by_app_user")
 	@PreAuthorize("hasAnyAuthority('wxmp_template_msg:all', 'wxmp_message:send_msg')")
 	public Optional<String> sendMsgByAppUser(@AuthenticationPrincipal CairoOAuthClientPrincipal principal, @Validated @RequestBody SendWxmpMsgByArgs args) {
		wxmpSendMsgClientApiService.sendWxmpMsgByAppUser(principal.getAppId(), args);
 		return Optional.empty();
 	}


	 /**
	  * 发送微信消息
	  *
	  * @param principal 凭证
	  * @param args      参数
	  * @return empty
	  */
	 @PostMapping("/send_msg")
	 @PreAuthorize("hasAnyAuthority('wxmp_template_msg:all', 'wxmp_message:send_msg')")
	 public Optional<String> sendMsg(@AuthenticationPrincipal CairoOAuthClientPrincipal principal, @Validated @RequestBody SendWxmpMsgArgs args) {
		 wxmpSendMsgClientApiService.sendWxmpMsg(args);
		 return Optional.empty();
	 }
 }
