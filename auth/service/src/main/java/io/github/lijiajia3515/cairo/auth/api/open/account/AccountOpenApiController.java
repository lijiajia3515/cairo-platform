package io.github.lijiajia3515.cairo.auth.api.open.account;

import io.github.lijiajia3515.cairo.auth.domain.api.open.account.ValidAccountUsernameResp;
import io.github.lijiajia3515.cairo.auth.domain.api.open.account.GetLoginAccountArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.open.account.LogoffAccountArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.open.account.OpenAccount;
import io.github.lijiajia3515.cairo.auth.domain.api.open.account.RegisterAccountArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.open.account.ResetPasswordPhoneNumberArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.open.account.ValidAccountEmailArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.open.account.ValidAccountEmailResp;
import io.github.lijiajia3515.cairo.auth.domain.api.open.account.ValidAccountPhoneNumberArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.open.account.ValidAccountPhoneNumberResp;
import io.github.lijiajia3515.cairo.auth.domain.api.open.account.ValidAccountUsernameArgs;
import io.github.lijiajia3515.cairo.auth.modules.captcha.token.VerifyCaptchaToken;
import io.github.lijiajia3515.cairo.web.bind.annotation.BusinessResultBody;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

/**
 * [open/api] account controller
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/open_api/account")
@RequiredArgsConstructor
@BusinessResultBody
public class AccountOpenApiController {
	private final AccountOpenApiService accountOpenApiService;

	/**
	 * 验证用户名
	 * <p>
	 * 匿名可用性查询,防账号枚举,须携带图形验证码令牌
	 *
	 * @param args args
	 * @return account
	 */
	@VerifyCaptchaToken
	@PostMapping("/valid_account_username")
	public ValidAccountUsernameResp validAccountUsername(@Validated @RequestBody ValidAccountUsernameArgs args) {
		return accountOpenApiService.validAccountUsername(args);
	}

	/**
	 * 验证手机号
	 * <p>
	 * 匿名可用性查询,防账号枚举,须携带图形验证码令牌
	 *
	 * @param args args
	 * @return account
	 */
	@VerifyCaptchaToken
	@PostMapping("/valid_account_phone_number")
	public ValidAccountPhoneNumberResp validAccountPhoneNumber(@Validated @RequestBody ValidAccountPhoneNumberArgs args) {
		return accountOpenApiService.validAccountPhoneNumber(args);
	}

	/**
	 * 验证邮箱
	 * <p>
	 * 匿名可用性查询,防账号枚举,须携带图形验证码令牌
	 *
	 * @param args args
	 * @return account
	 */
	@VerifyCaptchaToken
	@PostMapping("/valid_account_email")
	public ValidAccountEmailResp validAccountEmail(@Validated @RequestBody ValidAccountEmailArgs args) {
		return accountOpenApiService.validAccountEmail(args);
	}

	/**
	 * 注册账号
	 *
	 * @param args args
	 * @return account
	 */
	@PostMapping("/register_account")
	public Optional<String> registerAccount(@Validated @RequestBody RegisterAccountArgs args) {
		accountOpenApiService.registerAccount(args);
		return Optional.empty();
	}

	/**
	 * 注销账号
	 *
	 * @param args args
	 * @return account
	 */
	@PostMapping("/logoff_account")
	public Optional<String> logoffAccount(@Validated @RequestBody LogoffAccountArgs args) {
		accountOpenApiService.logoffAccount(args);
		return Optional.empty();
	}

	/**
	 * 根据手机号重置账号密码
	 *
	 * @param args args
	 * @return empty
	 */
	@PostMapping("/reset_account_password_by_phone_number")
	public Optional<String> resetPasswordByPhoneNumber(@Validated @RequestBody ResetPasswordPhoneNumberArgs args) {
		accountOpenApiService.resetAccountPasswordByPhoneNumber(args);
		return Optional.empty();
	}


	/**
	 * 查找账号登录信息
	 *
	 * @param args args
	 * @return account
	 */
	@PostMapping("/get_login_account")
	@VerifyCaptchaToken
	public Optional<OpenAccount> getLoginAccount(@Validated @RequestBody GetLoginAccountArgs args) {
		return Optional.ofNullable(accountOpenApiService.getLoginAccount(args));
	}
}
