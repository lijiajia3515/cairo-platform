package io.github.lijiajia3515.cairo.auth.modules.dict.biz;

import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import io.github.lijiajia3515.cairo.auth.CairoAuthClientSdkCoreFeignClientConfiguration;
import io.github.lijiajia3515.cairo.auth.domain.api.client.dict.biz.GetBizDictInfoArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.dict.biz.BizDict;
import io.github.lijiajia3515.cairo.auth.domain.dto.dict.biz.BizDictItem;
import io.github.lijiajia3515.cairo.auth.domain.dto.dict.biz.PathBizDict;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;
import java.util.Set;

/**
 * client-api biz dict feign client
 */
@FeignClient(
	contextId = "bizDictClientApiFeignClient",
	name = "${cairo.feign.client.cairo-auth-service:cairo-auth-service}",
	path = "/client_api/biz_dict",
	fallbackFactory = BizDictClientApiFeignClientFallbackFactory.class,
	configuration = CairoAuthClientSdkCoreFeignClientConfiguration.class
)
public interface BizDictClientApiFeignClient {

	/**
	 * 获取业务级字典项map
	 *
	 * @param dictIds 字典项ID
	 * @return biz dict item map
	 */
	@PostMapping("/get_biz_dict_item_map")
	ResponseEntity<BusinessResult<Map<String, Map<String, BizDictItem>>>> getBizDictItemMap(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
																										@RequestParam(name = "tenant_id", required = false) String tenantId,
																										@RequestBody Set<String> dictIds);

	/**
	 * 获取业务级字典部分字典项信息
	 *
	 * @param dictIdMap args
	 * @return biz dict item map
	 */
	@PostMapping("/get_biz_dict_item_id_map")
	ResponseEntity<BusinessResult<Map<String, Map<String, BizDictItem>>>> getBizDictItemIdMap(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
																										  @RequestParam(name = "tenant_id", required = false) String tenantId,
																										  @RequestBody Map<String, Set<String>> dictIdMap);

	/**
	 * 获多级业务级字典项部分信息map
	 *
	 * @return 字典详情
	 */
	@PostMapping("/get_path_biz_dict_item_id_map")
	ResponseEntity<BusinessResult<Map<String, Map<String, PathBizDict>>>> getPathBizDictItemIdMap(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
																											  @RequestParam(name = "tenant_id", required = false) String tenantId,
																											  @RequestBody Map<String, Set<String>> dictIdMap);

	/**
	 * 获取多级业务级字典项map
	 *
	 * @param dictIds 字典项ID
	 * @return biz dict item map
	 */
	@PostMapping("/get_path_biz_dict_item_map")
	ResponseEntity<BusinessResult<Map<String, Map<String, PathBizDict>>>> getPathBizDictItemMap(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
																											@RequestParam(name = "tenant_id", required = false) String tenantId,
																											@RequestBody Set<String> dictIds);

	/**
	 * 获业务级字典信息
	 *
	 * @param args args
	 * @return 字典详情
	 */
	@PostMapping("/get_biz_dict_detail_info")
	ResponseEntity<BusinessResult<BizDict>> getBizDictDetailInfo(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
																			 @RequestParam(name = "tenant_id", required = false) String tenantId,
																			 @RequestBody GetBizDictInfoArgs args);


}
