package io.github.lijiajia3515.cairo.auth.modules.dict.biz;

import io.github.lijiajia3515.cairo.core.exception.ErrorBusinessException;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import io.github.lijiajia3515.cairo.auth.domain.api.client.dict.biz.GetBizDictInfoArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.dict.biz.BizDict;
import io.github.lijiajia3515.cairo.auth.domain.dto.dict.biz.BizDictItem;
import io.github.lijiajia3515.cairo.auth.domain.dto.dict.biz.PathBizDict;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.Set;

/**
 * client-api biz dict fallback feign client
 */
public class BizDictClientApiFallbackFeignClient implements BizDictClientApiFeignClient {

	public static final RuntimeException EX = new ErrorBusinessException("系统服务-业务级字典子应用故障");


	@Override
	public ResponseEntity<BusinessResult<Map<String,Map<String, BizDictItem>>>> getBizDictItemMap(String authorization, String tenantId, Set<String> dictIds) {
		throw EX;
	}

	@Override
	public ResponseEntity<BusinessResult<Map<String,  Map<String, BizDictItem>>>> getBizDictItemIdMap(String authorization,String tenantId, Map<String, Set<String>> dictIdMap) {
		throw EX;
	}

	@Override
	public ResponseEntity<BusinessResult<Map<String, Map<String, PathBizDict>>>> getPathBizDictItemIdMap(String authorization, String tenantId, Map<String, Set<String>> dictIdMap) {
		throw EX;
	}

	@Override
	public ResponseEntity<BusinessResult<Map<String,Map<String, PathBizDict>>>> getPathBizDictItemMap(String authorization,String tenantId, Set<String> dictIds) {
		throw EX;
	}

	@Override
	public ResponseEntity<BusinessResult<BizDict>> getBizDictDetailInfo(String authorization, String tenantId, GetBizDictInfoArgs args) {
		throw EX;
	}
}
