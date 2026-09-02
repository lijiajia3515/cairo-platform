package io.github.lijiajia3515.cairo.mongodb.serial;

import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.Optional;


/**
 * 序号服务
 */
public class SerialService {

	private final String collectionName;
	private final TransactionTemplate transactionTemplate;
	private final MongoTemplate mongoTemplate;

	public SerialService(String collectionName, TransactionTemplate transactionTemplate, MongoTemplate mongoTemplate) {
		this.collectionName = collectionName;
		this.transactionTemplate = transactionTemplate;
		this.mongoTemplate = mongoTemplate;
	}

	/**
	 * 获取下一序号
	 *
	 * @param namespace 命名空间
	 * @param key       key
	 * @return 值
	 */
	public long next(String namespace, String key) {
		return next(namespace, key, 1, 0);

	}

	/**
	 * 获取下一序号
	 *
	 * @param namespace 命名空间
	 * @param key       key
	 * @return 值
	 */
	public String nextStr(String namespace, String key) {
		return "" + next(namespace, key, 1, 0);

	}

	/**
	 * 获取下一序号
	 *
	 * @param namespace 命名空间
	 * @param key       key
	 * @return 值
	 */
	public long next(String namespace, String key, long stepValue, long initValue) {
		Criteria criteria = Criteria.where(SerialMongodb.FIELD.NAMESPACE).is(namespace).and(SerialMongodb.FIELD.KEY).is(key);
		Query query = Query.query(criteria);
		query.fields().include(SerialMongodb.FIELD.VALUE);

		Update update = new Update();
		update.set(SerialMongodb.FIELD.METADATA.UPDATE_TIME, LocalDateTime.now());
		update.inc(SerialMongodb.FIELD.VALUE, stepValue);


		FindAndModifyOptions options = FindAndModifyOptions.options().upsert(true).returnNew(true);

		Long returnValue = transactionTemplate.execute(transactionStatus -> {
			try {
				boolean exists = mongoTemplate.exists(query, collectionName);
				if (exists) {
					return Optional.ofNullable(mongoTemplate.findAndModify(query, update, options, SerialMongodb.class, collectionName))
						.map(SerialMongodb::getValue).orElse(0L);

				} else {
					SerialMongodb newSerial = SerialMongodb.builder()
						.namespace(namespace)
						.key(key)
						.value(initValue)
						.build();
					mongoTemplate.insert(newSerial, collectionName);
					return initValue;
				}
			} catch (Exception e) {
				return 0L;
			}
		});
		return Optional.ofNullable(returnValue).orElse(0L);
	}

	/**
	 * 获取下一序号
	 *
	 * @param namespace 命名空间
	 * @param key       key
	 * @return 值
	 */
	public String nextStr(String namespace, String key, long stepValue, long initValue) {
		return "" + next(namespace, key, stepValue, initValue);
	}
}
