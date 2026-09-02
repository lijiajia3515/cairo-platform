package io.github.lijiajia3515.cairo.auth.modules.app_user;

import io.github.lijiajia3515.cairo.auth.CairoAuthExtensionConstants;
import io.github.lijiajia3515.cairo.auth.domain.api.client.app_user.GetAppUserClientArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_user.AppUser;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_user.AppUserExtension;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AppUserMetadataMongodb;
import io.github.lijiajia3515.cairo.auth.framework.sdk.CairoOAuthClientSdkService;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import org.springframework.http.ResponseEntity;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 用户客户端工具类
 */
public class CairoAppUserClientTool {

	protected final AppUserClientApiFeignClient appUserClientApiFeignClient;
	private final CairoOAuthClientSdkService cairoOAuthClientSdkService;

	public CairoAppUserClientTool(AppUserClientApiFeignClient cairoAuthRequestFeignClient, CairoOAuthClientSdkService cairoOAuthClientSdkService) {
		this.appUserClientApiFeignClient = cairoAuthRequestFeignClient;
		this.cairoOAuthClientSdkService = cairoOAuthClientSdkService;
	}

	/**
	 * 获取基础用户map，根据user metadata mongodb
	 *
	 * @param appId    应用ID
	 * @param userIds  用户ID
	 * @return map类型用户对象
	 */
	public Map<String, AppUser> getBasicUserByUserIds(String appId, Collection<String> userIds) {
		return Optional.ofNullable(userIds)
			.filter(x -> !x.isEmpty())
			.map(x -> {
				ResponseEntity<BusinessResult<List<AppUser>>> userListResp = appUserClientApiFeignClient.getAppUserList(
					cairoOAuthClientSdkService.getHeaderAuthorization(),
					GetAppUserClientArgs.builder()
					.appId(appId)
					.userIds(x)
					.extension(Collections.singletonMap(CairoAuthExtensionConstants.USER, AppUserExtension.BASIC.name()))
					.build());
				return Optional.ofNullable(userListResp.getBody())
					.map(BusinessResult::getData).orElse(Collections.emptyList())
					.stream()
					.collect(Collectors.toMap(AppUser::getUserId, g -> g));
			}).orElse(Collections.emptyMap());
	}

	/**
	 * 获取基础用户map，根据user metadata mongodb
	 *
	 * @param appId    应用ID
	 * @param metadata metadata
	 * @return map类型用户对象
	 */
	public Map<String, AppUser> getBasicUserMapByAppUserMetadata(String appId, Collection<AppUserMetadataMongodb> metadata) {
		return Optional.of(CairoAppUserTool.getAppUserMetadataUserIds(metadata))
			.filter(x -> !x.isEmpty())
			.map(x -> {
				ResponseEntity<BusinessResult<List<AppUser>>> userListResp = appUserClientApiFeignClient.getAppUserList(
					cairoOAuthClientSdkService.getHeaderAuthorization(),
					GetAppUserClientArgs.builder()
						.appId(appId)
						.userIds(x)
						.extension(Collections.singletonMap(CairoAuthExtensionConstants.USER, AppUserExtension.BASIC.name()))
						.build()
				);
				return Optional.ofNullable(userListResp.getBody())
					.map(BusinessResult::getData).orElse(Collections.emptyList())
					.stream()
					.collect(Collectors.toMap(AppUser::getUserId, g -> g));
			})
			.orElse(Collections.emptyMap());
	}

	/**
	 * 获取用户map，根据user metadata mongodb
	 *
	 * @param appId        应用ID
	 * @param metadata     metadata
	 * @param extensionMap ext插件
	 * @return map类型用户对象
	 */
	public Map<String, AppUser> getUserMapByTenantAppUserMetadata(String appId, Collection<AppUserMetadataMongodb> metadata, Map<String, String> extensionMap) {
		return Optional.of(CairoAppUserTool.getAppUserMetadataUserIds(metadata))
			.filter(x -> !x.isEmpty())
			.map(x -> {
				ResponseEntity<BusinessResult<List<AppUser>>> userListResp = appUserClientApiFeignClient.getAppUserList(
					cairoOAuthClientSdkService.getHeaderAuthorization(),
					GetAppUserClientArgs.builder()
					.appId(appId)
					.userIds(x)
					.extension(extensionMap)
					.build());
				return Optional.ofNullable(userListResp.getBody())
					.map(BusinessResult::getData).orElse(Collections.emptyList())
					.stream()
					.collect(Collectors.toMap(AppUser::getUserId, g -> g));
			}).orElse(Collections.emptyMap());
	}
}
