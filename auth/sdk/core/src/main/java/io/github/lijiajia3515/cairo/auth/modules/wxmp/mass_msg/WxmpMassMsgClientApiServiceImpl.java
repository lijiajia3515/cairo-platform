package io.github.lijiajia3515.cairo.auth.modules.wxmp.mass_msg;

import io.github.lijiajia3515.cairo.core.exception.ConflictBusinessException;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import io.github.lijiajia3515.cairo.auth.domain.api.client.mass.msg.DeleteWxmpMassMsgArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.mass.msg.SendWxmpMassMsgArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.mass.msg.WxmpMassMsgResult;
import io.github.lijiajia3515.cairo.auth.framework.sdk.CairoOAuthClientSdkService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

@Slf4j
public class WxmpMassMsgClientApiServiceImpl implements WxmpMassMsgClientApiService {

	private final WxmpMassMsgClientApiFeignClient wxmpMassMsgClientApiFeignClient;
	private final CairoOAuthClientSdkService cairoOAuthClientSdkService;

	public WxmpMassMsgClientApiServiceImpl(WxmpMassMsgClientApiFeignClient wxmpMassMsgClientApiFeignClient, CairoOAuthClientSdkService cairoOAuthClientSdkService) {
		this.wxmpMassMsgClientApiFeignClient = wxmpMassMsgClientApiFeignClient;
		this.cairoOAuthClientSdkService = cairoOAuthClientSdkService;
	}

	@Override
	public WxmpMassMsgResult sendWxmpMassMsg(SendWxmpMassMsgArgs args) {
		try {
			ResponseEntity<BusinessResult<WxmpMassMsgResult>> businessResultResponseEntity = wxmpMassMsgClientApiFeignClient.sendWxmpMassMsg(cairoOAuthClientSdkService.getHeaderAuthorization(),args);
			return Optional.ofNullable(businessResultResponseEntity.getBody()).map(BusinessResult::getData).orElse(WxmpMassMsgResult.builder().build());
		} catch (Exception e) {
			log.info("sendWxmpMassMsg：", e);
			throw new ConflictBusinessException("公众号群发送失败");
		}
	}

	@Override
	public String deleteWxmpMassMsg(DeleteWxmpMassMsgArgs args) {
		try {
			ResponseEntity<BusinessResult<String>> businessResultResponseEntity = wxmpMassMsgClientApiFeignClient.deleteWxmpMassMsg(cairoOAuthClientSdkService.getHeaderAuthorization(),args);
			return Optional.ofNullable(businessResultResponseEntity.getBody()).map(BusinessResult::getData).orElse(null);
		} catch (Exception e) {
			log.info("deleteWxmpMassMsg：", e);
			throw new ConflictBusinessException("公众号群发删除失败");
		}
	}
}
