package io.github.lijiajia3515.cairo.auth.modules.dict.sys;

import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import io.github.lijiajia3515.cairo.auth.CairoAuthClientSdkCoreFeignClientConfiguration;
import io.github.lijiajia3515.cairo.auth.domain.api.client.dict.sys.GetSysDictInfoArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.dict.sys.SysDict;
import io.github.lijiajia3515.cairo.auth.domain.dto.dict.sys.SysDictItem;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;
import java.util.Set;

/**
 * client-api system dict basic feign client
 */
@FeignClient(
	contextId = "sysDictBasicClientApiFeignClient",
	name = "${cairo.feign.client.cairo-auth-service:cairo-auth-service}",
	path = "/client_api/sys_dict",
	fallbackFactory = SysDictClientApiFeignClientFallbackFactory.class,
	configuration = CairoAuthClientSdkCoreFeignClientConfiguration.class
)
public interface SysDictClientApiBasicFeignClient {

	/**
	 * 字典查询
	 *
	 * @param dictIds 字典ids
	 * @return system dict item map
	 */
	@PostMapping("/get_sys_dict_item_map")
	ResponseEntity<BusinessResult<Map<String, Map<String, SysDictItem>>>> getSysDictItemMap(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization, @RequestBody Set<String> dictIds);

	/**
	 * 获取系统级字典部分字典项信息
	 *
	 * @param dictItemIds args
	 * @return system dict item map
	 */
	@PostMapping("/get_sys_dict_item_id_map")
	ResponseEntity<BusinessResult<Map<String, Map<String, SysDictItem>>>> getSysDictItemIdMap(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization, @RequestBody Map<String, Set<String>> dictItemIds);


	/**
	 * 获取系统级字典详细信息(单个)
	 *
	 * @param args args
	 * @return map 字典
	 */
	@PostMapping("/get_sys_dict_detail_info")
	ResponseEntity<BusinessResult<SysDict>> getSysDictDetailInfo(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization, @RequestBody GetSysDictInfoArgs args);

}
