package io.github.lijiajia3515.cairo.auth.modules.wxmp.template_msg;

import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthClientPrincipal;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import io.github.lijiajia3515.cairo.auth.CairoAuthClientSdkCoreFeignClientConfiguration;
import io.github.lijiajia3515.cairo.auth.domain.dto.wxmp.template_msg.SendWxmpMsgByArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.wxmp.template_msg.WxmpTemplateMsg;
import io.github.lijiajia3515.cairo.auth.modules.wxmp.send_msg.SendWxmpMsgArgs;
import io.github.lijiajia3515.cairo.auth.modules.wxmp.send_msg.WxmpSendMsgClientApiFeignClientFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Optional;


/**
 * wxmpTemplateMsg Client Feign client
 */
@FeignClient(
	contextId = "wxmpTemplateMsgClientApiFeignClient",
	name = "${cairo.feign.client.cairo-auth-service:cairo-auth-service}",
	path = "/client_api/wxmp_template_msg",
	fallbackFactory = WxmpTemplateMsgClientApiFeignClientFallbackFactory.class,
	configuration = CairoAuthClientSdkCoreFeignClientConfiguration.class
)
public interface WxmpTemplateMsgClientApiFeignClient {

	/**
	 * 获取微信模板消息
	 *
	 * @param args      参数
	 * @return empty
	 */
	@PostMapping("/get_wxmp_template_msg")
	ResponseEntity<BusinessResult<WxmpTemplateMsg>> getWxmpTemplateMsg(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,@Validated @RequestBody GetTemplateMsgArgs args);
}
