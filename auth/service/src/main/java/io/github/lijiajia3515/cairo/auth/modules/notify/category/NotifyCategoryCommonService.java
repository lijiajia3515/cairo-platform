package io.github.lijiajia3515.cairo.auth.modules.notify.category;

import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.notify.NotifyCategoryMongodb;
import io.github.lijiajia3515.cairo.auth.domain.dto.dict.sys.MetadataSysDictItem;
import io.github.lijiajia3515.cairo.auth.domain.dto.dict.sys.SysDictItem;
import io.github.lijiajia3515.cairo.auth.domain.dto.notify.category.NotifyCategory;
import io.github.lijiajia3515.cairo.auth.modules.notify.category.args.GetNotifyCategoryArgs;
import io.micrometer.tracing.annotation.NewSpan;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.validation.annotation.Validated;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 系统级公共服务
 */
@Component
public class NotifyCategoryCommonService {
	public static final Comparator<MetadataSysDictItem> METADATA_SYS_DICT_ITEM_COMPARATOR = Comparator.comparing(MetadataSysDictItem::getLeftNo).thenComparing(MetadataSysDictItem::getItemId);
	public static final Comparator<SysDictItem> SYS_DICT_ITEM_COMPARATOR = Comparator.comparing(SysDictItem::getLeftNo).thenComparing(SysDictItem::getItemId);
	private final MongoTemplate mongoTemplate;
	private final MongoTemplate readMongoTemplate;
	private final TransactionTemplate transactionTemplate;

	public NotifyCategoryCommonService(@Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate,
													@Qualifier("mongoTemplate") MongoTemplate mongoTemplate,
													TransactionTemplate transactionTemplate) {
		this.mongoTemplate = mongoTemplate;
		this.readMongoTemplate = readMongoTemplate;
		this.transactionTemplate = transactionTemplate;
	}

	/**
	 * 查询通知消息分类
	 *
	 * @param appId appId 应用ID
	 * @param args  args
	 * @return x
	 */
	@NewSpan
	public Map<String, NotifyCategory> getNotifyCategory(@Valid @NotNull String appId, @Validated GetNotifyCategoryArgs args) {
		if (args.getCategoryIds().isEmpty()) return Collections.emptyMap();
		Criteria criteria = Criteria
			.where(NotifyCategoryMongodb.FIELD.APP_ID).is(appId)
			.and(NotifyCategoryMongodb.FIELD.CATEGORY_ID).in(args.getCategoryIds());

		Query query = Query.query(criteria);

		List<NotifyCategoryMongodb> mongodbList = readMongoTemplate.find(query, NotifyCategoryMongodb.class, MongodbConstants.Collection.NOTIFY_CATEGORY);

		if (mongodbList.isEmpty()) return Collections.emptyMap();

		return mongodbList.stream()
			.map(NotifyCategoryConverter::convertNotifyCategory)
			.collect(Collectors.toMap(NotifyCategory::getCategoryId, x -> x));
	}
}
