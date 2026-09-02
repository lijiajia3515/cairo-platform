package io.github.lijiajia3515.cairo.auth.framework.auth_code;

import io.github.lijiajia3515.cairo.auth.modules.auth_code.AuthCodeClientApiService;

import java.util.Optional;

public class FeignAuthCodeVerifyServiceImpl implements AuthCodeVerifyService {
	private final AuthCodeClientApiService authCodeClientApiService;

	public FeignAuthCodeVerifyServiceImpl(AuthCodeClientApiService authCodeClientApiService) {
		this.authCodeClientApiService = authCodeClientApiService;
	}

	@Override
	public AuthCodeVerifyStat verify(VerifyAuthCodeArgs args) {
		AuthCodeVerifyStat authCodeVerifyStat = authCodeClientApiService.verifyAuthCode(args);
		return Optional.ofNullable(authCodeVerifyStat).orElse(AuthCodeVerifyStat.FAILED);
	}
}
