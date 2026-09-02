package io.github.lijiajia3515.cairo.auth.modules.captcha;

import io.github.lijiajia3515.cairo.auth.framework.sdk.CairoOAuthClientSdkService;
import io.github.lijiajia3515.cairo.auth.domain.api.client.captcha.VerifyCaptchaTokenArgs;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

@Slf4j
public class CaptchaClientApiServiceImpl implements CaptchaClientApiService {

	private final CaptchaClientApiFeignClient captchaClientApiFeignClient;
	private final CairoOAuthClientSdkService cairoOAuthClientSdkService;

	public CaptchaClientApiServiceImpl(CaptchaClientApiFeignClient captchaClientApiFeignClient, CairoOAuthClientSdkService cairoOAuthClientSdkService) {
		this.captchaClientApiFeignClient = captchaClientApiFeignClient;
		this.cairoOAuthClientSdkService = cairoOAuthClientSdkService;
	}


	@Override
	public Boolean verifyCaptchaToken(VerifyCaptchaTokenArgs args) {
		try {
			ResponseEntity<BusinessResult<Boolean>> captchaToken = captchaClientApiFeignClient.verifyCaptchaToken(cairoOAuthClientSdkService.getHeaderAuthorization(), args);
			return Optional.ofNullable(captchaToken.getBody()).map(BusinessResult::getData).orElse(null);
		} catch (Exception e) {
			log.error("captchaToken error", e);
			throw e;
		}
	}
}
