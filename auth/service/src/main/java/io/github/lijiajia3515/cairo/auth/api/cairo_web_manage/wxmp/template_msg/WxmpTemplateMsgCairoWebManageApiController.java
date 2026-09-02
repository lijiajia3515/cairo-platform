package io.github.lijiajia3515.cairo.auth.api.cairo_web_manage.wxmp.template_msg;

import io.github.lijiajia3515.cairo.auth.framework.context.CairoContext;
import io.github.lijiajia3515.cairo.auth.framework.context.CairoContextConstants;
import io.github.lijiajia3515.cairo.auth.framework.context.CairoContextHolder;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthSubappUserPrincipal;
import io.github.lijiajia3515.cairo.core.exception.ParamsErrorBusinessException;
import io.github.lijiajia3515.cairo.core.page.Page;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.wxmp.template_msg.CreateWxmpTemplateMsgArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.wxmp.template_msg.DeleteWxmpTemplateMsgArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.wxmp.template_msg.GetWxmpTemplateMsgArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.wxmp.template_msg.GetWxmpTemplateMsgInfoArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.wxmp.template_msg.ModifyWxmpTemplateMsgInfoArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.wxmp.template_msg.ModifyWxmpTemplateMsgStatusArgs;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurity;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityType;
import io.github.lijiajia3515.cairo.auth.domain.dto.wxmp.template_msg.MetadataWxmpTemplateMsg;
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
 * [cairo_web_manage/api] wxmp template controller
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/cairo_web_manage_api/wxmp_template_msg")
@CairoSecurity(type = CairoSecurityType.CAIRO_WEB_MANAGE_USER)
@BusinessResultBody
@RequiredArgsConstructor
public class WxmpTemplateMsgCairoWebManageApiController {
	private final WxmpTemplateMsgCairoWebManageApiService wxmpTemplateMsgCairoWebManageApiService;

	/**
	 * 创建微信模板 接口
	 *
	 * @param principal principal
	 * @param args      args
	 * @return empty
	 */
	@PostMapping("/create_wxmp_template_msg")
	@PreAuthorize("hasAnyAuthority('app_admin', 'wxmp_template_msg:all', 'wxmp_template_msg:create_wxmp_template_msg')")
	@CairoContext
	public Optional<String> createWxmpTemplateMsg(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
												  @Validated @RequestBody CreateWxmpTemplateMsgArgs args) {
		String appId = CairoContextHolder.getValue(CairoContextConstants.APP_ID).orElseThrow(() -> new ParamsErrorBusinessException("appId不能为空"));
		wxmpTemplateMsgCairoWebManageApiService.createWxmpTemplateMsg(appId, args);
		return Optional.empty();
	}

	/**
	 * 修改微信模板 接口
	 *
	 * @param principal principal
	 * @param args      args
	 * @return empty
	 */
	@PostMapping("/modify_wxmp_template_msg_info")
	@PreAuthorize("hasAnyAuthority('app_admin', 'wxmp_template_msg:all', 'wxmp_template_msg:modify_wxmp_template_msg_info')")
	@CairoContext
	public Optional<String> modifyWxmpTemplateMsgInfo(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
													  @Validated @RequestBody ModifyWxmpTemplateMsgInfoArgs args) {
		String appId = CairoContextHolder.getValue(CairoContextConstants.APP_ID).orElseThrow(() -> new ParamsErrorBusinessException("appId不能为空"));
		wxmpTemplateMsgCairoWebManageApiService.modifyWxmpTemplateMsgInfo(appId, args);
		return Optional.empty();
	}

	/**
	 * 修改微信模板状态 接口
	 *
	 * @param principal principal
	 * @param args      args
	 * @return empty
	 */
	@PostMapping("/modify_wxmp_template_msg_status")
	@PreAuthorize("hasAnyAuthority('app_admin', 'wxmp_template_msg:all', 'wxmp_template_msg:modify_wxmp_template_msg_status')")
	@CairoContext
	public Optional<String> modifyWxmpTemplateMsgStatus(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
														@Validated @RequestBody ModifyWxmpTemplateMsgStatusArgs args) {
		String appId = CairoContextHolder.getValue(CairoContextConstants.APP_ID).orElseThrow(() -> new ParamsErrorBusinessException("appId不能为空"));
		wxmpTemplateMsgCairoWebManageApiService.modifyWxmpTemplateMsgStatus(appId, args);
		return Optional.empty();
	}

	/**
	 * 删除微信模板参数
	 *
	 * @param principal principal
	 * @param args      args
	 * @return empty
	 */
	@PostMapping("/delete_wxmp_template_msg")
	@PreAuthorize("hasAnyAuthority('app_admin', 'wxmp_template_msg:all', 'wxmp_template_msg:delete_wxmp_template_msg')")
	@CairoContext
	public Optional<String> deleteWxmpTemplateMsg(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
												  @Validated @RequestBody DeleteWxmpTemplateMsgArgs args) {
		String appId = CairoContextHolder.getValue(CairoContextConstants.APP_ID).orElseThrow(() -> new ParamsErrorBusinessException("appId不能为空"));
		wxmpTemplateMsgCairoWebManageApiService.deleteWxmpTemplateMsg(appId, args);
		return Optional.empty();
	}

	/**
	 * 获取微信模板列表
	 *
	 * @param principal principal
	 * @param args      args
	 * @return 微信模板列表
	 */
	@PostMapping("/get_wxmp_template_msg_list")
	@PreAuthorize("hasAnyAuthority('app_admin', 'wxmp_template_msg:all', 'wxmp_template_msg:read')")
	@CairoContext
	public List<MetadataWxmpTemplateMsg> getWxmpTemplateMsgList(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
																@Validated @RequestBody GetWxmpTemplateMsgArgs args) {
		String appId = CairoContextHolder.getValue(CairoContextConstants.APP_ID).orElseThrow(() -> new ParamsErrorBusinessException("appId不能为空"));
		return wxmpTemplateMsgCairoWebManageApiService.getWxmpTemplateMsgList(appId, args);
	}

	/**
	 * 获取微信模板分页列表
	 *
	 * @param principal principal
	 * @param args      args
	 * @return 微信模板分页列表
	 */
	@PostMapping("/get_wxmp_template_msg_page_list")
	@PreAuthorize("hasAnyAuthority('app_admin', 'wxmp_template_msg:all', 'wxmp_template_msg:read')")
	@CairoContext
	public Page<MetadataWxmpTemplateMsg> getWxmpTemplateMsgPageList(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
																	@Validated @RequestBody GetWxmpTemplateMsgArgs args) {
		String appId = CairoContextHolder.getValue(CairoContextConstants.APP_ID).orElseThrow(() -> new ParamsErrorBusinessException("appId不能为空"));
		return wxmpTemplateMsgCairoWebManageApiService.getWxmpTemplateMsgPageList(appId, args);
	}

	/**
	 * 获取微信模板信息
	 *
	 * @param principal principal
	 * @param args      args
	 * @return 微信模板
	 */
	@PostMapping("/get_wxmp_template_msg_info")
	@PreAuthorize("hasAnyAuthority('app_admin', 'wxmp_template_msg:all', 'wxmp_template_msg:read')")
	@CairoContext
	public Optional<MetadataWxmpTemplateMsg> getWxmpTemplateMsgInfo(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
																	@Validated @RequestBody GetWxmpTemplateMsgInfoArgs args) {
		String appId = CairoContextHolder.getValue(CairoContextConstants.APP_ID).orElseThrow(() -> new ParamsErrorBusinessException("appId不能为空"));
		return Optional.ofNullable(wxmpTemplateMsgCairoWebManageApiService.getWxmpTemplateMsgInfo(appId, args.getBizId()));
	}

	/**
	 * 获取微信模板详情
	 *
	 * @param principal principal
	 * @param args      args
	 * @return 微信模板详情
	 */
	@PostMapping("/get_wxmp_template_msg_detail_info")
	@PreAuthorize("hasAnyAuthority('app_admin', 'wxmp_template_msg:all', 'wxmp_template_msg:read')")
	@CairoContext
	public Optional<MetadataWxmpTemplateMsg> getWxmpTemplateMsgDetailInfo(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
																		  @Validated @RequestBody GetWxmpTemplateMsgInfoArgs args) {
		String appId = CairoContextHolder.getValue(CairoContextConstants.APP_ID).orElseThrow(() -> new ParamsErrorBusinessException("appId不能为空"));
		return Optional.ofNullable(wxmpTemplateMsgCairoWebManageApiService.getWxmpTemplateMsgDetailInfo(appId, args.getBizId()));
	}
}
