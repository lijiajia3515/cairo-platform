package io.github.lijiajia3515.cairo.auth.modules.tenant_app_user.tenant_endpoint;

import io.github.lijiajia3515.cairo.auth.CairoAuthExtensionConstants;
import io.github.lijiajia3515.cairo.auth.domain.api.client.tenant_app_user.GetTenantAppUserArgs;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantAppUserMetadataMongodb;
import io.github.lijiajia3515.cairo.auth.framework.sdk.CairoOAuthClientSdkService;
import io.github.lijiajia3515.cairo.auth.modules.tenant_app_user.CairoTenantAppUserTool;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_user.TenantAppUser;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_user.TenantAppUserExtension;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import org.springframework.http.ResponseEntity;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 企业用户客户端工具类
 */
public class CairoTenantAppUserRequestTool {

	protected final TenantAppUserTenantAppUserApiRequestFeignClient client;
	private final CairoOAuthClientSdkService cairoOAuthClientSdkService;

	public CairoTenantAppUserRequestTool(TenantAppUserTenantAppUserApiRequestFeignClient client, CairoOAuthClientSdkService cairoOAuthClientSdkService) {
		this.client = client;
		this.cairoOAuthClientSdkService = cairoOAuthClientSdkService;
	}

	/**
	 * 获取基础用户map，根据user metadata mongodb
	 *
	 * @param tenantId 租户ID
	 * @param appId    应用ID
	 * @param userIds  用户ID
	 * @return map类型用户对象
	 */
	public Map<String, TenantAppUser> getBasicUserByUserIds(String tenantId, String appId, Collection<String> userIds) {
		return Optional.ofNullable(userIds)
			.filter(x -> !x.isEmpty())
			.map(x -> {
				ResponseEntity<BusinessResult<List<TenantAppUser>>> userListResp = client.getTenantAppUserList(
					cairoOAuthClientSdkService.getHeaderAuthorization(),
					GetTenantAppUserArgs.builder()
					.tenantId(tenantId)
					.appId(appId)
					.userIds(x)
					.extension(Collections.singletonMap(CairoAuthExtensionConstants.USER, TenantAppUserExtension.BASIC.name()))
					.build());
				return Optional.ofNullable(userListResp.getBody())
					.map(BusinessResult::getData).orElseThrow()
					.stream()
					.collect(Collectors.toMap(TenantAppUser::getUserId, g -> g));
			}).orElse(Collections.emptyMap());
	}

	/**
	 * 获取基础企业用户map，根据tenant metadata mongodb
	 *
	 * @param tenantId 租户ID
	 * @param appId    应用ID
	 * @param metadata metadata
	 * @return map类型用户对象
	 */
	public Map<String, TenantAppUser> getBasicUserMapByTenantAppUserMetadata(String tenantId, String appId, Collection<TenantAppUserMetadataMongodb> metadata) {
		return Optional.of(CairoTenantAppUserTool.getTenantAppUserMetadataUserIds(metadata))
			.filter(x -> !x.isEmpty())
			.map(x -> {
				ResponseEntity<BusinessResult<List<TenantAppUser>>> userListResp = client.getTenantAppUserList(
					cairoOAuthClientSdkService.getHeaderAuthorization(),
					GetTenantAppUserArgs.builder()
						.tenantId(tenantId)
						.appId(appId)
						.userIds(x)
						.extension(Collections.singletonMap(CairoAuthExtensionConstants.USER, TenantAppUserExtension.BASIC.name()))
						.build()
				);
				return Optional.ofNullable(userListResp.getBody())
					.map(BusinessResult::getData).orElseThrow()
					.stream()
					.collect(Collectors.toMap(TenantAppUser::getUserId, g -> g));
			})
			.orElse(Collections.emptyMap());
	}

	/**
	 * 获取企业用户map，根据user metadata mongodb
	 *
	 * @param tenantId     租户ID
	 * @param appId        应用ID
	 * @param metadata     metadata
	 * @param extensionMap ext插件
	 * @return map类型用户对象
	 */
	public Map<String, TenantAppUser> getUserMapByTenantAppUserMetadata(String tenantId, String appId, Collection<TenantAppUserMetadataMongodb> metadata, Map<String, String> extensionMap) {
		return Optional.of(CairoTenantAppUserTool.getTenantAppUserMetadataUserIds(metadata))
			.filter(x -> !x.isEmpty())
			.map(x -> {
				ResponseEntity<BusinessResult<List<TenantAppUser>>> userListResp = client.getTenantAppUserList(
					cairoOAuthClientSdkService.getHeaderAuthorization(),
					GetTenantAppUserArgs.builder()
					.tenantId(tenantId)
					.appId(appId)
					.userIds(x)
					.extension(extensionMap)
					.build());
				return Optional.ofNullable(userListResp.getBody())
					.map(BusinessResult::getData).orElseThrow()
					.stream()
					.collect(Collectors.toMap(TenantAppUser::getUserId, g -> g));
			}).orElse(Collections.emptyMap());
	}



}
