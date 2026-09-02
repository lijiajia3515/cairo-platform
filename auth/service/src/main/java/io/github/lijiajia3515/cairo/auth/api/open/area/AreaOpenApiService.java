package io.github.lijiajia3515.cairo.auth.api.open.area;

import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.core.tree.TreeConverter;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.api.open.area.GetAreaDetailArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.open.area.GetAreaListArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.open.area.GetAreaTreeArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.open.area.GetCityListArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.area.Area;
import io.github.lijiajia3515.cairo.auth.domain.dto.area.AreaDetail;
import io.github.lijiajia3515.cairo.auth.domain.dto.area.AreaTree;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AreaMongodb;
import io.github.lijiajia3515.cairo.auth.modules.area.*;
import io.micrometer.tracing.annotation.NewSpan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
public class AreaOpenApiService {
	private final MongoTemplate readMongoTemplate;

	public AreaOpenApiService(@Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate) {
		this.readMongoTemplate = readMongoTemplate;
	}

	@NewSpan
	@BizLog(
		bizId = "area:get_area_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public List<Area> getAreaList(GetAreaListArgs args) {
		Criteria criteria = Criteria
			.where(AreaMongodb.FIELD.PARENT_AREA_ID).is(Optional.ofNullable(args.getParentAreaId()).orElse(AreaConstants.ROOT));

		if (args.getEnabled() != null) {
			criteria.and(AreaMongodb.FIELD.ENABLED).is(args.getEnabled());
		}

		if (args.getHot() != null) {
			criteria.and(AreaMongodb.FIELD.HOT).is(args.getHot());
		}

		Query query = Query.query(criteria);
		query.with(Sort.by(Sort.Order.asc(AreaMongodb.FIELD.SORT)));
		List<AreaMongodb> list = readMongoTemplate.find(query, AreaMongodb.class, MongodbConstants.Collection.AREA);

		return list.stream().map(x -> AreaConverter.convertArea(x, args.isEnableShort())).collect(Collectors.toList());
	}

	@NewSpan
	@BizLog(
		bizId = "area:get_city_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public List<Area> getCityList(GetCityListArgs args) {
		Criteria criteria = Criteria
			.where(AreaMongodb.FIELD.DEPTH).is(2);

		if (args.getEnabled() != null) {
			criteria.and(AreaMongodb.FIELD.ENABLED).is(args.getEnabled());
		}

		if (args.getHot() != null) {
			criteria.and(AreaMongodb.FIELD.HOT).is(args.getHot());
		}

		Query query = Query.query(criteria);
		query.with(Sort.by(Sort.Order.asc(AreaMongodb.FIELD.PIN_YIN_PREFIX)));
		List<AreaMongodb> list = readMongoTemplate.find(query, AreaMongodb.class, MongodbConstants.Collection.AREA);

		return list.stream().map(x -> AreaConverter.convertCityArea(x, args.isEnableShort())).collect(Collectors.toList());
	}

	@NewSpan
	@BizLog(
		bizId = "area:get_area_tree_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public List<AreaTree> getAreaTreeList(GetAreaTreeArgs args) {
		List<String> areaIds = Collections.emptyList();
		String parentAreaId = Optional.ofNullable(args.getParentAreaId()).orElse(AreaConstants.ROOT);
		int depth = args.getDepth();

		if (!parentAreaId.equals(AreaConstants.ROOT)) {
			Criteria criteria = Criteria.where(AreaMongodb.FIELD.AREA_ID).is(parentAreaId);
			Query query = Query.query(criteria);
			query.fields().include(AreaMongodb.FIELD.DEPTH, AreaMongodb.FIELD.AREA_IDS.SELF);
			AreaMongodb one = readMongoTemplate.findOne(query, AreaMongodb.class, MongodbConstants.Collection.AREA);
			if (one == null) {
				return Collections.emptyList();
			}
			depth = depth + one.getDepth();
			areaIds = Optional.ofNullable(one.getAreaIds()).orElse(Collections.emptyList());
		}

		Criteria criteria = new Criteria();
		for (int i = 0; i < areaIds.size(); i++) {
			criteria.and(AreaMongodb.FIELD.AREA_IDS.field("" + i)).is(areaIds.get(i));
		}
		criteria.and(AreaMongodb.FIELD.DEPTH).lte(depth);

		Query query = Query.query(criteria);
		query.with(Sort.by(Sort.Order.asc(AreaMongodb.FIELD.DEPTH), Sort.Order.asc(AreaMongodb.FIELD.SORT)));
		List<AreaMongodb> list = readMongoTemplate.find(query, AreaMongodb.class, MongodbConstants.Collection.AREA);

		List<AreaTree> treeList = list.stream().map(x -> AreaConverter.convertAreaTree(x, args.isEnableShort())).collect(Collectors.toList());
		return TreeConverter.build(treeList, parentAreaId, AreaTree.COMPARATOR);
	}

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
}
