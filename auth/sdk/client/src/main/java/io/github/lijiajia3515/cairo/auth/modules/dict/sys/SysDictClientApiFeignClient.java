package io.github.lijiajia3515.cairo.auth.modules.dict.sys;

import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthTenantAppUserPrincipal;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import io.github.lijiajia3515.cairo.auth.CairoAuthClientSdkCoreFeignClientConfiguration;
import io.github.lijiajia3515.cairo.auth.domain.api.client.dict.sys.GetSysDictInfoArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.dict.sys.GetSysDictSubItemArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.dict.sys.PathSysDict;
import io.github.lijiajia3515.cairo.auth.domain.dto.dict.sys.SysDict;
import io.github.lijiajia3515.cairo.auth.domain.dto.dict.sys.SysDictItem;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * client-api system dict feign client
 */
@FeignClient(
	contextId = "sysDictClientApiFeignClient",
	name = "${cairo.feign.client.cairo-auth-service:cairo-auth-service}",
	path = "/client_api/sys_dict",
	fallbackFactory = SysDictClientApiFeignClientFallbackFactory.class,
	configuration = CairoAuthClientSdkCoreFeignClientConfiguration.class
)
public interface SysDictClientApiFeignClient {

	/**
	 * 字典查询
	 *
	 * @param dictIds 字典ids
	 * @return system dict item map
	 */
	@PostMapping("/get_sys_dict_item_map")
	ResponseEntity<BusinessResult<Map<String, Map<String, SysDictItem>>>> getSysDictItemMap(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
																											  @RequestBody Set<String> dictIds);

	/**
	 * 获取系统级字典部分字典项信息
	 *
	 * @param dictItemIds args
	 * @return system dict item map
	 */
	@PostMapping("/get_sys_dict_item_id_map")
	ResponseEntity<BusinessResult<Map<String, Map<String, SysDictItem>>>> getSysDictItemIdMap(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
																												@RequestBody Map<String, Set<String>> dictItemIds);


	/**
	 * 获取系统级字典详细信息(单个)
	 *
	 * @param args args
	 * @return map 字典
	 */
	@PostMapping("/get_sys_dict_detail_info")
	ResponseEntity<BusinessResult<SysDict>> getSysDictDetailInfo(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
																				   @RequestBody GetSysDictInfoArgs args);


	/**
	 * 获多级系统级字典项部分信息map
	 *
	 * @return 字典详情
	 */
	@PostMapping("/get_path_sys_dict_item_id_map")
	ResponseEntity<BusinessResult< Map<String, Map<String, PathSysDict>>>> getPathSysDictItemIdMap(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
																													 @RequestBody Map<String, Set<String>> dictIdMap);

	/**
	 * 获取多级系统级字典项map
	 *
	 * @param dictIds 字典项ID
	 * @return system dict item map
	 */
	@PostMapping("/get_path_sys_dict_item_map")
	ResponseEntity<BusinessResult< Map<String, Map<String, PathSysDict>>>> getPathSysDictItemMap(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
																												   @RequestBody Set<String> dictIds);


	/**
	 * 获系统级字典信息
	 *
	 * @param authorization authorization
	 * @param args      args
	 * @return 字典详情
	 */
	@PostMapping("/get_sys_dict_sub_item_list")
	ResponseEntity<BusinessResult<List<SysDictItem>>> getSysDictSubItemList(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
																							 @Validated @RequestBody GetSysDictSubItemArgs args);
}
