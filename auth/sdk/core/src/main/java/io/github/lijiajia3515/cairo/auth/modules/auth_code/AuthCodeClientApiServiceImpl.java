package io.github.lijiajia3515.cairo.auth.modules.auth_code;

import io.github.lijiajia3515.cairo.auth.framework.auth_code.AuthCodeVerifyStat;
import io.github.lijiajia3515.cairo.auth.framework.auth_code.VerifyAuthCodeArgs;
import io.github.lijiajia3515.cairo.auth.framework.sdk.CairoOAuthClientSdkService;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

@Slf4j
public class AuthCodeClientApiServiceImpl implements AuthCodeClientApiService {

	private final AuthCodeClientApiFeignClient authCodeClientApiFeignClient;
	private final CairoOAuthClientSdkService cairoOAuthClientSdkService;

	public AuthCodeClientApiServiceImpl(AuthCodeClientApiFeignClient authCodeClientApiFeignClient, CairoOAuthClientSdkService cairoOAuthClientSdkService) {
		this.authCodeClientApiFeignClient = authCodeClientApiFeignClient;
		this.cairoOAuthClientSdkService = cairoOAuthClientSdkService;
	}


	@Override
	public AuthCodeVerifyStat verifyAuthCode(VerifyAuthCodeArgs args) {
		try {
			ResponseEntity<BusinessResult<AuthCodeVerifyStat>> authCode = authCodeClientApiFeignClient.verifyAuthCode(cairoOAuthClientSdkService.getHeaderAuthorization(),args);
				return Optional.ofNullable(authCode.getBody()).map(BusinessResult::getData).orElse(null);
		} catch (Exception e) {
			log.error("authCode error", e);
			throw e;
		}
	}
}
