package io.github.lijiajia3515.cairo.auth.api.client.sms.message;

import com.baomidou.lock.annotation.Lock4j;
import io.github.lijiajia3515.cairo.auth.CairoAuthExtensionConstants;
import io.github.lijiajia3515.cairo.auth.domain.api.client.account.GetAccountInfoArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.account.GetAccountListArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.account.Account;
import io.github.lijiajia3515.cairo.auth.domain.dto.account.AccountExtension;
import io.github.lijiajia3515.cairo.auth.api.client.account.AccountClientApiService;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.auth.domain.api.client.sms.message.SmsMsgResult;
import io.github.lijiajia3515.cairo.auth.modules.sms.message.send_msg.SendMsgArgs;
import io.github.lijiajia3515.cairo.auth.modules.sms.message.send_msg.SendMsgSmsService;
import io.github.lijiajia3515.cairo.auth.domain.dto.sms.template.SmsTemplate;
import io.github.lijiajia3515.cairo.auth.modules.sms.template.SmsTemplateCommonService;
import io.github.lijiajia3515.cairo.auth.domain.api.client.sms.template.SendAccountSmsMsgArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.sms.template.SendPhoneNumberSmsMsgArgs;
import io.micrometer.tracing.annotation.NewSpan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Validated
@Component
public class SmsMsgClientApiService {
	private final SmsTemplateCommonService smsTemplateCommonService;
	private final SendMsgSmsService sendMsgSmsService;
	private final AccountClientApiService accountClientApiService;

	public SmsMsgClientApiService(SmsTemplateCommonService smsTemplateCommonService,
									  SendMsgSmsService sendMsgSmsService, AccountClientApiService accountClientApiService) {
		this.smsTemplateCommonService = smsTemplateCommonService;
		this.sendMsgSmsService = sendMsgSmsService;
		this.accountClientApiService = accountClientApiService;
	}

	@NewSpan
	@Lock4j(name = "send_msg_by_phone_number", keys = {"#args.appId","#args.phoneNumber","#args.bizId"})
	@BizLog(
		bizId = "sms_msg:send_msg_by_phone_number",
		scope = "write",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public SmsMsgResult sendMsgByPhoneNumber(@Validated SendPhoneNumberSmsMsgArgs args) {
		SendMsgArgs sendMsgArgs = null;
		try {
			SmsTemplate smsTemplate = smsTemplateCommonService.getSmsTemplate(args.getAppId(), args.getBizId());
			sendMsgArgs = SendMsgArgs.builder()
				.appId(args.getAppId())
				.phoneNumber(args.getPhoneNumber())
				.bizId(args.getBizId())
				.bizSign(args.getSign())
				.bizArgs(args.getArgs())
				.smsTemplate(smsTemplate)
				.build();

			// 异步发送消息
			sendMsgSmsService.sendMsg(sendMsgArgs);
		} catch (Exception e) {
			log.warn("send message by phone number: ", e);
		}
		return SmsMsgResult.builder()
			.msgId(Optional.ofNullable(sendMsgArgs).map(SendMsgArgs::getMsgId).orElse(null))
			.build();
	}

	/**
	 * 批量发送消息根据手机号
	 *
	 * @param argsList 参数集合
	 */
	@NewSpan
	@BizLog(
		bizId = "sms_msg:send_batch_message_by_phone_number",
		scope = "write",
		params = {
			@BizLog.Param(key = "argsList", value = "#argsList")
		}
	)
	public List<SmsMsgResult> sendBatchMessageByPhoneNumber(List<SendPhoneNumberSmsMsgArgs> argsList) {
		return argsList.stream().map(this::sendMsgByPhoneNumber).collect(Collectors.toList());
	}

	@NewSpan
	@Lock4j(name = "send_msg_by_account", keys = {"#args.appId","#args.accountId","#args.bizId"})
	@BizLog(
		bizId = "sms_msg:send_msg_by_account",
		scope = "write",
		params = {
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public SmsMsgResult sendMsgByAccount(@Validated SendAccountSmsMsgArgs args) {
		SendMsgArgs sendMsgArgs = null;
		try {
			SmsTemplate smsTemplate = smsTemplateCommonService.getSmsTemplate(args.getAppId(), args.getBizId());
			String phoneNumber = null;
			// 账号ID转换手机号
			if (args.getAccountId() != null) {
				Account accountInfo = accountClientApiService.getAccountInfo(GetAccountInfoArgs.builder()
					.accountId(args.getAccountId())
					.extension(Collections.singletonMap(CairoAuthExtensionConstants.ACCOUNT, AccountExtension.INFO.name()))
					.build()
				);

				phoneNumber = Optional.ofNullable(accountInfo)
					.map(Account::getPhoneNumber)
					.orElse(null);
			}

			sendMsgArgs = SendMsgArgs.builder()
				.appId(args.getAppId())
				.phoneNumber(phoneNumber)
				.bizId(args.getBizId())
				.bizSign(args.getSign())
				.bizArgs(args.getArgs())
				.smsTemplate(smsTemplate)
				.build();

			// 异步发送消息
			sendMsgSmsService.sendMsg(sendMsgArgs);
		} catch (Exception e) {
			log.warn("send message by account: ", e);
		}

		return SmsMsgResult.builder()
			.msgId(Optional.ofNullable(sendMsgArgs).map(SendMsgArgs::getMsgId).orElse(null))
			.build();
	}

	/**
	 * 批量发送消息根据账号
	 *
	 * @param argsList 参数集合
	 */
	@NewSpan
	@BizLog(
		bizId = "sms_msg:send_batch_message_by_account",
		scope = "write",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "argsList", value = "#argsList")
		}
	)
	public List<SmsMsgResult> sendBatchMessageByAccount(List<SendAccountSmsMsgArgs> argsList) {
		Set<String> accountIds = argsList.stream().map(SendAccountSmsMsgArgs::getAccountId).collect(Collectors.toSet());
		Map<String, String> phoneNumberMap;
		if (!accountIds.isEmpty()) {
			List<Account> accountList = accountClientApiService.getAccountList(GetAccountListArgs.builder()
				.accountIds(accountIds)
				.build());
			phoneNumberMap = Optional.ofNullable(accountList).orElse(Collections.emptyList()).stream()
				.collect(Collectors.toMap(Account::getAccountId, Account::getPhoneNumber));
		} else {
			phoneNumberMap = Collections.emptyMap();
		}

		return argsList.stream().map(args -> {
				String phoneNumber = phoneNumberMap.get(args.getAccountId());

				return sendMsgByPhoneNumber(SendPhoneNumberSmsMsgArgs.builder()
					.phoneNumber(phoneNumber)
					.appId(args.getAppId())
					.bizId(args.getBizId())
					.sign(args.getSign())
					.args(args.getArgs())
					.build());
			})
			.collect(Collectors.toList());
	}
}
