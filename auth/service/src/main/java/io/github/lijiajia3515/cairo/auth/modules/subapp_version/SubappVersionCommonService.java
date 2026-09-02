package io.github.lijiajia3515.cairo.auth.modules.subapp_version;

import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.dto.subapp_version.MetadataSubappVersion;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.SubappVersionMongodb;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityProperties;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_user.AppUser;
import io.github.lijiajia3515.cairo.auth.modules.app_user.AppUserCommonService;
import io.github.lijiajia3515.cairo.auth.modules.app_user.CairoAppUserTool;
import io.github.lijiajia3515.cairo.auth.domain.dto.subapp.Subapp;
import io.github.lijiajia3515.cairo.auth.modules.subapp.SubappCommonService;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.subapp_version.GetSubappVersionArgs;
import io.github.lijiajia3515.cairo.core.exception.ConflictBusinessException;
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
 * [common] subapp_version service
 */
@Slf4j
@Validated
@Component
public class SubappVersionCommonService {

	private final MongoTemplate readMongoTemplate;
	private final CairoSecurityProperties cairoSecurityProperties;
	private final SubappCommonService subappCommonService;
	private final AppUserCommonService appUserCommonService;

	public SubappVersionCommonService(@Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate,
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
	 * @return 端点集合
	 */
	@NewSpan
	List<MetadataSubappVersion> getSubappVersionList(@Validated GetSubappVersionArgs args) {
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

	public void checkSubappVersion(MongoTemplate mongoTemplate,String subappId,String subappVersion) {
		Criteria criteria = Criteria.where(SubappVersionMongodb.FIELD.SUBAPP_ID).is(subappId)
			.and(SubappVersionMongodb.FIELD.SUBAPP_VERSION).is(subappVersion);
		Query query = Query.query(criteria);
		if (!mongoTemplate.exists(query, SubappVersionMongodb.class, MongodbConstants.Collection.SUBAPP_VERSION)) {
			throw new ConflictBusinessException("subappVersion错误");
		}
	}

	/**
	 * 构建查询条件
	 *
	 * @param args 查询参数
	 * @return criteria
	 */
	private Criteria buildCriteria(GetSubappVersionArgs args) {
		Criteria criteria = new Criteria();

		if (args.getSubappId() != null) {
			criteria.and(SubappVersionMongodb.FIELD.SUBAPP_ID).is(args.getSubappId());
		}

		if (args.getEnabled() != null) {
			criteria.and(SubappVersionMongodb.FIELD.ENABLED).is(args.getEnabled());
		}

		if (args.getKeyword() != null) {
			criteria.and(SubappVersionMongodb.FIELD.SUBAPP_REMARK).regex(args.getKeyword());
		}

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
