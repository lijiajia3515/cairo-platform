package io.github.lijiajia3515.cairo.auth.api.cairo_web_manage.sms.template;

import io.github.lijiajia3515.cairo.auth.framework.context.CairoContext;
import io.github.lijiajia3515.cairo.auth.framework.context.CairoContextConstants;
import io.github.lijiajia3515.cairo.auth.framework.context.CairoContextHolder;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthSubappUserPrincipal;
import io.github.lijiajia3515.cairo.core.exception.ParamsErrorBusinessException;
import io.github.lijiajia3515.cairo.core.page.Page;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.sms.template.CreateSmsTemplateArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.sms.template.DeleteSmsTemplateArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.sms.template.GetSmsTemplateArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.sms.template.GetSmsTemplateInfoArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.sms.template.ModifySmsTemplateInfoArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.sms.template.ModifySmsTemplateStatusArgs;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurity;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityType;
import io.github.lijiajia3515.cairo.auth.domain.dto.sms.template.MetadataSmsTemplate;
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
 * [cairo_web_manage/api] sms template controller
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/cairo_web_manage_api/sms_template")
@CairoSecurity(type = CairoSecurityType.CAIRO_WEB_MANAGE_USER)
@BusinessResultBody
@RequiredArgsConstructor
public class SmsTemplateCairoWebManageApiController {
	private final SmsTemplateCairoWebManageApiService smsTemplateCairoWebManageApiService;

	/**
	 * 创建短信模板 接口
	 *
	 * @param principal principal
	 * @param args      args
	 * @return empty
	 */
	@PostMapping("/create_sms_template")
	@PreAuthorize("hasAnyAuthority('app_admin', 'sms_template:all', 'sms_template:create_sms_template')")
	@CairoContext
	public Optional<String> createSmsTemplate(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
											  @Validated @RequestBody CreateSmsTemplateArgs args) {
		String appId = CairoContextHolder.getValue(CairoContextConstants.APP_ID).orElseThrow(() -> new ParamsErrorBusinessException("appId不能为空"));
		smsTemplateCairoWebManageApiService.createSmsTemplate(appId, args);
		return Optional.empty();
	}

	/**
	 * 修改短信模板 接口
	 *
	 * @param principal principal
	 * @param args      args
	 * @return empty
	 */
	@PostMapping("/modify_sms_template_info")
	@PreAuthorize("hasAnyAuthority('app_admin', 'sms_template:all', 'sms_template:modify_sms_template_info')")
	@CairoContext
	public Optional<String> modifySmsTemplateInfo(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
												  @Validated @RequestBody ModifySmsTemplateInfoArgs args) {
		String appId = CairoContextHolder.getValue(CairoContextConstants.APP_ID).orElseThrow(() -> new ParamsErrorBusinessException("appId不能为空"));
		smsTemplateCairoWebManageApiService.modifySmsTemplateInfo(appId, args);
		return Optional.empty();
	}

	/**
	 * 修改短信模板状态 接口
	 *
	 * @param principal principal
	 * @param args      args
	 * @return empty
	 */
	@PostMapping("/modify_sms_template_status")
	@PreAuthorize("hasAnyAuthority('app_admin', 'sms_template:all', 'sms_template:modify_sms_template_status')")
	@CairoContext
	public Optional<String> modifySmsTemplateStatus(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
													@Validated @RequestBody ModifySmsTemplateStatusArgs args) {
		String appId = CairoContextHolder.getValue(CairoContextConstants.APP_ID).orElseThrow(() -> new ParamsErrorBusinessException("appId不能为空"));
		smsTemplateCairoWebManageApiService.modifySmsTemplateStatus(appId, args);
		return Optional.empty();
	}

	/**
	 * 删除短信模板参数
	 *
	 * @param principal principal
	 * @param args      args
	 * @return empty
	 */
	@PostMapping("/delete_sms_template")
	@PreAuthorize("hasAnyAuthority('app_admin', 'sms_template:all', 'sms_template:delete_sms_template')")
	@CairoContext
	public Optional<String> deleteSmsTemplate(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
											  @Validated @RequestBody DeleteSmsTemplateArgs args) {
		String appId = CairoContextHolder.getValue(CairoContextConstants.APP_ID).orElseThrow(() -> new ParamsErrorBusinessException("appId不能为空"));
		smsTemplateCairoWebManageApiService.deleteSmsTemplate(appId, args);
		return Optional.empty();
	}

	/**
	 * 获取短信模板列表
	 *
	 * @param principal principal
	 * @param args      args
	 * @return 短信模板列表
	 */
	@PostMapping("/get_sms_template_list")
	@PreAuthorize("hasAnyAuthority('app_admin', 'sms_template:all', 'sms_template:read')")
	@CairoContext
	public List<MetadataSmsTemplate> getSmsTemplateList(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
														@Validated @RequestBody GetSmsTemplateArgs args) {
		String appId = CairoContextHolder.getValue(CairoContextConstants.APP_ID).orElseThrow(() -> new ParamsErrorBusinessException("appId不能为空"));
		return smsTemplateCairoWebManageApiService.getSmsTemplateList(appId, args);
	}

	/**
	 * 获取短信模板分页列表
	 *
	 * @param principal principal
	 * @param args      args
	 * @return 短信模板分页列表
	 */
	@PostMapping("/get_sms_template_page_list")
	@PreAuthorize("hasAnyAuthority('app_admin', 'sms_template:all', 'sms_template:read')")
	@CairoContext
	public Page<MetadataSmsTemplate> getSmsTemplatePageList(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
															@Validated @RequestBody GetSmsTemplateArgs args) {
		String appId = CairoContextHolder.getValue(CairoContextConstants.APP_ID).orElseThrow(() -> new ParamsErrorBusinessException("appId不能为空"));
		return smsTemplateCairoWebManageApiService.getSmsTemplatePageList(appId, args);
	}

	/**
	 * 获取短信模板信息
	 *
	 * @param principal principal
	 * @param args      args
	 * @return 短信模板
	 */
	@PostMapping("/get_sms_template_info")
	@PreAuthorize("hasAnyAuthority('app_admin', 'sms_template:all', 'sms_template:read')")
	@CairoContext
	public Optional<MetadataSmsTemplate> getSmsTemplateInfo(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
															@Validated @RequestBody GetSmsTemplateInfoArgs args) {
		String appId = CairoContextHolder.getValue(CairoContextConstants.APP_ID).orElseThrow(() -> new ParamsErrorBusinessException("appId不能为空"));
		return Optional.ofNullable(smsTemplateCairoWebManageApiService.getSmsTemplateInfo(appId, args.getBizId()));
	}

	/**
	 * 获取短信模板详情
	 *
	 * @param principal principal
	 * @param args      args
	 * @return 短信模板详情
	 */
	@PostMapping("/get_sms_template_detail_info")
	@PreAuthorize("hasAnyAuthority('app_admin', 'sms_template:all', 'sms_template:read')")
	@CairoContext
	public Optional<MetadataSmsTemplate> getSmsTemplateDetailInfo(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
																  @Validated @RequestBody GetSmsTemplateInfoArgs args) {
		String appId = CairoContextHolder.getValue(CairoContextConstants.APP_ID).orElseThrow(() -> new ParamsErrorBusinessException("appId不能为空"));
		return Optional.ofNullable(smsTemplateCairoWebManageApiService.getSmsTemplateDetailInfo(appId, args.getBizId()));
	}
}
