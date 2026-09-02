package io.github.lijiajia3515.cairo.auth.modules.wxmp.send_msg;

import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import io.github.lijiajia3515.cairo.auth.CairoAuthClientSdkCoreFeignClientConfiguration;
import io.github.lijiajia3515.cairo.auth.domain.dto.wxmp.template_msg.SendWxmpMsgByArgs;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Optional;


/**
 * wxmpSendMsg Client Feign client
 */
@FeignClient(
	contextId = "wxmpSendMsgClientApiFeignClient",
	name = "${cairo.feign.client.cairo-auth-service:cairo-auth-service}",
	path = "/client_api/wxmp_send_msg",
	fallbackFactory = WxmpSendMsgClientApiFeignClientFallbackFactory.class,
	configuration = CairoAuthClientSdkCoreFeignClientConfiguration.class
)
public interface WxmpSendMsgClientApiFeignClient {


	/**
	 * 应用用户发送微信消息
	 *需要权限 wxmp_message:send_msg | wxmp_template_msg:all
	 *
	 * @param args      参数
	 * @return empty
	 */
	@PostMapping("/send_msg_by_app_user")
	ResponseEntity<BusinessResult<String>>  sendMsgByAppUser(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization, @Validated @RequestBody SendWxmpMsgByArgs args);



	/**
	 * 发送微信消息
	 *需要权限 wxmp_message:send_msg | wxmp_template_msg:all
	 *
	 * @param args      参数
	 * @return empty
	 */
	@PostMapping("/send_msg")
	ResponseEntity<BusinessResult<Optional<String>>> sendMsg(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,@Validated @RequestBody SendWxmpMsgArgs args);

}
