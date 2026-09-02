package io.github.lijiajia3515.cairo.auth.api.cairo_web_manage.notify.cairo_web_manage;

import io.github.lijiajia3515.cairo.auth.framework.context.CairoContext;
import io.github.lijiajia3515.cairo.auth.framework.context.CairoContextConstants;
import io.github.lijiajia3515.cairo.auth.framework.context.CairoContextHolder;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthSubappUserPrincipal;
import io.github.lijiajia3515.cairo.core.exception.ParamsErrorBusinessException;
import io.github.lijiajia3515.cairo.core.page.Page;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.notify.template.CreateNotifyTemplateArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.notify.template.DeleteNotifyTemplateArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.notify.template.GetNotifyTemplateArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.notify.template.GetNotifyTemplateInfoArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.notify.template.ModifyNotifyTemplateStatusArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.notify.template.ModifyNotificationTemplateInfoArgs;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurity;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityType;
import io.github.lijiajia3515.cairo.auth.domain.dto.notify.template.MetadataNotifyTemplate;
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
 * [cairo_web_manage/api] notification message template controller
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/cairo_web_manage_api/notify_template")
@CairoSecurity(type = CairoSecurityType.CAIRO_WEB_MANAGE_USER)
@BusinessResultBody
@RequiredArgsConstructor
public class NotifyTemplateCairoWebManageApiController {
	private final NotifyTemplateCairoWebManageApiService notifyTemplateCairoWebManageApiService;


	/**
	 * 通知消息模板列表
	 *
	 * @param principal principal
	 * @param args      args
	 * @return 短信模板列表
	 */
	@PostMapping("/get_notify_template_list")
	@PreAuthorize("hasAnyAuthority('app_admin', 'notify_template:all', 'notify_template:read')")
	@CairoContext
	public List<MetadataNotifyTemplate> getSmsTemplateList(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
																		@Validated @RequestBody GetNotifyTemplateArgs args) {
		String appId = CairoContextHolder.getValue(CairoContextConstants.APP_ID).orElseThrow(() -> new ParamsErrorBusinessException("appId不能为空"));
		return notifyTemplateCairoWebManageApiService.getNotifyTemplateList(appId, args);
	}

	/**
	 * 获取通知消息模板分页列表
	 *
	 * @param principal principal
	 * @param args      args
	 * @return 短信模板分页列表
	 */
	@PostMapping("/get_notify_template_page_list")
	@PreAuthorize("hasAnyAuthority('app_admin', 'notify_template:all', 'notify_template:read')")
	@CairoContext
	public Page<MetadataNotifyTemplate> getSmsTemplatePageList(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
																			@Validated @RequestBody GetNotifyTemplateArgs args) {
		String appId = CairoContextHolder.getValue(CairoContextConstants.APP_ID).orElseThrow(() -> new ParamsErrorBusinessException("appId不能为空"));
		return notifyTemplateCairoWebManageApiService.getNotifyTemplatePageList(appId, args);
	}

	/**
	 * 获取通知消息模板信息
	 *
	 * @param principal principal
	 * @param args      args
	 * @return 短信模板
	 */
	@PostMapping("/get_notify_template_info")
	@PreAuthorize("hasAnyAuthority('app_admin', 'notify_template:all', 'notify_template:read')")
	@CairoContext
	public Optional<MetadataNotifyTemplate> getNotifyTemplateInfo(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
																							@Validated @RequestBody GetNotifyTemplateInfoArgs args) {
		String appId = CairoContextHolder.getValue(CairoContextConstants.APP_ID).orElseThrow(() -> new ParamsErrorBusinessException("appId不能为空"));
		return Optional.ofNullable(notifyTemplateCairoWebManageApiService.getNotifyTemplateInfo(appId, args.getTemplateId()));
	}

	/**
	 * 获取短信模板详情
	 *
	 * @param principal principal
	 * @param args      args
	 * @return 短信模板详情
	 */
	@PostMapping("/get_notify_template_detail_info")
	@PreAuthorize("hasAnyAuthority('app_admin', 'notify_template:all', 'notify_template:read')")
	@CairoContext
	public Optional<MetadataNotifyTemplate> getNotifyTemplateDetailInfo(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
																								  @Validated @RequestBody GetNotifyTemplateInfoArgs args) {
		String appId = CairoContextHolder.getValue(CairoContextConstants.APP_ID).orElseThrow(() -> new ParamsErrorBusinessException("appId不能为空"));
		return Optional.ofNullable(notifyTemplateCairoWebManageApiService.getNotifyTemplateDetailInfo(appId, args.getTemplateId()));
	}

	/**
	 * 创建短信模板 接口
	 *
	 * @param principal principal
	 * @param args      args
	 * @return empty
	 */
	@PostMapping("/create_notify_template")
	@PreAuthorize("hasAnyAuthority('app_admin', 'notify_template:all', 'notify_template:create_notify_template')")
	@CairoContext
	public Optional<String> createNotifyTemplate(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
															  @Validated @RequestBody CreateNotifyTemplateArgs args) {
		String appId = CairoContextHolder.getValue(CairoContextConstants.APP_ID).orElseThrow(() -> new ParamsErrorBusinessException("appId不能为空"));
		notifyTemplateCairoWebManageApiService.createNotifyTemplate(appId, args);
		return Optional.empty();
	}

	/**
	 * 修改短信模板 接口
	 *
	 * @param principal principal
	 * @param args      args
	 * @return empty
	 */
	@PostMapping("/modify_notify_template_info")
	@PreAuthorize("hasAnyAuthority('app_admin', 'notify_template:all', 'notify_template:modify_notify_template_info')")
	@CairoContext
	public Optional<String> modifySmsTemplateInfo(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
												  @Validated @RequestBody ModifyNotificationTemplateInfoArgs args) {
		String appId = CairoContextHolder.getValue(CairoContextConstants.APP_ID).orElseThrow(() -> new ParamsErrorBusinessException("appId不能为空"));
		notifyTemplateCairoWebManageApiService.modifyNotifyTemplateInfo(appId, args);
		return Optional.empty();
	}

	/**
	 * 修改短信模板状态 接口
	 *
	 * @param principal principal
	 * @param args      args
	 * @return empty
	 */
	@PostMapping("/modify_notify_template_status")
	@PreAuthorize("hasAnyAuthority('app_admin', 'notify_template:all', 'notify_template:modify_notify_template_status')")
	@CairoContext
	public Optional<String> modifySmsTemplateStatus(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
													@Validated @RequestBody ModifyNotifyTemplateStatusArgs args) {
		String appId = CairoContextHolder.getValue(CairoContextConstants.APP_ID).orElseThrow(() -> new ParamsErrorBusinessException("appId不能为空"));
		notifyTemplateCairoWebManageApiService.modifyNotifyTemplateStatus(appId, args);
		return Optional.empty();
	}

	/**
	 * 删除通知消息模板参数
	 *
	 * @param principal principal
	 * @param args      args
	 * @return empty
	 */
	@PostMapping("/delete_notify_template")
	@PreAuthorize("hasAnyAuthority('app_admin', 'notify_template:all', 'notify_template:delete_notify_template')")
	@CairoContext
	public Optional<String> deleteSmsTemplate(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
											  @Validated @RequestBody DeleteNotifyTemplateArgs args) {
		String appId = CairoContextHolder.getValue(CairoContextConstants.APP_ID).orElseThrow(() -> new ParamsErrorBusinessException("appId不能为空"));
		notifyTemplateCairoWebManageApiService.deleteNotifyTemplate(appId, args);
		return Optional.empty();
	}

}
