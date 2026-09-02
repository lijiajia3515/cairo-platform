package io.github.lijiajia3515.cairo.auth.modules.dict.sys;

import io.github.lijiajia3515.cairo.core.exception.ErrorBusinessException;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import io.github.lijiajia3515.cairo.auth.domain.api.client.dict.sys.GetSysDictInfoArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.dict.sys.GetSysDictSubItemArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.dict.sys.PathSysDict;
import io.github.lijiajia3515.cairo.auth.domain.dto.dict.sys.SysDict;
import io.github.lijiajia3515.cairo.auth.domain.dto.dict.sys.SysDictItem;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * client-api system dict fallback feign client
 */
public class SysDictClientApiFallbackFeignClient implements SysDictClientApiFeignClient {

	public static final RuntimeException EX = new ErrorBusinessException("系统服务-系统级字典子应用故障");


	@Override
	public ResponseEntity<BusinessResult<Map<String, Map<String, SysDictItem>>>> getSysDictItemMap(String authorization, Set<String> dictIds) {
		throw EX;
	}

	@Override
	public ResponseEntity<BusinessResult<Map<String, Map<String, SysDictItem>>>> getSysDictItemIdMap(String authorization,Map<String, Set<String>> dictItemIds) {
		throw EX;
	}

	@Override
	public ResponseEntity<BusinessResult<SysDict>> getSysDictDetailInfo(String authorization, GetSysDictInfoArgs args) {
		throw EX;
	}

	@Override
	public ResponseEntity<BusinessResult<Map<String, Map<String, PathSysDict>>>> getPathSysDictItemIdMap(String authorization, Map<String, Set<String>> dictIdMap) {
		throw EX;
	}

	@Override
	public ResponseEntity<BusinessResult<Map<String, Map<String, PathSysDict>>>> getPathSysDictItemMap(String authorization,Set<String> dictIds) {
		throw EX;
	}

	@Override
	public ResponseEntity<BusinessResult<List<SysDictItem>>> getSysDictSubItemList(String authorization, GetSysDictSubItemArgs args) {
		throw EX;
	}
}
