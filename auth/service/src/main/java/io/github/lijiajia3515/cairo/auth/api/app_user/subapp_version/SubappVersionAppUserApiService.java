package io.github.lijiajia3515.cairo.auth.api.app_user.subapp_version;

import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.SubappVersionMongodb;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.auth.domain.dto.subapp.Subapp;
import io.github.lijiajia3515.cairo.auth.modules.subapp.SubappCommonService;
import io.github.lijiajia3515.cairo.auth.domain.dto.subapp_version.SubappVersion;
import io.github.lijiajia3515.cairo.auth.modules.subapp_version.SubappVersionVersionConverter;
import io.github.lijiajia3515.cairo.auth.domain.api.app_user.subapp_version.GetSubappVersionArgs;
import io.micrometer.tracing.annotation.NewSpan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * [subapp_version/api] app endpoint service
 */
@Slf4j
@Validated
@Component
public class SubappVersionAppUserApiService {

	private final MongoTemplate readMongoTemplate;
	private final SubappCommonService subappCommonService;

	public SubappVersionAppUserApiService(@Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate,
											   SubappCommonService subappCommonService) {
		this.readMongoTemplate = readMongoTemplate;
		this.subappCommonService = subappCommonService;
	}

	/**
	 * 子应用版本查询
	 *
	 * @return 子应用版本查询
	 */
	@NewSpan
	@BizLog(
		bizId = "subapp_version:get_subapp_version_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public List<SubappVersion> getSubappVersionList(@Validated GetSubappVersionArgs args) {
		Criteria criteria = new Criteria();

		if (args.getSubappId() != null && !args.getSubappId().isBlank()) {
			criteria.and(SubappVersionMongodb.FIELD.SUBAPP_ID).is(args.getSubappId());
		}

		if (args.getEnabled() != null) {
			criteria.and(SubappVersionMongodb.FIELD.ENABLED).is(args.getEnabled());
		}

		Query subappVersionQuery = Query.query(criteria);
		List<SubappVersionMongodb> mongodbList = readMongoTemplate.find(subappVersionQuery, SubappVersionMongodb.class, MongodbConstants.Collection.SUBAPP_VERSION);
		return getSubappVersionList(mongodbList);
	}

	/**
	 * 包装数据
	 *
	 * @param ms ms
	 * @return subapp_version list
	 */
	List<SubappVersion> getSubappVersionList(List<SubappVersionMongodb> ms) {
		List<String> subappIds = ms.stream().map(SubappVersionMongodb::getSubappId).distinct().collect(Collectors.toList());
		Map<String, Subapp> subappMap = Optional.of(subappIds)
			.filter(pIds -> !pIds.isEmpty())
			.map(subappCommonService::getSubappMapBySubappIds)
			.orElse(Collections.emptyMap());
		return ms.stream().map(x -> SubappVersionVersionConverter.convertSubappVersion(x, subappMap)).collect(Collectors.toList());
	}
}
