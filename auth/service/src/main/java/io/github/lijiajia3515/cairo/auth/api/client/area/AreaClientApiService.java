package io.github.lijiajia3515.cairo.auth.api.client.area;

import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.api.client.area.GetAreaDetailArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.area.GetAreaDetailMapArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.area.GetAreaListArgs;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AreaMongodb;
import io.github.lijiajia3515.cairo.auth.domain.dto.area.Area;
import io.github.lijiajia3515.cairo.auth.modules.area.AreaConstants;
import io.github.lijiajia3515.cairo.auth.modules.area.AreaConverter;
import io.github.lijiajia3515.cairo.auth.domain.dto.area.AreaDetail;
import io.micrometer.tracing.annotation.NewSpan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * [client/api]区域服务
 */
@Service
public class AreaClientApiService {
	private final MongoTemplate readMongoTemplate;

	public AreaClientApiService(@Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate) {
		this.readMongoTemplate = readMongoTemplate;
	}

	/**
	 * 区域列表
	 *
	 * @param args 参数
	 * @return 区域列表
	 */
	@NewSpan
	@BizLog(
		bizId = "area:get_area_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public List<Area> getAreaList(GetAreaListArgs args) {
		Criteria criteria = Criteria.where(AreaMongodb.FIELD.PARENT_AREA_ID).is(Optional.ofNullable(args.getParentAreaId()).orElse(AreaConstants.ROOT));

		Query query = Query.query(criteria);
		query.with(Sort.by(Sort.Order.asc(AreaMongodb.FIELD.SORT)));
		List<AreaMongodb> list = readMongoTemplate.find(query, AreaMongodb.class, MongodbConstants.Collection.AREA);

		return list.stream().map(x -> AreaConverter.convertArea(x, args.isEnableShort())).collect(Collectors.toList());
	}

	/**
	 * 获取区域详情
	 *
	 * @param args 参数
	 * @return 区域详情
	 */
	@NewSpan
	@BizLog(
		bizId = "area:get_area_detail",
		scope = "read",
		params = {
			@BizLog.Param(key = "args", value = "#args"),

		}
	)
	public AreaDetail getAreaDetail(GetAreaDetailArgs args) {
		Criteria criteria = Criteria.where(AreaMongodb.FIELD.AREA_ID).is(args.getAreaId());

		Query query = Query.query(criteria);
		AreaMongodb one = readMongoTemplate.findOne(query, AreaMongodb.class, MongodbConstants.Collection.AREA);
		if (one != null) {
			return AreaConverter.convertAreaDetail(one, args.isEnableShort());
		}
		return null;
	}

	/**
	 * 获取区域详情map
	 *
	 * @param args 参数
	 * @return 区域详情map
	 */
	@NewSpan
	@BizLog(
		bizId = "area:get_area_detail_map",
		scope = "read",
		params = {
			@BizLog.Param(key = "args", value = "#args"),

		}
	)
	public Map<String, AreaDetail> getAreaDetailMap(GetAreaDetailMapArgs args) {
		if (args.getAreaIds() == null || args.getAreaIds().isEmpty()) return Collections.emptyMap();
		Criteria criteria = Criteria.where(AreaMongodb.FIELD.AREA_ID).in(args.getAreaIds());

		Query query = Query.query(criteria);
		List<AreaMongodb> list = readMongoTemplate.find(query, AreaMongodb.class, MongodbConstants.Collection.AREA);
		return list.stream().map(x -> AreaConverter.convertAreaDetail(x, args.isEnableShort())).collect(Collectors.toMap(AreaDetail::getAreaId, x -> x));
	}
}
