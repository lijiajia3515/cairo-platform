package io.github.lijiajia3515.cairo.auth.modules.wxmp.mass_msg;

import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import io.github.lijiajia3515.cairo.auth.CairoAuthClientSdkCoreFeignClientConfiguration;
import io.github.lijiajia3515.cairo.auth.domain.api.client.mass.msg.DeleteWxmpMassMsgArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.mass.msg.SendWxmpMassMsgArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.mass.msg.WxmpMassMsgResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * wxmpMass Client Feign client
 */
@FeignClient(
	contextId = "wxmpMassMsgClientApiFeignClient",
	name = "${cairo.feign.client.cairo-auth-service:cairo-auth-service}",
	path = "/client_api/wxmp_mass_msg",
	fallbackFactory = WxmpMassMsgClientApiFeignClientFallbackFactory.class,
	configuration = CairoAuthClientSdkCoreFeignClientConfiguration.class
)
public interface WxmpMassMsgClientApiFeignClient {

	/**
	 * 公众号群发 发送
	 *
	 * @param args 参数
	 * @return WxmpMassMsgResult
	 */
	@PostMapping("/send")
	ResponseEntity<BusinessResult<WxmpMassMsgResult>> sendWxmpMassMsg(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization, @RequestBody SendWxmpMassMsgArgs args);

	/**
	 * 公众号群发 删除
	 *
	 * @param args 参数
	 */
	@PostMapping("/delete")
	ResponseEntity<BusinessResult<String>> deleteWxmpMassMsg(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
															 @RequestBody DeleteWxmpMassMsgArgs args);


}
