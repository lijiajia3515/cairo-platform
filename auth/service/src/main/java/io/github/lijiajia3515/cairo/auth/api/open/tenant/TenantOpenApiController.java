package io.github.lijiajia3515.cairo.auth.api.open.tenant;

import io.github.lijiajia3515.cairo.auth.domain.api.open.tenant.GetTenantByTenantAliasNameArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.open.tenant.GetTenantByTenantNameArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.open.tenant.OpenTenant;
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
 * [open/api] tenant controller
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/open_api/tenant")
@RequiredArgsConstructor
@BusinessResultBody
public class TenantOpenApiController {
	private final TenantOpenApiService tenantOpenApiService;

	/**
	 * 通过企业名称查询tenant
	 *
	 * @param args args
	 * @return account
	 */
	@PostMapping("/get_tenant_by_tenant_name")
	public Optional<OpenTenant> getTenantByTenantName(@Validated @RequestBody GetTenantByTenantNameArgs args) {
		return Optional.ofNullable(tenantOpenApiService.getTenantByTenantName(args));
	}

	/**
	 * 通过企业别名查询tenant
	 *
	 * @param args args
	 * @return account
	 */
	@PostMapping("/get_tenant_by_tenant_alias_name")
	public Optional<OpenTenant> getTenantByTenantAliasName(@Validated @RequestBody GetTenantByTenantAliasNameArgs args) {
		return Optional.ofNullable(tenantOpenApiService.getTenantByTenantAliasName(args));
	}
}
