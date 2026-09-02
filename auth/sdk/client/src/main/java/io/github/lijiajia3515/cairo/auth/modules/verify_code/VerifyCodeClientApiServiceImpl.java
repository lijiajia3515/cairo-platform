package io.github.lijiajia3515.cairo.auth.modules.verify_code;

import io.github.lijiajia3515.cairo.auth.domain.api.client.verify_code.SendAccountPhoneNumberVerifyCodeArgs;
import io.github.lijiajia3515.cairo.auth.framework.sdk.CairoOAuthClientSdkService;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

@Slf4j
public class VerifyCodeClientApiServiceImpl implements VerifyCodeClientApiService {

	private final VerifyCodeClientApiFeignClient verifyCodeClientApiFeignClient;
	private final CairoOAuthClientSdkService cairoOAuthClientSdkService;

	public VerifyCodeClientApiServiceImpl(VerifyCodeClientApiFeignClient verifyCodeClientApiFeignClient,
                                          CairoOAuthClientSdkService cairoOAuthClientSdkService) {
		this.verifyCodeClientApiFeignClient = verifyCodeClientApiFeignClient;
		this.cairoOAuthClientSdkService = cairoOAuthClientSdkService;
	}
	@Override
	public Optional<String> sendAccountPhoneNumberVerifyCode(SendAccountPhoneNumberVerifyCodeArgs args) {
		try {
			ResponseEntity<BusinessResult<Optional<String>>> sendAccountPhoneNumberVerifyCode = verifyCodeClientApiFeignClient.sendAccountPhoneNumberVerifyCode(cairoOAuthClientSdkService.getHeaderAuthorization(), args);
			return Optional.ofNullable(sendAccountPhoneNumberVerifyCode.getBody()).map(BusinessResult::getData).orElse(null);
		} catch (Exception e) {
			log.error("sendAccountPhoneNumberVerifyCode error", e);
			throw e;
		}
	}
}
