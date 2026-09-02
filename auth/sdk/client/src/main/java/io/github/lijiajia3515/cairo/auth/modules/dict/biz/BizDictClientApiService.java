package io.github.lijiajia3515.cairo.auth.modules.dict.biz;


import io.github.lijiajia3515.cairo.auth.domain.api.client.dict.biz.GetBizDictInfoArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.dict.biz.BizDict;
import io.github.lijiajia3515.cairo.auth.domain.dto.dict.biz.BizDictItem;
import io.github.lijiajia3515.cairo.auth.domain.dto.dict.biz.PathBizDict;

import java.util.Map;
import java.util.Set;

public interface BizDictClientApiService {

	/**
	 * 获取业务级字典项map
	 *
	 * @param dictIds 字典项ID
	 * @return biz dict item map
	 */
	Map<String, Map<String, BizDictItem>> getBizDictItemMap(String tenantId, Set<String> dictIds);

	/**
	 * 获取业务级字典部分字典项信息
	 *
	 * @param dictIdMap args
	 * @return biz dict item map
	 */
	Map<String, Map<String, BizDictItem>> getBizDictItemIdMap(String tenantId, Map<String, Set<String>> dictIdMap);

	/**
	 * 获多级业务级字典项部分信息map
	 *
	 * @return 字典详情
	 */
	Map<String, Map<String, PathBizDict>> getPathBizDictItemIdMap(String tenantId, Map<String, Set<String>> dictIdMap);

	/**
	 * 获取多级业务级字典项map
	 *
	 * @param dictIds 字典项ID
	 * @return biz dict item map
	 */
	Map<String, Map<String, PathBizDict>> getPathBizDictItemMap(String tenantId,Set<String> dictIds);

	/**
	 * 获业务级字典信息
	 *
	 * @param args      args
	 * @return 字典详情
	 */
	 BizDict getBizDictDetailInfo(String tenantId, GetBizDictInfoArgs args);

}
