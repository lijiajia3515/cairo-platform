package io.github.lijiajia3515.cairo.auth.api.client.sms.message;

import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthClientPrincipal;
import io.github.lijiajia3515.cairo.auth.domain.api.client.sms.message.SmsMsgResult;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurity;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityType;
import io.github.lijiajia3515.cairo.auth.domain.api.client.sms.template.SendAccountSmsMsgArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.sms.template.SendPhoneNumberSmsMsgArgs;
import io.github.lijiajia3515.cairo.web.bind.annotation.BusinessResultBody;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

/**
 * [client/api] sms controller
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/client_api/sms_msg")
@CairoSecurity(type = CairoSecurityType.CLIENT)
@BusinessResultBody
@RequiredArgsConstructor
public class SmsMsgClientApiController {
	private final SmsMsgClientApiService smsMsgClientApiService;

	/**
	 * 根据手机号发送消息
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return empty
	 */
	@PostMapping("/send_msg_by_phone_number")
	@PreAuthorize("hasAnyAuthority('sms_msg:all', 'sms_msg:send_msg_by_phone_number')")
	public Optional<SmsMsgResult> sendMsgByPhoneNumber(@AuthenticationPrincipal CairoOAuthClientPrincipal principal, @Validated @RequestBody SendPhoneNumberSmsMsgArgs args) {
		return Optional.of(smsMsgClientApiService.sendMsgByPhoneNumber(args));

	}

	/**
	 * 根据手机号批量发送消息
	 *
	 * @param principal principal
	 * @param argList   参数
	 * @return empty
	 */
	@PostMapping("/send_batch_message_by_phone_number")
	@PreAuthorize("hasAnyAuthority('sms_msg:all', 'sms_msg:send_msg_by_phone_number')")
	public List<SmsMsgResult> sendBatchMessageByPhoneNumber(@AuthenticationPrincipal CairoOAuthClientPrincipal principal, @Validated @RequestBody List<SendPhoneNumberSmsMsgArgs> argList) {
		return smsMsgClientApiService.sendBatchMessageByPhoneNumber(argList);
	}

	/**
	 * 根据账号发送消息
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return empty
	 */
	@PostMapping("/send_msg_by_account")
	@PreAuthorize("hasAnyAuthority('sms_msg:all', 'sms_msg:send_msg_by_account')")
	public SmsMsgResult sendMsgByAccount(@AuthenticationPrincipal CairoOAuthClientPrincipal principal, @Validated @RequestBody SendAccountSmsMsgArgs args) {
		return smsMsgClientApiService.sendMsgByAccount(args);

	}

	/**
	 * 根据账号批量发送消息
	 *
	 * @param principal principal
	 * @param argList   参数
	 * @return empty
	 */
	@PostMapping("/send_batch_message_by_account")
	@PreAuthorize("hasAnyAuthority('sms_msg:all', 'sms_msg:send_msg_by_account')")
	public List<SmsMsgResult> sendBatchMessageByAccount(@AuthenticationPrincipal CairoOAuthClientPrincipal principal, @Validated @RequestBody List<SendAccountSmsMsgArgs> argList) {
		return smsMsgClientApiService.sendBatchMessageByAccount(argList);
	}

}
