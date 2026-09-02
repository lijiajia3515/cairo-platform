package io.github.lijiajia3515.cairo.auth.modules.dict.biz;

import io.github.lijiajia3515.cairo.auth.framework.security.CairoSecurityContextHolder;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.BizDictItemMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.BizDictMongodb;
import io.micrometer.tracing.annotation.NewSpan;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import static io.github.lijiajia3515.cairo.auth.constants.CairoAuthConstants.ROOT_ID;
import static io.github.lijiajia3515.cairo.auth.constants.CairoAuthConstants.ROOT_PARENT_ID;

/**
 * 应用级公共服务
 */
@Component
public class BizDictCommonService {
	private final MongoTemplate mongoTemplate;
	private final TransactionTemplate transactionTemplate;

	public BizDictCommonService(MongoTemplate mongoTemplate, TransactionTemplate transactionTemplate) {
		this.mongoTemplate = mongoTemplate;
		this.transactionTemplate = transactionTemplate;
	}

	@NewSpan
	public BizDictItemMongodb getRootItem(String appId,String tenantId, String dictId) {
		return transactionTemplate.execute(status -> {
			Criteria criteria = Criteria.where(BizDictMongodb.FIELD.APP_ID).is(appId)
				.and(BizDictMongodb.FIELD.DICT_ID).is(dictId);
			Query query = Query.query(criteria);
			BizDictMongodb bizDictMongodb = mongoTemplate.findOne(query, BizDictMongodb.class, MongodbConstants.Collection.BIZ_DICT);

			if (bizDictMongodb != null && (bizDictMongodb.getLeftNo() == null || bizDictMongodb.getRightNo() == null)) {
				Update update = new Update();
				update.set(BizDictMongodb.FIELD.LEFT_NO, 1);
				update.set(BizDictMongodb.FIELD.RIGHT_NO, 2);
				update.set(BizDictMongodb.FIELD.METADATA.UPDATE_USER_ID, CairoSecurityContextHolder.getTenantAppUserId());
				update.currentDate(BizDictMongodb.FIELD.METADATA.UPDATE_TIME);

				FindAndModifyOptions options = FindAndModifyOptions.options().returnNew(true);
				bizDictMongodb = mongoTemplate.findAndModify(query, update, options, BizDictMongodb.class, MongodbConstants.Collection.BIZ_DICT);
			}

			if (bizDictMongodb == null) {
				return null;
			}

			return BizDictItemMongodb.builder()
				.parentItemId(ROOT_PARENT_ID)
				.itemId(ROOT_ID)
				.depth(0)
				.leftNo(bizDictMongodb.getLeftNo())
				.rightNo(bizDictMongodb.getRightNo())
				.build();
		});
	}
}
