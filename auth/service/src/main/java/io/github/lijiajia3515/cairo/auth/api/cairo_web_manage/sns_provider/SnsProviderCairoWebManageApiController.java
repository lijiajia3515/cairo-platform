package io.github.lijiajia3515.cairo.auth.api.cairo_web_manage.sns_provider;

import io.github.lijiajia3515.cairo.auth.framework.context.CairoContext;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurity;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityType;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.sns_partner.GetProviderPartnerArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.sns_provider.MetadataSnsProvider;
import io.github.lijiajia3515.cairo.auth.domain.dto.sns_provider.ProviderPartner;
import io.github.lijiajia3515.cairo.auth.domain.dto.sns_provider.ProviderType;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.sns_provider.CreateSnsProviderArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.sns_provider.DeleteSnsProviderArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.sns_provider.GetProviderTypeArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.sns_provider.GetSnsProviderArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.sns_provider.ModifySnsProviderInfoArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.sns_provider.ModifySnsProviderStatusArgs;
import io.github.lijiajia3515.cairo.core.page.Page;
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
 * [cairo_web_manage/api] sns provider controller
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/cairo_web_manage_api/sns_provider")
@CairoSecurity(type = CairoSecurityType.CAIRO_WEB_MANAGE_USER)
@RequiredArgsConstructor
@BusinessResultBody
public class SnsProviderCairoWebManageApiController {

	private final SnsProviderCairoWebManageApiService snsProviderCairoWebManageApiService;

	/**
	 * 获取第三方认证提供方集合
	 * @param args      参数
	 * @return snsProvider list
	 */
	@PostMapping("/get_sns_provider_list")
	@PreAuthorize("hasAnyAuthority('app_admin', 'sns_provider:all', 'sns_provider:read')")
	@CairoContext
	public List<MetadataSnsProvider> getSnsProviderList(@Validated @RequestBody(required = false) GetSnsProviderArgs args) {
		if (args == null) {
			args = new GetSnsProviderArgs();
		}
		return snsProviderCairoWebManageApiService.getSnsProviderList(args);
	}

	/**
	 * 获取第三方认证提供方分页集合
	 * @param args      参数
	 * @return snsProvider page list
	 */
	@PostMapping("/get_sns_provider_page_list")
	@PreAuthorize("hasAnyAuthority('app_admin', 'sns_provider:all', 'sns_provider:read')")
	@CairoContext
	public Page<MetadataSnsProvider> getSnsProviderPageList(@Validated @RequestBody(required = false) GetSnsProviderArgs args) {
		if (args == null) {
			args = new GetSnsProviderArgs();
		}
		return snsProviderCairoWebManageApiService.getSnsProviderPageList(args);
	}

	/**
	 * 创建第三方认证提供方
	 * @param args      参数
	 * @return empty
	 */
	@PostMapping("/create_sns_provider")
	@PreAuthorize("hasAnyAuthority('app_admin', 'sns_provider:all', 'sns_provider:create_sns_provider')")
	@CairoContext
	public Optional<String> createSnsProvider(@Validated @RequestBody CreateSnsProviderArgs args) {
		snsProviderCairoWebManageApiService.createSnsProvider(args);

		return Optional.empty();
	}

	/**
	 * 修改第三方认证提供方信息
	 * @param args      参数
	 * @return empty
	 */
	@PostMapping("/modify_sns_provider")
	@PreAuthorize("hasAnyAuthority('app_admin', 'sns_provider:all', 'sns_provider:modify_sns_provider')")
	@CairoContext
	public Optional<Object> modifySnsProvider(@Validated @RequestBody ModifySnsProviderInfoArgs args) {
		snsProviderCairoWebManageApiService.modifySnsProvider(args);
		return Optional.empty();
	}


	/**
	 * 修改第三方认证提供方状态
	 * @param args      参数
	 * @return empty
	 */
	@PostMapping("/modify_sns_provider_status")
	@PreAuthorize("hasAnyAuthority('app_admin', 'sns_provider:all', 'sns_provider:modify_sns_provider_status')")
	@CairoContext
	public Optional<Object> modifySnsProviderStatus(@Validated @RequestBody ModifySnsProviderStatusArgs args) {
		snsProviderCairoWebManageApiService.modifySnsProviderStatus(args);
		return Optional.empty();
	}

	/**
	 * 删除第三方认证提供方
	 * @param args      参数
	 * @return empty
	 */
	@PostMapping("/delete_sns_provider")
	@PreAuthorize("hasAnyAuthority('app_admin', 'sns_provider:all', 'sns_provider:delete_sns_provider')")
	@CairoContext
	public Optional<Object> deleteSnsProvider(@Validated @RequestBody DeleteSnsProviderArgs args) {
		snsProviderCairoWebManageApiService.deleteSnsProvider(args);
		return Optional.empty();
	}

	/**
	 * 获取第三方认证提供方类型集合
	 * @param args      参数
	 * @return snsProvider list
	 */
	@PostMapping("/get_provider_type_list")
	@PreAuthorize("hasAnyAuthority('app_admin', 'sns_provider:all', 'sns_provider:read')")
	@CairoContext
	public List<ProviderType> getproviderTypeList(@Validated @RequestBody(required = false) GetProviderTypeArgs args) {
		if (args == null) {
			args = new GetProviderTypeArgs();
		}
		return snsProviderCairoWebManageApiService.getproviderTypeList(args);
	}

	/**
	 * 获取第三方认证提供方厂商集合
	 * @param args      参数
	 * @return snsProvider list
	 */
	@PostMapping("/get_provider_partner_list")
	@PreAuthorize("hasAnyAuthority('app_admin', 'sns_provider:all', 'sns_provider:read')")
	@CairoContext
	public List<ProviderPartner> getProviderPartnerList(@Validated @RequestBody(required = false) GetProviderPartnerArgs args) {
		if (args == null) {
			args = new GetProviderPartnerArgs();
		}
		return snsProviderCairoWebManageApiService.getProviderPartnerList(args);
	}
}
