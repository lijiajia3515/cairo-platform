package io.github.lijiajia3515.cairo.auth.autoconfigure;

// ===== account =====
import io.github.lijiajia3515.cairo.auth.modules.account.AccountClientApiFeignClient;
import io.github.lijiajia3515.cairo.auth.modules.account.AccountClientApiFeignClientFallbackFactory;
import io.github.lijiajia3515.cairo.auth.modules.account_authorization.account.AccountAuthorizationClientApiFeignClient;
import io.github.lijiajia3515.cairo.auth.modules.account_authorization.account.AccountAuthorizationClientApiFeignClientFallbackFactory;
import io.github.lijiajia3515.cairo.auth.modules.account_sns.AccountSnsClientApiFeignClient;
import io.github.lijiajia3515.cairo.auth.modules.account_sns.AccountSnsClientApiFeignClientFallbackFactory;
import io.github.lijiajia3515.cairo.auth.modules.verify_code.VerifyCodeClientApiFeignClient;
import io.github.lijiajia3515.cairo.auth.modules.verify_code.VerifyCodeClientApiFeignClientFallbackFactory;

// ===== app =====
import io.github.lijiajia3515.cairo.auth.modules.permission.PermissionClientApiFeignClient;
import io.github.lijiajia3515.cairo.auth.modules.permission.PermissionClientApiFeignClientFallbackFactory;
import io.github.lijiajia3515.cairo.auth.modules.app.AppClientApiFeignClient;
import io.github.lijiajia3515.cairo.auth.modules.app.AppClientApiFeignClientFallbackFactory;
import io.github.lijiajia3515.cairo.auth.modules.app_department.AppDepartmentClientApiFeignClient;
import io.github.lijiajia3515.cairo.auth.modules.app_department.AppDepartmentClientApiFeignClientFallbackFactory;
import io.github.lijiajia3515.cairo.auth.modules.app_doc.client.AppDocClientApiFeignClient;
import io.github.lijiajia3515.cairo.auth.modules.app_doc.client.AppDocClientApiFeignClientFallbackFactory;
import io.github.lijiajia3515.cairo.auth.modules.endpoint.EndpointClientApiFeignClient;
import io.github.lijiajia3515.cairo.auth.modules.endpoint.EndpointClientApiFeignClientFallbackFactory;
import io.github.lijiajia3515.cairo.auth.modules.app_user_authorization.AppUserAuthorizationClientApiFeignClient;
import io.github.lijiajia3515.cairo.auth.modules.app_user_authorization.AppUserAuthorizationClientApiFeignClientFallbackFactory;
import io.github.lijiajia3515.cairo.auth.modules.subapp_user_authorization.SubappUserAuthorizationClientApiFeignClient;
import io.github.lijiajia3515.cairo.auth.modules.subapp_user_authorization.SubappUserAuthorizationClientApiFeignClientFallbackFactory;
import io.github.lijiajia3515.cairo.auth.modules.app_role.AppRoleClientApiFeignClient;
import io.github.lijiajia3515.cairo.auth.modules.app_role.AppRoleClientApiFeignClientFallbackFactory;
import io.github.lijiajia3515.cairo.auth.modules.app_user.AppUserClientApiFeignClient;
import io.github.lijiajia3515.cairo.auth.modules.app_user.AppUserClientApiFeignClientFallbackFactory;
import io.github.lijiajia3515.cairo.auth.modules.client.ClientClientApiFeignClient;
import io.github.lijiajia3515.cairo.auth.modules.client.ClientClientApiFeignClientFallbackFactory;
import io.github.lijiajia3515.cairo.auth.modules.menu.MenuClientApiFeignClient;
import io.github.lijiajia3515.cairo.auth.modules.menu.MenuClientApiFeignClientFallbackFactory;
import io.github.lijiajia3515.cairo.auth.modules.subapp.SubappClientApiFeignClient;
import io.github.lijiajia3515.cairo.auth.modules.subapp.SubappClientApiFeignClientFallbackFactory;
import io.github.lijiajia3515.cairo.auth.modules.subapp_version.SubappVersionClientApiFeignClient;
import io.github.lijiajia3515.cairo.auth.modules.subapp_version.SubappVersionClientApiFeignClientFallbackFactory;
import io.github.lijiajia3515.cairo.auth.modules.tenant_app_user_template.TenantAppUserTemplateClientApiFeignClient;
import io.github.lijiajia3515.cairo.auth.modules.tenant_app_user_template.TenantAppUserTemplateClientApiFeignClientFallbackFactory;
import io.github.lijiajia3515.cairo.auth.modules.dict.sys.SysDictClientApiBasicFeignClient;
import io.github.lijiajia3515.cairo.auth.modules.dict.sys.SysDictClientApiBasicFeignClientFallbackFactory;
import io.github.lijiajia3515.cairo.auth.modules.dict.sys.SysDictClientApiFeignClient;
import io.github.lijiajia3515.cairo.auth.modules.dict.sys.SysDictClientApiFeignClientFallbackFactory;
import io.github.lijiajia3515.cairo.auth.modules.file.app_file.AppFileClientApiFeignClient;
import io.github.lijiajia3515.cairo.auth.modules.file.app_file.AppFileClientApiFeignClientFallbackFactory;
import io.github.lijiajia3515.cairo.auth.modules.file.temporary_file.TemporaryFileClientApiFeignClient;
import io.github.lijiajia3515.cairo.auth.modules.file.temporary_file.TemporaryFileClientApiFeignClientFallbackFactory;

// ===== tenant-app =====
import io.github.lijiajia3515.cairo.auth.modules.dict.biz.BizDictClientApiFeignClient;
import io.github.lijiajia3515.cairo.auth.modules.dict.biz.BizDictClientApiFeignClientFallbackFactory;
import io.github.lijiajia3515.cairo.auth.modules.file.TenantAppFileClientApiFeignClient;
import io.github.lijiajia3515.cairo.auth.modules.file.TenantAppFileClientApiFeignClientFallbackFactory;
import io.github.lijiajia3515.cairo.auth.modules.file.TenantFileClientApiFeignClient;
import io.github.lijiajia3515.cairo.auth.modules.file.TenantFileClientApiFeignClientFallbackFactory;
import io.github.lijiajia3515.cairo.auth.modules.tenant.TenantClientApiFeignClient;
import io.github.lijiajia3515.cairo.auth.modules.tenant.TenantClientApiFeignClientFallbackFactory;
import io.github.lijiajia3515.cairo.auth.modules.tenant_app.TenantAppClientApiFeignClient;
import io.github.lijiajia3515.cairo.auth.modules.tenant_app.TenantAppClientApiFeignClientFallbackFactory;
import io.github.lijiajia3515.cairo.auth.modules.tenant_app_authorization.TenantAppUserAuthorizationClientApiFeignClient;
import io.github.lijiajia3515.cairo.auth.modules.tenant_app_authorization.TenantAppUserAuthorizationClientApiFeignClientFallbackFactory;
import io.github.lijiajia3515.cairo.auth.modules.tenant_app_department.TenantAppDepartmentApiClientFeignClient;
import io.github.lijiajia3515.cairo.auth.modules.tenant_app_department.TenantAppDepartmentApiClientFeignClientFallbackFactory;
import io.github.lijiajia3515.cairo.auth.modules.tenant_app_doc.TenantAppDocClientApiFeignClient;
import io.github.lijiajia3515.cairo.auth.modules.tenant_app_doc.TenantAppDocClientApiFeignClientFallbackFactory;
import io.github.lijiajia3515.cairo.auth.modules.tenant_endpoint.TenantEndpointApiClientFeignClient;
import io.github.lijiajia3515.cairo.auth.modules.tenant_endpoint.TenantEndpointApiClientFeignClientFallbackFactory;
import io.github.lijiajia3515.cairo.auth.modules.tenant_subapp.TenantSubappApiClientFeignClient;
import io.github.lijiajia3515.cairo.auth.modules.tenant_subapp.TenantSubappApiClientFeignClientFallbackFactory;
import io.github.lijiajia3515.cairo.auth.modules.tenant_subapp_user_authorization.TenantSubappUserAuthorizationClientApiFeignClient;
import io.github.lijiajia3515.cairo.auth.modules.tenant_subapp_user_authorization.TenantSubappUserAuthorizationClientApiFeignClientFallbackFactory;
import io.github.lijiajia3515.cairo.auth.modules.tenant_app_role.TenantAppRoleApiClientFeignClient;
import io.github.lijiajia3515.cairo.auth.modules.tenant_app_role.TenantAppRoleApiClientFeignClientFallbackFactory;
import io.github.lijiajia3515.cairo.auth.modules.tenant_app_user.client.TenantAppUserClientApiFeignClient;
import io.github.lijiajia3515.cairo.auth.modules.tenant_app_user.client.TenantAppUserClientApiFeignClientFallbackFactory;
import io.github.lijiajia3515.cairo.auth.modules.tenant_app_user.tenant_endpoint.TenantAppUserTenantAppUserApiRequestFeignClient;
import io.github.lijiajia3515.cairo.auth.modules.tenant_app_user.tenant_endpoint.TenantAppUserTenantAppUserApiRequestFeignClientFallbackFactory;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * merged feign fallback configuration for sdk:client (account + app + tenant-app)
 */
@Configuration(proxyBeanMethods = false)
public class CairoAuthSdkClientFeignConfiguration {

	// ===== account =====

	@Bean
	@ConditionalOnClass(AccountClientApiFeignClient.class)
	@ConditionalOnMissingBean(AccountClientApiFeignClientFallbackFactory.class)
	AccountClientApiFeignClientFallbackFactory accountClientFeignClientFallbackFactory() {
		return new AccountClientApiFeignClientFallbackFactory();
	}

	@Bean
	@ConditionalOnClass(AccountAuthorizationClientApiFeignClient.class)
	@ConditionalOnMissingBean(AccountAuthorizationClientApiFeignClientFallbackFactory.class)
	AccountAuthorizationClientApiFeignClientFallbackFactory accountAuthorizationClientApiFeignClientFallbackFactory() {
		return new AccountAuthorizationClientApiFeignClientFallbackFactory();
	}

	@Bean
	@ConditionalOnClass(AccountSnsClientApiFeignClient.class)
	@ConditionalOnMissingBean(AccountSnsClientApiFeignClientFallbackFactory.class)
	AccountSnsClientApiFeignClientFallbackFactory accountSnsClientFeignClientFallbackFactory() {
		return new AccountSnsClientApiFeignClientFallbackFactory();
	}

	@Bean
	@ConditionalOnClass(VerifyCodeClientApiFeignClient.class)
	@ConditionalOnMissingBean(VerifyCodeClientApiFeignClientFallbackFactory.class)
	VerifyCodeClientApiFeignClientFallbackFactory verifyCodeClientFeignClientFallbackFactory() {
		return new VerifyCodeClientApiFeignClientFallbackFactory();
	}

	// ===== app =====

	@Bean
	@ConditionalOnClass(AppClientApiFeignClient.class)
	@ConditionalOnMissingBean(AppClientApiFeignClientFallbackFactory.class)
	AppClientApiFeignClientFallbackFactory appClientApiFeignClientFallbackFactory() {
		return new AppClientApiFeignClientFallbackFactory();
	}

	@Bean
	@ConditionalOnClass(EndpointClientApiFeignClient.class)
	@ConditionalOnMissingBean(EndpointClientApiFeignClientFallbackFactory.class)
	EndpointClientApiFeignClientFallbackFactory endpointClientApiFeignClientFallbackFactory() {
		return new EndpointClientApiFeignClientFallbackFactory();
	}

	@Bean
	@ConditionalOnClass(MenuClientApiFeignClient.class)
	@ConditionalOnMissingBean(MenuClientApiFeignClientFallbackFactory.class)
	MenuClientApiFeignClientFallbackFactory menuClientApiFeignClientFallbackFactory() {
		return new MenuClientApiFeignClientFallbackFactory();
	}

	@Bean
	@ConditionalOnClass(PermissionClientApiFeignClient.class)
	@ConditionalOnMissingBean(PermissionClientApiFeignClientFallbackFactory.class)
	PermissionClientApiFeignClientFallbackFactory permissionClientApiFeignClientFallbackFactory() {
		return new PermissionClientApiFeignClientFallbackFactory();
	}

	@Bean
	@ConditionalOnClass(ClientClientApiFeignClient.class)
	@ConditionalOnMissingBean(ClientClientApiFeignClientFallbackFactory.class)
	ClientClientApiFeignClientFallbackFactory clientClientApiFeignClientFallbackFactory() {
		return new ClientClientApiFeignClientFallbackFactory();
	}

	@Bean
	@ConditionalOnClass(AppUserClientApiFeignClient.class)
	@ConditionalOnMissingBean(AppUserClientApiFeignClientFallbackFactory.class)
	AppUserClientApiFeignClientFallbackFactory appUserClientApiFeignClientFallbackFactory() {
		return new AppUserClientApiFeignClientFallbackFactory();
	}

	@Bean
	@ConditionalOnClass(AppRoleClientApiFeignClient.class)
	@ConditionalOnMissingBean(AppRoleClientApiFeignClientFallbackFactory.class)
	AppRoleClientApiFeignClientFallbackFactory appRoleClientFeignClientFallbackFactory() {
		return new AppRoleClientApiFeignClientFallbackFactory();
	}

	@Bean
	@ConditionalOnClass(AppDepartmentClientApiFeignClient.class)
	@ConditionalOnMissingBean(AppDepartmentClientApiFeignClientFallbackFactory.class)
	AppDepartmentClientApiFeignClientFallbackFactory appDepartmentClientApiFeignClientFallbackFactory() {
		return new AppDepartmentClientApiFeignClientFallbackFactory();
	}


	@Bean
	@ConditionalOnClass(AppUserAuthorizationClientApiFeignClient.class)
	@ConditionalOnMissingBean(AppUserAuthorizationClientApiFeignClientFallbackFactory.class)
	AppUserAuthorizationClientApiFeignClientFallbackFactory appUserAuthorizationClientApiFeignClientFallbackFactory() {
		return new AppUserAuthorizationClientApiFeignClientFallbackFactory();
	}

	@Bean
	@ConditionalOnClass(SubappUserAuthorizationClientApiFeignClient.class)
	@ConditionalOnMissingBean(SubappUserAuthorizationClientApiFeignClientFallbackFactory.class)
	SubappUserAuthorizationClientApiFeignClientFallbackFactory subappUserAuthorizationClientApiFeignClientFallbackFactory() {
		return new SubappUserAuthorizationClientApiFeignClientFallbackFactory();
	}

	@Bean
	@ConditionalOnClass(SubappClientApiFeignClient.class)
	@ConditionalOnMissingBean(SubappClientApiFeignClientFallbackFactory.class)
	SubappClientApiFeignClientFallbackFactory subappClientApiFeignClientFallbackFactory() {
		return new SubappClientApiFeignClientFallbackFactory();
	}

	@Bean
	@ConditionalOnClass(SubappVersionClientApiFeignClient.class)
	@ConditionalOnMissingBean(SubappVersionClientApiFeignClientFallbackFactory.class)
	SubappVersionClientApiFeignClientFallbackFactory subappVersionClientApiFeignClientFallbackFactory() {
		return new SubappVersionClientApiFeignClientFallbackFactory();
	}

	@Bean
	@ConditionalOnClass(TenantAppUserTemplateClientApiFeignClient.class)
	@ConditionalOnMissingBean(TenantAppUserTemplateClientApiFeignClientFallbackFactory.class)
	TenantAppUserTemplateClientApiFeignClientFallbackFactory tenantAppUserTemplateClientApiFeignClientFallbackFactory() {
		return new TenantAppUserTemplateClientApiFeignClientFallbackFactory();
	}

	@Bean
	@ConditionalOnClass(AppFileClientApiFeignClient.class)
	@ConditionalOnMissingBean(AppFileClientApiFeignClientFallbackFactory.class)
	AppFileClientApiFeignClientFallbackFactory appFileClientApiFeignClientFallbackFactory() {
		return new AppFileClientApiFeignClientFallbackFactory();
	}

	@Bean
	@ConditionalOnClass(AppDocClientApiFeignClient.class)
	@ConditionalOnMissingBean(AppDocClientApiFeignClientFallbackFactory.class)
	AppDocClientApiFeignClientFallbackFactory appDocClientApiFeignClientFallbackFactory() {
		return new AppDocClientApiFeignClientFallbackFactory();
	}

	@Bean
	@ConditionalOnClass(SysDictClientApiFeignClient.class)
	@ConditionalOnMissingBean(SysDictClientApiFeignClientFallbackFactory.class)
	SysDictClientApiFeignClientFallbackFactory sysDictClientApiFeignClientFallbackFactory() {
		return new SysDictClientApiFeignClientFallbackFactory();
	}

	@Bean
	@ConditionalOnClass(SysDictClientApiBasicFeignClient.class)
	@ConditionalOnMissingBean(SysDictClientApiBasicFeignClientFallbackFactory.class)
	SysDictClientApiBasicFeignClientFallbackFactory sysDictClientApiBasicFeignClientFallbackFactory() {
		return new SysDictClientApiBasicFeignClientFallbackFactory();
	}

	@Bean
	@ConditionalOnClass(TemporaryFileClientApiFeignClient.class)
	@ConditionalOnMissingBean(TemporaryFileClientApiFeignClientFallbackFactory.class)
	TemporaryFileClientApiFeignClientFallbackFactory temporaryFileClientApiFeignClientFallbackFactory() {
		return new TemporaryFileClientApiFeignClientFallbackFactory();
	}

	// ===== tenant-app =====

	@Bean
	@ConditionalOnClass(TenantClientApiFeignClient.class)
	@ConditionalOnMissingBean(TenantClientApiFeignClientFallbackFactory.class)
	TenantClientApiFeignClientFallbackFactory tenantClientFeignClientFallbackFactory() {
		return new TenantClientApiFeignClientFallbackFactory();
	}

	@Bean
	@ConditionalOnClass(TenantAppClientApiFeignClient.class)
	@ConditionalOnMissingBean(TenantAppClientApiFeignClientFallbackFactory.class)
	TenantAppClientApiFeignClientFallbackFactory tenantAppClientApiFeignClientFallbackFactory() {
		return new TenantAppClientApiFeignClientFallbackFactory();
	}

	@Bean
	@ConditionalOnClass(TenantAppUserAuthorizationClientApiFeignClient.class)
	@ConditionalOnMissingBean(TenantAppUserAuthorizationClientApiFeignClientFallbackFactory.class)
	TenantAppUserAuthorizationClientApiFeignClientFallbackFactory tenantAppUserAuthorizationClientApiFeignClientFallbackFactory() {
		return new TenantAppUserAuthorizationClientApiFeignClientFallbackFactory();
	}

	@Bean
	@ConditionalOnClass(TenantSubappUserAuthorizationClientApiFeignClient.class)
	@ConditionalOnMissingBean(TenantSubappUserAuthorizationClientApiFeignClientFallbackFactory.class)
	TenantSubappUserAuthorizationClientApiFeignClientFallbackFactory tenantSubappUserAuthorizationClientApiFeignClientFallbackFactory() {
		return new TenantSubappUserAuthorizationClientApiFeignClientFallbackFactory();
	}

	@Bean
	@ConditionalOnClass(TenantAppUserClientApiFeignClient.class)
	@ConditionalOnMissingBean(TenantAppUserClientApiFeignClientFallbackFactory.class)
	TenantAppUserClientApiFeignClientFallbackFactory tenantAppUserClientApiFeignClientFallbackFactory() {
		return new TenantAppUserClientApiFeignClientFallbackFactory();
	}

	@Bean
	@ConditionalOnClass(TenantAppRoleApiClientFeignClient.class)
	@ConditionalOnMissingBean(TenantAppRoleApiClientFeignClientFallbackFactory.class)
	TenantAppRoleApiClientFeignClientFallbackFactory tenantAppRoleApiClientFeignClientFallbackFactory() {
		return new TenantAppRoleApiClientFeignClientFallbackFactory();
	}

	@Bean
	@ConditionalOnClass(TenantAppDepartmentApiClientFeignClient.class)
	@ConditionalOnMissingBean(TenantAppDepartmentApiClientFeignClientFallbackFactory.class)
	TenantAppDepartmentApiClientFeignClientFallbackFactory tenantAppDepartmentApiClientFeignClientFallbackFactory() {
		return new TenantAppDepartmentApiClientFeignClientFallbackFactory();
	}

	@Bean
	@ConditionalOnClass(TenantAppUserTenantAppUserApiRequestFeignClient.class)
	@ConditionalOnMissingBean(TenantAppUserTenantAppUserApiRequestFeignClientFallbackFactory.class)
	TenantAppUserTenantAppUserApiRequestFeignClientFallbackFactory tenantAppUserTenantAppUserApiRequestFeignClientFallbackFactory() {
		return new TenantAppUserTenantAppUserApiRequestFeignClientFallbackFactory();
	}

	@Bean
	@ConditionalOnClass(TenantEndpointApiClientFeignClient.class)
	@ConditionalOnMissingBean(TenantEndpointApiClientFeignClientFallbackFactory.class)
	TenantEndpointApiClientFeignClientFallbackFactory tenantEndpointApiClientFeignClientFallbackFactory() {
		return new TenantEndpointApiClientFeignClientFallbackFactory();
	}

	@Bean
	@ConditionalOnClass(TenantSubappApiClientFeignClient.class)
	@ConditionalOnMissingBean(TenantSubappApiClientFeignClientFallbackFactory.class)
	TenantSubappApiClientFeignClientFallbackFactory tenantSubappApiClientFeignClientFallbackFactory() {
		return new TenantSubappApiClientFeignClientFallbackFactory();
	}

	@Bean
	@ConditionalOnClass(TenantFileClientApiFeignClient.class)
	@ConditionalOnMissingBean(TenantFileClientApiFeignClientFallbackFactory.class)
	TenantFileClientApiFeignClientFallbackFactory tenantFileClientApiFeignClientFallbackFactory() {
		return new TenantFileClientApiFeignClientFallbackFactory();
	}

	@Bean
	@ConditionalOnClass(TenantAppFileClientApiFeignClient.class)
	@ConditionalOnMissingBean(TenantAppFileClientApiFeignClientFallbackFactory.class)
	TenantAppFileClientApiFeignClientFallbackFactory tenantAppFileClientApiFeignClientFallbackFactory() {
		return new TenantAppFileClientApiFeignClientFallbackFactory();
	}

	@Bean
	@ConditionalOnClass(TenantAppDocClientApiFeignClient.class)
	@ConditionalOnMissingBean(TenantAppDocClientApiFeignClientFallbackFactory.class)
	TenantAppDocClientApiFeignClientFallbackFactory tenantAppDocClientApiFeignClientFallbackFactory() {
		return new TenantAppDocClientApiFeignClientFallbackFactory();
	}

	@Bean
	@ConditionalOnClass(BizDictClientApiFeignClient.class)
	@ConditionalOnMissingBean(BizDictClientApiFeignClientFallbackFactory.class)
	BizDictClientApiFeignClientFallbackFactory bizDictClientApiFeignClientFallbackFactory() {
		return new BizDictClientApiFeignClientFallbackFactory();
	}
}
