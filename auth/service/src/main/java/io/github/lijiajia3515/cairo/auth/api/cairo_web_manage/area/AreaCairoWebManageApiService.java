package io.github.lijiajia3515.cairo.auth.api.cairo_web_manage.area;


import com.baomidou.lock.annotation.Lock4j;
import com.mongodb.client.result.UpdateResult;
import io.github.lijiajia3515.cairo.auth.domain.api.client.app_user.GetAppUserClientArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_user.AppUser;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AppUserMetadataMongodb;
import io.github.lijiajia3515.cairo.auth.framework.security.CairoSecurityContextHolder;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.core.exception.ConflictBusinessException;
import io.github.lijiajia3515.cairo.core.page.Page;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.area.CreateAreaArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.area.DeleteAreaArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.area.GetAreaDetailArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.area.GetAreaPageListArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.area.ModifyAreaHotArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.area.ModifyAreaInfoArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.area.ModifyAreaStatusArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.area.MoveAreaArgs;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AreaMongodb;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityProperties;
import io.github.lijiajia3515.cairo.auth.modules.area.AreaConverter;
import io.github.lijiajia3515.cairo.auth.domain.dto.area.MetadataArea;
import io.github.lijiajia3515.cairo.auth.domain.dto.area.MetadataAreaDetail;
import io.micrometer.tracing.annotation.NewSpan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.github.lijiajia3515.cairo.auth.modules.area.AreaConstants.ROOT;
import io.github.lijiajia3515.cairo.auth.modules.app_user.AppUserCommonService;

/**
 * [cairo_web_manage/api] area service
 */
@Slf4j
@Validated
@Component
public class AreaCairoWebManageApiService {
	private final TransactionTemplate transactionTemplate;
	private final MongoTemplate mongoTemplate;
	private final MongoTemplate readMongoTemplate;
	private final AppUserCommonService appUserCommonService;
	private final CairoSecurityProperties cairoSecurityProperties;

	public AreaCairoWebManageApiService(TransactionTemplate transactionTemplate,
										@Qualifier("mongoTemplate") MongoTemplate mongoTemplate,
										@Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate,
										AppUserCommonService appUserCommonService,
										CairoSecurityProperties cairoSecurityProperties) {
		this.transactionTemplate = transactionTemplate;
		this.mongoTemplate = mongoTemplate;
		this.readMongoTemplate = readMongoTemplate;
		this.appUserCommonService = appUserCommonService;
		this.cairoSecurityProperties = cairoSecurityProperties;
	}

	/**
	 * 区域分页列表
	 *
	 * @param args 参数
	 * @return 区域列表
	 */
	@NewSpan
	@BizLog(
		bizId = "area:get_area_page_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public Page<MetadataArea> getAreaPageList(GetAreaPageListArgs args) {
		String parentAreaId = Optional.ofNullable(args.getParentAreaId()).orElse(ROOT);
		Criteria criteria = Criteria.where(AreaMongodb.FIELD.PARENT_AREA_ID).is(parentAreaId);
		if (args.getEnabled() != null) {
			criteria.and(AreaMongodb.FIELD.ENABLED).is(args.getEnabled());
		}
		if (args.getHot() != null) {
			criteria.and(AreaMongodb.FIELD.HOT).is(args.getHot());
		}
		if (args.getKeyword() != null && !args.getKeyword().isBlank()) {
			criteria.and(AreaMongodb.FIELD.AREA_NAME).regex(args.getKeyword());
		}
		Query query = Query.query(criteria);
		long count = readMongoTemplate.count(query, AreaMongodb.class, MongodbConstants.Collection.AREA);

		query.with(args.pageable());
		query.with(Sort.by(Sort.Order.asc(AreaMongodb.FIELD.SORT)));
		List<AreaMongodb> list = readMongoTemplate.find(query, AreaMongodb.class, MongodbConstants.Collection.AREA);
		Set<String> userIds = list.stream().map(AreaMongodb::getMetadata).toList().stream().flatMap(x -> Stream.of(x.getCreateUserId(), x.getUpdateUserId())).filter(Objects::nonNull).collect(Collectors.toSet());
		Map<String, AppUser> metadataUserMap = appUserCommonService.getAppUserMapByAppUserIds(cairoSecurityProperties.getCairoAppId(), userIds);
		List<MetadataArea> metadataAreaList = list.stream().map(x -> AreaConverter.convertMetadataArea(x, metadataUserMap)).collect(Collectors.toList());
		return new Page<>(args, metadataAreaList, count);
	}

	@NewSpan
	@BizLog(
		bizId = "area:get_area_detail",
		scope = "read",
		params = {
			@BizLog.Param(key = "args", value = "#args"),

		}
	)
	public MetadataAreaDetail getAreaDetail(GetAreaDetailArgs args) {
		Criteria criteria = Criteria.where(AreaMongodb.FIELD.AREA_ID).is(args.getAreaId());

		Query query = Query.query(criteria);
		AreaMongodb one = readMongoTemplate.findOne(query, AreaMongodb.class, MongodbConstants.Collection.AREA);
		if (one != null) {
			Set<String> userIds = Optional.ofNullable(one.getMetadata()).stream().flatMap(x -> Stream.of(x.getCreateUserId(), x.getUpdateUserId())).filter(Objects::nonNull).collect(Collectors.toSet());
			Map<String, AppUser> metadataUserMap = appUserCommonService.getAppUserMapByAppUserIds(cairoSecurityProperties.getCairoAppId(), userIds);

			return AreaConverter.convertMetadataAreaDetail(one, metadataUserMap);
		}
		throw new ConflictBusinessException("区域不存在");
	}

	/**
	 * 修改区域信息
	 *
	 * @param args 参数
	 */
	@NewSpan
	@BizLog(
		bizId = "area:create_area",
		scope = "write",
		params = {
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public void createArea(CreateAreaArgs args) {
		String parentAreaId = Optional.ofNullable(args.getParentAreaId()).orElse(ROOT);
		AreaMongodb insertedArea = transactionTemplate.execute(status -> {
			try {
				int depth;
				List<String> areaIds, areaNames, shortAreaNames;
				if (!parentAreaId.equals(ROOT)) {
					Criteria parentCriteria = Criteria.where(AreaMongodb.FIELD.AREA_ID).is(args.getParentAreaId());
					Query parentQuery = Query.query(parentCriteria);
					AreaMongodb parentArea = mongoTemplate.findOne(parentQuery, AreaMongodb.class, MongodbConstants.Collection.AREA);
					if (parentArea == null) {
						throw new ConflictBusinessException("创建区域失败（上级区域不存在）");
					}
					depth = parentArea.getDepth() + 1;
					areaIds = new ArrayList<>(depth);
					areaNames = new ArrayList<>(depth);
					shortAreaNames = new ArrayList<>(depth);

					areaIds.addAll(parentArea.getAreaIds());
					areaIds.add(args.getAreaId());
					areaNames.addAll(parentArea.getAreaNames());
					areaNames.add(args.getAreaName());
					shortAreaNames.addAll(parentArea.getShortAreaNames());
					shortAreaNames.add(args.getShortAreaName());
				} else {
					depth = 1;
					areaIds = List.of(args.getAreaId());
					areaNames = List.of(args.getAreaName());
					shortAreaNames = List.of(args.getShortAreaName());
				}
				AreaMongodb newArea = AreaMongodb.builder()
					.areaId(args.getAreaId())
					.areaName(args.getAreaName())
					.shortAreaName(args.getShortAreaName())
					.pinYin(args.getPinYin())
					.pinYinPrefix(args.getPinYinPrefix())
					.depth(depth)
					.enabled(args.isEnabled())
					.hot(args.isHot())
					.parentAreaId(parentAreaId)
					.areaIds(areaIds)
					.areaNames(areaNames)
					.shortAreaNames(shortAreaNames)
					.sort((int) LocalDateTime.now().toEpochSecond(ZoneOffset.ofHours(8)))
					.metadata(AppUserMetadataMongodb.builder()
						.createUserId(CairoSecurityContextHolder.getAppUserId())
						.updateUserId(CairoSecurityContextHolder.getAppUserId())
						.build())
					.build();
				return mongoTemplate.insert(newArea, MongodbConstants.Collection.AREA);
			} catch (ConflictBusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (RuntimeException e) {
				log.debug("createArea", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("新增区域失败");
			}
		});
		if (insertedArea != null) {
			log.info("[新增区域] AreaId:{} AreaName: {}", insertedArea.getAreaId(), insertedArea.getAreaName());
		}
	}

	/**
	 * 修改区域信息
	 *
	 * @param args 参数
	 */
	@NewSpan
	@Lock4j(name = "modify_area_info", keys = {"#args.areaId"})
	@BizLog(
		bizId = "area:modify_area_info",
		scope = "write",
		params = {
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public void modifyAreaInfo(ModifyAreaInfoArgs args) {
		Criteria rootCriteria = Criteria.where(AreaMongodb.FIELD.AREA_ID).is(args.getAreaId());
		Query rootQuery = Query.query(rootCriteria);
		AreaMongodb rootArea = mongoTemplate.findOne(rootQuery, AreaMongodb.class, MongodbConstants.Collection.AREA);
		if (rootArea == null) {
			throw new ConflictBusinessException("修改区域失败（区域不存在）");
		}
		Update rootUpdate = new Update();
		rootUpdate.set(AreaMongodb.FIELD.AREA_NAME, args.getAreaName());
		rootUpdate.set(AreaMongodb.FIELD.SHORT_AREA_NAME, args.getShortAreaName());
		rootUpdate.set(AreaMongodb.FIELD.PIN_YIN, args.getPinYin());
		rootUpdate.set(AreaMongodb.FIELD.PIN_YIN_PREFIX, args.getPinYinPrefix());
		rootUpdate.set(AreaMongodb.FIELD.SORT, args.getSort());
		rootUpdate.currentDate(AreaMongodb.FIELD.METADATA.UPDATE_TIME);
		rootUpdate.set(AreaMongodb.FIELD.METADATA.UPDATE_USER_ID, CairoSecurityContextHolder.getAppUserId());

		Criteria subCriteria = new Criteria();
		for (int i = 0; i < rootArea.getAreaIds().size(); i++) {
			subCriteria.and(AreaMongodb.FIELD.AREA_IDS.index(+i)).is(rootArea.getAreaIds().get(i));
		}
		Query subQuery = Query.query(subCriteria);

		Update subUpdate = new Update();
		subUpdate.set(AreaMongodb.FIELD.AREA_NAMES.index(rootArea.getDepth() - 1), args.getAreaName());
		subUpdate.set(AreaMongodb.FIELD.SHORT_AREA_NAMES.index(rootArea.getDepth() - 1), args.getShortAreaName());

		transactionTemplate.executeWithoutResult(status -> {
			try {
				UpdateResult rootUpdateResult = mongoTemplate.updateFirst(rootQuery, rootUpdate, AreaMongodb.class, MongodbConstants.Collection.AREA);
				UpdateResult subUpdateResult = mongoTemplate.updateMulti(subQuery, subUpdate, AreaMongodb.class, MongodbConstants.Collection.AREA);
				log.debug("修改区域信息：{}条", rootUpdateResult.getModifiedCount());
				log.debug("修改子区域冗余信息：{}条", subUpdateResult.getModifiedCount());
				if (rootUpdateResult.getModifiedCount() < 1) {
					throw new ConflictBusinessException("修改区域失败");
				}
			} catch (ConflictBusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (RuntimeException e) {
				log.debug("modifyAreaInfo", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("修改区域失败");
			}
		});
	}

	/**
	 * 修改区域热门状态
	 *
	 * @param args 参数
	 */
	@NewSpan
	@Lock4j(name = "modify_area_hot", keys = {"#args.areaId"})
	@BizLog(
		bizId = "area:modify_area_hot",
		scope = "write",
		params = {
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public void modifyAreaHot(ModifyAreaHotArgs args) {
		Criteria rootCriteria = Criteria.where(AreaMongodb.FIELD.AREA_ID).is(args.getAreaId());
		Query rootQuery = Query.query(rootCriteria);
		AreaMongodb rootArea = mongoTemplate.findOne(rootQuery, AreaMongodb.class, MongodbConstants.Collection.AREA);
		if (rootArea == null) {
			throw new ConflictBusinessException("修改区域热门失败（区域不存在）");
		}
		Update rootUpdate = new Update();
		rootUpdate.set(AreaMongodb.FIELD.HOT, args.isHot());
		rootUpdate.currentDate(AreaMongodb.FIELD.METADATA.UPDATE_TIME);
		rootUpdate.set(AreaMongodb.FIELD.METADATA.UPDATE_USER_ID, CairoSecurityContextHolder.getAppUserId());

		transactionTemplate.executeWithoutResult(status -> {
			try {
				UpdateResult rootUpdateResult = mongoTemplate.updateFirst(rootQuery, rootUpdate, AreaMongodb.class, MongodbConstants.Collection.AREA);
				log.debug("修改区域热门：{}条", rootUpdateResult.getModifiedCount());
				if (rootUpdateResult.getModifiedCount() < 1) {
					throw new ConflictBusinessException("修改区域热门失败");
				}
			} catch (ConflictBusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (RuntimeException e) {
				log.debug("modifyAreaStatus", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("修改区域热门失败");
			}
		});
	}

	/**
	 * 修改区域状态
	 *
	 * @param args 参数
	 */
	@NewSpan
	@Lock4j(name = "modify_area_status", keys = {"#args.areaId"})
	@BizLog(
		bizId = "area:modify_area_status",
		scope = "write",
		params = {
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public void modifyAreaStatus(ModifyAreaStatusArgs args) {
		Criteria rootCriteria = Criteria.where(AreaMongodb.FIELD.AREA_ID).is(args.getAreaId());
		Query rootQuery = Query.query(rootCriteria);
		AreaMongodb rootArea = mongoTemplate.findOne(rootQuery, AreaMongodb.class, MongodbConstants.Collection.AREA);
		if (rootArea == null) {
			throw new ConflictBusinessException("修改区域状态失败（区域不存在）");
		}
		Update rootUpdate = new Update();
		rootUpdate.set(AreaMongodb.FIELD.ENABLED, args.isEnabled());
		rootUpdate.currentDate(AreaMongodb.FIELD.METADATA.UPDATE_TIME);
		rootUpdate.set(AreaMongodb.FIELD.METADATA.UPDATE_USER_ID, CairoSecurityContextHolder.getAppUserId());

		transactionTemplate.executeWithoutResult(status -> {
			try {
				UpdateResult rootUpdateResult = mongoTemplate.updateFirst(rootQuery, rootUpdate, AreaMongodb.class, MongodbConstants.Collection.AREA);
				log.debug("修改区域状态信息：{}条", rootUpdateResult.getModifiedCount());
				if (rootUpdateResult.getModifiedCount() < 1) {
					throw new ConflictBusinessException("修改区域状态失败");
				}
			} catch (ConflictBusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (RuntimeException e) {
				log.debug("modifyAreaStatus", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("修改区域状态失败");
			}
		});
	}

	/**
	 * 移动区域
	 *
	 * @param args 参数
	 */
	@NewSpan
	@Lock4j(name = "move_area", keys = {"#args.moveAreaId1","#args.moveAreaId2"})
	@BizLog(
		bizId = "area:move_area",
		scope = "write",
		params = {
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public void moveArea(MoveAreaArgs args) {
		Criteria criteria = Criteria.where(AreaMongodb.FIELD.AREA_ID).in(args.getMoveAreaId1(), args.getMoveAreaId2());
		Query query = Query.query(criteria);

		transactionTemplate.executeWithoutResult(status -> {
			try {
				Map<String, AreaMongodb> areaMap = mongoTemplate.find(query, AreaMongodb.class, MongodbConstants.Collection.AREA).stream().collect(Collectors.toMap(AreaMongodb::getAreaId, x -> x));
				AreaMongodb area1 = areaMap.get(args.getMoveAreaId1());
				AreaMongodb area2 = areaMap.get(args.getMoveAreaId2());
				if (area1 == null || area2 == null) {
					throw new ConflictBusinessException("区域不存在");
				}
				Query areaQuery1 = Query.query(Criteria.where(AreaMongodb.FIELD.AREA_ID).is(area1.getAreaId()));
				Update areaUpdate1 = Update.update(AreaMongodb.FIELD.SORT, Optional.of(area2.getSort()).orElse((int) (LocalDateTime.now().toEpochSecond(ZoneOffset.ofHours(8)))));

				Query areaQuery2 = Query.query(Criteria.where(AreaMongodb.FIELD.AREA_ID).is(area2.getAreaId()));
				Update areaUpdate2 = Update.update(AreaMongodb.FIELD.SORT, Optional.of(area1.getSort()).orElse((int) (LocalDateTime.now().toEpochSecond(ZoneOffset.ofHours(8)))));

				UpdateResult updateResult = mongoTemplate.updateFirst(areaQuery1, areaUpdate1, AreaMongodb.class, MongodbConstants.Collection.AREA);
				UpdateResult updateResult1 = mongoTemplate.updateFirst(areaQuery2, areaUpdate2, AreaMongodb.class, MongodbConstants.Collection.AREA);
			} catch (ConflictBusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (RuntimeException e) {
				log.debug("moveArea", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("移动区域失败");
			}
		});
	}

	/**
	 * 删除区域
	 *
	 * @param args 参数
	 */
	@NewSpan
	@Lock4j(name = "delete_area", keys = {"#args.areaId"})
	@BizLog(
		bizId = "area:delete_area",
		scope = "write",
		params = {
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public void deleteArea(DeleteAreaArgs args) {
		Criteria rootCriteria = Criteria.where(AreaMongodb.FIELD.AREA_ID).is(args.getAreaId());
		Query rootQuery = Query.query(rootCriteria);

		transactionTemplate.executeWithoutResult(status -> {
			try {
				AreaMongodb rootArea = mongoTemplate.findOne(rootQuery, AreaMongodb.class, MongodbConstants.Collection.AREA);
				if (rootArea == null) {
					throw new ConflictBusinessException("删除区域失败（区域不存在）");
				}

				Criteria deleteCriteria = new Criteria();
				for (int i = 0; i < rootArea.getAreaIds().size(); i++) {
					deleteCriteria.and(AreaMongodb.FIELD.AREA_IDS.index(+i)).is(rootArea.getAreaIds().get(i));
				}
				Query subQuery = Query.query(deleteCriteria);

				Update subUpdate = new Update();
				subUpdate.currentDate(AreaMongodb.FIELD.METADATA.UPDATE_TIME);
				subUpdate.set(AreaMongodb.FIELD.METADATA.UPDATE_USER_ID, CairoSecurityContextHolder.getAppUserId());

				UpdateResult updateResult = mongoTemplate.updateMulti(subQuery, subUpdate, AreaMongodb.class, MongodbConstants.Collection.AREA);
				List<AreaMongodb> removeList = mongoTemplate.findAllAndRemove(subQuery, AreaMongodb.class, MongodbConstants.Collection.AREA);
				if (!removeList.isEmpty()) {
					mongoTemplate.insert(removeList, MongodbConstants.DeletedCollection.AREA);
				}
				if (updateResult.getModifiedCount() <= 0) {
					throw new ConflictBusinessException("删除区域失败");
				}
			} catch (ConflictBusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (RuntimeException e) {
				log.debug("deleteArea", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("删除区域失败");
			}
		});
	}
}
