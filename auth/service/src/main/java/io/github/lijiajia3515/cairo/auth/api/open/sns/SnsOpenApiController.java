package io.github.lijiajia3515.cairo.auth.api.open.sns;

import io.github.lijiajia3515.cairo.auth.framework.phone_number_sns.PhoneNumberSnsInfo;
import io.github.lijiajia3515.cairo.auth.framework.sns.SnsInfo;
import io.github.lijiajia3515.cairo.auth.modules.sns.SnsCommonService;
import io.github.lijiajia3515.cairo.auth.domain.dto.sns.SnsToken;
import io.github.lijiajia3515.cairo.auth.domain.api.open.sns.GetPhoneNumberArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.open.sns.GetSnsInfoArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.open.sns.VerifySnsTokenArgs;
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
 * [open/api] sns controller
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/open_api/sns")
@RequiredArgsConstructor
@BusinessResultBody
public class SnsOpenApiController {

	private final SnsOpenApiService snsOpenApiService;
	private final SnsCommonService snsCommonService;

	/**
	 * 获取SNS信息
	 *
	 * @param args 参数
	 * @return 联接信息
	 */
	@Deprecated
	@PostMapping("/get_sns_info")
	public Optional<SnsInfo> getSnsInfo(@Validated @RequestBody GetSnsInfoArgs args) {
		return Optional.of(snsOpenApiService.getSnsInfo(args.getSnsType(), args.getSnsProviderId(), args.getSnsCode()));
	}

	/**
	 * 预处理sns信息
	 *
	 * @param args 参数
	 * @return 联接信息
	 */
	@PostMapping("/get_sns_token")
	public Optional<SnsToken> getSnsToken(@Validated @RequestBody GetSnsInfoArgs args) {
		return Optional.of(snsOpenApiService.getSnsToken(args.getSnsType(), args.getSnsProviderId(), args.getSnsCode()));
	}

	/**
	 * 获取手机号
	 *
	 * @param args 参数
	 * @return 手机号信息
	 */
	@PostMapping("/verify_sns_token")
	public Optional<SnsToken> verifySnsToken(@Validated @RequestBody VerifySnsTokenArgs args) {
		return Optional.of(snsCommonService.verifySnsToken(args.getToken()));
	}

	/**
	 * 获取手机号
	 *
	 * @param args 参数
	 * @return 手机号信息
	 */
	@PostMapping("/get_phone_number")
	public Optional<PhoneNumberSnsInfo> getPhoneNumber(@Validated @RequestBody GetPhoneNumberArgs args) {
		return Optional.of(snsOpenApiService.getPhoneNumber(args.getSnsProviderId(), args.getSnsCode()));
	}
}
