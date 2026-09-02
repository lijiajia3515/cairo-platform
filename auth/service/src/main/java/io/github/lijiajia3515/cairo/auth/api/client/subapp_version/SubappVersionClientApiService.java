package io.github.lijiajia3515.cairo.auth.api.client.subapp_version;

import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.SubappVersionMongodb;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityProperties;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_user.AppUser;
import io.github.lijiajia3515.cairo.auth.modules.app_user.AppUserCommonService;
import io.github.lijiajia3515.cairo.auth.modules.app_user.CairoAppUserTool;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.auth.domain.dto.subapp.Subapp;
import io.github.lijiajia3515.cairo.auth.modules.subapp.SubappCommonService;
import io.github.lijiajia3515.cairo.auth.domain.dto.subapp_version.MetadataSubappVersion;
import io.github.lijiajia3515.cairo.auth.modules.subapp_version.SubappVersionVersionConverter;
import io.github.lijiajia3515.cairo.auth.domain.api.client.subapp_version.GetSubappVersionClientArgs;
import io.micrometer.tracing.annotation.NewSpan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * [client/api] subapp_version service
 */
@Slf4j
@Validated
@Component
public class SubappVersionClientApiService {

	private final MongoTemplate readMongoTemplate;
	private final CairoSecurityProperties cairoSecurityProperties;
	private final SubappCommonService subappCommonService;
	private final AppUserCommonService appUserCommonService;
	public SubappVersionClientApiService(@Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate,
                                          CairoSecurityProperties cairoSecurityProperties,
                                          SubappCommonService subappCommonService,
                                          AppUserCommonService appUserCommonService) {
		this.cairoSecurityProperties = cairoSecurityProperties;
		this.readMongoTemplate = readMongoTemplate;
		this.subappCommonService = subappCommonService;
		this.appUserCommonService = appUserCommonService;
	}


	/**
	 * 获取子应用版本集合
	 *
	 * @param args 参数
	 * @return 子应用版本集合
	 */
	@NewSpan
	@BizLog(
		bizId = "subapp_version:get_subapp_version_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	List<MetadataSubappVersion> getSubappVersionList(@Validated GetSubappVersionClientArgs args) {
		Criteria criteria = buildCriteria(args);
		Query query = Query
			.query(criteria)
			.with(
				Sort.by(
					Sort.Order.desc(SubappVersionMongodb.FIELD.SUBAPP_ID),
					Sort.Order.desc(SubappVersionMongodb.FIELD.SUBAPP_VERSION),
					Sort.Order.desc(SubappVersionMongodb.FIELD._ID)
				)
			);

		List<SubappVersionMongodb> tms = readMongoTemplate.find(query, SubappVersionMongodb.class, MongodbConstants.Collection.SUBAPP_VERSION);
		return getSubappVersionList(tms);
	}

	/**
	 * 构建查询条件
	 *
	 * @param args 查询参数
	 * @return criteria
	 */
	private Criteria buildCriteria(GetSubappVersionClientArgs args) {
		Criteria criteria = new Criteria();

		if (args.getSubappId() != null) {
			criteria.and(SubappVersionMongodb.FIELD.SUBAPP_ID).is(args.getSubappId());
		}

		if (args.getEnabled() != null) {
			criteria.and(SubappVersionMongodb.FIELD.ENABLED).is(args.getEnabled());
		}

		Optional.ofNullable(args.getKeyword()).ifPresent(x -> criteria.orOperator(
			Criteria.where(SubappVersionMongodb.FIELD.SUBAPP_VERSION).regex(x),
			Criteria.where(SubappVersionMongodb.FIELD.SUBAPP_REMARK).regex(x)
		));

		return criteria;
	}

	/**
	 * 包装数据
	 *
	 * @param ms ms
	 * @return metadata subapp_version list
	 */
	List<MetadataSubappVersion> getSubappVersionList(List<SubappVersionMongodb> ms) {
		List<String> subappIds = ms.stream().map(SubappVersionMongodb::getSubappId).distinct().collect(Collectors.toList());
		Map<String, Subapp> subappMap = Optional.of(subappIds)
			.filter(pIds -> !pIds.isEmpty())
			.map(subappCommonService::getSubappMapBySubappIds)
			.orElse(Collections.emptyMap());

		Set<String> metadataUserIds = CairoAppUserTool.getAppUserMetadataUserIds(ms.stream().map(SubappVersionMongodb::getMetadata).collect(Collectors.toList()));
		Map<String, AppUser> metadataUserMap = Optional.of(metadataUserIds)
			.filter(userIds -> !userIds.isEmpty())
			.map(userIds -> appUserCommonService.getAppUserMapByAppUserIds(cairoSecurityProperties.getCairoAppId(), userIds))
			.orElse(Collections.emptyMap());

		return ms.stream().map(x -> SubappVersionVersionConverter.convertMetadataSubapp(x, subappMap, metadataUserMap)).collect(Collectors.toList());
	}
}
