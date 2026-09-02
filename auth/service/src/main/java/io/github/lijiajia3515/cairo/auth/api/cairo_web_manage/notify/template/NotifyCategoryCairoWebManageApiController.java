package io.github.lijiajia3515.cairo.auth.api.cairo_web_manage.notify.template;

import io.github.lijiajia3515.cairo.auth.framework.context.CairoContext;
import io.github.lijiajia3515.cairo.auth.framework.context.CairoContextConstants;
import io.github.lijiajia3515.cairo.auth.framework.context.CairoContextHolder;
import io.github.lijiajia3515.cairo.core.exception.ParamsErrorBusinessException;
import io.github.lijiajia3515.cairo.core.page.Page;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.notify.category.CreateNotifyCategoryArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.notify.category.DeleteNotifyCategoryArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.notify.category.GetNotifyCategoryArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.notify.category.ModifyNotifyCategoryInfoArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.notify.category.ModifyNotifyCategoryStatusArgs;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurity;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityType;
import io.github.lijiajia3515.cairo.auth.domain.dto.notify.category.MetadataNotifyCategory;
import io.github.lijiajia3515.cairo.web.bind.annotation.BusinessResultBody;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

/**
 * [cairo_web_manage/api] system dict api
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/cairo_web_manage_api/notify_category")
@CairoSecurity(type = CairoSecurityType.CAIRO_WEB_MANAGE_USER)
@BusinessResultBody
@RequiredArgsConstructor
public class NotifyCategoryCairoWebManageApiController {

	private final NotifyCategoryCairoWebManageApiService notifyCategoryCairoWebManageApiService;

	/**
	 * 获取通知消息类型列表
	 *
	 * @param args args
	 * @return 短信模板分页列表
	 */
	@PostMapping("/get_notify_category_list")
	@PreAuthorize("hasAnyAuthority('app_admin', 'notify_category:all', 'notify_category:read')")
	@CairoContext
	public List<MetadataNotifyCategory> getNotifyCategoryList(@Validated @RequestBody GetNotifyCategoryArgs args) {
		String appId = CairoContextHolder.getValue(CairoContextConstants.APP_ID).orElseThrow(() -> new ParamsErrorBusinessException("appId不能为空"));
		return notifyCategoryCairoWebManageApiService.getNotifyCategoryList(appId, args);
	}

	/**
	 * 获取通知消息类型分页列表
	 *
	 * @param args args
	 * @return 短信模板分页列表
	 */
	@PostMapping("/get_notify_category_page_list")
	@PreAuthorize("hasAnyAuthority('app_admin', 'notify_category:all', 'notify_category:read')")
	@CairoContext
	public Page<MetadataNotifyCategory> getNotifyCategoryPageList(@Validated @RequestBody GetNotifyCategoryArgs args) {
		String appId = CairoContextHolder.getValue(CairoContextConstants.APP_ID).orElseThrow(() -> new ParamsErrorBusinessException("appId不能为空"));
		return notifyCategoryCairoWebManageApiService.getNotifyCategoryPageList(appId, args);
	}

	/**
	 * 创建通知消息类型
	 *
	 * @param args args
	 * @return 短信模板分页列表
	 */
	@PostMapping("/create_notify_category")
	@PreAuthorize("hasAnyAuthority('app_admin', 'notify_category:all', 'notify_category:create')")
	@CairoContext
	public Optional<String> createNotifyCategory(@Validated @RequestBody CreateNotifyCategoryArgs args) {
		String appId = CairoContextHolder.getValue(CairoContextConstants.APP_ID).orElseThrow(() -> new ParamsErrorBusinessException("appId不能为空"));
		notifyCategoryCairoWebManageApiService.createNotifyCategory(appId, args);
		return Optional.empty();
	}

	/**
	 * 修改通知消息类型信息
	 *
	 * @param args args
	 * @return 短信模板分页列表
	 */
	@PostMapping("/modify_notify_category_info")
	@PreAuthorize("hasAnyAuthority('app_admin', 'notify_category:all', 'notify_category:modify_info')")
	@CairoContext
	public Optional<String> modifyNotifyCategoryInfo(@Validated @RequestBody ModifyNotifyCategoryInfoArgs args) {
		String appId = CairoContextHolder.getValue(CairoContextConstants.APP_ID).orElseThrow(() -> new ParamsErrorBusinessException("appId不能为空"));
		notifyCategoryCairoWebManageApiService.modifyNotifyCategoryInfo(appId, args);
		return Optional.empty();
	}

	/**
	 * 修改通知消息类型状态
	 *
	 * @param args args
	 * @return 短信模板分页列表
	 */
	@PostMapping("/modify_notify_category_status")
	@PreAuthorize("hasAnyAuthority('app_admin', 'notify_category:all', 'notify_category:modify_status')")
	@CairoContext
	public Optional<String> modifyNotifyCategoryStatus(@Validated @RequestBody ModifyNotifyCategoryStatusArgs args) {
		String appId = CairoContextHolder.getValue(CairoContextConstants.APP_ID).orElseThrow(() -> new ParamsErrorBusinessException("appId不能为空"));
		notifyCategoryCairoWebManageApiService.modifyNotifyCategoryStatus(appId, args);
		return Optional.empty();
	}

	/**
	 * 删除通知消息类型
	 *
	 * @param args args
	 * @return 短信模板分页列表
	 */
	@PostMapping("/delete_notify_category")
	@PreAuthorize("hasAnyAuthority('app_admin', 'notify_category:all', 'notify_category:delete')")
	@CairoContext
	public Optional<String> deleteNotifyCategory(@Validated @RequestBody DeleteNotifyCategoryArgs args) {
		String appId = CairoContextHolder.getValue(CairoContextConstants.APP_ID).orElseThrow(() -> new ParamsErrorBusinessException("appId不能为空"));
		notifyCategoryCairoWebManageApiService.deleteNotifyCategory(appId, args);
		return Optional.empty();
	}


}

