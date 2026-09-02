package io.github.lijiajia3515.cairo.auth.modules.dict.sys.common;

import io.github.lijiajia3515.cairo.auth.framework.security.CairoSecurityContextHolder;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.SysDictItemMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.SysDictMongodb;
import io.github.lijiajia3515.cairo.auth.domain.dto.dict.sys.MetadataSysDictItem;
import io.github.lijiajia3515.cairo.auth.modules.dict.sys.SysDictConverter;
import io.github.lijiajia3515.cairo.auth.domain.dto.dict.sys.SysDictItem;
import io.github.lijiajia3515.cairo.auth.modules.dict.sys.common.args.GetDictItemMapArgs;
import io.micrometer.tracing.annotation.NewSpan;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.validation.annotation.Validated;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.github.lijiajia3515.cairo.auth.constants.CairoAuthConstants.ROOT_ID;
import static io.github.lijiajia3515.cairo.auth.constants.CairoAuthConstants.ROOT_PARENT_ID;

/**
 * 系统级公共服务
 */
@Component
public class SysDictCommonService {
	public static final Comparator<MetadataSysDictItem> METADATA_SYS_DICT_ITEM_COMPARATOR = Comparator.comparing(MetadataSysDictItem::getLeftNo).thenComparing(MetadataSysDictItem::getItemId);
	public static final Comparator<SysDictItem> SYS_DICT_ITEM_COMPARATOR = Comparator.comparing(SysDictItem::getLeftNo).thenComparing(SysDictItem::getItemId);
	private final MongoTemplate mongoTemplate;
	private final MongoTemplate readMongoTemplate;
	private final TransactionTemplate transactionTemplate;

	public SysDictCommonService(@Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate,
										 @Qualifier("mongoTemplate") MongoTemplate mongoTemplate,
										  TransactionTemplate transactionTemplate) {
		this.mongoTemplate = mongoTemplate;
        this.readMongoTemplate = readMongoTemplate;
        this.transactionTemplate = transactionTemplate;
	}

	@NewSpan
	public SysDictItemMongodb getRootItem(String appId, String dictId) {
		return transactionTemplate.execute(status -> {
			Criteria criteria = Criteria.where(SysDictMongodb.FIELD.APP_ID).is(appId)
				.and(SysDictMongodb.FIELD.DICT_ID).is(dictId);
			Query query = Query.query(criteria);
			SysDictMongodb sysDictMongodb = mongoTemplate.findOne(query, SysDictMongodb.class, MongodbConstants.Collection.SYS_DICT);

			if (sysDictMongodb != null && (sysDictMongodb.getLeftNo() == null || sysDictMongodb.getRightNo() == null)) {
				Update update = new Update();
				update.set(SysDictMongodb.FIELD.LEFT_NO, 1);
				update.set(SysDictMongodb.FIELD.RIGHT_NO, 2);
				update.set(SysDictMongodb.FIELD.METADATA.UPDATE_ACCOUNT_ID, CairoSecurityContextHolder.getSubappAccountId());
				update.currentDate(SysDictMongodb.FIELD.METADATA.UPDATE_TIME);

				FindAndModifyOptions options = FindAndModifyOptions.options().returnNew(true);
				sysDictMongodb = mongoTemplate.findAndModify(query, update, options, SysDictMongodb.class, MongodbConstants.Collection.SYS_DICT);
			}

			if (sysDictMongodb == null) {
				return null;
			}

			return SysDictItemMongodb.builder()
				.parentItemId(ROOT_PARENT_ID)
				.itemId(ROOT_ID)
				.depth(0)
				.leftNo(sysDictMongodb.getLeftNo())
				.rightNo(sysDictMongodb.getRightNo())
				.build();
		});
	}

	/**
	 * 查询系统级字典项信息
	 *
	 * @param appId appId
	 * @param args  args
	 * @return x
	 */
	@NewSpan
	public Map<String, SysDictItem> getSysDictItemMap(@Valid @NotNull String appId, @Validated GetDictItemMapArgs args) {
		if (args.getItemIds().isEmpty()) return Collections.emptyMap();
		Criteria sdiCriteria = Criteria
			.where(SysDictItemMongodb.FIELD.APP_ID).is(appId)
			.and(SysDictItemMongodb.FIELD.DICT_ID).is(args.getDictId())
			.and(SysDictItemMongodb.FIELD.ITEM_ID).in(args.getItemIds());
		Query sdiQuery = Query.query(sdiCriteria);
		sdiQuery.with(Sort.by(Sort.Order.asc(SysDictItemMongodb.FIELD.LEFT_NO)));
		List<SysDictItemMongodb> itemList = readMongoTemplate.find(sdiQuery, SysDictItemMongodb.class, MongodbConstants.Collection.SYS_DICT_ITEM);

		if (itemList.isEmpty()) return Collections.emptyMap();


		return itemList.stream()
			.map(SysDictConverter::convertSysDictItem)
			.collect(Collectors.toMap(SysDictItem::getItemId, x -> x));
	}
}
