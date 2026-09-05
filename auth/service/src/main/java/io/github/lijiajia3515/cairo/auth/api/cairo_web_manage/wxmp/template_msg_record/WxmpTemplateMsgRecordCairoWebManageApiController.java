package io.github.lijiajia3515.cairo.auth.api.cairo_web_manage.wxmp.template_msg_record;

import io.github.lijiajia3515.cairo.auth.framework.context.CairoContext;
import io.github.lijiajia3515.cairo.auth.framework.context.CairoContextConstants;
import io.github.lijiajia3515.cairo.auth.framework.context.CairoContextHolder;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthSubappUserPrincipal;
import io.github.lijiajia3515.cairo.core.exception.ParamsErrorBusinessException;
import io.github.lijiajia3515.cairo.core.page.Page;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurity;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityType;
import io.github.lijiajia3515.cairo.auth.domain.dto.wxmp.template_msg_record.MetadataWxmpTemplateMsgRecord;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.wxmp.template_msg_record.GetWxmpTemplateMsgRecordArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.wxmp.template_msg_record.RetryWxmpTemplateMsgRecordArgs;
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
 * [cairo_web_manage/api] wxms template controller
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/cairo_web_manage_api/wxmp_template_msg_record")
@CairoSecurity(type = CairoSecurityType.CAIRO_WEB_MANAGE_USER)
@BusinessResultBody
@RequiredArgsConstructor
public class WxmpTemplateMsgRecordCairoWebManageApiController {
	private final WxmpTemplateMsgRecordCairoWebManageApiService wxmsMessageCairoWebManageApiService;

	/**
	 * 获取微信模板消息记录分页列表
	 *
	 * @param principal principal
	 * @param args      args
	 * @return 微信模板消息分页列表
	 */
	@PostMapping("/get_wxmp_template_msg_record_page_list")
	@PreAuthorize("hasAnyAuthority('app_admin', 'wxmp_template_msg_record:all', 'wxmp_template_msg_record:read')")
	@CairoContext
	public Page<MetadataWxmpTemplateMsgRecord> getWxmpTemplateMsgRecordPageList(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
																				@Validated @RequestBody GetWxmpTemplateMsgRecordArgs args) {
		String appId = CairoContextHolder.getValue(CairoContextConstants.APP_ID).orElseThrow(() -> new ParamsErrorBusinessException("appId不能为空"));
		return wxmsMessageCairoWebManageApiService.getWxmpTemplateMsgRecordPageList(appId, args);
	}

	/**
	 * 重试微信模板消息
	 *
	 * @param principal principal
	 * @param args      args
	 * @return empty
	 */
	@PostMapping("/retry_wxmp_template_msg_record")
	@PreAuthorize("hasAnyAuthority('app_admin', 'wxmp_template_msg_record:all', 'wxmp_template_msg_record:retry_wxmp_template_msg_record')")
	@CairoContext
	public Optional<String> retryWxmpTemplateMsgRecord(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
											@Validated @RequestBody RetryWxmpTemplateMsgRecordArgs args) {
		String appId = CairoContextHolder.getValue(CairoContextConstants.APP_ID).orElseThrow(() -> new ParamsErrorBusinessException("appId不能为空"));
		wxmsMessageCairoWebManageApiService.retryWxmpTemplateMsgRecord(appId, args);
		return Optional.empty();
	}
}
