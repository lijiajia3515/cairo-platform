package io.github.lijiajia3515.cairo.auth.api.cairo_web_manage.sms.message;

import io.github.lijiajia3515.cairo.auth.framework.context.CairoContext;
import io.github.lijiajia3515.cairo.auth.framework.context.CairoContextConstants;
import io.github.lijiajia3515.cairo.auth.framework.context.CairoContextHolder;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthSubappUserPrincipal;
import io.github.lijiajia3515.cairo.core.exception.ParamsErrorBusinessException;
import io.github.lijiajia3515.cairo.core.page.Page;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurity;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityType;
import io.github.lijiajia3515.cairo.auth.domain.dto.sms.message.MetadataSmsMsg;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.sms.message.GetSmsMsgArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.sms.message.RetrySmsMsgArgs;
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

import java.util.Optional;

/**
 * [cairo_web_manage/api] sms template controller
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/cairo_web_manage_api/sms_msg")
@CairoSecurity(type = CairoSecurityType.CAIRO_WEB_MANAGE_USER)
@BusinessResultBody
@RequiredArgsConstructor
public class SmsMsgCairoWebManageApiController {
	private final SmsMsgCairoWebManageApiService smsMsgCairoWebManageApiService;

	/**
	 * 获取短信记录分页列表
	 *
	 * @param principal principal
	 * @param args      args
	 * @return 短信模板分页列表
	 */
	@PostMapping("/get_sms_msg_page_list")
	@PreAuthorize("hasAnyAuthority('app_admin', 'sms_msg:all', 'sms_msg:read')")
	@CairoContext
	public Page<MetadataSmsMsg> getSmsMsgPageList(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
                                                          @Validated @RequestBody GetSmsMsgArgs args) {
		String appId = CairoContextHolder.getValue(CairoContextConstants.APP_ID).orElseThrow(() -> new ParamsErrorBusinessException("appId不能为空"));
		return smsMsgCairoWebManageApiService.getSmsMsgPageList(appId, args);
	}

	/**
	 * 重试短信消息
	 *
	 * @param principal principal
	 * @param args      args
	 * @return empty
	 */
	@PostMapping("/retry_sms_msg")
	@PreAuthorize("hasAnyAuthority('app_admin', 'sms_msg:all', 'sms_msg:retry_sms_msg')")
	@CairoContext
	public Optional<String> retrySmsMsg(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
											@Validated @RequestBody RetrySmsMsgArgs args) {
		String appId = CairoContextHolder.getValue(CairoContextConstants.APP_ID).orElseThrow(() -> new ParamsErrorBusinessException("appId不能为空"));
		smsMsgCairoWebManageApiService.retrySmsMsg(appId, args);
		return Optional.empty();
	}
}
